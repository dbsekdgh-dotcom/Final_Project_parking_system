package com.example.demo.api.user.mypage;

import com.example.demo.domain.resident.mypage.dto.response.MyPageDashboardResponseDto;
import com.example.demo.domain.resident.mypage.dto.request.MyPageDashboardUpdateRequestDto;
import com.example.demo.domain.resident.mypage.service.MyPageDashboardService;
import com.example.demo.domain.auth.user.principal.PrincipalDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "8. 마이페이지 (MyPage)", description = "로그인한 사용자의 회원 정보 조회 및 수정 API")
@RestController
@RequestMapping("/api/user/mypage")
@RequiredArgsConstructor
public class MyPageDashBoardController {

    private final MyPageDashboardService mypageDashboardService;

    @Operation(summary = "회원 정보 조회", description = "로그인한 사용자의 이름, 전화번호, 생일, 세대 정보 등을 반환합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    // 1. 회원정보 조회
    @GetMapping
    public ResponseEntity<MyPageDashboardResponseDto> getUserInfo(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long userId = principalDetails.getUserId();
        return ResponseEntity.ok(mypageDashboardService.getUserInfo(userId));
    }

    @Operation(summary = "회원 정보 수정", description = "전화번호 또는 생일 중 null이 아닌 필드만 부분 수정합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    // 2. 회원정보 수정 (전화번호 or 생일 - null이 아닌 필드만 수정)
    @PutMapping
    public ResponseEntity<String> updateProfile(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Valid @RequestBody MyPageDashboardUpdateRequestDto memberDto) {
        Long userId = principalDetails.getUserId();
        mypageDashboardService.updateProfile(userId, memberDto);
        return ResponseEntity.ok("회원 정보 수정 완료!");
    }
}
