package com.example.demo.domain.user.MyPage.controller;

import com.example.demo.domain.user.MyPage.dto.MemberResponseDto;
import com.example.demo.domain.user.MyPage.dto.MemberUpdateRequestDto;
import com.example.demo.domain.user.MyPage.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController//JSON 형태로 응답하는 컨트롤러
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MemberController {
     private final MemberService memberService;

     //1.회원정보 조회
    @GetMapping("/{memberId}")
    public ResponseEntity<MemberResponseDto> getMemberInfo(@PathVariable Long memberId){
        MemberResponseDto memberDto = memberService.getMemberInfo(memberId);

        if(memberDto == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(memberDto);
    }
    //2. 회원정보 수정
    @PutMapping("/{memberId}")
    public ResponseEntity<String> updateProfile(@PathVariable Long memberId,
                                                @RequestBody MemberUpdateRequestDto memberDto){
        memberService.updateProfile(memberId,memberDto);
        return ResponseEntity.ok("회원 정보 수정 완료!");
    }
    //3. 비밀번호 변경
    @PatchMapping("/{memberId}/password")
    public ResponseEntity<String >updatePassword(@PathVariable Long memberId,
                                                 @RequestParam String password){
        memberService.updatePassword(memberId,password);
        return ResponseEntity.ok("비밀번호 변경완료!");
    }

    //4. 회원 탈퇴
    @DeleteMapping("/{memberId}")
    public ResponseEntity<String > withdraw(@PathVariable Long memberId){
        memberService.withdraw(memberId);
        return ResponseEntity.ok("회원탈퇴 완료!");
    }
}
