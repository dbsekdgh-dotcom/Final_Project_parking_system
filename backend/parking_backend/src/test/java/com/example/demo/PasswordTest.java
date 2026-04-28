package com.example.demo;


import com.example.demo.domain.auth.admin.entity.Admin;
import com.example.demo.domain.auth.admin.enums.AdminStatus;
import com.example.demo.domain.auth.admin.repository.AdminRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class PasswordTest {
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AdminRepository adminRepository;

    @Test
    @Rollback(value = false)
    @DisplayName("로컬 개발용 관리자 계정 생성")
    void createAdminAccount(){
        //1. 비밀번호 암호화
        String rawPassword="1234";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        //2. 관리자 엔티티 생성
        Admin admin = Admin.builder()
                .loginId("admin05")
                .password(encodedPassword)
                .name("이윤진")
                .status(AdminStatus.ACTIVE)
                .build();

        //3. DB저장
        Admin savedAdmin=adminRepository.save(admin);

        System.out.println("=====================================");
        System.out.println("✅ 로컬 관리자 계정 생성 성공!");
        System.out.println("로그인 ID: " + savedAdmin.getLoginId());
        System.out.println("비밀번호: " + rawPassword + " (암호화되어 저장됨)");
        System.out.println("=====================================");
    }
}
