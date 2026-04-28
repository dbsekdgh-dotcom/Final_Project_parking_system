package com.example.demo.domain.notification.service;

import com.example.demo.domain.notification.Notification;
import com.example.demo.domain.notification.dtos.NotificationResponseDto;
import com.example.demo.domain.notification.enums.Status;
import com.example.demo.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

     private final NotificationRepository notificationRepository;

    // 유저별 알림 목록 조회
    public List<NotificationResponseDto> getNotifications(Long userId){
        return notificationRepository.findByUser_UserIdAndStatusOrderByCreatedAtDesc(userId, Status.ACTIVE)
                .stream()
                .map(NotificationResponseDto::from)//dto변환
                .collect(Collectors.toList());
    }

    // 일림 읽음 처리(확인 시간 기록)
    @Transactional
    public void readNotification(Long notificationID){
        notificationRepository.findById(notificationID)
                .ifPresent(Notification::markAsRead);
    }

    // 특정 알림 개별 삭제
    @Transactional
    public void deleteNotification(Long notificationId){
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 알림이 존재하지 않습니다."));

        notification.markDeleted();
    }

    @Transactional(readOnly = true)
    public Long getUnreadCount(Long userId){
        return notificationRepository.countUnreadNotifications(userId);
    }

}
