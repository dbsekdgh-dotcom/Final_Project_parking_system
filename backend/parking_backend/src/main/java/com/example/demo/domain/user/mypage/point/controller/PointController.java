package com.example.demo.domain.user.mypage.point.controller;

import com.example.demo.domain.user.mypage.point.dto.PointResponseDto;
import com.example.demo.domain.user.mypage.point.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/mypage/point")
public class PointController {

    private final PointService pointService;

    //1. 포인트 조회
    @GetMapping("/{userId}")
    public PointResponseDto gerUserPoint(@PathVariable Long userId){
        return pointService.getUserPoint(userId);
    }

    //2. 포인트 적림
    @PostMapping("/earn")
    public String earnPoints(
            @RequestParam Long userId,
            @RequestParam Long paymentId,
            @RequestParam int amount,
            @RequestParam String description
    ){
        //실제 User와  Payment객체를 Service에서 조회하도록
        pointService.earnPoints(userId,paymentId,amount,description);
        return "포인트 적립 완료";
    }

    //3. 포인트 사용
    @PostMapping("/use")
    public String usePoints(
            @RequestParam Long userId,
            @RequestParam Long paymentId,
            @RequestParam int amount,
            @RequestParam String description
    ){
        pointService.usePoints(userId,paymentId,amount,description);
        return "포인트 사용 완료";
    }
}
