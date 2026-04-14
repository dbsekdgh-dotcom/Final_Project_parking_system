package com.example.demo.domain.shared.reservation;

import com.example.demo.domain.shared.household.Household;
import com.example.demo.domain.shared.reservation.enums.Purpose;
import com.example.demo.domain.shared.reservation.enums.Status;
import com.example.demo.domain.shared.user.User;
import com.example.demo.domain.shared.vehicle.Vehicle;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = {"household","vehicle"})
@Builder
@Table(name = "reservation")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;
    @Column(nullable = false)
    private String carNumber;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status =Status.PENDING;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Purpose purpose;
    @Column(nullable = false)
    private LocalDateTime visitStartAt;
    @Column(nullable = false)
    private LocalDateTime visitEndAt;
    private LocalDateTime actual_entry_at;
    @CreationTimestamp
    private LocalDateTime createdAt;
    private LocalDateTime cancelledAt;
    @Builder.Default
    @Column(name = "is_free", nullable = false)
    private boolean isFree=true;
}
