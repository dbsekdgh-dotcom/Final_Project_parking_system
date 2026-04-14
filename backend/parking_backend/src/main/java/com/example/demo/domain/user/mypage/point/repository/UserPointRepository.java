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

    //비관적 락(Pessimistic Lock) : 데이터를 읽는 순간 DB 레벨에서 해당 행(Row)에 데이터 잠금
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select up from UserPoint up where up.user.userId=:userId")
    Optional<UserPoint> findByUserUserIdWithLock(@Param("userId") long userId);
}
