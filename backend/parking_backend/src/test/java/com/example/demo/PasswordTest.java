package com.example.demo;

import com.example.demo.domain.admin.entity.Admin;
import com.example.demo.domain.admin.enums.AdminStatus;
import com.example.demo.domain.admin.repository.AdminRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class PasswordTest {
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AdminRepository adminRepository;

    @Test
    @Rollback(value = false)
    void createAdminAccount(){
        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode("1234");

        // 관리자 엔티티 생성
        Admin admin = Admin.builder()
                .loginId("admin05")
                .password(encodedPassword)
                .name("이윤진")
                .status(AdminStatus.ACTIVE)
                .build();
        adminRepository.save(admin); // DB저장
        System.out.println("=====================================");
        System.out.println("관리자 계정 생성 완료!!"+admin.getLoginId());
        System.out.println("=====================================");
    }
}
