package com.example.demo.domain.payment.point.repository;

import com.example.demo.domain.payment.point.entity.PointLog;
import com.example.demo.domain.payment.point.entity.UserPoint;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PointLogRepository extends JpaRepository<PointLog,Long> {

    //특정 유저의 포인트 이력 조회(최신순)
    List<PointLog> findByUserUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByPaymentPaymentId(long paymentId);

    Optional<PointLog> findByPaymentPaymentId(long paymentId);


}
