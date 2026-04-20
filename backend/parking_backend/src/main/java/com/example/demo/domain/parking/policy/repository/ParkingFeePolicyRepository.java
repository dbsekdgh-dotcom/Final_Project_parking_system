package com.example.demo.domain.parking.policy.repository;

import com.example.demo.domain.parking.policy.ParkingFeePolicy;
import com.example.demo.domain.parking.policy.enums.ParkingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

import java.util.List;

public interface ParkingFeePolicyRepository extends JpaRepository<ParkingFeePolicy,Long> {

    //현재 적용 중인 정책 조회
    @Query("select fp from ParkingFeePolicy fp where fp.isActive=true and :now between fp.effectiveFrom and fp.effectiveTo")
    List<ParkingFeePolicy> findCurrentEffectivePolicy(@Param("now")LocalDateTime now);

    //적용 예정인 정책 조회
    @Query("select fp from ParkingFeePolicy fp where fp.isActive=false and :now < fp.effectiveFrom")
    List<ParkingFeePolicy> findUpcomingEffectivePolicy(@Param("now")LocalDateTime now);

    // 특정 시점에 유효한 활성 정책 조회
    @Query("select p from ParkingFeePolicy p where p.parkingType=:parkingType and p.isActive=true and :now between p.effectiveFrom and p.effectiveTo order by p.id desc")
    Optional<ParkingFeePolicy> findValidPolicy(@Param("parkingType")ParkingType parkingType,@Param("now")LocalDateTime now);

    //가장 최근 정책 조회
    @Query("select p from ParkingFeePolicy p where p.parkingType=:parkingType order by p.version desc limit 1")
    Optional<ParkingFeePolicy> getLatestVersion(@Param("parkingType")ParkingType parkingType);

    //만료되었으나 isActive=true 정책들 가져오기
    @Query("select p from ParkingFeePolicy p where p.isActive=true and p.effectiveTo<=:now")
    List<ParkingFeePolicy> findPoliciesToInActivate(@Param("now")LocalDateTime now);

    // isActive=true 정책들 가져오기
    @Query("select p from ParkingFeePolicy p where p.isActive=true and p.parkingType=:parkingType")
    List<ParkingFeePolicy> findPoliciesToInactivateByType(@Param("parkingType")ParkingType parkingType );

    // 정책 시작시간이 지난 정책 중 최근 버전 가져오기
    @Query("select p from ParkingFeePolicy p where p.isActive=false and p.parkingType=:parkingType and p.effectiveFrom<:now order by p.version desc limit 1")
    List<ParkingFeePolicy> findPoliciesToActivate(@Param("now")LocalDateTime now, @Param("parkingType")ParkingType parkingType );

    // 키오스크 입차 시 현재 유효한 정책 단순 조회
    @Query("""
        SELECT p FROM ParkingFeePolicy p
        WHERE p.parkingType = :type
          AND p.isActive = true
          AND CURRENT_TIMESTAMP BETWEEN p.effectiveFrom AND p.effectiveTo
        """)
    Optional<ParkingFeePolicy> findActivePolicy(@Param("type") ParkingType type);
}
