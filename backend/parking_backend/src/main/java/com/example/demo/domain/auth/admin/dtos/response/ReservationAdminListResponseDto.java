package com.example.demo.domain.auth.admin.dtos.response;

import com.example.demo.domain.reservation.Reservation;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReservationAdminListResponseDto {
    private final Long reservationId;
    private final String userName;
    private final Integer unitNo;
    private final String carNumber;
    private final String purpose;
    private final LocalDateTime visitStartAt;
    private final LocalDateTime visitEndAt;
    private final LocalDateTime createdAt;
    private final String status;

    public ReservationAdminListResponseDto(Reservation r){
        this.reservationId = r.getReservationId();
        this.userName = r.getUser().getName();
        this.unitNo = r.getUser().getHousehold() != null ? r.getUser().getHousehold().getUnitNo() : null;
        this.carNumber = r.getCarNumber();
        this.purpose = r.getPurpose().name();
        this.visitStartAt = r.getVisitStartAt();
        this.visitEndAt = r.getVisitEndAt();
        this.createdAt = r.getCreatedAt();
        this.status = r.getStatus().name();
    }
}
