package com.example.demo.domain.parking.chatbot.dtos.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KioskFeePolicyResponseDto {
    private List<FeePolicyDto> policies;
    private int postPaymentGraceMinutes;
    private List<DiscountTicketDto> discountTickets;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class FeePolicyDto{
        private String parkingType;
        private String label;
        private int turnaroundGraceMinutes;
        private int baseFee;
        private int unitMinutes;
        private int unitFee;
        private int dailyMaxFee;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class DiscountTicketDto{
        private String name;
        private String discountType;
        private int discountAmount;
        private int price;
    }
}
