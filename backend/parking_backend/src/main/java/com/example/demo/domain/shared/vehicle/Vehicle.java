package com.example.demo.domain.shared.vehicle;

import com.example.demo.domain.shared.user.User;
import com.example.demo.domain.shared.vehicle.enums.VehicleStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "vehicle",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_vehicle_car_number", columnNames = "car_number")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vehicle_id")
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            foreignKey = @ForeignKey(name = "fk_vehicle_user")
    )
    private User user;

    @Column(name = "vehicle_name", length = 100)
    private String vehicleName;

    @Column(name = "car_number", nullable = false, length = 25)
    private String carNumber;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private VehicleStatus status;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = VehicleStatus.ACTIVE;
        }
    }
    public void softDelete() {
        this.status = VehicleStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }

    public void changeName(String name) {
        this.vehicleName = name;
    }

    public void assignUser(User user) {
        this.user = user;
    }
}