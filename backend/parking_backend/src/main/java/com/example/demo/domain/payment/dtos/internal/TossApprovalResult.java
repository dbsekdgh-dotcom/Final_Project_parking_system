package com.example.demo.domain.payment.dtos.internal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TossApprovalResult {
    private boolean success;
    private String paymentKey;
    private String errorMessage;

    public static TossApprovalResult success(String paymentKey){
        return new TossApprovalResult(true,paymentKey,null);
    }
    public static TossApprovalResult fail(String errorMessage){
        return new TossApprovalResult(false,null,errorMessage);
    }
}
