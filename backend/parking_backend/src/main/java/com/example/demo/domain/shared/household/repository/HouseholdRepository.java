package com.example.demo.domain.shared.household.repository;

import com.example.demo.domain.shared.household.Household;
import com.example.demo.domain.shared.household.enums.IsActive;
import com.example.demo.domain.shared.household.projections.HouseholdSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface HouseholdRepository extends JpaRepository<Household,Long> {

}
