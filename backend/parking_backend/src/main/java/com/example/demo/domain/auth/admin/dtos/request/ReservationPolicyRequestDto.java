package com.example.demo.domain.auth.admin.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ReservationPolicyRequestDto {
    @NotBlank(message = "정책명은 필수 입니다")
    private String eventName;
    @NotNull(message = "시작일은 필수 입니다.")
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer dailyLimitPerHousehold;
    private Integer monthlyLimitPerHousehold;
    @NotNull(message = "동시 활성 예약 수는 필수 입니다.")
    private Integer maxActiveReservations;
    @NotNull(message = "허용 주차 시간은 필수 입니다.")
    private Integer permittedMinutes;
    @NotNull(message = "노쇼 페널티 여부는 필수 입니다.")
    private Boolean noShowPenaltyEnabled;
}
