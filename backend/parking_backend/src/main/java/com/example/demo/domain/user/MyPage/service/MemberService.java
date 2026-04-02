package com.example.demo.domain.user.MyPage.service;

import com.example.demo.domain.shared.user.UserRepository;
import com.example.demo.domain.user.MyPage.dto.MemberResponseDto;
import com.example.demo.domain.user.MyPage.dto.MemberUpdateRequestDto;
import com.example.demo.domain.user.MyPage.entity.MemberEntity;
import com.example.demo.domain.user.MyPage.entity.MemberStatus;
import com.example.demo.domain.user.MyPage.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional //DB 변경이 일어나면 트랜잭션 처리
public class MemberService {
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    //1.회원정보 조회(Response DTO로 변경)
    public MemberResponseDto getMemberInfo(Long memberId){
        //Member 엔티티에서 DTO로 변환,    탈퇴한 회원은 조회에서 제외
        MemberEntity memberEntity = memberRepository.findById(memberId)
                .filter(m -> m.getStatus()== MemberStatus.ACTIVE)//탈퇴회원 제외
                .orElseThrow(()->new IllegalArgumentException("회원이 존재하지 않습니다."));

        return new MemberResponseDto(
                    memberEntity.getName(),
                    memberEntity.getPhone(),
                    memberEntity.getBirth()
        );
    }

    //2.회원 정보 수정
    public void updateProfile(Long memberId, MemberUpdateRequestDto memberDto){
        //DTO에서 넘어온 값으로 Member.updateProfile() 호출,  전화번호 중복 체크도 포함
        MemberEntity memberEntity = memberRepository.findById(memberId)
                .filter(m -> m.getStatus() == MemberStatus.ACTIVE)
                .orElseThrow(()-> new IllegalArgumentException("회원이 존재하지 않습니다."));

        //전화번호 중복 체크
        if (memberRepository.existsByPhone(memberDto.getPhone()) && !memberEntity.getPhone().equals(memberDto.getPhone())) {
            throw new IllegalArgumentException("이미 사용중인 전화번호입니다.");
        }
        memberEntity.updateProfile(memberDto.getName(),memberDto.getPhone(),memberDto.getBirth());
    }

    //3. 비밀 번호 변경
    public void updatePassword(Long memberId,String rawPassword){
        //비밀번호는 암호화 후 저장, PasswordEncoder 필요
        MemberEntity memberEntity = memberRepository.findById(memberId)
                .filter(m->m.getStatus() == MemberStatus.ACTIVE)
                .orElseThrow(()-> new IllegalArgumentException("회원이 존재하지 않습니다."));

        String encodePassword = passwordEncoder.encode(rawPassword);

        memberEntity.updatePassword(encodePassword);
    }

    //4. 회원탈퇴
    public void withdraw(Long memberId){
        //회원 탈퇴 시 상태를 DELETED로 변경하고 탈퇴 시간 기록
        MemberEntity memberEntity = memberRepository.findById(memberId)
                .filter(m -> m.getStatus() == MemberStatus.ACTIVE)
                .orElseThrow(()-> new IllegalArgumentException("회원이 존재하지 않습니다."));
        memberEntity.withdraw();
    }
}
