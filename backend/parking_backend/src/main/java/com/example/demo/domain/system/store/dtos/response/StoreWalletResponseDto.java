package com.example.demo.domain.system.store.dtos.response;

import com.example.demo.domain.system.store.wallet.StoreTicketWallet;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StoreWalletResponseDto {
    private Long walletId;
    private Long ticketPolicyId;
    private String policyName;
    private String discountType;
    private Integer discountValue;
    private int issuedCount;
    private int usedCount;
    private int remainingCount;

    public static StoreWalletResponseDto from(StoreTicketWallet wallet){
        return StoreWalletResponseDto.builder()
                .walletId(wallet.getWalletId())
                .ticketPolicyId(wallet.getTicketPolicy().getTicketPolicyId())
                .policyName(wallet.getTicketPolicy().getName())
                .discountType(wallet.getTicketPolicy().getDiscountType().name())
                .discountValue(wallet.getTicketPolicy().getDiscountValue())
                .issuedCount(wallet.getIssuedCount())
                .remainingCount(wallet.getRemainingCount())
                .build();
    }

}
