package com.example.demo.domain.user.mypage.dashboard.controller;

import com.example.demo.domain.user.mypage.dashboard.dto.request.MyPageDashboardPasswordUpdateRequestDto;
import com.example.demo.domain.user.mypage.dashboard.dto.response.MyPageDashboardResponseDto;
import com.example.demo.domain.user.mypage.dashboard.dto.request.MyPageDashboardUpdateRequestDto;
import com.example.demo.domain.user.mypage.dashboard.service.MyPageDashboardService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController//JSON 형태로 응답하는 컨트롤러
@RequestMapping("/api/user/mypage/dashboard")
@RequiredArgsConstructor
public class MyPageDashBoardController {
     private final MyPageDashboardService mypageDashboardService;

     //1.회원정보 조회
    @GetMapping("/{memberId}")
    public ResponseEntity<MyPageDashboardResponseDto> getMemberInfo(@PathVariable Long memberId){
        MyPageDashboardResponseDto memberDto = mypageDashboardService.getMemberInfo(memberId);
        return ResponseEntity.ok(memberDto);
    }
    //2. 회원정보 수정
    @PutMapping("/{memberId}")
    public ResponseEntity<String> updateProfile(@PathVariable Long memberId,
                                                @RequestBody MyPageDashboardUpdateRequestDto memberDto){
        mypageDashboardService.updateProfile(memberId,memberDto);
        return ResponseEntity.ok("회원 정보 수정 완료!");
    }
    //3. 비밀번호 변경
    @PatchMapping("/{memberId}/password")
    public ResponseEntity<String >updatePassword(@PathVariable Long memberId,
                                                 @RequestBody MyPageDashboardPasswordUpdateRequestDto dto){
        mypageDashboardService.updatePassword(memberId,dto.getPassword());
        return ResponseEntity.ok("비밀번호 변경완료!");
    }

    //4. 회원 탈퇴
    @DeleteMapping("/{memberId}")
    public ResponseEntity<String > withdraw(@PathVariable Long memberId){
        mypageDashboardService.withdraw(memberId);
        return ResponseEntity.ok("회원탈퇴 완료!");
    }
}
