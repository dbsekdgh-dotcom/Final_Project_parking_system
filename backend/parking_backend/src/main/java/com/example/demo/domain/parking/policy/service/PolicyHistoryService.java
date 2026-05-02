package com.example.demo.domain.parking.policy.service;

import com.example.demo.domain.parking.policy.ParkingFeePolicy;
import com.example.demo.domain.parking.policy.dtos.response.ParkingFeePolicyResponseDto;
import com.example.demo.domain.parking.policy.enums.ParkingType;
import com.example.demo.domain.parking.policy.repository.ParkingFeePolicyRepository;
import com.example.demo.domain.payment.ticketpolicy.repository.TicketPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class PolicyHistoryService {
    private final ParkingFeePolicyRepository parkingFeePolicyRepository;

    public List<ParkingFeePolicyResponseDto> getPolicyHistoryFiltered(
            ParkingType parkingType, Long version, LocalDate startDate, LocalDate endDate) {
        return parkingFeePolicyRepository.findPolicyHistoryByType(parkingType, LocalDateTime.now())
                .stream()
                .filter(p -> version == null || p.getVersion().equals(version))
                .filter(p -> startDate == null || !p.getEffectiveFrom().toLocalDate().isBefore(startDate))
                .filter(p -> endDate == null || !p.getEffectiveFrom().toLocalDate().isAfter(endDate))
                .map(ParkingFeePolicyResponseDto::toPolicyDto)
                .toList();
    }
}
