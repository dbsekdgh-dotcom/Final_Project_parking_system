package com.example.demo.domain.user.reservaion.dtos.response;

import com.example.demo.domain.shared.reservation.Reservation;
import com.example.demo.domain.shared.reservation.enums.Purpose;
import com.example.demo.domain.shared.reservation.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReservationDetailResponseDto {

    private Long reservationId;
    private String carNumber;
    private Status status;
    private Purpose purpose;
    private LocalDateTime visitStartAt;
    private LocalDateTime visitEndAt;
    private LocalDateTime createdAt;


    public static ReservationDetailResponseDto fromEntity(Reservation reservation) {
        return ReservationDetailResponseDto.builder()
                .reservationId(reservation.getReservationId())
                .carNumber(reservation.getCarNumber())
                .status(reservation.getStatus()) // .name() 제거! Enum 객체 그대로 전달
                .purpose(reservation.getPurpose())
                .visitStartAt(reservation.getVisitStartAt())
                .visitEndAt(reservation.getVisitEndAt())
                .createdAt(reservation.getCreatedAt())
                .build();
    }
}
