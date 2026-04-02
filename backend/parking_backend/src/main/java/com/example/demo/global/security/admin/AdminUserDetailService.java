package com.example.demo.global.security.admin;

import com.example.demo.domain.admin.entity.Admin;
import com.example.demo.domain.admin.enums.AdminStatus;
import com.example.demo.domain.admin.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// 스프링시큐리티의 인증시스템과 DB(Admin테이블)를 연결해주는 다리 역할
@Service
@RequiredArgsConstructor
@Log4j2 // 코드 안에 로그(기록)를 남길 수 있는 전광판을 설치해주는 도구 (실제 서비스 운영 환경에선 sout보다 전문 로그 라이브러리를 사용함)
public class AdminUserDetailService implements UserDetailsService {
    private final AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        log.info("------------ [AdminAuth] 로그인 시도 ID: "+loginId+" ------------");

        //1. DB에서 loginId로 관리자 정보 조회
        Admin admin = adminRepository.findByLoginId(loginId)
                .orElseThrow(()->new UsernameNotFoundException("해당 아이디를 가진 관리자가 없습니다: "+loginId));

        //2. 계정 상태 체크 (ACTIVE 상태일때만 로그인 허용)
        if(admin.getStatus()!= AdminStatus.ACTIVE){
            log.error("---------- [AdminAuth] 비활성화된 계정 접근: "+loginId+" ----------");
            throw new DisabledException("비활성화되거나 탈퇴된 관리자 계정입니다.");
        }

        //3. AdminAuthDto 객체 생성 및 반환
        AdminAuthDto adminAuthDto = new AdminAuthDto(
                admin.getLoginId(),admin.getPassword(),admin.getName()
        );

        log.info("---------- [AdminAuth] 인증 객체 생성 완료: "+admin.getName()+" ----------");

        return adminAuthDto;
    }
}
