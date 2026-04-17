package com.example.demo.domain.shared.parkinglog.repository;

import com.example.demo.domain.kiosk.payment.dtos.internal.PaymentUserInfoResult;
import com.example.demo.domain.shared.parkinglog.ParkingLog;
import com.example.demo.domain.shared.parkinglog.dtos.response.ParkingLogSettlementDto;
import com.example.demo.domain.shared.parkinglog.enums.ParkingStatus;
import com.example.demo.domain.shared.parkinglog.enums.PaymentStatus;
import com.example.demo.domain.shared.parkingspace.enums.Floor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ParkingLogRepository extends JpaRepository<ParkingLog,Long>, ParkingLogRepositoryCustom {
    //차량번호 4자리 입력 후 차량 조회 시 조회될 차량번호 목록
    @Query("select new com.example.demo.domain.shared.parkinglog.dtos.response.ParkingLogSettlementDto(" +
            "p.parkingLogId,p.carNumberSnapshot, u.userId, up.currentPoint) " +
            "from ParkingLog p " +
            "left join p.vehicle v " +
            "left join v.user u " +
            "left join UserPoint up on up.user = u " +
            "where p.carNumberSnapshot like %:vehicleNumber% and p.exitedAt is null and p.enteredAt is Not null")
    List<ParkingLogSettlementDto> getActiveVehicleList(@Param("vehicleNumber") String vehicleNumber);

    @Query("select p " +
            "from ParkingLog p " +
            "left join fetch p.vehicle v " +
            "left join fetch v.user u " +
            "left join fetch UserPoint up on up.user = u " +
            "where p.parkingLogId=:parkingLogId")
    Optional<ParkingLog> getDetailLogInfo(Long parkingLogId);

    Optional<ParkingLog> findByParkingLogId(Long parkingLogId);


    // 차번호 + 상태로 조회 (입차/출차 분기 판단용)
    Optional<ParkingLog> findFirstByCarNumberSnapshotAndParkingStatus(String carNumber, ParkingStatus status);

    //차량번호 스냅샷에 키워드가 포함된 데이터를 페이징하여 조회
    Page<ParkingLog> findByCarNumberSnapshotContaining(String carNumber, Pageable pageable);

    //현재 주차중 (입차 완료(ENTERED) 상태이면서 출차시간이 없는경우)
    Page<ParkingLog> findByParkingStatusAndExitedAtIsNull(ParkingStatus status, Pageable pageable);

    //미납
    Page<ParkingLog> findByPaymentStatus(PaymentStatus status,Pageable pageable);

    //금일 출차완료 (출차시간이 오늘 00:00:00~23:59:59 사이)
    Page<ParkingLog> findByExitedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    //금일 로그 (오늘 생성된 모든 데이터)
    Page<ParkingLog> findByEntryTimeBetween(LocalDateTime start,LocalDateTime end,Pageable pageable);

    // DETECTED 상태로 cutoff 시간보다 오래된 로그 일괄 ENTRY_CANCELLED 처리
    @Modifying
    @Query("UPDATE ParkingLog p SET p.parkingStatus = 'ENTRY_CANCELLED' " +
           "WHERE p.parkingStatus = 'DETECTED' AND p.entryTime < :cutoff")
    int cancelExpiredDetected(@Param("cutoff") LocalDateTime cutoff);


    // 방치된 EXIT_REQUESTED 차량 조회
    @Query("SELECT p FROM ParkingLog  p WHERE p.parkingStatus='EXIT_REQUESTED' AND p.exitTime<:cutoff")
    List<ParkingLog> findStaledExitRequestedLogs(@Param("cutoff")LocalDateTime cutoff);

    @Query("SELECT COUNT(p) > 0 FROM ParkingLog p " +
            "WHERE p.carNumberSnapshot = :carNumber " +
            "AND p.parkingStatus IN (com.example.demo.domain.shared.parkinglog.enums.ParkingStatus.DETECTED, " +
            "                        com.example.demo.domain.shared.parkinglog.enums.ParkingStatus.ENTERED, " +
            "                        com.example.demo.domain.shared.parkinglog.enums.ParkingStatus.EXIT_REQUESTED)")
    boolean isAlreadyInParkingLot(@Param("carNumber") String carNumber);

    List<ParkingLog> findTop5ByCarNumberSnapshotOrderByEntryTimeDesc(String carNumber);

    //해당 층에 현재 주차중인 차량들 조회
    @Query("select pl from ParkingLog pl " +
            "join fetch pl.parkingSpace ps " +
            "where ps.floor =:floor and pl.parkingStatus = 'ENTERED'")
    List<ParkingLog> findActiveLogsByFloor(@Param("floor") Floor floor);

    // 차량 ID로 현재 입차 중인 로그가 있는지 확인 (출차 전 상태들)
    // 그 중에서도 혜택을 받는 타입(RESIDENT, SUBSCRIPTION, RESERVATION)인지 확인
    @Query("SELECT EXISTS (SELECT 1 FROM ParkingLog p " +
            "WHERE p.vehicle.id = :vehicleId " +
            "AND p.parkingStatus IN ('ENTERED', 'DETECTED', 'EXIT_REQUESTED') " +
            "AND p.parkingTypeSnapshot IN ('RESIDENT', 'SUBSCRIPTION'))")
    boolean existsActiveBenefitLogByVehicleId(@Param("vehicleId") Long vehicleId);
}
