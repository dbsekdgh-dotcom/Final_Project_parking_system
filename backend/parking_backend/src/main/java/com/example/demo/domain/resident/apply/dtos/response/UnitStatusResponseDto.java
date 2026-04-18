package com.example.demo.domain.resident.apply.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "전체 호수 상태 항목 응답 — 세대 선택 그리드 렌더링 시 입주 가능/불가 상태를 포함합니다.")
public class UnitStatusResponseDto {

    private Long householdId;
    private Integer unitNo;
    private String status;
}
