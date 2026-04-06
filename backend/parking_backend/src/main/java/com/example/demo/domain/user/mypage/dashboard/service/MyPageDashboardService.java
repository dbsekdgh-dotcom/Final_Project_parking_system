package com.example.demo.domain.user.mypage.dashboard.service;

import com.example.demo.domain.shared.user.User;
import com.example.demo.domain.shared.user.enums.Status;
import com.example.demo.domain.user.mypage.dashboard.dto.response.MyPageDashboardResponseDto;
import com.example.demo.domain.user.mypage.dashboard.dto.request.MyPageDashboardUpdateRequestDto;
import com.example.demo.domain.user.mypage.dashboard.repository.UserDashboardRepository;
import com.example.demo.global.exception.AuthException;
import com.example.demo.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional //DB 변경이 일어나면 트랜잭션 처리
public class MyPageDashboardService {
    private final UserDashboardRepository userDashboardRepository;
    private final PasswordEncoder passwordEncoder;

    //1.회원정보 조회(Response DTO로 변경)
    public MyPageDashboardResponseDto getMemberInfo(Long memberId){
        //Member 엔티티에서 DTO로 변환,    탈퇴한 회원은 조회에서 제외
        User user = userDashboardRepository.findById(memberId)
                .filter(u -> u.getStatus()== Status.ACTIVE)//탈퇴회원 제외
                .orElseThrow(()->new AuthException(ErrorCode.USER_NOT_FOUND));

        return new MyPageDashboardResponseDto(
                    user.getName(),
                    user.getPhone(),
                    user.getBirth()
        );
    }

    //2.회원 정보 수정
    public void updateProfile(Long memberId, MyPageDashboardUpdateRequestDto dto){
        //DTO에서 넘어온 값으로 Member.updateProfile() 호출,  전화번호 중복 체크도 포함
        User user = userDashboardRepository.findById(memberId)
                .filter(u -> u.getStatus() == Status.ACTIVE)
                .orElseThrow(()-> new AuthException(ErrorCode.USER_NOT_FOUND));

        //전화번호 중복 체크
        if (userDashboardRepository.existsByPhone(dto.getPhone()) && !user.getPhone().equals(dto.getPhone())) {
            throw new AuthException(ErrorCode.PHONE_DUPLICATE);
        }
        user.updateUserInfo(dto.getName(),dto.getPhone());
    }

    //3. 회원탈퇴
    public void withdraw(Long memberId){
        //회원 탈퇴 시 상태를 DELETED로 변경하고 탈퇴 시간 기록
        User user = userDashboardRepository.findById(memberId)
                .filter(m -> m.getStatus() == Status.ACTIVE)
                .orElseThrow(()-> new AuthException(ErrorCode.USER_NOT_FOUND));
        user.setStatus(Status.DELETED);

        user.setDeletedAt(LocalDateTime.now());
    }
}
