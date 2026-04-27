package com.example.demo.domain.system.activitylog.dtos.response;

import com.example.demo.domain.system.activitylog.ActivityLog;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ActivityLogDetailDto {
    private long activityId;
    private String activityType;
    private String activityTypeLabel;
    private String userName;
    private String userPhone;
    private Integer unitNo;
    private String carNumber;
    private String message;
    private LocalDateTime createdAt;
    private Long parkingLogId;
    private Long paymentId;
    private Long reservationId;

    public static ActivityLogDetailDto from(ActivityLog a){
        return new ActivityLogDetailDto(
                a.getActivityId(),
                a.getActivityType().name(),
                a.getActivityType().getDescription(),
                a.getUser() != null ? a.getUser().getName() : null,
                a.getUser() != null ? a.getUser().getPhone() : null,
                a.getHousehold() != null ? a.getHousehold().getUnitNo() : null,
                a.getCarNumber(),
                a.getMessage(),
                a.getCreatedAt(),
                a.getParkingLog() != null ? a.getParkingLog().getParkingLogId() : null,
                a.getPayment() != null ? a.getPayment().getPaymentId() : null,
                a.getReservation() != null ? a.getReservation().getReservationId() : null
        );
    }
}
