package com.example.demo.api.admin.fee;

import com.example.demo.domain.payment.statistics.dtos.request.StatsRequestDto;
import com.example.demo.domain.payment.statistics.dtos.response.RevenueAnalysisDto;
import com.example.demo.domain.payment.statistics.dtos.response.RevenueResponseDto;
import com.example.demo.domain.payment.statistics.service.PaymentUsageStats;
import com.example.demo.domain.payment.statistics.service.RevenueStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
public class statsController {
    private final RevenueStats revenueStats;
    private final PaymentUsageStats paymentUsageStats;

    @GetMapping
    public RevenueResponseDto stats(@ModelAttribute StatsRequestDto dto){
        return revenueStats.revenueReponse(dto.getStart(),dto.getEnd());
    }

    @GetMapping("/analysis")
    public RevenueAnalysisDto statsDetail(@ModelAttribute StatsRequestDto dto){
        return paymentUsageStats.findPaymentMethodStats(dto.getStart(),dto.getEnd());
    }
}
