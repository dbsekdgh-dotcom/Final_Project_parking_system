package com.example.demo.domain.notification.controller;

import com.example.demo.domain.auth.user.principal.PrincipalDetails;

import com.example.demo.domain.notification.dtos.NotificationResponseDto;
import com.example.demo.domain.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "9. 알림 (Notification)", description = "사용자 알림 목록 조회, 읽음 처리, 삭제, 미읽음 개수 조회 API")
@Slf4j
@RestController
@RequestMapping("/api/user/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private  final NotificationService notificationService;

    @Operation(summary = "알림 목록 조회", description = "로그인한 사용자의 전체 알림 목록을 반환합니다.", security = @SecurityRequirement(name = "jwtAuth"))
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

    @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 상태로 변경합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> readNotification(@PathVariable Long notificationId){
        notificationService.readNotification(notificationId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "알림 삭제", description = "특정 알림을 삭제합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long notificationId){
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.noContent().build();

    }
    @Operation(summary = "미읽음 알림 개수 조회", description = "로그인한 사용자의 읽지 않은 알림 개수를 반환합니다. 미인증 시 0을 반환합니다.", security = @SecurityRequirement(name = "jwtAuth"))
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
