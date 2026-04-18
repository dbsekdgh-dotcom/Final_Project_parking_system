package com.example.demo.domain.resident.apply.dtos.response;

import com.example.demo.domain.resident.household.Household;
import com.example.demo.domain.resident.apply.dtos.projection.UnitStatusProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "신청 가능 세대 목록 항목 — 세대 ID, 호수, 활성화 상태를 포함합니다.")
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