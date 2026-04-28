package com.example.demo.domain.management.store.dtos.response;
import com.example.demo.domain.store.StoreTicketConfig;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StoreTicketConfigResponseDto {
    private Long storeTicketConfigId;
    private Long ticketPolicyId;
    private String ticketPolicyName;
    private int monthlyQuota;

    public static StoreTicketConfigResponseDto from(StoreTicketConfig config){
        return StoreTicketConfigResponseDto.builder()
                .storeTicketConfigId(config.getStoreTicketConfigId())
                .ticketPolicyId(config.getTicketPolicy().getTicketPolicyId())
                .ticketPolicyName(config.getTicketPolicy().getName())
                .monthlyQuota(config.getMonthlyQuota())
                .build();
    }
}
