package com.example.demo.domain.vehicle.blacklist.repository;

import com.example.demo.domain.vehicle.blacklist.VehicleBlacklistEntity;
import com.example.demo.domain.vehicle.blacklist.enums.BlacklistReasonType;
import com.example.demo.domain.vehicle.blacklist.enums.BlacklistStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VehicleBlacklistRepository extends JpaRepository<VehicleBlacklistEntity, Long> {

    //현재 차단 중인지 환인하는 쿼리 메서드
    @Query("SELECT COUNT(v) > 0 FROM VehicleBlacklistEntity v " +
            "WHERE v.carNumber = :carNumber " +
            "AND v.status = com.example.demo.domain.vehicle.blacklist.enums.BlacklistStatus.ACTIVE " +
            "AND :now BETWEEN v.startDate AND v.endDate")
    boolean isCurrentlyBlacklisted(@Param("carNumber") String carNumber, @Param("now") LocalDateTime now);


    //차량 번호와 상태로 이미 차단된 차가 있는지 확인하는 기능
    boolean existsByCarNumberAndStatus(String carNumber, BlacklistStatus status);

    Optional<VehicleBlacklistEntity> findFirstByCarNumberOrderByCreatedAtDesc(String carNumber);

    //전체 목록
    Page<VehicleBlacklistEntity> findAll(Pageable pageable);

    Page<VehicleBlacklistEntity> findByCarNumberContaining(String carNumber,Pageable pageable);

    //차량번호, 상태, 사유 검색
    Page<VehicleBlacklistEntity> findByCarNumberContainingAndStatusAndReasonType(String carNumber,BlacklistStatus status, BlacklistReasonType reasonType, Pageable pageable);

    Page<VehicleBlacklistEntity> findByStatusAndReasonType(BlacklistStatus status,BlacklistReasonType reasonType,Pageable pageable);


}
