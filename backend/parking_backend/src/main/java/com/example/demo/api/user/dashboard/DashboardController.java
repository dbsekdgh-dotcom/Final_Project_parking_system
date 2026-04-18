package com.example.demo.api.user.dashboard;

import com.example.demo.domain.resident.dashboard.dto.DashboardResponseDto;
import com.example.demo.domain.resident.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardResponseDto> getDashboardData(@RequestParam(value = "userId", required = false)Long userId){
        Long testUserID = (userId != null) ? userId :69L;

        DashboardResponseDto response = dashboardService.getUserDashboardData(testUserID);

        System.out.println(">>> [API 호출] 유저ID: " + testUserID);
        System.out.println(">>> [데이터 결과] 포인트: " + response.getMyPoint() + ", D-Day: " + response.getSubscriptionDDay());

        return ResponseEntity.ok(response);
    }
}
