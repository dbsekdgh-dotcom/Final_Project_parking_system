package com.example.demo.domain.shared.household.repository;

import com.example.demo.domain.shared.household.Household;
import com.example.demo.domain.shared.household.enums.IsActive;
import com.example.demo.domain.shared.household.projections.HouseholdSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface HouseholdRepository extends JpaRepository<Household,Long> {
    @Query("SELECT h.householdId AS householdId, " +
            "h.unitNo AS unitNo, " +
            "h.createdAt AS createdAt, " +
            "h.isActive AS isActive, " +
            "h.totalVisitCount AS totalVisitCount, " +
            "h.todayVisitCount AS todayVisitCount, " +
            "h.monthlyVisitCount AS monthlyVisitCount, " +
            "h.activeReservationCount AS activeReservationCount " +
            "FROM Household h WHERE h.householdId = :householdId and h.isActive = :isActive")
    Optional<HouseholdSummary> findActiveHouseholdById(@Param("householdId") Long householdId,@Param("isActive") IsActive isActive);
}
