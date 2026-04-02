package com.example.demo.domain.shared.subscription.repository;

import com.example.demo.domain.shared.subscription.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription,Long> {
    @Query("select s.subscriptionId from Subscription s where s.vehicle.id=:vehicleId and s.endDate > CURRENT_TIMESTAMP ")
    Optional<Long> getSubscriptionIdByVehicleId(Long vehicleId);
}
