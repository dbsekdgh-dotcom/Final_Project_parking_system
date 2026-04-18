package com.example.demo.domain.resident.household.projections;

import com.example.demo.domain.resident.household.enums.IsActive;

import java.time.LocalDateTime;

//jpa 쿼리 작성시 반환타입을 인터페이스로 지정하면, 규칙에 따라 jpa에서 구현체를 생성 및 데이터를 조회할 수 있게 함
public interface HouseholdSummary {
    Long getHouseholdId();
    String getUnitNo();
    LocalDateTime getCreatedAt();
    IsActive getIsActive();
    Integer getTotalVisitCount();
    Integer getTodayVisitCount();
    Integer getMonthlyVisitCount();
    Integer getActiveReservationCount();
}
