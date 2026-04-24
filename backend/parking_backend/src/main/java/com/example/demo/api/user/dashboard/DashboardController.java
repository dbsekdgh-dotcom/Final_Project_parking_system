package com.example.demo.api.user.dashboard;

import com.example.demo.domain.auth.user.principal.PrincipalDetails;
import com.example.demo.domain.resident.dashboard.dto.DashboardResponseDto;
import com.example.demo.domain.resident.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardResponseDto> getDashboardData(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestParam(value = "page", defaultValue = "0") int page){

        Long currentUserId = principalDetails.getUserId();

        log.info(">>> [보안 검증 완료] 접속 유저 ID: {}, 페이지: {}", currentUserId, page);

        DashboardResponseDto response = dashboardService.getUserDashboardData(currentUserId, page);

        log.info(">>>[데이터 결과] 포인트: {}, D-Day: {}",response.getMyPoint(), response.getSubscriptionDDay());
        return ResponseEntity.ok(response);
    }
}
