package com.example.demo.domain.user.mypage.point.controller;

import com.example.demo.domain.user.mypage.point.dto.PointResponseDto;
import com.example.demo.domain.user.mypage.point.service.PointService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/mypage/point")
public class PointController {

    private final PointService pointService;

    @GetMapping("/{userId}")
    public PointResponseDto gerUserPoint(@PathVariable Long userId){
        return pointService.getUserPoint(userId);
    }
}
