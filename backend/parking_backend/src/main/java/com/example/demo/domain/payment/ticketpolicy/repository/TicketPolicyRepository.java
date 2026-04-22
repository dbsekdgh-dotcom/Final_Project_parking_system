package com.example.demo.domain.payment.ticketpolicy.repository;

import com.example.demo.domain.payment.ticketpolicy.TicketPolicy;
import com.example.demo.domain.payment.ticketpolicy.enums.Status;
import com.example.demo.domain.payment.ticketpolicy.enums.UseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TicketPolicyRepository extends JpaRepository<TicketPolicy,Long> {
    List<TicketPolicy> findAllByUseTypeAndStatus(UseType useType, Status status);

    //특정 status가 아닌 내역 조회
    List<TicketPolicy> findByStatusIsNot(Status status);

}
