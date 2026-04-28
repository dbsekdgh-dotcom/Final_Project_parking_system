package com.example.demo.domain.payment.subscription.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Schema(description = "정기권 결제 승인 요청 - 토스 승인 결과와 함께 최종적으로 정기권을 활성화합니다.")
public class SubscriptionConfirmRequestDto {

    @NotBlank(message = "결제 키가 누락되었습니다.")
    private String paymentKey;

    @NotBlank(message = "주문 ID가 누락되었습니다.")
    private String orderId;

    @NotNull(message = "결제 금액은 필수입니다.")
    private Long amount;

    @NotBlank(message = "차량 번호 정보가 필요합니다.")
    private String carNumber;

    @NotNull(message = "시작일은 필수입니다.")
    private LocalDateTime startDate;

    @NotNull(message = "종료일은 필수입니다.")
    private LocalDateTime endDate;
}
