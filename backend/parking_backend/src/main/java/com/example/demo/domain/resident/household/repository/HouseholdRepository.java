package com.example.demo.domain.resident.household.repository;

import com.example.demo.domain.resident.household.Household;
import com.example.demo.domain.resident.household.enums.IsActive;
import com.example.demo.domain.approval.enums.ApprovalStatus; // 추가
import com.example.demo.domain.approval.enums.ApprovalType;   // 추가
import com.example.demo.domain.resident.apply.dtos.projection.UnitStatusProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HouseholdRepository extends JpaRepository<Household, Long> {

    List<Household> findByIsActive(IsActive isActive);

    default List<Household> findEmptyHouseholds() {
        return findByIsActive(IsActive.INACTIVE);
    }

    Household findByUnitNo(Integer unitNo);

    /**
     * [개선 1] 신청 가능한 호수 목록 조회 (기존 로직 유지)
     */
    @Query("SELECT h.unitNo FROM Household h " +
            "WHERE h.householdId NOT IN (" +
            "    SELECT a.targetId FROM Approval a " +
            "    WHERE a.approvalType = :type " +
            "    AND a.status IN (:pending, :approved)" +
            ")")
    List<Integer> findAvailableUnitNos(
            @Param("type") ApprovalType type,
            @Param("pending") ApprovalStatus pending,
            @Param("approved") ApprovalStatus approved
    );

    /**
     * [개선 2] 전체 세대 상태 조회 (성능 최적화 버전)
     * 서브쿼리를 제거하고 LEFT JOIN을 사용하여 단 1회의 쿼리로 처리합니다.
     */
    @Query("SELECT h.householdId as householdId, h.unitNo as unitNo, " +
            "CASE " +
            "  WHEN h.isActive = :active THEN 'OCCUPIED' " +
            "  WHEN a.approvalId IS NOT NULL THEN 'PENDING' " +
            "  ELSE 'AVAILABLE' " +
            "END as status " +
            "FROM Household h " +
            "LEFT JOIN Approval a ON a.targetId = h.householdId " + // JOIN으로 변경
            "AND a.approvalType = :type " +
            "AND a.status = :pending")
    List<UnitStatusProjection> findAllUnitStatus(
            @Param("active") IsActive active,
            @Param("type") ApprovalType type,
            @Param("pending") ApprovalStatus pending
    );

    @Modifying
    @Query("UPDATE Household h SET h.activeReservationCount = h.activeReservationCount + 1 WHERE h.householdId = :id")
    void incrementActiveReservationCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Household h SET h.totalVisitCount = h.totalVisitCount + 1 WHERE h.householdId = :id")
    void incrementTotalVisitCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Household h SET h.activeReservationCount = h.activeReservationCount - 1 WHERE h.householdId = :id AND h.activeReservationCount > 0")
    void decrementActiveReservationCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Household h SET h.todayVisitCount = 0")
    void resetTodayVisitCount();

    @Modifying
    @Query("UPDATE Household h SET h.monthlyVisitCount = 0")
    void resetMonthlyVisitCount();
}