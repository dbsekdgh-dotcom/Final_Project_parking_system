package com.example.demo.domain.kiosk.entry.repository;

import com.example.demo.domain.shared.parkingspace.ParkingSpace;
import com.example.demo.domain.shared.parkingspace.enums.SpaceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface EntryParkingSpaceRepository extends JpaRepository<ParkingSpace,Long> {
    Optional<ParkingSpace> findByIdAndStatus(
      Long id,
      SpaceStatus status
    );

    @Query("""
        SELECT COUNT(ps)
        FROM ParkingSpace ps
        where ps.status ='AVAILABLE'
        AND ps.isDisabled=false 
    """)
    long countAvailableSpace();
}
