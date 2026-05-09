package com.example.demo.domain.parking.policy.service;

import com.example.demo.domain.auth.admin.entity.Admin;
import com.example.demo.domain.auth.admin.entity.AdminActionLog;
import com.example.demo.domain.auth.admin.enums.ActionType;
import com.example.demo.domain.auth.admin.enums.AdminStatus;
import com.example.demo.domain.auth.admin.enums.TargetType;
import com.example.demo.domain.auth.admin.repository.AdminActionLogRepository;
import com.example.demo.domain.auth.admin.service.AdminActionLogService;
import com.example.demo.domain.parking.policy.dtos.request.ParkingFeePolicyChangeRequestDto;
import com.example.demo.domain.parking.policy.dtos.request.ParkingFeePolicyUpdateRequestDto;
import com.example.demo.domain.parking.policy.dtos.response.ParkingFeePolicyResponseDto;
import com.example.demo.domain.payment.ticketpolicy.TicketPolicy;
import com.example.demo.domain.payment.ticketpolicy.dtos.response.TicketPolicyResponseDto;
import com.example.demo.domain.auth.admin.repository.AdminRepository;
import com.example.demo.domain.parking.policy.ParkingFeePolicy;
import com.example.demo.domain.parking.policy.enums.ParkingType;
import com.example.demo.domain.parking.policy.repository.ParkingFeePolicyRepository;
import com.example.demo.domain.payment.ticketpolicy.enums.Status;
import com.example.demo.domain.payment.ticketpolicy.enums.UseType;
import com.example.demo.domain.payment.ticketpolicy.repository.TicketPolicyRepository;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import com.example.demo.global.security.admin.AdminAuthDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.util.Json;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ParkingFeePolicyService {
    private final ParkingFeePolicyRepository parkingFeePolicyRepository;
    private final TicketPolicyRepository ticketPolicyRepository;
    private final AdminRepository adminRepository;
    private final AdminActionLogService adminActionLogService;

    //요금 정책 조회
    public Map<String,Object> getEffectiveParkingFeePolicy() {
        Map<String,Object> finalResponse=new HashMap<>();
        LocalDateTime now=LocalDateTime.now();
        //현재 진행중인 요금 정책
        List<ParkingFeePolicy> policies=parkingFeePolicyRepository.findCurrentEffectivePolicy(now);
        Map<ParkingType,ParkingFeePolicyResponseDto> currentPolicies=policies.stream().collect(Collectors.toMap(
                ParkingFeePolicy::getParkingType,
                ParkingFeePolicyResponseDto::toPolicyDto,
                (exist, replace)
                        ->replace.getVersion()>exist.getVersion()?replace:exist
        ));
        //시행 예정인 요금 정책
        List<ParkingFeePolicy> upcoming=parkingFeePolicyRepository.findUpcomingEffectivePolicy(now);
        Map<ParkingType,ParkingFeePolicyResponseDto> upcomingPolicies=upcoming.stream().collect(Collectors.toMap(
                ParkingFeePolicy::getParkingType,
                ParkingFeePolicyResponseDto::toPolicyDto,
                (exist,replace)
                        ->replace.getVersion()>exist.getVersion()?replace:exist
        ));
        //할인권 리스트
        List<TicketPolicy> ticketPolicies=ticketPolicyRepository.findByStatusIsNot(Status.DELETED);
        //상가 사용 할인권
        List<TicketPolicyResponseDto> storeTicket=ticketPolicies.stream().filter(t-> UseType.STORE.equals(t.getUseType())).map(TicketPolicyResponseDto::toTicketPolicyDto).toList();
        //관리자 사용 할인권
        List<TicketPolicyResponseDto> adminTicket=ticketPolicies.stream().filter(t-> UseType.ADMIN.equals(t.getUseType())).filter(t->t.isFreeTicket()==false).map(TicketPolicyResponseDto::toTicketPolicyDto).toList();
        //상가 기본 지급 할인권
        List<TicketPolicyResponseDto> monthlyTicket=ticketPolicies.stream().filter(t-> t.isFreeTicket()==true).map(TicketPolicyResponseDto::toTicketPolicyDto).toList();

        finalResponse.put("currentVisitor",currentPolicies.get(ParkingType.VISIT));
        finalResponse.put("currentReservation",currentPolicies.get(ParkingType.RESERVATION));
        finalResponse.put("upcomingVisitor",upcomingPolicies.get(ParkingType.VISIT));
        finalResponse.put("upcomingReservation",upcomingPolicies.get(ParkingType.RESERVATION));
        finalResponse.put("storeTicket",storeTicket);
        finalResponse.put("adminTicket",adminTicket);
        finalResponse.put("monthlyTicket",monthlyTicket);
        return finalResponse;
    }

    //요금 정책 일부 수정
    public long changeParkingFeePolicy(ParkingFeePolicyChangeRequestDto dto){
        //스프링시큐리티가 헤더에서 꺼내서 저장해둔 유저정보 가져오기
        Admin admin=adminActionLogService.getAdmin();

        //실행 예정중인 정책이 있으나, 현재 적용중인 정책 수정 요청시
        ParkingFeePolicy oldParkingFeePolicy=parkingFeePolicyRepository.getLatestVersion(dto.getParkingType()).orElseThrow(()->new BusinessException(ErrorCode.INVALID_REQUEST));
        if(oldParkingFeePolicy.getVersion().equals(dto.getVersion())){
            throw new BusinessException(ErrorCode.POLICY_NOT_MODIFIABLE);
        }
        String oldPolicyStr=adminActionLogService.toJson(oldParkingFeePolicy);

        //새로운 정책 insert
        ParkingFeePolicy parkingFeePolicy=insertFeePolicy(admin,dto.getParkingType(),dto.getGraceMinutes(),dto.getBaseFee(),dto.getUnitMinutes(),dto.getUnitFee(),dto.getDaliyMaxFee(),dto.getEffectiveFrom(),oldParkingFeePolicy.getVersion());
        String newPolicy=adminActionLogService.toJson(parkingFeePolicy);
        adminActionLogService.insertPolicyAdminLog(admin,ActionType.CREATE,parkingFeePolicy.getId(),newPolicy,newPolicy);

        //기존 정책 유효기간 설정
        oldParkingFeePolicy.setEffectiveTo(dto.getEffectiveFrom().minusSeconds(1));
        ParkingFeePolicy updatedOldPolicy=parkingFeePolicyRepository.saveAndFlush(oldParkingFeePolicy);
        String updatedOldPolicyStr=adminActionLogService.toJson(updatedOldPolicy);
        adminActionLogService.insertPolicyAdminLog(admin,ActionType.UPDATE,updatedOldPolicy.getId(),oldPolicyStr,updatedOldPolicyStr);

        return parkingFeePolicy.getId();
    }
    
    //요금 정책 전체 업데이트 
    public long updateParkingFeePolicy(ParkingFeePolicyUpdateRequestDto dto){
        //스프링시큐리티가 헤더에서 꺼내서 저장해둔 유저정보 가져오기
        Admin admin=adminActionLogService.getAdmin();

        //기존 정책 중 최신 정책 버전 가져오기
        ParkingFeePolicy oldParkingFeePolicy=parkingFeePolicyRepository.getLatestVersion(dto.getParkingType()).orElseThrow(()->new BusinessException(ErrorCode.INVALID_REQUEST));
        String oldPolicyStr=adminActionLogService.toJson(oldParkingFeePolicy);

        //새로운 정책 insert
        ParkingFeePolicy parkingFeePolicy=insertFeePolicy(admin,dto.getParkingType(),dto.getGraceMinutes(),dto.getBaseFee(),dto.getUnitMinutes(),dto.getUnitFee(),dto.getDaliyMaxFee(),dto.getEffectiveFrom(),oldParkingFeePolicy.getVersion());
        String newPolicy=adminActionLogService.toJson(parkingFeePolicy);
        adminActionLogService.insertPolicyAdminLog(admin,ActionType.CREATE,parkingFeePolicy.getId(),newPolicy,newPolicy);

        //기존 정책 유효기간 설정
        oldParkingFeePolicy.setEffectiveTo(dto.getEffectiveFrom().minusSeconds(1));
        ParkingFeePolicy updatedOldPolicy=parkingFeePolicyRepository.saveAndFlush(oldParkingFeePolicy);
        String updatedOldPolicyStr=adminActionLogService.toJson(updatedOldPolicy);
        adminActionLogService.insertPolicyAdminLog(admin,ActionType.UPDATE,updatedOldPolicy.getId(),oldPolicyStr,updatedOldPolicyStr);

        return parkingFeePolicy.getId();
    }

    //새로운 정책 insert
    private ParkingFeePolicy  insertFeePolicy(Admin admin,ParkingType parkingType,int graceMinutes,int baseFee,int unitMinutes,int unitFee,int dailyMaxFee,LocalDateTime effectiveFrom,long oldVersion ){
        ParkingFeePolicy newPolicy=ParkingFeePolicy.builder()
                .admin(admin)
                .parkingType(parkingType)
                .graceMinutes(graceMinutes)
                .baseFee(baseFee)
                .unitMinutes(unitMinutes)
                .unitFee(unitFee)
                .dailyMaxFee(dailyMaxFee)
                .isActive(false)
                .effectiveFrom(effectiveFrom)
                .version(oldVersion+1)
                .build();
        return  parkingFeePolicyRepository.save(newPolicy);
    }

}
