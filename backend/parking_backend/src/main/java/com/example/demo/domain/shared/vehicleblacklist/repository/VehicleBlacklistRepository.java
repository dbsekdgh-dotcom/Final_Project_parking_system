package com.example.demo.domain.shared.vehicleblacklist.repository;

import com.example.demo.domain.shared.vehicleblacklist.VehicleBlacklist;
import com.example.demo.domain.shared.vehicleblacklist.enums.BlacklistStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface VehicleBlacklistRepository extends JpaRepository<VehicleBlacklist,Long> {

    boolean existsByCarNumberAndStatus(String carNumber, BlacklistStatus status);

    // VehicleBlacklistRepository에 추가
    @Query("SELECT COUNT(vb) > 0 FROM VehicleBlacklist vb " +
            "WHERE vb.carNumber = :carNumber " +
            "AND vb.status = 'ACTIVE' " +
            "AND :now BETWEEN vb.startDate AND vb.endDate")
    boolean isCurrentlyBlacklisted(@Param("carNumber") String carNumber, @Param("now") LocalDateTime now);
}
