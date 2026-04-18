package com.example.demo.domain.reservation.dtos.request;


import com.example.demo.domain.reservation.enums.Purpose;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Schema(description = "방문 예약 신청·수정 요청 데이터 — 방문 차량 번호, 방문 목적, 방문 시작 시각을 포함합니다.")
public class ReservationApplyRequestDto {

    @NotBlank(message = "차량 번호는 필수입니다.")
    private String carNumber;
    @NotNull(message = "방문 목적은 필수입니다.")
    private Purpose purpose;

    @NotNull(message = "방문 시작 시간은 필수입니다.")
    @Future(message = "방문 예약은 미래 시간만 가능합니다.")
    private LocalDateTime visitStartAt;

}
