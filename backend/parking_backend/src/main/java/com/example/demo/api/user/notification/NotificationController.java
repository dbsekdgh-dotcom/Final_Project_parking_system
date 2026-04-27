package com.example.demo.api.user.notification;

import com.example.demo.domain.auth.user.principal.PrincipalDetails;
import com.example.demo.domain.notification.dtos.NotificationResponseDto;
import com.example.demo.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    //유저별 알림 목록 조회
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponseDto>> getNotification(@AuthenticationPrincipal PrincipalDetails principalDetails){
        List<NotificationResponseDto> list = notificationService.getNotifications(principalDetails.getUser().getUserId());
        return ResponseEntity.ok(list);
    }

    //알림 읽음 처리(확인 시간 기록)
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> readNotification(@PathVariable Long notificationId) {
        notificationService.readNotification(notificationId);
        return ResponseEntity.ok().build();
    }

    //알림 개별 삭제
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long notificationId){
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.noContent().build();
    }
}
