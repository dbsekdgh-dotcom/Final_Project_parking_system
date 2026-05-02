package com.example.demo.domain.auth.admin.repository;

import com.example.demo.domain.auth.admin.entity.Admin;
import com.example.demo.domain.auth.admin.enums.AdminStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    // 단순 아이디 조회 / Optional : 아이디가 없을때의 예외(NPE)를 알아서 처리해줌
    Optional<Admin> findByLoginId(String loginId);

    // 아이디가 일치하고, 'ACTIVE' 상태인 관리자만 조회
    Optional<Admin> findByLoginIdAndStatus(String loginId, AdminStatus status);

    // Active/INACTIVE 상태인 관리자 조회
    List<Admin> findAllByStatus(AdminStatus status);
}
