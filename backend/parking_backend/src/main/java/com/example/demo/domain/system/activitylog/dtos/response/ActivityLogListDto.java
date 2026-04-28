package com.example.demo.domain.system.activitylog.dtos.response;

import com.example.demo.domain.system.activitylog.ActivityLog;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ActivityLogListDto {
    private long activityId;
    private String activityType;
    private String activityTypeLabel;
    private String userName;
    private Integer unitNo;
    private String carNumber;
    private String message;
    private LocalDateTime createdAt;

    public static ActivityLogListDto from(ActivityLog a){
        return new ActivityLogListDto(
                a.getActivityId(),
                a.getActivityType().name(),
                a.getActivityType().getDescription(),
                a.getUser() != null ? a.getUser().getName() : null,
                a.getHousehold() != null ? a.getHousehold().getUnitNo() : null,
                a.getCarNumber(),
                a.getMessage(),
                a.getCreatedAt()
        );
    }
}
