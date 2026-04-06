package com.example.demo.domain.kiosk.entry.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EntryCheckResponse {
    private Long vehicleId;
    private String carNumber;
    private boolean isResident;
    private boolean hasActiveSubscription;
}
