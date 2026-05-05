package com.example.demo.domain.management.dtos.request;

import com.example.demo.domain.vehicle.blacklist.enums.BlacklistReasonType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class AdminBlacklistRequestDto {

    @NotBlank
    private String carNumber;

    @NotNull
    private BlacklistReasonType reasonType;

    private String reasonDetail;

    // null이면 영구 차단 (3000-01-01)
    private LocalDateTime endDate;
}
