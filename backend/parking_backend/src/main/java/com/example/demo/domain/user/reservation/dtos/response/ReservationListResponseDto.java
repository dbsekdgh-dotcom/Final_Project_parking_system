package com.example.demo.domain.user.reservation.dtos.response;

import com.example.demo.domain.shared.reservation.Reservation;
import com.example.demo.domain.shared.reservation.enums.Purpose;
import com.example.demo.domain.shared.reservation.enums.Status;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Schema(description = "방문 예약 목록 항목 응답 — 내 전체 방문 예약 목록 조회 시 각 항목에 대한 요약 정보를 반환합니다.")
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
