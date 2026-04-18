package com.example.demo.domain.reservation.policy.repository;

import com.example.demo.domain.reservation.policy.ReservationEventPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
@Repository
public interface ReservationEventPolicyRepository extends JpaRepository<ReservationEventPolicy, Long> {

    Optional<ReservationEventPolicy> findTopByOrderByIdDesc();

    Optional<ReservationEventPolicy> findByEventName(String eventName);

    @Query("SELECT r FROM ReservationEventPolicy r " +
            "WHERE r.startDate <= :now " +
            "AND (r.endDate IS NULL OR r.endDate >= :now) " +
            "ORDER BY r.id DESC LIMIT 1")
    Optional<ReservationEventPolicy> findActivePolicy(@Param("now") LocalDateTime now);

}
