package com.example.demo.domain.resident.household;

import com.example.demo.domain.resident.household.enums.IsActive;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Household {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long householdId;
    @Column(nullable = false, unique = true)
    private Integer unitNo;
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IsActive isActive =IsActive.INACTIVE;
    @Column(nullable = false)
    @Builder.Default
    private Integer totalVisitCount=0;
    @Column(nullable = false)
    @Builder.Default
    private Integer todayVisitCount=0;
    @Column(nullable = false)
    @Builder.Default
    private Integer monthlyVisitCount=0;
    @Column(nullable = false)
    @Builder.Default
    private Integer activeReservationCount=0;
}