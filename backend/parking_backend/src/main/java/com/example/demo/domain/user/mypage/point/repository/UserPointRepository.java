package com.example.demo.domain.user.mypage.point.repository;

import com.example.demo.domain.user.mypage.point.entity.UserPoint;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserPointRepository extends JpaRepository<UserPoint,Long> {
    Optional<UserPoint> findByUserUserId(Long userId);

    @Query("select up from UserPoint up where up.user.userId=:userId")
    Optional<UserPoint> findByUserUserIdWithLock(@Param("userId") long userId);
}
