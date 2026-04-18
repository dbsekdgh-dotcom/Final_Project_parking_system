package com.example.demo.domain.user.reservation.dtos.response;

import com.example.demo.domain.shared.reservation.Reservation;
import com.example.demo.domain.shared.reservation.enums.Purpose;
import com.example.demo.domain.shared.reservation.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "방문 예약 상세 정보 응답 — 예약 신청·수정 후 결과로 반환되며 예약 ID, 차량 번호, 상태, 방문 일시 등을 포함합니다.")
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
