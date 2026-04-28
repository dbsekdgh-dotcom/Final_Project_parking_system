package com.example.demo.domain.notification.controller;

import com.example.demo.domain.auth.user.principal.PrincipalDetails;

import com.example.demo.domain.notification.dtos.NotificationResponseDto;
import com.example.demo.domain.notification.service.NotificationService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/user/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private  final NotificationService notificationService;

    //현재 로그인한 유저의 알림 목록 조회
    @GetMapping
    public ResponseEntity<List<NotificationResponseDto>> getNotifications(
            @AuthenticationPrincipal PrincipalDetails principalDetails){


//       보안체크
        if (principalDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 로그인된 유저의 실제 Id 추출
        Long userId = principalDetails.getUser().getUserId();

        System.out.println("로그인 유저 확인됨:"+ userId);

        // 해당 유저의 알림 목록 조회
        List<NotificationResponseDto> notifications = notificationService.getNotifications(userId);
        return ResponseEntity.ok(notifications);

    }

    //알림 읽은 처리
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> readNotification(@PathVariable Long notificationId){
        notificationService.readNotification(notificationId);
        return ResponseEntity.noContent().build();
    }

    //알림 삭제 처리
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long notificationId){
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.noContent().build();

    }
    //안 읽은 알림 갯수
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(
            @AuthenticationPrincipal PrincipalDetails principalDetails){

        //보안 체크: 인증되지 않은 사용자는 0개반환
        if(principalDetails == null){
            return ResponseEntity.ok(0L);
        }
        Long userId = principalDetails.getUser().getUserId();
        Long count = notificationService.getUnreadCount(userId);

        return ResponseEntity.ok(count);
    }

}
