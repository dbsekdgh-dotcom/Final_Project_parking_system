package com.example.demo.domain.parking.log.repository;

import com.example.demo.domain.dashboard.dtos.response.UsageDailyProjection;
import com.example.demo.domain.payment.dtos.internal.PaymentUserInfoResult;
import com.example.demo.domain.parking.log.ParkingLog;
import com.example.demo.domain.parking.log.dtos.response.ParkingLogSettlementDto;
import com.example.demo.domain.parking.log.enums.ParkingStatus;
import com.example.demo.domain.parking.log.enums.PaymentStatus;
import com.example.demo.domain.parking.space.enums.Floor;
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
    @Query("select new com.example.demo.domain.parking.log.dtos.response.ParkingLogSettlementDto(" +
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
            "AND p.parkingStatus IN (com.example.demo.domain.parking.log.enums.ParkingStatus.DETECTED, " +
            "                        com.example.demo.domain.parking.log.enums.ParkingStatus.ENTERED, " +
            "                        com.example.demo.domain.parking.log.enums.ParkingStatus.EXIT_REQUESTED)")
    boolean isAlreadyInParkingLot(@Param("carNumber") String carNumber);

    @Query("SELECT COUNT(p) > 0 FROM ParkingLog p " +
            "WHERE p.carNumberSnapshot IN :carNumbers " +
            "AND p.parkingStatus IN (com.example.demo.domain.parking.log.enums.ParkingStatus.ENTERED, " +
            "                        com.example.demo.domain.parking.log.enums.ParkingStatus.EXIT_REQUESTED)")
    boolean existsActiveByCarNumbers(@Param("carNumbers") List<String> carNumbers);

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

    //Kiosk 상가 - 내부 차량 찾기
    @Query("SELECT p FROM ParkingLog p " +
            "WHERE p.carNumberSnapshot LIKE %:query% " +
            "AND p.exitedAt IS NULL AND p.enteredAt IS NOT NULL")
    List<ParkingLog> findActiveByCarNumberContaining(@Param("query") String query);

    @Query("SELECT p FROM ParkingLog p LEFT JOIN FETCH p.parkingSpace WHERE p.carNumberSnapshot LIKE %:query% AND p.exitedAt IS NULL AND p.enteredAt IS NOT NULL ")
    List<ParkingLog> findActiveWithSpaceByCarNumber(@Param("query") String query);

    //Admin/UserVehicle Page용
    @Query("SELECT p FROM ParkingLog p LEFT JOIN FETCH p.parkingSpace WHERE p.carNumberSnapshot = :carNumber AND p.exitedAt IS NULL AND p.enteredAt IS NOT NULL ORDER BY p.enteredAt DESC LIMIT 1")
    Optional<ParkingLog> findCurrentParkingByCarNumber(@Param("carNumber") String carNumber);

    //정상적인 입차완료와 출차완료 상태만 최신순으로 가져오기
    @Query("SELECT p FROM ParkingLog p " +
            "WHERE (p.vehicle.user.userId = :userId OR " +
            "       EXISTS (SELECT r FROM Reservation r " +
            "               WHERE r.vehicle = p.vehicle " +
            "               AND r.user.userId = :userId)) " +
            "AND p.parkingStatus IN (com.example.demo.domain.parking.log.enums.ParkingStatus.ENTERED, " +
            "                        com.example.demo.domain.parking.log.enums.ParkingStatus.EXITED)")
    Page<ParkingLog> findMyAndReservedLogs(@Param("userId") Long userId, Pageable pageable);


    //기간별 로그
    @Query("select p from ParkingLog p " +
            "where p.exitedAt >= :start and p.exitedAt < :end and p.enteredAt is not null")
    List<ParkingLog> findParkingLogWithPolicy(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    //Admin Dashboard 사용량
    @Query("SELECT COUNT(p) FROM ParkingLog p WHERE p.exitedAt BETWEEN :start AND :end AND p.parkingStatus IN (com.example.demo.domain.parking.log.enums.ParkingStatus.EXITED, com.example.demo.domain.parking.log.enums.ParkingStatus.FORCE_EXITED)")
    long countExitedBetween(@Param("start")LocalDateTime start,
                            @Param("end")LocalDateTime end);

    @Query(value =
            "SELECT DATE_FORMAT(p.exited_at, '%Y-%m-%d') AS date, " +
                    "'주차장' AS category, " +
                    "COUNT(*) AS usageCount, " +
                    "COUNT(*) AS transactionCount " +
                    "FROM parking_log p " +
                    "WHERE p.exited_at BETWEEN :from AND :to " +
                    "  AND p.parking_status IN ('EXITED', 'FORCE_EXITED') " +
                    "GROUP BY DATE(p.exited_at) " +
                    "ORDER BY DATE(p.exited_at) DESC",
            nativeQuery = true)
    List<UsageDailyProjection> findDailyExitedRows(@Param("from") LocalDateTime from,
                                                   @Param("to") LocalDateTime to);
}
