package com.example.demo.domain.management.dtos.response;

import com.example.demo.domain.approval.Approval;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class AdminVehicleDetailResponseDto {
    private Long vehicleId;
    private String carNumber;
    private String vehicleName;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;

    private String ownerName;
    private String ownerEmail;
    private String ownerPhone;

    private List<RegistrationHistoryDto> registrationHistory;
    private boolean hasReRegistration;

    private boolean currentlyParked;
    private LocalDateTime parkedSince;
    private String spaceCode;

    private boolean hasActiveSubscription;
    private LocalDateTime subscriptionEndDate;

    @Getter
    @Builder
    public static class RegistrationHistoryDto{
        private String requesterName;
        private LocalDateTime appliedAt;
        private String result;

        public static RegistrationHistoryDto from(Approval a){
            return RegistrationHistoryDto.builder()
                    .requesterName(a.getRequestUserId() != null ? a.getRequestUserId().getName() : "알 수 없음")
                    .appliedAt(a.getCreatedAt())
                    .result(a.getStatus().name())
                    .build();
        }
    }
}
