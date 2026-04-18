package com.example.demo.domain.user.reservation.dtos.response;

import com.example.demo.domain.shared.reservation.Reservation;
import com.example.demo.domain.shared.reservation.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Schema(description = "방문 예약 취소 결과 응답 — 취소된 예약 ID, 차량 번호, 변경된 상태(CANCELLED), 취소 시각을 반환합니다.")
public class ReservationCancelResponseDto {

    private final Long reservationId;
    private final String carNumber;
    private final Status status;
    private final LocalDateTime cancelledAt;

    public ReservationCancelResponseDto(Reservation reservation) {
        this.reservationId = reservation.getReservationId();
        this.carNumber = reservation.getCarNumber();
        this.status = reservation.getStatus();
        this.cancelledAt = reservation.getCancelledAt();
    }
}
