package com.example.demo.domain.shared.parkingfeepolicy.repository;

import com.example.demo.domain.shared.parkingfeepolicy.ParkingFeePolicy;
import com.example.demo.domain.shared.parkingfeepolicy.enums.ParkingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

import java.util.List;

public interface ParkingFeePolicyRepository extends JpaRepository<ParkingFeePolicy,Long> {

    @Query("select fp from ParkingFeePolicy fp where fp.isActive=true and CURRENT_TIMESTAMP between fp.effectiveFrom and fp.effectiveTo")
    List<ParkingFeePolicy> findCurrentEffectivePolicy();

    @Query("select fp from ParkingFeePolicy fp where fp.isActive=true and CURRENT_TIMESTAMP < fp.effectiveFrom")
    List<ParkingFeePolicy> findUpcomingEffectivePolicy();

    // 특정 시점에 유효한 활성 정책 조회
    @Query("select p from ParkingFeePolicy p where p.parkingType=:parkingType and p.isActive=true and :now between p.effectiveFrom and p.effectiveTo order by p.id desc")
    Optional<ParkingFeePolicy> findValidPolicy(@Param("parkingType")ParkingType parkingType,@Param("now")LocalDateTime now);

    //가장 최근 정책 조회
    @Query("select p.id from ParkingFeePolicy p where p.parkingType=:parkingType and p.isActive=true order by p.version desc limit 1")
    long getLatestVersion(@Param("parkingType")ParkingType parkingType);


}
