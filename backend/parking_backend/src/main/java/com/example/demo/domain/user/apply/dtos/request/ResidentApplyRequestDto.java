package com.example.demo.domain.user.apply.dtos.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResidentApplyRequestDto {
    @NotNull(message = "호수 ID는 필수 입력값입니다.")
    @JsonProperty("householdId")
    private Long householdId;
}
