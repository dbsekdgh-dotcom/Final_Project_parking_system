package com.example.demo.domain.user.apply.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Schema(description = "입주 신청 가능한 호수 번호 목록 응답 — 드롭다운 등 선택 UI에 사용합니다.")
public class AvailableUnitResponseDto {
    private List<Integer> availableUnits;
}
