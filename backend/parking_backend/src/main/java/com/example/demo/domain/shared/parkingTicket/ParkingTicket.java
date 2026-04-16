package com.example.demo.domain.shared.parkingTicket;

import com.example.demo.domain.shared.parkinglog.ParkingLog;
import com.example.demo.domain.shared.store.Store;
import com.example.demo.domain.shared.ticketPolicy.TicketPolicy;
import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString(exclude = {"store","parkingLog","ticketPolicy"})
@Entity
@Builder
public class ParkingTicket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long parkingTicketId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id",nullable = false)
    private Store store;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_log_id",nullable = false)
    private ParkingLog parkingLog;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_policy_id",nullable = false)
    private TicketPolicy ticketPolicy;
    @Enumerated(EnumType.STRING)
    @Column(name = "status",nullable = false)
    private Status status=Status.STORE;
    @Column(name = "applied_amount",nullable = false)
    private Integer appliedAmount = 0;


    //요금 결제 시
    public void updateAppliedAmount(int amount){
        this.appliedAmount=amount;
    }
}
