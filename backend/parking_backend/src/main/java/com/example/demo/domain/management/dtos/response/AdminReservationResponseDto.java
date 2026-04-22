package com.example.demo.domain.management.dtos.response;

import com.example.demo.domain.reservation.Reservation;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminReservationResponseDto {
    private Long reservationId;
    private String userName;
    private String carNumber;
    private String purpose;
    private LocalDateTime visitStartAt;
    private LocalDateTime visitEndAt;
    private String status;
    private boolean isFree;
    private LocalDateTime createdAt;

    public static AdminReservationResponseDto from(Reservation r) {
        return AdminReservationResponseDto.builder()
                .reservationId(r.getReservationId())
                .userName(r.getUser().getName())
                .carNumber(r.getCarNumber())
                .purpose(r.getPurpose().name())
                .visitStartAt(r.getVisitStartAt())
                .visitEndAt(r.getVisitEndAt())
                .status(r.getStatus().name())
                .isFree(r.isFree())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
