package com.example.demo.domain.system.store.wallet;

import com.example.demo.domain.payment.ticketpolicy.TicketPolicy;
import com.example.demo.domain.system.store.Store;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "store_ticket_wallet")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreTicketWallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long walletId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_policy_id", nullable = false)
    private TicketPolicy ticketPolicy;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @Builder.Default
    private int issuedCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private int usedCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private int remainingCount = 0;

    public void purchase(int quantity) {
        this.issuedCount += quantity;
        this.remainingCount += quantity;
    }

    public void use(int quantity) {
        this.usedCount += quantity;
        this.remainingCount -= quantity;
    }

    // 지갑 전체 초기화
    public void reset(){
        this.issuedCount = 0;
        this.usedCount = 0;
        this.remainingCount = 0;
    }
}
