package com.example.demo.domain.shared.reservation.repository;

import com.example.demo.domain.shared.reservation.Reservation;
import com.example.demo.domain.shared.reservation.enums.Status;
import com.example.demo.domain.shared.user.User;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    //방문예약자가 입차&요금정책&출차시간 이내 출차인지
    @Query("select count(r.reservationId) from Reservation r where r.carNumber=:carNumber and r.status=:status and r.visitEndAt>current_timestamp and r.isFree=:isFree")
    int getCountbyCarNumber(@Param("carNumber") String carNumber, @Param("status") Status status, @Param("isFree") Boolean isFree);


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
            "AND r.status NOT IN (com.example.demo.domain.shared.reservation.enums.Status.CANCELLED, " +
            "                     com.example.demo.domain.shared.reservation.enums.Status.REJECTED)")
    long countMonthlyReservations(@Param("householdId") Long householdId,
                                  @Param("startOfMonth") LocalDateTime startOfMonth);

    /**
     * 5. 아파트 전체 세대 일일 총 예약 횟수 체크
     * 오늘 신청된 모든 예약(취소/거절 제외)의 총합을 구합니다.
     */
    @Query("SELECT COUNT(r) FROM Reservation r " +
            "WHERE r.createdAt >= :startOfDay AND r.createdAt <= :endOfDay " +
            "AND r.status NOT IN (com.example.demo.domain.shared.reservation.enums.Status.CANCELLED, " +
            "                     com.example.demo.domain.shared.reservation.enums.Status.REJECTED)")
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
}
