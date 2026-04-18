package com.example.demo.domain.resident.apply.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "사용자 입주 상태 응답 — 현재 상태(RESIDENT/PENDING/NONE)와 활성 승인 ID를 반환합니다.")
public class UserStatusResponseDto {

    private String userStatus;

    private Long activeApprovalId;
}
