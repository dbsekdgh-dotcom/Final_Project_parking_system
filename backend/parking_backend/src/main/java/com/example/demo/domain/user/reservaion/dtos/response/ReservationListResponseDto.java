package com.example.demo.domain.user.reservaion.dtos.response;

import com.example.demo.domain.shared.reservation.Reservation;
import com.example.demo.domain.shared.reservation.enums.Purpose;
import com.example.demo.domain.shared.reservation.enums.Status;


import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReservationListResponseDto {

    private final Long reservationId;
    private final String carNumber;
    private final Status status;
    private final Purpose purpose;
    private final LocalDateTime visitStartAt;
    private final LocalDateTime visitEndAt;
    private final LocalDateTime createdAt;

    public ReservationListResponseDto(Reservation reservation) {
        this.reservationId = reservation.getReservationId();
        this.carNumber = reservation.getCarNumber();
        this.status = reservation.getStatus();
        this.purpose = reservation.getPurpose();
        this.visitStartAt = reservation.getVisitStartAt();
        this.visitEndAt = reservation.getVisitEndAt();
        this.createdAt = reservation.getCreatedAt();
    }


}
