package com.example.demo.domain.payment.subscription.repository;

import com.example.demo.domain.payment.subscription.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription,Long> {

    @Query("SELECT COUNT(s) > 0 FROM Subscription s " +
            "WHERE s.vehicle.carNumber = :carNumber " +
            "AND s.status = 'ACTIVE' " +
            "AND :now BETWEEN s.startDate AND s.endDate")
    boolean hasActiveSubscription(@Param("carNumber") String carNumber, @Param("now") LocalDateTime now);

    @Query("SELECT s FROM Subscription  s WHERE s.user.userId = :userId ORDER BY s.endDate DESC LIMIT 1")
    Optional<Subscription> findLatestSubscription (@Param("userId") Long userId);


    @Query("SELECT COUNT(s) > 0 FROM Subscription s " +
            "WHERE s.vehicle.id = :vehicleId " +
            "AND s.status = 'ACTIVE' " +
            "AND s.endDate >= :now")
    boolean hasActiveOrFutureSubscription(@Param("vehicleId")Long vehicleId, @Param("now") LocalDateTime now);

    @Query("""
        SELECT s.endDate
        FROM Subscription s
        WHERE s.vehicle.id = :vehicleId
          AND s.status = 'ACTIVE'
          AND CURRENT_TIMESTAMP BETWEEN s.startDate AND s.endDate
        """)
    Optional<LocalDateTime> findActiveSubscriptionEndDate(@Param("vehicleId") Long vehicleId);

    @Query("SELECT COUNT(s) FROM Subscription s " +
            "WHERE s.status = 'ACTIVE' " +
            "AND s.endDate > CURRENT_TIMESTAMP " +
            "AND s.startDate < :newEndDate " +
            "AND s.endDate > :newStartDate")
    long countOverlappingActiveSubscriptions(
            @Param("newStartDate") LocalDateTime newStartDate,
            @Param("newEndDate") LocalDateTime newEndDate
    );

    @Query("SELECT COUNT(s) > 0 FROM Subscription s " +
            "WHERE s.vehicle.id = :vehicleId " +
            "AND s.status = 'ACTIVE' " +
            "AND s.startDate < :newEndDate " +
            "AND s.endDate > :newStartDate")
    boolean hasVehicleOverlappingSubscription(
            @Param("vehicleId") Long vehicleId,
            @Param("newStartDate") LocalDateTime newStartDate,
            @Param("newEndDate") LocalDateTime newEndDate
    );

    // 환불 시 vehicle, payment를 한 번에 JOIN FETCH — LazyInitializationException 방지
    @Query("SELECT s FROM Subscription s JOIN FETCH s.vehicle v JOIN FETCH s.payment p WHERE s.subscriptionId = :id")
    Optional<Subscription> findByIdWithVehicleAndPayment(@Param("id") Long id);

    // 구매 이력 전체 조회 — 모든 상태 포함, 최신순 정렬
    @Query("SELECT s FROM Subscription s JOIN FETCH s.vehicle v " +
            "WHERE s.user.userId = :userId " +
            "ORDER BY s.startDate DESC")
    List<Subscription> findAllByUserId(@Param("userId") Long userId);

    // 내 정기권 조회 — ACTIVE 상태이고 endDate가 아직 안 지난 것 중 가장 최신 1건
    @Query("SELECT s FROM Subscription s JOIN FETCH s.vehicle v " +
            "WHERE s.user.userId = :userId " +
            "AND s.status = 'ACTIVE' " +
            "AND s.endDate >= :now " +
            "ORDER BY s.startDate DESC LIMIT 1")
    Optional<Subscription> findMyActiveSubscription(@Param("userId") Long userId, @Param("now") java.time.LocalDateTime now);

    // 스케줄러용 — endDate가 지났는데 아직 ACTIVE인 정기권 일괄 EXPIRED 처리
    @Modifying
    @Query("UPDATE Subscription s SET s.status = 'EXPIRED' WHERE s.status = 'ACTIVE' AND s.endDate < :now")
    int expireSubscriptions(@Param("now") LocalDateTime now);

}


