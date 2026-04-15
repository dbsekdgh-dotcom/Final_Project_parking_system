package com.example.demo.domain.shared.parkingfeepolicy.repository;

import com.example.demo.domain.shared.parkingfeepolicy.ParkingFeePolicy;
import com.example.demo.domain.shared.parkingfeepolicy.enums.ParkingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ParkingFeePolicyRepository extends JpaRepository<ParkingFeePolicy,Long> {
    // 특정 시점에 유효한 활성 정책 조회
    @Query("select p from ParkingFeePolicy p where p.parkingType=:parkingType and p.isActive=true and :now between p.effectiveFrom and p.effectiveTo order by p.id desc")
    Optional<ParkingFeePolicy> findValidPolicy(@Param("parkingType")ParkingType parkingType,
                                               @Param("now")LocalDateTime now);
}
