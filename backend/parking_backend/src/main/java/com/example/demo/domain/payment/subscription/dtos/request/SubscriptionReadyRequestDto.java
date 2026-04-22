package com.example.demo.domain.payment.subscription.dtos.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;


@Getter
@Builder
@Schema(description = "정기권 구매 준비 요청 - 차량 정보와 사용자가 선택한 시작일을 전달받아 수량 및 중복 여부를 검증합니다.")
public class SubscriptionReadyRequestDto {

    @NotBlank(message = "차량 번호는 필수입니다.")
    @Schema(description = "정기권을 등록할 차량 번호")
    private final String carNumber;
    @NotNull(message = "시작일은 필수입니다.")
    @Schema(description = "정기권 시작일 (사용자 선택)")
    private final LocalDateTime startDate;
    @NotNull(message = "결제 금액 확인이 필요합니다.")
    @Schema(description = "사용자가 확인한 결제 금액 (서버 설정값과 비교 검증용)")
    private final Long amount;


}
