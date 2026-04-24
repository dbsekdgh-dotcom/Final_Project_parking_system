package com.example.demo.domain.notification.dtos;

import com.example.demo.domain.notification.Notification;
import com.example.demo.domain.notification.enums.Type;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class NotificationResponseDto {
    private Long notificationId;
    private Type type;
    private String title;
    private String content;
    private LocalDateTime readAt;
    private LocalDateTime deletedAt;
    private LocalDateTime createdAt;
    private boolean isRead;

    //Dto변환
    public static NotificationResponseDto from(Notification notification){
        return NotificationResponseDto.builder()
                .notificationId(notification.getNotificationId())
                .type(notification.getType())
                .title(notification.getTitle())
                .content(notification.getContent())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .deletedAt(notification.getDeletedAt())
                .isRead(notification.getReadAt() != null)
                .build();
    }
}

