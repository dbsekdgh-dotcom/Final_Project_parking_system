package com.example.demo.api.user.mypage;

import com.example.demo.domain.resident.mypage.dto.response.MyPageDashboardResponseDto;
import com.example.demo.domain.resident.mypage.dto.request.MyPageDashboardUpdateRequestDto;
import com.example.demo.domain.resident.mypage.service.MyPageDashboardService;
import com.example.demo.domain.auth.user.principal.PrincipalDetails;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/mypage")
@RequiredArgsConstructor
public class MyPageDashBoardController {

    private final MyPageDashboardService mypageDashboardService;

    // 1. 회원정보 조회
    @GetMapping
    public ResponseEntity<MyPageDashboardResponseDto> getUserInfo(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long userId = principalDetails.getUserId();
        return ResponseEntity.ok(mypageDashboardService.getUserInfo(userId));
    }

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
