package com.example.demo.domain.kiosk.entry.repository;

import com.example.demo.domain.kiosk.entry.dtos.response.EntryCheckResponse;
import com.example.demo.domain.shared.vehicle.Vehicle;
import com.example.demo.domain.shared.vehicle.enums.VehicleStatus;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface EntryVehicleRepository extends JpaRepository<Vehicle,Long> {


    @Query("""
        SELECT new com.example.demo.domain.kiosk.entry.dtos.response.EntryCheckResponse(
            v.id,
            v.carNumber,

            CASE 
                WHEN u.household IS NOT NULL THEN true
                ELSE false
            END,

            CASE 
                WHEN COUNT(s) > 0 THEN true
                ELSE false
            END
        )
        FROM Vehicle v
        LEFT JOIN v.user u
        LEFT JOIN Subscription s 
            ON s.vehicle = v
            AND s.status = 'ACTIVE'
            AND CURRENT_TIMESTAMP BETWEEN s.startDate AND s.endDate
        WHERE v.carNumber = :carNumber
        GROUP BY v.id, v.carNumber, u.household
    """)
    Optional<EntryCheckResponse> findEntryCheckInfo(@Param("carNumber") String carNumber);
}
