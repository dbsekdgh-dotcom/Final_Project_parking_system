package com.example.demo.domain.admin.repository;

import com.example.demo.domain.admin.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    // 로그인 아이디로 관리자 한명 조회하는 메서드 / Optional : 아이디가 없을때의 예외(NPE)를 알아서 처리해줌
    Optional<Admin> findByLoginId(String loginId);

}
