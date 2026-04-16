package com.example.demo.domain.kiosk.entry.repository;

import com.example.demo.domain.shared.parkingspace.ParkingSpace;
import com.example.demo.domain.shared.parkingspace.enums.Floor;
import com.example.demo.domain.shared.parkingspace.enums.SpaceStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EntryParkingSpaceRepository extends JpaRepository<ParkingSpace,Long> {
    Optional<ParkingSpace> findByIdAndStatus(
      Long id,
      SpaceStatus status
    );

    List<ParkingSpace> findByFloorAndStatusNot(Floor floor, SpaceStatus status);

    @Query("""
        SELECT COUNT(ps)
        FROM ParkingSpace ps
        where ps.status ='AVAILABLE'
        AND ps.isDisabled=false
    """)
    long countAvailableSpace();

    //비관전락, assignSpace 용
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ps FROM ParkingSpace ps WHERE ps.id=:id")
    Optional<ParkingSpace> findByIdWithLock(@Param("id")Long id);

    int countByFloorAndStatus(Floor floor, SpaceStatus status);
}
