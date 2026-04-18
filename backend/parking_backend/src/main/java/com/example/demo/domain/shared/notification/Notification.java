package com.example.demo.domain.shared.notification;

import com.example.demo.domain.shared.notification.enums.Status;
import com.example.demo.domain.shared.notification.enums.Type;
import com.example.demo.domain.shared.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = "user")
@Builder
@Table(name = "notification")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long notificationId;
    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @Comment("알림을 받는 유저 ID")
    private User user;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Comment("알림 유형 (결제, 예약, 이벤트, 경고, 시스템)")
    private Type type;
    @Column(nullable = false)
    @Comment("알림 제목")
    private String title;
    @Column(nullable = false)
    @Comment("알림 본문 내용")
    private String content;
    @Comment("사용자가 알림을 확인한 시각 (NULL이면 미확인)")
    private LocalDateTime readAt;
    @CreationTimestamp
    @Comment("알림 생성일")
    private LocalDateTime createdAt;
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Status status=Status.ACTIVE;
    @Comment("알림 삭제 일시")
    private LocalDateTime deletedAt;

}
