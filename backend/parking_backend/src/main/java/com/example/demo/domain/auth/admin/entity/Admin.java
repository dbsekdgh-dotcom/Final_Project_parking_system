package com.example.demo.domain.auth.admin.entity;

import com.example.demo.domain.auth.admin.enums.AdminStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "admin")
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adminId;
    @Comment("관리자 로그인 ID")
    @Column(unique = true,nullable = false,length = 100)
    private String loginId;
    @Comment("암호화된 비밀번호")
    @Column(nullable = false,length = 255)
    private String password;
    @Comment("관리자 이름")
    @Column(nullable = false,length = 100)
    private String name;
    @Enumerated(EnumType.STRING)
    @Comment("계정상태")
    @Column(nullable = false)
    private AdminStatus status = AdminStatus.ACTIVE;
    @CreationTimestamp // 생성 시간 자동 입력
    @Column(nullable = false,updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private LocalDateTime deletedAt;
}
