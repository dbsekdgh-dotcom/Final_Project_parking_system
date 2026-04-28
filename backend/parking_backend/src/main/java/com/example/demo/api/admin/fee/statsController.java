package com.example.demo.api.admin.fee;

import com.example.demo.domain.payment.statistics.dtos.request.StatsRequestDto;
import com.example.demo.domain.payment.statistics.dtos.response.RevenueAnalysisDto;
import com.example.demo.domain.payment.statistics.dtos.response.RevenueResponseDto;
import com.example.demo.domain.payment.statistics.service.PaymentUsageStats;
import com.example.demo.domain.payment.statistics.service.RevenueStats;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Tag(name = "7. 통계 (Statistics)", description = "요금 관리 탭 매출 통계 및 결제 수단 분석 API")
@Slf4j
@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
public class statsController {
    private final RevenueStats revenueStats;
    private final PaymentUsageStats paymentUsageStats;

    @Operation(summary = "기간별 매출 통계", description = "start~end 기간의 일별 매출 합계를 반환합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @GetMapping
    public RevenueResponseDto stats(@ModelAttribute StatsRequestDto dto){
        return revenueStats.revenueReponse(dto.getStart(),dto.getEnd());
    }

    @Operation(summary = "결제 수단별 분석", description = "start~end 기간의 결제 수단(카드/포인트/할인권 등)별 비율 및 금액을 반환합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @GetMapping("/analysis")
    public RevenueAnalysisDto statsDetail(@ModelAttribute StatsRequestDto dto){
        return paymentUsageStats.findPaymentMethodStats(dto.getStart(),dto.getEnd());
    }
}
