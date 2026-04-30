package com.example.demo.domain.payment.point.repository;

import com.example.demo.domain.dashboard.dtos.response.UsageDailyProjection;
import com.example.demo.domain.payment.point.entity.PointLog;
import com.example.demo.domain.payment.point.entity.PointReason;
import com.example.demo.domain.payment.point.entity.UserPoint;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PointLogRepository extends JpaRepository<PointLog,Long> {

    //특정 유저의 포인트 이력 조회(최신순)
    List<PointLog> findByUserUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByPaymentPaymentId(long paymentId);

    Optional<PointLog> findByPaymentPaymentId(long paymentId);

    /**
     * 특정 결제건(paymentId)에 대해 이미 특정 사유(PointReason)로 포인트 처리가 되었는지 확인
     * 예: 특정 결제 건에 대해 이미 'REFUND' 이력이 있는지 체크할 때 사용
     */
    boolean existsByPaymentPaymentIdAndReason(Long paymentId, PointReason reason);

    //Admin Dashboard 사용량
    @Query("SELECT COUNT(p) FROM PointLog p " +
            "WHERE p.reason = com.example.demo.domain.payment.point.entity.PointReason.PAYMENT_USE " +
            "AND p.createdAt BETWEEN :start AND :end")
    long countUsedBetween(
            @Param("start") LocalDateTime start,
            @Param("end")   LocalDateTime end
    );

    @Query(value =
            "SELECT DATE_FORMAT(p.created_at, '%Y-%m-%d') AS date, " +
                    "'포인트' AS category, " +
                    "SUM(ABS(p.change_amount)) AS usageCount, " +
                    "COUNT(*) AS transactionCount " +
                    "FROM point_log p " +
                    "WHERE p.reason = 'PAYMENT_USE' " +
                    "  AND p.created_at BETWEEN :from AND :to " +
                    "GROUP BY DATE_FORMAT(p.created_at, '%Y-%m-%d') " +
                    "ORDER BY DATE_FORMAT(p.created_at, '%Y-%m-%d') DESC",
            nativeQuery = true)
    List<UsageDailyProjection> findDailyUsedRows(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to
    );

}
