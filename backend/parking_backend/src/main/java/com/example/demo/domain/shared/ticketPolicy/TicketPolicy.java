package com.example.demo.domain.shared.ticketPolicy;

import com.example.demo.domain.shared.ticketPolicy.enums.DiscountType;
import com.example.demo.domain.shared.ticketPolicy.enums.Status;
import com.example.demo.domain.shared.ticketPolicy.enums.UseType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Builder
@Entity
@Table(name = "ticket_policy")
public class TicketPolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ticketPolicyId;
    @Column(nullable = false)
    private String name;
    private String description;
    @Column(nullable = false)
    @Builder.Default
    private Integer price=0;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DiscountType discountType;
    @Column(nullable = false)
    private Integer discountValue;
    @Column(name = "use_type",nullable = false)
    @Enumerated(EnumType.STRING)
    private UseType useType=UseType.STORE;
    private Integer maxDiscountAmount;
    private Integer validMinutes;
    private Integer validDays;
    @Builder.Default
    private boolean isFreeTicket=false;
    @Builder.Default
    @Column(nullable = false)
    private boolean stackable=true;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private Status status=Status.ACTIVE;
    @CreationTimestamp
    private LocalDateTime createdAt;

    public Integer getValidDays() {
        return this.validDays == null ? 36500 : this.validDays;
    }

    public Integer getValidMinutes(){
        return validMinutes != null ? validMinutes : 0;
    }
}
