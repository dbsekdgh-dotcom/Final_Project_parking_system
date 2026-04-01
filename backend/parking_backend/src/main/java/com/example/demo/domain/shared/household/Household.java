package com.example.demo.domain.shared.household.entity;

import com.example.demo.domain.shared.household.enums.IsActive; // 새로 만든 이넘 임포트
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "household")
public class Household {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long householdId; // PK: BIGINT

    @Column(name = "unit_no", nullable = false, unique = true)
    private Integer unitNo; // 호수 (예: 101)

    @Enumerated(EnumType.STRING)
    @Column(name = "is_active", nullable = false)
    private IsActive isActive = IsActive.ACTIVE; // 초기값 설정

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder
    public Household(Integer unitNo, IsActive isActive) {
        this.unitNo = unitNo;
        // 빌더를 통해 들어온 값이 없으면 기본값 ACTIVE 사용
        this.isActive = (isActive != null) ? isActive : IsActive.ACTIVE;
    }
}