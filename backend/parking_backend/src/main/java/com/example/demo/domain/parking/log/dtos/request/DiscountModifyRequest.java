package com.example.demo.domain.parking.log.dtos.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscountModifyRequest {
    @NotNull(message = "적용할 할인 정책 ID는 필수입니다.")
    private Long ticketPolicyId;

    @NotBlank(message = "할인 수정 사유를 입력해주세요.")
    @Size(max = 200, message = "사유는 200자 이내로 입력해주세요.")
    private String reason;
}
