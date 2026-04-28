package com.example.demo.domain.store;

import com.example.demo.domain.payment.ticketpolicy.TicketPolicy;
import com.example.demo.domain.system.store.Store;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "store_ticket_config",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_store_policy",
        columnNames = {"store_id", "ticket_policy_id"}
    )
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreTicketConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long storeTicketConfigId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_policy_id", nullable = false)
    private TicketPolicy ticketPolicy;

    @Column(nullable = false)
    @Builder.Default
    private int monthlyQuota = 0;

    public void update(TicketPolicy newPolicy, int newQuota){
        this.ticketPolicy = newPolicy;
        this.monthlyQuota = newQuota;
    }
}
