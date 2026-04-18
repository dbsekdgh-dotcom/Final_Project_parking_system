package com.example.demo.domain.parking.entry.repository;

import com.example.demo.domain.parking.entry.dtos.response.EntryCheckResponse;
import com.example.demo.domain.vehicle.VehicleRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EntryVehicleRepository extends VehicleRepository {


    @Query("""
        SELECT new com.example.demo.domain.parking.entry.dtos.response.EntryCheckResponse(
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
