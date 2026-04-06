package com.example.demo.domain.kiosk.entry.repository;

import com.example.demo.domain.shared.vehicleblacklist.VehicleBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EntryVehicleBlacklistRepository extends JpaRepository<VehicleBlacklist,Long> {

    @Query("""
    SELECT CASE WHEN COUNT(vb) > 0 THEN true ELSE false END
        FROM VehicleBlacklist vb
            WHERE vb.carNumber=:carNumber
                AND vb.status='ACTIVE'
                    AND CURRENT_TIMESTAMP BETWEEN vb.startDate AND vb.endDate
    """)
    boolean existsActiveBlacklist(@Param("carNumber")String carNumber);
}
