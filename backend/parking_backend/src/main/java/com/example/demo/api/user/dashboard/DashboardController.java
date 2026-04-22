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
    public ResponseEntity<DashboardResponseDto> getDashboardData(@RequestParam(value = "userId")Long userId){

        //유저 ID가 없는 경우에 대한 방어 로직
        if(userId == null){
            return ResponseEntity.badRequest().build();
        }

        DashboardResponseDto response = dashboardService.getUserDashboardData(userId);

        System.out.println(">>> [API 호출] 유저ID: " + userId);
        System.out.println(">>> [데이터 결과] 포인트: " + response.getMyPoint() + ", D-Day: " + response.getSubscriptionDDay());

        return ResponseEntity.ok(response);
    }
}
