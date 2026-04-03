package com.example.demo.domain.shared.parkingfeepolicy.repository;

import com.example.demo.domain.shared.parkingfeepolicy.ParkingFeePolicy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParkingFeePolicyRepository extends JpaRepository<ParkingFeePolicy,Long> {
}
