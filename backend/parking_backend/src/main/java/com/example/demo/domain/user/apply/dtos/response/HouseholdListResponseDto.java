package com.example.demo.domain.user.apply.dtos.response;

import com.example.demo.domain.shared.household.Household;
import com.example.demo.domain.user.apply.dtos.projection.UnitStatusProjection;
import lombok.Getter;

@Getter
public class HouseholdListResponseDto {

    private final Long householdId;
    private final Integer unitNo;
    private String status; // 필드 추가

    // [추가] 1. 기존 엔티티를 위한 생성자 (findAllActiveHouseholds용)
    public HouseholdListResponseDto(Household household) {
        this.householdId = household.getHouseholdId();
        this.unitNo = household.getUnitNo();
        this.status = household.getIsActive().name(); // 엔티티의 활성화 상태를 넣어줌
    }

    // [유지] 2. 프로젝션을 위한 생성자 (성능 최적화 쿼리용)
    public HouseholdListResponseDto(UnitStatusProjection projection) {
        this.householdId = projection.getHouseholdId();
        this.unitNo = projection.getUnitNo();
        this.status = projection.getStatus();
    }
}