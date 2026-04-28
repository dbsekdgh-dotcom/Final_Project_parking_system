package com.example.demo.api.user.point;

import com.example.demo.domain.payment.point.dto.PointResponseDto;
import com.example.demo.domain.payment.point.service.PointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "9. 포인트 (Point)", description = "사용자 포인트 조회, 적립, 사용 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/mypage/point")
public class PointController {

    private final PointService pointService;

    @Operation(summary = "포인트 조회", description = "userId로 해당 사용자의 포인트 잔액과 내역을 반환합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    //1. 포인트 조회
    @GetMapping("/{userId}")
    public PointResponseDto gerUserPoint(@PathVariable Long userId){
        return pointService.getUserPoint(userId);
    }

    @Operation(summary = "포인트 적립", description = "결제 완료 후 userId·paymentId·금액·설명을 받아 포인트를 적립합니다.", security = @SecurityRequirement(name = "jwtAuth"))
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

    @Operation(summary = "포인트 사용", description = "결제 시 userId·paymentId·사용금액·설명을 받아 포인트를 차감합니다.", security = @SecurityRequirement(name = "jwtAuth"))
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
