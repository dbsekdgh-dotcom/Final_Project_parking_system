package com.example.demo.domain.resident.dashboard.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardRecentLogDto {
        private Long parkingLogId;
        private String type;
        private String carNumber;
        private String location;
        private String status;
        private String message;
        private LocalDateTime createdAt;

}
