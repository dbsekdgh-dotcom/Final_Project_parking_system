package com.example.demo.domain.user.auth.service;

import com.example.demo.domain.shared.user.User;
import com.example.demo.domain.shared.user.enums.Status;
import com.example.demo.domain.user.auth.dtos.request.UserSocialRecoverRequestDto;
import com.example.demo.domain.user.auth.repository.SocialAccountRepository;
import com.example.demo.domain.user.auth.repository.UserAuthRepository;
import com.example.demo.domain.user.entity.SocialAccount;
import com.example.demo.global.exception.AuthException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSocialRecoverService {

    private final UserAuthRepository userAuthRepository;
    private final SocialAccountRepository socialAccountRepository;

    @Transactional
    public User socialRecover(UserSocialRecoverRequestDto dto) {

        // 1. 탈퇴 유저 먼저 조회 (비교를 위해 유저 정보가 먼저 필요합니다)
        User user = userAuthRepository.findByEmailAndStatus(dto.getEmail(), Status.DELETED)
                .orElseThrow(() -> new AuthException(ErrorCode.USER_NOT_FOUND));

        // 2. 소셜 계정 존재 여부 확인
        Optional<SocialAccount> existingSocial = socialAccountRepository.findByProviderAndProviderId(
                dto.getProvider(),
                dto.getProviderId()
        );

        // [중요 수정] 이미 소셜 계정이 있다면?
        if (existingSocial.isPresent()) {
            SocialAccount social = existingSocial.get();

            // 그 소셜 계정의 주인이 현재 복구하려는 유저가 아니라면 (남의 계정이라면) 에러!
            if (!social.getUser().getUserId().equals(user.getUserId())) {
                log.warn("### [복구 실패] 해당 소셜 계정은 이미 다른 유저와 연동됨: {}", dto.getProviderId());
                throw new AuthException(ErrorCode.ALREADY_LINKED_SOCIAL);
            }

            // 내 계정이라면? "이미 연동되어 있음"을 인지하고 로직 계속 진행 (에러 던지지 않음)
            log.info("### [복구 진행] 로그인 시도 중 생성된 소셜 계정 확인됨(본인 계정). 계속 진행합니다.");
        }

        // 3. 전화번호 형식 검증 및 원복
        String phoneInDb = user.getPhone();
        if (phoneInDb == null || !phoneInDb.contains("_")) {
            log.error("탈퇴 유저의 전화번호 형식이 올바르지 않음: {}", user.getEmail());
            throw new AuthException(ErrorCode.INVALID_USER_DATA_FORMAT);
        }

        String rawPhone = phoneInDb.split("_")[0];

        // 4. 활성 계정 중 중복 번호 체크
        if (userAuthRepository.existsByPhoneAndStatus(rawPhone, Status.ACTIVE)) {
            throw new AuthException(ErrorCode.SOCIAL_RECOVERY_PHONE_CONFLICT);
        }

        // 5. 유저 복구 (Dirty Checking)
        user.recover(rawPhone);

        // 6. 소셜 연동 정보 저장 (DB에 없는 경우에만 저장)
        if (existingSocial.isEmpty()) {
            SocialAccount socialAccount = SocialAccount.builder()
                    .user(user)
                    .provider(dto.getProvider())
                    .providerId(dto.getProviderId())
                    .build();

            socialAccountRepository.save(socialAccount);
            log.info("### [복구 성공] 소셜 계정 신규 생성 및 연동 완료 - Email: {}", user.getEmail());
        } else {
            log.info("### [복구 성공] 기존 소셜 계정 활용 - Email: {}", user.getEmail());
        }

        return user;
    }
}