package com.example.demo.domain.admin.management.fee.dtos.request;

import com.example.demo.domain.admin.repository.AdminRepository;
import com.example.demo.domain.shared.parkingfeepolicy.ParkingFeePolicy;
import com.example.demo.domain.shared.parkingfeepolicy.enums.ParkingType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
@ToString
@NoArgsConstructor
public class ParkingFeePolicyChangeRequestDto {
    private Long parkingFeePolicyId;
    private Long adminId;
    private ParkingType parkingType;
    private int graceMinutes;
    private int baseFee;
    private int unitMinutes;
    private int unitFee;
    private int daliyMaxFee;
    private boolean active;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime effectiveFrom;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime effectiveTo;
    private Long version;
}
