package com.example.demo.domain.reservation.policy.service;

import com.example.demo.domain.auth.admin.dtos.request.ReservationPolicyRequestDto;
import com.example.demo.domain.auth.admin.dtos.response.ReservationPolicyResponseDto;
import com.example.demo.domain.auth.admin.entity.Admin;
import com.example.demo.domain.auth.admin.enums.ActionType;
import com.example.demo.domain.auth.admin.enums.TargetType;
import com.example.demo.domain.auth.admin.service.AdminActionLogService;
import com.example.demo.domain.reservation.policy.ReservationEventPolicy;
import com.example.demo.domain.reservation.policy.repository.ReservationEventPolicyRepository;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReservationPolicyService {
    private final ReservationEventPolicyRepository reservationEventPolicyRepository;
    private final AdminActionLogService adminActionLogService;

    public Page<ReservationPolicyResponseDto> getPolicies(Pageable pageable){
        return reservationEventPolicyRepository.findAll(pageable)
                .map(ReservationPolicyResponseDto::new);
    }
    public ReservationPolicyResponseDto getActivePolicy(){
        return reservationEventPolicyRepository.findActivePolicy(LocalDateTime.now())
                .map(ReservationPolicyResponseDto::new)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
    }
    @Transactional
    public ReservationPolicyResponseDto createPolicy(ReservationPolicyRequestDto dto){
        Admin admin = adminActionLogService.getAdmin();
        reservationEventPolicyRepository.findTopByOrderByIdDesc().ifPresent(latest ->{
            if (latest.getEndDate() == null || latest.getEndDate().isAfter(dto.getStartDate().minusSeconds(1))){
                latest.setEndDate(dto.getStartDate().minusSeconds(1));
            }
        });
        ReservationEventPolicy policy = ReservationEventPolicy.builder()
                .admin(admin)
                .eventName(dto.getEventName())
                .startDate(dto.getStartDate())
                .dailyLimitPerHousehold(dto.getDailyLimitPerHousehold())
                .monthlyLimitPerHousehold(dto.getMonthlyLimitPerHousehold())
                .maxActiveReservations(dto.getMaxActiveReservations())
                .permittedMinutes(dto.getPermittedMinutes())
                .noShowPenaltyEnabled(dto.getNoShowPenaltyEnabled())
                .build();
        reservationEventPolicyRepository.save(policy);

        String json = adminActionLogService.toJson(new ReservationPolicyResponseDto(policy));
        adminActionLogService.insertAdminlog(
                admin, ActionType.CREATE, TargetType.RESERVATION_POLICY,
                policy.getId(), json,json
        );
        return new ReservationPolicyResponseDto(policy);
    }

    @Transactional
    public ReservationPolicyResponseDto updatePolicy(Long policyId, ReservationPolicyRequestDto dto){
        ReservationEventPolicy policy = reservationEventPolicyRepository.findById(policyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_POLICY_NOT_FOUND));

        String before = adminActionLogService.toJson(new ReservationPolicyResponseDto(policy));
        if (!policy.getStartDate().equals(dto.getStartDate())) {
            // 이 정책 바로 이전 정책 (ID 기준) 찾아서 endDate 재조정
            reservationEventPolicyRepository.findPreviousPolicy(policyId).ifPresent(prev -> {
                if (prev.getEndDate() == null
                        || prev.getEndDate().isAfter(dto.getStartDate().minusSeconds(1))) {
                    prev.setEndDate(dto.getStartDate().minusSeconds(1));
                }
            });
        }

        policy.setEventName(dto.getEventName());
        policy.setStartDate(dto.getStartDate());
        policy.setDailyLimitPerHousehold(dto.getDailyLimitPerHousehold());
        policy.setMonthlyLimitPerHousehold(dto.getMonthlyLimitPerHousehold());
        policy.setMaxActiveReservations(dto.getMaxActiveReservations());
        policy.setPermittedMinutes(dto.getPermittedMinutes());
        policy.setNoShowPenaltyEnabled(dto.getNoShowPenaltyEnabled());

        String after = adminActionLogService.toJson(new ReservationPolicyResponseDto(policy));

        Admin admin = adminActionLogService.getAdmin();
        adminActionLogService.insertAdminlog(
                admin, ActionType.UPDATE, TargetType.RESERVATION_POLICY,
                policyId, before, after
        );
        return new ReservationPolicyResponseDto(policy);
    }

}
