package com.example.demo.domain.kiosk.entry.repository;

import com.example.demo.domain.shared.parkingfeepolicy.ParkingFeePolicy;
import com.example.demo.domain.shared.parkingfeepolicy.enums.ParkingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EntryParkingFeePolicyRepository extends JpaRepository<ParkingFeePolicy,Long> {
    @Query("""
    select p
    from ParkingFeePolicy p
    where p.parkingType= :type
    AND p.isActive = true
    AND current_timestamp between p.effectiveFrom and p.effectiveTo
""")
    Optional<ParkingFeePolicy> findActivePolicy(@Param("type")ParkingType type);
}
