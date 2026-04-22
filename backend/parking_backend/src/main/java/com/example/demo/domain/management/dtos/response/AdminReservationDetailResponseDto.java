package com.example.demo.domain.management.dtos.response;

import com.example.demo.domain.reservation.Reservation;
import com.example.demo.domain.resident.household.Household;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminReservationDetailResponseDto {
    private Long reservationId;
    private String carNumber;
    private String purpose;
    private LocalDateTime visitStartAt;
    private LocalDateTime visitEndAt;
    private LocalDateTime actualEntryAt;
    private String status;
    private boolean isFree;
    private LocalDateTime createdAt;
    private LocalDateTime cancelledAt;

    private String requesterName;
    private String requesterEmail;
    private String requesterPhone;

    private Integer unitNo;
    private String householdStatus;

    public static AdminReservationDetailResponseDto from(Reservation r){
        Household h = r.getUser().getHousehold();
        return AdminReservationDetailResponseDto.builder()
                .reservationId(r.getReservationId())
                .carNumber(r.getCarNumber())
                .purpose(r.getPurpose().name())
                .visitStartAt(r.getVisitStartAt())
                .visitEndAt(r.getVisitEndAt())
                .actualEntryAt(r.getActual_entry_at())
                .status(r.getStatus().name())
                .isFree(r.isFree())
                .createdAt(r.getCreatedAt())
                .cancelledAt(r.getCancelledAt())
                .requesterName(r.getUser().getName())
                .requesterEmail(r.getUser().getEmail())
                .requesterPhone(r.getUser().getPhone())
                .unitNo(h != null ? h.getUnitNo() : null)
                .householdStatus( h != null ? h.getIsActive().name() : null)
                .build();
    }
}
