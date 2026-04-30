package com.example.demo.domain.reservation.repository;

import com.example.demo.domain.dashboard.dtos.response.UsageDailyProjection;
import com.example.demo.domain.reservation.Reservation;
import com.example.demo.domain.reservation.enums.Purpose;
import com.example.demo.domain.reservation.enums.Status;
import com.example.demo.domain.resident.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    //방문예약자가 입차&요금정책&출차시간 이내 출차인지
    @Query("select count(r.reservationId) from Reservation r where r.carNumber=:carNumber and r.status=:status and r.visitEndAt>current_timestamp and r.isFree=:isFree")
    int getCountbyCarNumber(@Param("carNumber") String carNumber, @Param("status") Status status, @Param("isFree") Boolean isFree);

    //isFree = false 업데이트 쿼리
    @Modifying
    @Transactional
    @Query("UPDATE Reservation r SET r.isFree = false WHERE r.carNumber = :carNumber AND r.status= 'ENTERED'")
    void updateIsFreeByCarNumber(@Param("carNumber") String carNumber);

    //status = ENTERED 업데이트 쿼리
    @Modifying
    @Transactional
    @Query("UPDATE Reservation r SET r.status = 'ENTERED' WHERE r.carNumber = :carNumber AND r.status = 'RESERVED' AND current_timestamp BETWEEN r.visitStartAt AND r.visitEndAt")
    void updateStatusToEntered(@Param("carNumber") String carNumber);


    /**
     * 1. 중복 신청 방지
     * 특정 차량이 현재 '대기(PENDING)' 또는 '승인(RESERVED)' 상태인 예약이 있는지 확인
     */
    boolean existsByCarNumberAndStatusIn(String carNumber, List<Status> statuses);

    /**
     * 2. 세대별 동시 활성 예약 수 체크
     * Household의 activeReservationCount 컬럼이 있지만, DB 데이터 정합성을 위해
     * 실제 Reservation 테이블에서 카운트하는 쿼리가 필요할 때가 있습니다.
     */
    @Query("SELECT COUNT(r) FROM Reservation r " +
            "WHERE r.user.household.householdId = :householdId " +
            "AND r.status IN :statuses")
    long countByHouseholdIdAndStatusIn(@Param("householdId") Long householdId,
                                       @Param("statuses") List<Status> statuses);

    /**
     * 3. 세대별 일일 예약 횟수 체크 (정책 검증용)
     * 오늘(00:00:00 ~ 23:59:59) 해당 세대에서 신청한 총 예약 건수 (취소 제외)
     */
    @Query("SELECT COUNT(r) FROM Reservation r " +
            "WHERE r.user.household.householdId = :householdId " +
            "AND r.createdAt >= :startOfDay AND r.createdAt <= :endOfDay " +
            "AND r.status != 'CANCELLED'")
    long countDailyReservations(@Param("householdId") Long householdId,
                                @Param("startOfDay") LocalDateTime startOfDay,
                                @Param("endOfDay") LocalDateTime endOfDay);


    @Query("SELECT r FROM Reservation r JOIN FETCH r.user WHERE r.user = :user ORDER BY r.createdAt DESC")
    List<Reservation> findByUserOrderByCreatedAtDesc(@Param("user") User user);



    /**
     * 4. 세대별 월간 총 예약 횟수 체크 (정책 검증용 - 추가된 부분)
     */
    @Query("SELECT COUNT(r) FROM Reservation r " +
            "WHERE r.user.household.householdId = :householdId " +
            "AND r.createdAt >= :startOfMonth " +
            "AND r.status NOT IN (com.example.demo.domain.reservation.enums.Status.CANCELLED, " +
            "                     com.example.demo.domain.reservation.enums.Status.REJECTED)")
    long countMonthlyReservations(@Param("householdId") Long householdId,
                                  @Param("startOfMonth") LocalDateTime startOfMonth);

    /**
     * 5. 아파트 전체 세대 일일 총 예약 횟수 체크
     * 오늘 신청된 모든 예약(취소/거절 제외)의 총합을 구합니다.
     */
    @Query("SELECT COUNT(r) FROM Reservation r " +
            "WHERE r.createdAt >= :startOfDay AND r.createdAt <= :endOfDay " +
            "AND r.status NOT IN (com.example.demo.domain.reservation.enums.Status.CANCELLED, " +
            "                     com.example.demo.domain.reservation.enums.Status.REJECTED)")
    long countAllDailyReservations(@Param("startOfDay") LocalDateTime startOfDay,
                                   @Param("endOfDay") LocalDateTime endOfDay);



    @Query("SELECT COUNT(r) > 0 FROM Reservation r " +
            "WHERE r.carNumber = :carNumber " +
            "AND r.reservationId != :currentResId " +
            "AND r.status IN :statuses")
    boolean existsByCarNumberAndStatusInAndReservationIdNot(
            @Param("carNumber") String carNumber,
            @Param("statuses") List<Status> statuses,
            @Param("currentResId") Long currentResId
    );

    @Modifying
    @Transactional
    @Query("UPDATE Reservation r SET r.status = :status WHERE r.carNumber = :carNumber AND r.status =:currentStatus")
    void updateStatusToCompleted(@Param("carNumber") String carNumber,
                                 @Param("status") Status status,
                                 @Param("currentStatus") Status currentStatus
    );

    @Query("""
        SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
        FROM Reservation r
        WHERE r.carNumber = :carNumber
          AND r.status = 'RESERVED'
          AND CURRENT_TIMESTAMP BETWEEN r.visitStartAt AND r.visitEndAt
        """)
    boolean existsValidReservation(@Param("carNumber") String carNumber);

    @Query("SELECT r FROM Reservation r WHERE r.user.userId = :userId AND r.status IN :statuses")
    List<Reservation> findByUserIdAndStatusIn(@Param("userId") Long userId,
                                              @Param("statuses") List<Status> statuses);

    // 노쇼 처리: visitEndAt 이 지났는데 RESERVED 또는 PENDING 상태인 예약 → NO_SHOW
    @Modifying
    @Query("""
        UPDATE Reservation r
        SET r.status = 'NO_SHOW'
        WHERE r.status IN ('RESERVED', 'PENDING')
          AND r.visitEndAt < :now
        """)
    int bulkMarkNoShow(@Param("now") LocalDateTime now);

    //Admin/UserVehicle Page 용

    @Query(
            value = """
            SELECT r FROM Reservation r JOIN FETCH r.user u
            WHERE r.status != 'PENDING'
            AND (:keyword IS NULL OR r.carNumber LIKE %:keyword% OR u.name LIKE %:keyword%)
            AND (:status IS NULL OR r.status = :status)
            ORDER BY r.createdAt DESC
            """,
            countQuery = """
            SELECT COUNT(r) FROM Reservation r JOIN r.user u
            WHERE r.status != 'PENDING'
            AND (:keyword IS NULL OR r.carNumber LIKE %:keyword% OR u.name LIKE %:keyword%)
            AND (:status IS NULL OR r.status = :status)
"""
    )
    Page<Reservation> findAllForAdmin(
            @Param("keyword") String keyword,
            @Param("status") Status status,
            Pageable pageable
    );

    //AdminReservaion 관리
    @Query(
            value = """
      SELECT r FROM Reservation r
      JOIN FETCH r.user u
      LEFT JOIN FETCH u.household h
      WHERE (:keyword IS NULL
             OR r.carNumber LIKE %:keyword%
             OR u.name LIKE %:keyword%
             OR CAST(h.unitNo AS string) LIKE %:keyword%)
      AND (:status IS NULL OR r.status = :status)
      AND (:purpose IS NULL OR r.purpose = :purpose)
      AND (:startDate IS NULL OR r.visitStartAt >= :startDate)
      AND (:endDate IS NULL OR r.visitStartAt <= :endDate)
      ORDER BY r.createdAt DESC
      """,
            countQuery = """
      SELECT COUNT(r) FROM Reservation r
      JOIN r.user u
      LEFT JOIN u.household h
      WHERE (:keyword IS NULL
             OR r.carNumber LIKE %:keyword%
             OR u.name LIKE %:keyword%
             OR CAST(h.unitNo AS string) LIKE %:keyword%)
      AND (:status IS NULL OR r.status = :status)
      AND (:purpose IS NULL OR r.purpose = :purpose)
      AND (:startDate IS NULL OR r.visitStartAt >= :startDate)
      AND (:endDate IS NULL OR r.visitStartAt <= :endDate)
      """
    )
    Page<Reservation> findAllForAdminFull(
            @Param("keyword") String keyword,
            @Param("status") Status status,
            @Param("purpose") Purpose purpose,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
    // 통계용 오늘 예약 건수 status 별 집계
    @Query("""
      SELECT r.status, COUNT(r)
      FROM Reservation r
      WHERE r.createdAt >= :startOfDay AND r.createdAt <= :endOfDay
      GROUP BY r.status
      """)
    List<Object[]> countTodayByStatus(
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );
    //Admin Dashboard 사용량
    @Query("SELECT COUNT(r) FROM Reservation r " +
            "WHERE r.createdAt BETWEEN :start AND :end " +
            "AND r.status IN " +
            "  (com.example.demo.domain.reservation.enums.Status.COMPLETED, " +
            "   com.example.demo.domain.reservation.enums.Status.ENTERED)")
    long countCompletedBetween(
            @Param("start") LocalDateTime start,
            @Param("end")   LocalDateTime end);

    @Query(value =
            "SELECT DATE_FORMAT(r.created_at, '%Y-%m-%d') AS date, " +
                    "'방문예약' AS category, " +
                    "COUNT(*) AS usageCount, " +
                    "COUNT(*) AS transactionCount " +
                    "FROM reservation r " +
                    "WHERE r.created_at BETWEEN :from AND :to " +
                    "  AND r.status IN ('COMPLETED', 'ENTERED') " +
                    "GROUP BY DATE_FORMAT(r.created_at, '%Y-%m-%d') " +
                    "ORDER BY DATE_FORMAT(r.created_at, '%Y-%m-%d') DESC",
            nativeQuery = true)
    List<UsageDailyProjection> findDailyCompletedRows(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to
    );
}
