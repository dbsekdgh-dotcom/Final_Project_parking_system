package com.example.demo.domain.kiosk.entry.repository;

import com.example.demo.domain.shared.reservation.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EntryReservationRepository extends JpaRepository<Reservation,Long> {
    @Query("""
    SELECT CASE WHEN COUNT(r) > 0 THEN true else false end 
        FROM Reservation r
            where r.carNumber = :carNumber
                AND r.status = 'RESERVED'
                    AND CURRENT_TIMESTAMP 
                        BETWEEN r.visitStartAt AND r.visitEndAt
    """)
    boolean existsValidReservation(@Param("carNumber") String carNumber);
}
