package com.example.demo.domain.user.mypage.point.repository;

import com.example.demo.domain.user.mypage.point.entity.UserPoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserPointRepository extends JpaRepository<UserPoint,Long> {
    Optional<UserPoint> findByUserUserId(Long userId);

}
