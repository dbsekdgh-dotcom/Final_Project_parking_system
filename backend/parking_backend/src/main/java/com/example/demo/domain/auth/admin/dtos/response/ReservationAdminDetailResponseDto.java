package com.example.demo.domain.auth.admin.dtos.response;

import com.example.demo.domain.reservation.Reservation;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReservationAdminDetailResponseDto {
    private final Long reservationId;
    private final Long userId;
    private final String userName;
    private final Integer unitNo;
    private final boolean registeredVehicle;
    private final String carNumber;
    private final String purpose;
    private final LocalDateTime visitStartAt;
    private final LocalDateTime visitEndAt;
    private final LocalDateTime actualEntryAt;
    private final String status;
    private final boolean isFree;
    private final LocalDateTime createdAt;
    private final LocalDateTime cancelledAt;

    public ReservationAdminDetailResponseDto(Reservation r) {
        this.reservationId = r.getReservationId();
        this.userId = r.getUser().getUserId();
        this.userName = r.getUser().getName();
        this.unitNo = r.getUser().getHousehold() != null ? r.getUser().getHousehold().getUnitNo() : null;
        this.registeredVehicle = r.getVehicle() != null;
        this.carNumber = r.getCarNumber();
        this.purpose = r.getPurpose().name();
        this.visitStartAt = r.getVisitStartAt();
        this.visitEndAt = r.getVisitEndAt();
        this.actualEntryAt = r.getActual_entry_at();
        this.status = r.getStatus().name();
        this.isFree = r.isFree();
        this.createdAt = r.getCreatedAt();
        this.cancelledAt = r.getCancelledAt();
    }
}
