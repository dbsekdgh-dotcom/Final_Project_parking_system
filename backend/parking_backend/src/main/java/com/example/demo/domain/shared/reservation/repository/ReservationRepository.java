package com.example.demo.domain.shared.reservation.repository;

import com.example.demo.domain.shared.reservation.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    @Query("select r.household.householdId from Reservation r where r.carNumber=:carNumber")
    Optional<Long> getHostUserIdbyCarNumber(String carNumber);
}
