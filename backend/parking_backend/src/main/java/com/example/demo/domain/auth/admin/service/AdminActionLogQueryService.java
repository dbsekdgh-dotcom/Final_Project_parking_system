package com.example.demo.domain.auth.admin.service;

import com.example.demo.domain.auth.admin.dtos.response.ActionLogResponseDto;
import com.example.demo.domain.auth.admin.entity.Admin;
import com.example.demo.domain.auth.admin.entity.AdminActionLog;
import com.example.demo.domain.auth.admin.enums.ActionType;
import com.example.demo.domain.auth.admin.enums.AdminStatus;
import com.example.demo.domain.auth.admin.enums.TargetType;
import com.example.demo.domain.auth.admin.repository.AdminActionLogRepository;
import com.example.demo.domain.auth.admin.repository.AdminRepository;
import com.example.demo.domain.parking.log.ParkingLog;
import com.example.demo.domain.parking.log.enums.ParkingStatus;
import com.example.demo.domain.parking.log.repository.ParkingLogRepository;
import com.example.demo.domain.parking.policy.ParkingFeePolicy;
import com.example.demo.domain.parking.policy.repository.ParkingFeePolicyRepository;
import com.example.demo.domain.parking.space.ParkingSpace;
import com.example.demo.domain.parking.space.enums.SpaceStatus;
import com.example.demo.domain.parking.space.repository.ParkingSpaceRepository;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import com.example.demo.global.security.admin.AdminAuthDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminActionLogQueryService {
    private final AdminActionLogRepository adminActionLogRepository;
    private final AdminRepository adminRepository;
    private final ParkingFeePolicyRepository parkingFeePolicyRepository;
    private final ParkingSpaceRepository parkingSpaceRepository;
    private final ParkingLogRepository parkingLogRepository;
    private final ObjectMapper objectMapper;


    //헬퍼
    private Admin getLoginAdmin(){
        AdminAuthDto principal = (AdminAuthDto)
                SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return adminRepository.findByLoginIdAndStatus(principal.getUsername(), AdminStatus.ACTIVE)
                .orElseThrow(()-> new BusinessException(ErrorCode.ADMIN_NOT_FOUND));
    }
    private void revertPolicy(Long targetId, JsonNode before){
        ParkingFeePolicy policy = parkingFeePolicyRepository.findById(targetId)
                .orElseThrow(()-> new BusinessException(ErrorCode.INVALID_REQUEST));
        policy.setEffectiveTo(LocalDateTime.parse(before.get("effectiveTo").asText()));
    }
    private void revertParkingSpace(Long targetId, JsonNode before){
        ParkingSpace space = parkingSpaceRepository.findById(targetId)
                .orElseThrow(()-> new BusinessException(ErrorCode.INVALID_REQUEST));
        space.setStatus(SpaceStatus.valueOf(before.get("status").asText()));
        space.setIsDisabled(before.get("isDisabled").asBoolean());
        space.setIsEvCharge(before.get("isEvCharge").asBoolean());
    }
    private void revertParkingLog(Long targetId, JsonNode before){
        ParkingLog log = parkingLogRepository.findById(targetId)
                .orElseThrow(()->new BusinessException(ErrorCode.INVALID_REQUEST));
        log.setParkingStatus(ParkingStatus.valueOf(before.get("parkingStatus").asText()));
    }

    @Transactional(readOnly = true)
    public Page<ActionLogResponseDto> getActionLogs(
            TargetType targetType, ActionType actionType,
            Boolean isReverted, String keyword,
            LocalDateTime startDate, LocalDateTime endDate,
            Pageable pageable
    ){
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword;
        return adminActionLogRepository
                .findAllWithFilters(targetType, actionType, isReverted, kw, startDate, endDate, pageable)
                .map(ActionLogResponseDto::from);
    }
    public void revert(Long actionId){
        Admin admin = getLoginAdmin();
        AdminActionLog log = adminActionLogRepository.findById(actionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST));

        if (log.getActionType() != ActionType.UPDATE){
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        if (log.getIsReverted()){
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        try{
            JsonNode before = objectMapper.readTree(log.getBeforeData());
            switch (log.getTargetType()){
                case POLICY -> revertPolicy(log.getTargetId(),before);
                case PARKING_SPACE -> revertParkingSpace(log.getTargetId(), before);
                case PARKING_LOG -> revertParkingLog(log.getTargetId(), before);
                default -> throw new BusinessException(ErrorCode.INVALID_REQUEST);
            }
        } catch (BusinessException e){
            throw e;
        } catch (Exception e){
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        log.markAsReverted(admin);
    }
}
