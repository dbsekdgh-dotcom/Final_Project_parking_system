package com.example.demo.domain.user.report.entity;

import com.example.demo.domain.shared.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.LocalDateTime;

@Entity
@Table(name= "report")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long id;

    //신고한 유저
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_user_id", nullable =false,foreignKey = @ForeignKey(name="fk_rep_reporter")
    )
    private User reporter;

    //차량 ID(선택)
    @Column(name = "vehicle_id")
    private Long vehicleId;

    //차량 번호(핵심)
    @Column(name = "car_number", nullable = false,length = 25)
    private String carNumber;

    //신고 유형
    @Enumerated(EnumType.STRING)
    @Column(name ="report_type",nullable = false)
    private ReportType reportType;

    //상세 내용
    @Column(name = "description")
    private String description;

    //이미지
    @Column(name = "image_url",length = 512)
    private String imageUrl;

    //상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;

    //관리자 ID
    @Column(name = "admin_id")
    private Long adminId;

    //생성일
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    //처리일
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @PrePersist
    protected void onCreate(){
        if(this.status == null){
            this.status = ReportStatus.PENDING;
        }
    }

    //비즈니스 로직
    public void cancel(){
        this.status = ReportStatus.CANCELLED;
    }

    public  void approve(Long adminId){
        this.status = ReportStatus.APPROVED;
        this.adminId = adminId;
        this.resolvedAt = LocalDateTime.now();
    }

    public void reject(Long adminId){
        this.status = ReportStatus.REJECTED;
        this.adminId = adminId;
        this.resolvedAt = LocalDateTime.now();
    }
}
