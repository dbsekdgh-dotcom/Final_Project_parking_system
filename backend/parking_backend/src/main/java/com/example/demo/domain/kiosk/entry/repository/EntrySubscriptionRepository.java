package com.example.demo.domain.kiosk.entry.repository;

import com.example.demo.domain.shared.subscription.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EntrySubscriptionRepository extends JpaRepository<Subscription, Long> {

    // 차량 ID로 현재 유효한 정기권의 종료일 조회


    // free_exit_until = 정기권 endDate 세팅에 사용
    @Query("""
        SELECT s.endDate
        FROM Subscription s
        WHERE s.vehicle.id = :vehicleId
          AND s.status = 'ACTIVE'
          AND CURRENT_TIMESTAMP BETWEEN s.startDate AND s.endDate
    """)
    Optional<LocalDateTime> findActiveSubscriptionEndDate(@Param("vehicleId") Long vehicleId);
}
