package com.example.demo.domain.notification.controller;

import com.example.demo.domain.auth.user.principal.PrincipalDetails;

import com.example.demo.domain.notification.dtos.NotificationResponseDto;
import com.example.demo.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private  final NotificationService notificationService;

    //현재 로그인한 유저의 알림 목록 조회
    @GetMapping
    public ResponseEntity<List<NotificationResponseDto>> getNotifications(
            @AuthenticationPrincipal PrincipalDetails principalDetails){

//        Long userId = principalDetails.getUser().getUserId();
//
//        List<NotificationResponseDto> notifications = notificationService.getNotifications(userId);
//
//        return ResponseEntity.ok(notifications);
//       보안체크
        if (principalDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 로그인된 유저의 실제 Id 추출
        Long userId = principalDetails.getUser().getUserId();

        System.out.println("로그인 정보가 없어 임시로 1번 유저 데이터를 사용합니다.");

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

}
