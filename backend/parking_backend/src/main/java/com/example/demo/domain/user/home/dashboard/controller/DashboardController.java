package com.example.demo.domain.user.home.dashboard.controller;

import com.example.demo.domain.user.home.dashboard.dto.DashboardResponseDto;
import com.example.demo.domain.user.home.dashboard.service.DashboardService;
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
        Long testUserID = (userId != null) ? userId : 1L;

        DashboardResponseDto response = dashboardService.getUserDashboardData(testUserID);
        return ResponseEntity.ok(response);
    }
}
