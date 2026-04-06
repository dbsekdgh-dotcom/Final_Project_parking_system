package com.example.demo.domain.kiosk.entry.repository;

import com.example.demo.domain.shared.parkinglog.ParkingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EntryParkingLogRepository extends JpaRepository<ParkingLog,Long> {
    @Query("""
    SELECT count(pl)>0
    from ParkingLog pl
    where pl.carNumberSnapshot=:plate
    and pl.parkingStatus in (
        com.example.demo.domain.shared.parkinglog.enums.ParkingStatus.DETECTED,
        com.example.demo.domain.shared.parkinglog.enums.ParkingStatus.ENTERED,
        com.example.demo.domain.shared.parkinglog.enums.ParkingStatus.EXIT_REQUESTED
    )
""")
    boolean existsActiveParking(@Param("plate")String plate);
}
