package com.example.demo.domain.user.mypage.dashboard.repository;

import com.example.demo.domain.shared.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserDashboardRepository extends JpaRepository<User,Long> {
    boolean existsByPhone(String phone);
}
