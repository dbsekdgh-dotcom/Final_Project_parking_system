package com.example.demo.domain.shared.ticketPolicy.respository;

import com.example.demo.domain.shared.ticketPolicy.TicketPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketPolicyRepository extends JpaRepository<TicketPolicy,Long> {
}
