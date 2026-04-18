package com.example.demo.domain.resident.apply.dtos.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "입주 신청 요청 데이터")
public class ResidentApplyRequestDto {
    @NotNull(message = "호수 ID는 필수 입력값입니다.")
    @JsonProperty("householdId")
    private Long householdId;
}
