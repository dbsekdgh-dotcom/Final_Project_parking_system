package com.example.demo.domain.shared.subscription.repository;

import com.example.demo.domain.shared.subscription.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription,Long> {

    @Query("SELECT COUNT(s) > 0 FROM Subscription s " +
            "WHERE s.vehicle.carNumber = :carNumber " +
            "AND s.status = 'ACTIVE' " +
            "AND :now BETWEEN s.startDate AND s.endDate")
    boolean hasActiveSubscription(@Param("carNumber") String carNumber, @Param("now") LocalDateTime now);

    @Query("SELECT s FROM Subscription  s WHERE s.user.userId = :userId ORDER BY s.endDate DESC LIMIT 1")
    Optional<Subscription> findLatestSubscription (@Param("userId") Long userId);
}
