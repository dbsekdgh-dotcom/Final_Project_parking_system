package com.example.demo.domain.system.store.transaction;

import com.example.demo.domain.payment.ticketpolicy.TicketPolicy;
import com.example.demo.domain.system.store.Store;
import com.example.demo.domain.system.store.transaction.enums.CreatedByType;
import com.example.demo.domain.system.store.transaction.enums.ReferenceType;
import com.example.demo.domain.system.store.transaction.enums.TransactionType;
import com.example.demo.domain.system.store.wallet.StoreTicketWallet;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "store_ticket_transaction")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreTicketTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private StoreTicketWallet wallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_policy_id", nullable = false)
    private TicketPolicy ticketPolicy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private int beforeBalance;

    @Column(nullable = false)
    private int afterBalance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReferenceType referenceType;

    private Long referenceId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CreatedByType createdByType;

    private Long createdById;
}
