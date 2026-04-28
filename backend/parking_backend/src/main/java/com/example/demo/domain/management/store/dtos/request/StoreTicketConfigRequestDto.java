package com.example.demo.domain.management.store.dtos.request;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StoreTicketConfigRequestDto {
    private Long ticketPolicyId;
    @Min(value = 1,message = "월 지급 수량을 1 이상으로 입력해주세요.")
    private int monthlyQuota;
}
