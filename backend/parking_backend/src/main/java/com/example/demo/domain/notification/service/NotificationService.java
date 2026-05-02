package com.example.demo.domain.notification.service;

import com.example.demo.domain.notification.Notification;
import com.example.demo.domain.notification.dtos.NotificationResponseDto;
import com.example.demo.domain.notification.enums.Status;
import com.example.demo.domain.notification.enums.Type;
import com.example.demo.domain.notification.repository.NotificationRepository;
import com.example.demo.domain.resident.User;
import com.example.demo.domain.resident.UserRepository;
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
     private final UserRepository userRepository;

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

    @Transactional
    public void createNotification(Long userId, String title, String content, Type type){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다. ID:" + userId));

        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .content(content)
                .type(type)
                .status(Status.ACTIVE)
                .build();
        notificationRepository.save(notification);
    }


}
