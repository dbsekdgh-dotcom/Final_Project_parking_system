package com.example.demo.domain.parking.policy.service;

import com.example.demo.domain.auth.admin.entity.Admin;
import com.example.demo.domain.auth.admin.enums.AdminStatus;
import com.example.demo.domain.parking.policy.dtos.request.ParkingFeePolicyChangeRequestDto;
import com.example.demo.domain.parking.policy.dtos.response.ParkingFeePolicyResponseDto;
import com.example.demo.domain.parking.policy.dtos.response.TicketPolicyResponseDto;
import com.example.demo.domain.auth.admin.repository.AdminRepository;
import com.example.demo.domain.parking.policy.ParkingFeePolicy;
import com.example.demo.domain.parking.policy.enums.ParkingType;
import com.example.demo.domain.parking.policy.repository.ParkingFeePolicyRepository;
import com.example.demo.domain.payment.ticketpolicy.enums.Status;
import com.example.demo.domain.payment.ticketpolicy.repository.TicketPolicyRepository;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import com.example.demo.global.security.admin.AdminAuthDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ParkingFeePolicyService {
    private final ParkingFeePolicyRepository parkingFeePolicyRepository;
    private final TicketPolicyRepository ticketPolicyRepository;
    private final AdminRepository adminRepository;

    //요금 정책 조회
    public Map<String,Object> getEffectiveParkingFeePolicy() {
        Map<String,Object> finalResponse=new HashMap<>();

        //현재 진행중인 요금 정책
        List<ParkingFeePolicy> policies=parkingFeePolicyRepository.findCurrentEffectivePolicy();
        Map<ParkingType,ParkingFeePolicyResponseDto> currenrPolicies=policies.stream().collect(Collectors.toMap(
                ParkingFeePolicy::getParkingType,
                ParkingFeePolicyResponseDto::toPolicyDto,
                (exist, replace)->replace.getVersion()>exist.getVersion()?replace:exist
        ));
        //시행 예정인 요금 정책
        List<ParkingFeePolicy> upcoming=parkingFeePolicyRepository.findUpcomingEffectivePolicy();
        Map<ParkingType,ParkingFeePolicyResponseDto> upcomingPolicies=upcoming.stream().collect(Collectors.toMap(
                ParkingFeePolicy::getParkingType,
                ParkingFeePolicyResponseDto::toPolicyDto,
                (exist,replace)->replace.getVersion()>exist.getVersion()?replace:exist
        ));

        //할인권
        List<TicketPolicyResponseDto> ticketPolicies=ticketPolicyRepository.findTicketPoliciesByStatus(Status.ACTIVE)
                .stream().map(TicketPolicyResponseDto::toTicketPolicyDto).toList();

        finalResponse.put("currentVisitor",currenrPolicies.get(ParkingType.VISIT));
        finalResponse.put("currentReservation",currenrPolicies.get(ParkingType.RESERVATION));
        finalResponse.put("upcomingVisitor",upcomingPolicies.get(ParkingType.VISIT));
        finalResponse.put("upcomingReservation",upcomingPolicies.get(ParkingType.RESERVATION));
        finalResponse.put("ticketPolicies",ticketPolicies);
        return finalResponse;
    }

    //요금 정책 수정
    public long changeParkingFeePolicy(ParkingFeePolicyChangeRequestDto dto){
        //스프링시큐리티가 헤더에서 꺼내서 저장해둔 유저정보 가져오기
        Object principal=SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginId=((AdminAuthDto)principal).getUsername();
        Admin admin=adminRepository.findByLoginIdAndStatus(loginId, AdminStatus.ACTIVE).orElseThrow(()->new BusinessException(ErrorCode.ADMIN_NOT_FOUND));
        System.out.println("ddddddddddddddddd");
        long latestVersionId=parkingFeePolicyRepository.getLatestVersion(dto.getParkingType());
        if(latestVersionId!=dto.getVersion()){
            throw new BusinessException(ErrorCode.POLICY_NOT_MODIFIABLE);
        }
        System.out.println("vvvvvvvvvvvvvvvvvv");
        //새로운 정책 insert
        ParkingFeePolicy newPolicy=ParkingFeePolicy.builder()
                .admin(admin)
                .parkingType(dto.getParkingType())
                .graceMinutes(dto.getGraceMinutes())
                .baseFee(dto.getBaseFee())
                .unitMinutes(dto.getUnitMinutes())
                .unitFee(dto.getUnitFee())
                .dailyMaxFee(dto.getDaliyMaxFee())
                .isActive(false)
                .effectiveFrom(dto.getEffectiveFrom())
                .version(dto.getVersion()+1)
                .build();
        ParkingFeePolicy parkingFeePolicy=parkingFeePolicyRepository.save(newPolicy);
        System.out.println("qqqqqqqqqqqqqqqqqqqqqqqq");
        //기존 정책 유효기간 설정
        ParkingFeePolicy oldPolicy=parkingFeePolicyRepository.findById(dto.getParkingFeePolicyId()).orElseThrow(()->new BusinessException(ErrorCode.INVALID_REQUEST));
        oldPolicy.setEffectiveTo(dto.getEffectiveFrom().minusSeconds(1));

        return parkingFeePolicy.getId();
    }
}
