package com.example.demo.domain.management.dtos.request;

import com.example.demo.domain.vehicle.blacklist.enums.BlacklistReasonType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminBlacklistRequestDto {

    private String carNumber;
    private BlacklistReasonType reasonType;
    private String reasonDetail;

    private LocalDateTime endDate;
}
