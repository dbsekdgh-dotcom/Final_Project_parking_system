package com.example.demo.domain.payment.point.repository;

import com.example.demo.domain.payment.point.entity.UserPoint;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserPointRepository extends JpaRepository<UserPoint,Long> {
    Optional<UserPoint> findByUserUserId(Long userId);

    @Query("select up from UserPoint up where up.user.userId=:userId")
    Optional<UserPoint> findByUserUserId(@Param("userId") long userId);

    //포인트 차감 : 현재 잔액보다 많은 경우만 차감
    //entityManager.refresh(userPoint)  쿼리 실행 후 영속성 컨텍스트를 최신화 합니다.
    @Modifying(clearAutomatically = true) //쿼리 실행 후 영속성 컨텍스트를 비움
    @Query("update UserPoint up set up.currentPoint=:currentPoint-:amount where up.userId=:userId and up.currentPoint>=:amount")
    int decreasePoint (@Param("userId") long userId,@Param("amount") int amount);

    @Modifying(clearAutomatically = true)
    @Query("update UserPoint up set up.currentPoint=:currentPoint+:amount where up.userId=:userId")
    int increasePoint (@Param("userId") long userId,@Param("amount") int amount);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select up from UserPoint up where up.user.userId = :userId")
    Optional<UserPoint> findByUserUserIdWithLock(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE UserPoint up SET up.currentPoint = 0 WHERE up.user.userId = :userId")
    void resetPoint(@Param("userId") Long userId);
}
