package com.example.demo.domain.parking.policy.service;

import com.example.demo.domain.payment.ticketpolicy.TicketPolicy;
import com.example.demo.domain.payment.ticketpolicy.enums.Status;
import com.example.demo.domain.payment.ticketpolicy.repository.TicketPolicyRepository;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TicketPolicyService {
    private final TicketPolicyRepository ticketPolicyRepository;

    public void deleteTicketPolicy(long parkingTicketId){
        TicketPolicy ticketPolicy=ticketPolicyRepository.findById(parkingTicketId).orElseThrow(()->new BusinessException(ErrorCode.INVALID_REQUEST));
        ticketPolicy.setStatus(Status.DELETED);
    }
}
