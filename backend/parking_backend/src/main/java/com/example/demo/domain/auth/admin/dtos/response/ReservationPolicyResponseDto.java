package com.example.demo.domain.auth.admin.dtos.response;

import com.example.demo.domain.reservation.policy.ReservationEventPolicy;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReservationPolicyResponseDto {
    private final Long id;
    private final String adminLoginId;
    private final String eventName;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final Integer dailyLimitPerHousehold;
    private final Integer monthlyLimitPerHousehold;
    private final Integer maxActiveReservations;
    private final Integer permittedMinutes;
    private final boolean noShowPenaltyEnabled;
    private final String policyStatus;  // ACTIVE / SCHEDULED / EXPIRED / PERMANENT

    public ReservationPolicyResponseDto(ReservationEventPolicy p) {
        LocalDateTime now = LocalDateTime.now();
        this.id                       = p.getId();
        this.adminLoginId             = p.getAdmin().getLoginId();
        this.eventName                = p.getEventName();
        this.startDate                = p.getStartDate();
        this.endDate                  = p.getEndDate();
        this.dailyLimitPerHousehold   = p.getDailyLimitPerHousehold();
        this.monthlyLimitPerHousehold = p.getMonthlyLimitPerHousehold();
        this.maxActiveReservations    = p.getMaxActiveReservations();
        this.permittedMinutes         = p.getPermittedMinutes();
        this.noShowPenaltyEnabled     = p.isNoShowPenaltyEnabled();
        this.policyStatus             = calcStatus(p,now);
    }
    private String calcStatus(ReservationEventPolicy p, LocalDateTime now){
        if (now.isBefore(p.getStartDate())) return "SCHEDULED";
        if (p.getEndDate() == null) return "PENDING";
        if (now.isAfter(p.getEndDate())) return "EXPIRED";
        return "ACTIVE";
    }
}
