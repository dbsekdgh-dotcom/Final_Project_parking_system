package com.example.demo.api.user.space;

import com.example.demo.domain.parking.space.dtos.response.UserParkingSummaryDto;
import com.example.demo.domain.parking.space.service.UserParkingSpaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "10. 주차공간 조회 (Parking Space)", description = "사용자 유형별 주차 층 요약 정보 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/space")
@CrossOrigin(origins = "http://localhost:5202")
public class UserParkingSpaceController {

    private final UserParkingSpaceService userParkingSpaceService;

    @Operation(summary = "주차 층 요약 조회", description = "userType(거주자/방문자 등)에 따라 해당 사용자가 주차 가능한 층의 잔여 자리 수를 반환합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @GetMapping("/summary")
    public ResponseEntity<UserParkingSummaryDto> getSummary(
            @RequestParam(name = "userType") String userType) {
        return ResponseEntity.ok(userParkingSpaceService.getMyFloorSummary(userType));
    }
}
