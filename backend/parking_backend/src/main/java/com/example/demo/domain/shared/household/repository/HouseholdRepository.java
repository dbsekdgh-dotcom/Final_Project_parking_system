package com.example.demo.domain.shared.household.repository;

import com.example.demo.domain.shared.household.Household;
import com.example.demo.domain.shared.household.enums.IsActive;
import com.example.demo.domain.shared.approval.enums.ApprovalStatus; // 추가
import com.example.demo.domain.shared.approval.enums.ApprovalType;   // 추가
import com.example.demo.domain.user.apply.dtos.projection.UnitStatusProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HouseholdRepository extends JpaRepository<Household, Long> {

    List<Household> findByIsActive(IsActive isActive);

    default List<Household> findEmptyHouseholds() {
        return findByIsActive(IsActive.INACTIVE);
    }

    Household findByUnitNo(Integer unitNo);

    // 1. 기존 쿼리 개선: 파라미터 바인딩 사용
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

    // 2. 신규 쿼리 개선: 파라미터 바인딩 사용
    @Query("SELECT h.householdId as householdId, h.unitNo as unitNo, " +
            "CASE " +
            "  WHEN h.isActive = :active THEN 'OCCUPIED' " +
            "  WHEN (SELECT COUNT(a) FROM Approval a WHERE a.targetId = h.householdId " +
            "        AND a.approvalType = :type " +
            "        AND a.status = :pending) > 0 THEN 'PENDING' " +
            "  ELSE 'AVAILABLE' " +
            "END as status " +
            "FROM Household h")
    List<UnitStatusProjection> findAllUnitStatus(
            @Param("active") IsActive active,
            @Param("type") ApprovalType type,
            @Param("pending") ApprovalStatus pending
    );
}