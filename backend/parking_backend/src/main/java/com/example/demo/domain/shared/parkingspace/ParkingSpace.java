package com.example.demo.domain.shared.parkingspace;

import com.example.demo.domain.shared.parkingspace.enums.Floor;
import com.example.demo.domain.shared.parkingspace.enums.SpaceStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "parking_space")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ParkingSpace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "parking_space_id")
    private Long id;

    @Column(name = "space_code", length = 50, unique = true, nullable = false)
    private String spaceCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "floor", nullable = false)
    private Floor floor;

    @Builder.Default
    @Column(name = "is_reservation", nullable = false)
    private Boolean isReservation = false;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false)
    private SpaceStatus status = SpaceStatus.AVAILABLE;

    @UpdateTimestamp
    @Column(name = "last_status_changed_at", nullable = false)
    private LocalDateTime lastStatusChangedAt;

    @Builder.Default
    @Column(name = "is_disabled", nullable = false)
    @Comment("장애인 전용자리")
    private Boolean isDisabled = false;

    @Builder.Default
    @Column(name = "is_ev_charge", nullable = false)
    @Comment("전기차 전용자리")
    private Boolean isEvCharge = false;
}
