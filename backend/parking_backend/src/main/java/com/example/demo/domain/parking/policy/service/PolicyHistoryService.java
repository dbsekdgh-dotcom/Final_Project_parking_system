package com.example.demo.domain.parking.policy.service;

import com.example.demo.domain.parking.policy.ParkingFeePolicy;
import com.example.demo.domain.parking.policy.dtos.response.ParkingFeePolicyResponseDto;
import com.example.demo.domain.parking.policy.enums.ParkingType;
import com.example.demo.domain.parking.policy.repository.ParkingFeePolicyRepository;
import com.example.demo.domain.payment.ticketpolicy.repository.TicketPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PolicyHistoryService {
    private final ParkingFeePolicyRepository parkingFeePolicyRepository;
    private final TicketPolicyRepository ticketPolicyRepository;

    public Map<String,Object> getPolicyHistory() {
        Map<String, Object> result = new HashMap<>();
        List<ParkingFeePolicy> list = parkingFeePolicyRepository.findPolicyHistory();
        //외부인 과거 요금 정책
        List<ParkingFeePolicyResponseDto> visitorPolicyHistory = list.stream().filter(l -> ParkingType.VISIT.equals(l.getParkingType()))
                .map(ParkingFeePolicyResponseDto::toPolicyDto)
                .toList();
        //방문객 과거 요금 정책
        List<ParkingFeePolicyResponseDto> reservationPolicyHistory = list.stream().filter(l -> ParkingType.RESERVATION.equals(l.getParkingType()))
                .map(ParkingFeePolicyResponseDto::toPolicyDto)
                .toList();

        result.put("visitorPolicyHistory", visitorPolicyHistory);
        result.put("reservationPolicyHistory", reservationPolicyHistory);
        return result;
    }
}
