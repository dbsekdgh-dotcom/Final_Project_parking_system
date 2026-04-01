package com.example.demo.domain.shared.user;

import com.example.demo.domain.shared.household.entity.Household;
import com.example.demo.domain.user.enums.Status; // 방금 만든 Status 이넘 임포트
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId; // PK: BIGINT

    // [핵심] household_id를 객체로 참조합니다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id") // DB의 FK 컬럼명과 매핑
    private Household household;

    @Column(name = "password")
    private String password; // 소셜 로그인은 NULL 가능하므로 기본값 없음

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "birth", nullable = false)
    private LocalDate birth; // DB: DATE -> Java: LocalDate

    @Column(name = "phone", nullable = false, unique = true, length = 30)
    private String phone;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.ACTIVE; // 기본값 ACTIVE

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

    public void updateUserInfo(String name, String phone) {
        this.name = name;
        this.phone = phone;
        this.updatedAt = LocalDateTime.now();
    }
}