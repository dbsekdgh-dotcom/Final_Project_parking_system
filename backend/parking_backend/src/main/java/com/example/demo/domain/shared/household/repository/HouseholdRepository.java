package com.example.demo.domain.shared.household.repository;

import com.example.demo.domain.shared.household.Household;
import com.example.demo.domain.shared.household.enums.IsActive;
import com.example.demo.domain.shared.household.projections.HouseholdSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HouseholdRepository extends JpaRepository<Household,Long> {

    List<Household> findByIsActive(IsActive isActive);


    // 서비스에서 findByIsActive(IsActive.INACTIVE)라고 부르는 것보다 실수를 방지해줍니다.
    default List<Household> findEmptyHouseholds() {
        return findByIsActive(IsActive.INACTIVE);
    }

    Household findByUnitNo(Integer unitNo);

    @Query("SELECT h.unitNo FROM Household h " +
            "WHERE h.householdId NOT IN (" +
            "    SELECT a.targetId FROM Approval a " +
            "    WHERE a.approvalType = 'RESIDENT' " + // 입주 신청 타입만 필터링
            "    AND a.status IN (com.example.demo.domain.shared.approval.enums.ApprovalStatus.PENDING, " +
            "                     com.example.demo.domain.shared.approval.enums.ApprovalStatus.APPROVED)" +
            ")")
    List<Integer> findAvailableUnitNos();
}
