package com.example.demo.domain.user.reservaion.dtos.response;

import com.example.demo.domain.shared.reservation.Reservation;
import com.example.demo.domain.shared.reservation.enums.Status;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
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
