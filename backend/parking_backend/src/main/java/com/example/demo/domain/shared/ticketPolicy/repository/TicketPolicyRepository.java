package com.example.demo.domain.shared.ticketPolicy.repository;

import com.example.demo.domain.shared.ticketPolicy.TicketPolicy;
import com.example.demo.domain.shared.ticketPolicy.enums.Status;
import com.example.demo.domain.shared.ticketPolicy.enums.UseType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketPolicyRepository extends JpaRepository<TicketPolicy,Long> {
    List<TicketPolicy> findAllByUseTypeAndStatus(UseType useType, Status status);

    //특정 status가 아닌 내역 조회
    List<TicketPolicy> findByStatusIsNot(Status status);

    //특정 status 내역 조회
    List<TicketPolicy> findTicketPoliciesByStatus(Status status);
}
