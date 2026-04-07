package com.example.demo.domain.shared.user;

import com.example.demo.domain.shared.household.Household;
import com.example.demo.domain.shared.user.enums.Status;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime; // 추가

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id")
    private Household household;

    @Column(name = "password")
    private String password;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "birth", nullable = false)
    private LocalDate birth;

    @Column(name = "phone", nullable = false, unique = true, length = 30)
    private String phone;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.ACTIVE;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public User(Household household, String password, String email, String name,
                LocalDate birth, String phone, Status status) {
        this.household = household;
        this.password = password;
        this.email = email;
        this.name = name;
        this.birth = birth;
        this.phone = phone;
        this.status = (status != null) ? status : Status.ACTIVE;
    }

    // ==========================================
    // [강제 KST 주입 로직]
    // 시스템 서버 시간이 UTC라도 무시하고 한국 시간을 꽂습니다.
    // ==========================================

    @PrePersist
    public void onPrePersist() {
        // ZonedDateTime을 통해 물리적으로 한국(+09:00) 시간을 가져와서 변환
        this.createdAt = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDateTime();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    public void onPreUpdate() {
        this.updatedAt = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDateTime();
    }

    // ==========================================

    public void updateUserInfo(String name, String phone) {
        this.name = name;
        this.phone = phone;
        // 정보 수정 시에도 동일하게 적용
        this.updatedAt = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDateTime();
    }

    public void addLocalPassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void withdraw() {
        this.status = Status.DELETED;
        this.deletedAt = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDateTime();
        // 수정 시간도 탈퇴 시간과 동일하게 맞춰줍니다.
        this.updatedAt = this.deletedAt;
    }

}