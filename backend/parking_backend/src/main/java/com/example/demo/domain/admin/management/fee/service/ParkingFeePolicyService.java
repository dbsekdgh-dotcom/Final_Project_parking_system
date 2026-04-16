package com.example.demo.domain.admin.management.fee.service;

import com.example.demo.domain.admin.management.fee.dtos.response.ParkingFeePolicyResponseDto;
import com.example.demo.domain.admin.management.fee.dtos.response.TicketPolicyResponseDto;
import com.example.demo.domain.shared.parkingfeepolicy.ParkingFeePolicy;
import com.example.demo.domain.shared.parkingfeepolicy.enums.ParkingType;
import com.example.demo.domain.shared.parkingfeepolicy.repository.ParkingFeePolicyRepository;
import com.example.demo.domain.shared.ticketPolicy.TicketPolicy;
import com.example.demo.domain.shared.ticketPolicy.enums.Status;
import com.example.demo.domain.shared.ticketPolicy.respository.TicketPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParkingFeePolicyService {
    private final ParkingFeePolicyRepository parkingFeePolicyRepository;
    private final TicketPolicyRepository ticketPolicyRepository;

    //요금 정책 조회
    public Map<String,Object> getEffectiveParkingFeePolicy() {
        List<ParkingFeePolicy> policies=parkingFeePolicyRepository.findEffectiveParkingFeePolicy();
        Map<String,Object> finalResponse=new HashMap<>();
        //외부인/방문객 요금
        Map<ParkingType,ParkingFeePolicyResponseDto> feePolicy=policies.stream().collect(Collectors.toMap(
                ParkingFeePolicy::getParkingType,
                ParkingFeePolicyResponseDto::toPolicyDto,
                (existing, replacement)->replacement.getVersion()>existing.getVersion()?replacement:existing
        ));
        //할인권
        List<TicketPolicyResponseDto> ticketPolicies=ticketPolicyRepository.findByStatusIsNot(Status.DELETED)
                .stream().map(TicketPolicyResponseDto::toTicketPolicyDto).toList();

        finalResponse.put("visitorPolicy",feePolicy.get(ParkingType.VISIT));
        finalResponse.put("reservaionPolicy",feePolicy.get(ParkingType.RESERVATION));
        finalResponse.put("ticketPolicies",ticketPolicies);
        return finalResponse;
    }

}
