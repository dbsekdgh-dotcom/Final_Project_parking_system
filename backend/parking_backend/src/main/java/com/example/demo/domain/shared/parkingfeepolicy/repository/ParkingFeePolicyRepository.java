package com.example.demo.domain.shared.parkingfeepolicy.repository;

import com.example.demo.domain.shared.parkingfeepolicy.ParkingFeePolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ParkingFeePolicyRepository extends JpaRepository<ParkingFeePolicy,Long> {

    @Query("select fp from ParkingFeePolicy fp where fp.isActive=true and CURRENT_TIMESTAMP between fp.effectiveFrom and fp.effectiveTo")
    List<ParkingFeePolicy> findEffectiveParkingFeePolicy();
}
