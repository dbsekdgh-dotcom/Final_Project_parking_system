package com.example.demo.api.user.dashboard;

import com.example.demo.domain.auth.user.principal.PrincipalDetails;
import com.example.demo.domain.resident.dashboard.dto.DashboardResponseDto;
import com.example.demo.domain.resident.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "13. 대시보드 (Dashboard)", description = "사용자 메인 대시보드 — 층별 주차 현황, 포인트, 정기권 D-Day, 최근 입출차 내역 API")
@Slf4j
@RestController
@RequestMapping("/api/user/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "대시보드 데이터 조회", description = "층별 주차 현황, 포인트 잔액, 정기권 만료 D-Day, 최근 입출차 내역을 페이징으로 반환합니다.", security = @SecurityRequirement(name = "jwtAuth"))
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
