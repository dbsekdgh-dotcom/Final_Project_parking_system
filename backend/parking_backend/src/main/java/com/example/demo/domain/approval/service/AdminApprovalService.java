package com.example.demo.domain.approval.service;

import com.example.demo.domain.approval.Approval;
import com.example.demo.domain.approval.dtos.response.ApprovalPageResponseDto;
import com.example.demo.domain.approval.dtos.response.ApprovalResponseDto;
import com.example.demo.domain.approval.dtos.response.ApprovalStatsDto;
import com.example.demo.domain.approval.enums.ApprovalStatus;
import com.example.demo.domain.approval.enums.ApprovalType;
import com.example.demo.domain.approval.repository.ApprovalRepository;
import com.example.demo.domain.auth.admin.entity.Admin;
import com.example.demo.domain.auth.admin.entity.AdminActionLog;
import com.example.demo.domain.auth.admin.enums.ActionType;
import com.example.demo.domain.auth.admin.enums.AdminStatus;
import com.example.demo.domain.auth.admin.enums.TargetType;
import com.example.demo.domain.auth.admin.repository.AdminActionLogRepository;
import com.example.demo.domain.auth.admin.repository.AdminRepository;
import com.example.demo.domain.notification.enums.Type;
import com.example.demo.domain.notification.service.NotificationAiClient;
import com.example.demo.domain.notification.service.NotificationService;
import com.example.demo.domain.reservation.Reservation;
import com.example.demo.domain.reservation.enums.Status;
import com.example.demo.domain.reservation.repository.ReservationRepository;
import com.example.demo.domain.resident.household.Household;
import com.example.demo.domain.resident.household.enums.IsActive;
import com.example.demo.domain.resident.household.repository.HouseholdRepository;
import com.example.demo.domain.vehicle.Vehicle;
import com.example.demo.domain.vehicle.VehicleRepository;
import com.example.demo.domain.vehicle.enums.VehicleStatus;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import com.example.demo.global.security.admin.AdminAuthDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminApprovalService {

    private final ApprovalRepository approvalRepository;
    private final AdminRepository adminRepository;
    private final AdminActionLogRepository adminActionLogRepository;
    private final VehicleRepository vehicleRepository;
    private final ReservationRepository reservationRepository;
    private final HouseholdRepository householdRepository;
    private final NotificationAiClient notificationAiClient;
    private final NotificationService notificationService;

    //헬퍼
    private void notifyVehicleApproved(Approval approval, Vehicle vehicle){
        try {
            Map<String, Object> ctx = Map.of("car_number", vehicle.getCarNumber());
            NotificationAiClient.NotificationContent ai =
                    notificationAiClient.generate("VEHICLE_APPROVED", ctx);
            String title = (ai != null) ? ai.title() : "차량 등록 완료";
            String content = (ai != null) ? ai.content() : vehicle.getCarNumber() + "차량 등록이 승인되었습니다.";

            notificationService.createNotification(
                    approval.getRequestUserId().getUserId(), title, content, Type.SYSTEM
            );
        }catch (Exception e){
            log.warn("차량 승인 알림 생성 실패: {}", e.getMessage());
        }
    }
    private void notifyVehicleRejected(Approval approval, Vehicle vehicle, String reason){
        try {
            Map<String,Object> ctx = Map.of(
                    "car_number", vehicle.getCarNumber(),
                    "reason", reason != null ? reason : "사유 없음"
            );
            NotificationAiClient.NotificationContent ai =
                    notificationAiClient.generate("VEHICLE_REJECTED", ctx);

            String title = (ai != null) ? ai.title() : "차량 등록 반려";
            String content = (ai != null) ? ai.content() : vehicle.getCarNumber() +"차량 등록이 반려되었습니다.";

            notificationService.createNotification(
                    approval.getRequestUserId().getUserId(), title, content, Type.WARNING
            );
        } catch (Exception e){
            log.warn("차량 거절 알림 생성 실패: {}", e.getMessage());
        }
    }
    private void notifyResidentApproved(Approval approval, Household household){
        try {
            Map<String,Object> ctx = Map.of("unit_no", household.getUnitNo());
            NotificationAiClient.NotificationContent ai =
                    notificationAiClient.generate("RESIDENT_APPROVED", ctx);
            String title = (ai != null) ? ai.title() : "입주민 등록 완료";
            String content = (ai != null) ? ai.content() : household.getUnitNo() + "호 입주민 등록이 완료되었습니다.";

            notificationService.createNotification(approval.getRequestUserId().getUserId(), title, content, Type.SYSTEM);
        }catch (Exception e){
            log.warn("입주민 승인 알림 생성 실패: {}", e.getMessage());
        }
    }
    private void notifyReservationApproved(Approval approval, Reservation reservation){
        try {
            Map<String,Object> ctx = Map.of(
                    "car_number", reservation.getCarNumber(),
                    "visit_date", reservation.getVisitStartAt().toString()
            );
            NotificationAiClient.NotificationContent ai =
                    notificationAiClient.generate("RESERVATION_APPROVED", ctx);

            String title = (ai != null) ? ai.title() : "방문 예약 승인";
            String content = (ai != null) ? ai.content() : reservation.getCarNumber() + " 방문 예약이 승인되었습니다.";

            notificationService.createNotification(
                    approval.getRequestUserId().getUserId(), title, content, Type.RESERVATION
            );
        }catch (Exception e){
            log.warn("예약 승인 알림 생성 실패: {}",e.getMessage());
        }
    }
    private void notifyReservationRejected(Approval approval, Reservation reservation, String reason){
        try {
            Map<String,Object> ctx = Map.of(
                    "car_number", reservation.getCarNumber(),
                    "visit_date", reservation.getVisitStartAt().toString(),
                    "reason", reason != null ? reason : "사유 없음"
            );
            NotificationAiClient.NotificationContent ai =
                    notificationAiClient.generate("RESERVATION_REJECTED",ctx);
            String title = (ai != null) ? ai.title() : "방문 예약 반려";
            String content = (ai != null) ? ai.content() : reservation.getCarNumber() + " 방문 예약이 반려되었습니다.";

            notificationService.createNotification(
                    approval.getRequestUserId().getUserId(), title, content, Type.RESERVATION
            );
        }catch (Exception e){
            log.warn("예약 거절 알림 생성 실패:{}", e.getMessage());
        }
    }
    private Approval findPendingApproval(Long approvalId){
        Approval approval = approvalRepository.findById(approvalId)
                .orElseThrow(()-> new BusinessException(ErrorCode.INVALID_REQUEST));
        if (approval.getStatus() != ApprovalStatus.PENDING){
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        return approval;
    }
    private Admin getLoginAdmin(){
        AdminAuthDto principal = (AdminAuthDto)
                SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return adminRepository
                .findByLoginIdAndStatus(principal.getUsername(), AdminStatus.ACTIVE)
                .orElseThrow(()-> new BusinessException(ErrorCode.ADMIN_NOT_FOUND));
    }
    private void saveActionLog(Admin admin, Approval approval, ActionType actionType, String before, String after){
        TargetType targetType=switch (approval.getApprovalType()){
            case RESIDENT -> TargetType.USER;
            case VEHICLE -> TargetType.VEHICLE;
            case RESERVATION -> TargetType.RESERVATION;
            default -> TargetType.USER;
        };
        adminActionLogRepository.save(AdminActionLog.builder()
                .admin(admin)
                .targetType(targetType)
                .actionType(actionType)
                .targetId(approval.getApprovalId())
                .beforeData(before)
                .afterData(after)
                .build());
    }
    private ApprovalResponseDto toResponseDto(Approval approval){
        String content="";
        LocalDateTime visitStart = null;
        LocalDateTime visitEnd = null;

        switch (approval.getApprovalType()){
            case RESIDENT -> content = "입주민 등록 신청";
            case VEHICLE -> {
                Vehicle v = vehicleRepository.findById(approval.getTargetId()).orElse(null);
                content = (v != null) ? v.getCarNumber() + " 차량 등록 신청" : " 차량 등록 신청 ";
            }
            case RESERVATION -> {
                Reservation r = reservationRepository.findById(approval.getTargetId()).orElse(null);
                if(r != null){
                    content = r.getCarNumber() + " 방문 예약 신청 ";
                    visitStart = r.getVisitStartAt();
                    visitEnd = r.getVisitEndAt();
                }else {
                    content = " 방문 예약 신청 ";
                }
            }
        }
        return ApprovalResponseDto.from(approval, content, visitStart, visitEnd);
    }
    private String buildStatusJson(String status){
        return String.format("{\"status\":\"%s\"}", status);
    }
    //목록조회
    public ApprovalPageResponseDto getApprovals(
            ApprovalType type,
            ApprovalStatus status,
            String keyword,
            Pageable pageable
    ){
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword;
        Page<Approval> page = approvalRepository.findAllWithFilters(type,status,kw,pageable);

        List<ApprovalResponseDto> dtoList = page.getContent().stream()
                .map(this::toResponseDto)
                .toList();
        ApprovalStatsDto stats = ApprovalStatsDto.builder()
                .pendingCount(approvalRepository.countByStatus(ApprovalStatus.PENDING))
                .approvedCount(approvalRepository.countByStatus(ApprovalStatus.APPROVED))
                .rejectedCount(approvalRepository.countByStatus(ApprovalStatus.REJECTED))
                .build();
        return ApprovalPageResponseDto.builder()
                .content(dtoList)
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .number(page.getNumber())
                .stats(stats)
                .build();
    }

    // 유형별 승인 처리
    private void approveResident(Approval approval){
        // RESIDENT 승인 시 targetId는 householdId (ResidentApplyService 참조)
        Household household = householdRepository.findById(approval.getTargetId())
                .orElseThrow(()->new BusinessException(ErrorCode.INVALID_REQUEST));
        household.setIsActive(IsActive.ACTIVE);
        household.setTotalVisitCount(0);
        household.setTodayVisitCount(0);
        household.setMonthlyVisitCount(0);
        household.setActiveReservationCount(0);
        // 신청자 User를 해당 Household에 연결 — 이 연결이 없으면 승인 후에도 입주민 미인식
        approval.getRequestUserId().setHousehold(household);
    }
    private void approveVehicle(Approval approval){
        Vehicle vehicle = vehicleRepository.findById(approval.getTargetId())
                .orElseThrow(()->new BusinessException(ErrorCode.INVALID_REQUEST));
        vehicle.updateRegistration(vehicle.getUser(), vehicle.getVehicleName(), VehicleStatus.ACTIVE);
    }
    private void approveReservation(Approval approval){
        Reservation reservation = reservationRepository.findById(approval.getTargetId())
                .orElseThrow(()->new BusinessException(ErrorCode.INVALID_REQUEST));
        reservation.updateStatus(Status.RESERVED);
    }
    //유형별 거절 처리
    private void rejectVehicle(Approval approval){
        Vehicle vehicle = vehicleRepository.findById(approval.getTargetId())
                .orElseThrow(()-> new BusinessException(ErrorCode.INVALID_REQUEST));
        vehicle.softDelete();
    }
    private void rejectReservation(Approval approval){
        Reservation reservation = reservationRepository.findById(approval.getTargetId())
                .orElseThrow(()-> new BusinessException(ErrorCode.INVALID_REQUEST));
        reservation.updateStatus(Status.REJECTED);
        // 예약 생성 시 증가한 activeReservationCount 복원
        var user = approval.getRequestUserId();
        if (user != null && user.getHousehold() != null) {
            householdRepository.decrementActiveReservationCount(user.getHousehold().getHouseholdId());
        }
    }
    // 승인
    public void approve(Long approvalId){
        Admin admin = getLoginAdmin();
        Approval approval = findPendingApproval(approvalId);
        String beforeData = buildStatusJson(approval.getStatus().name());

        switch (approval.getApprovalType()){
            case RESIDENT -> approveResident(approval);
            case VEHICLE -> approveVehicle(approval);
            case RESERVATION -> approveReservation(approval);
        }

        approval.updateStatus(ApprovalStatus.APPROVED);
        approval.setProcessedAt(LocalDateTime.now());
        approval.setProcessedByAdminId(admin.getAdminId());

        saveActionLog(admin, approval, ActionType.APPROVE, beforeData, buildStatusJson("APPROVED"));

        switch (approval.getApprovalType()){
            case VEHICLE -> {
                Vehicle v = vehicleRepository.findById(approval.getTargetId()).orElse(null);
                if (v != null) notifyVehicleApproved(approval,v);
            }
            case RESIDENT -> {
                Household h = householdRepository.findById(approval.getTargetId()).orElse(null);
                if (h != null) notifyResidentApproved(approval,h);
            }
            case RESERVATION -> {
                Reservation r = reservationRepository.findById(approval.getTargetId()).orElse(null);
                if (r != null) notifyReservationApproved(approval,r);
            }
        }
    }

    //거절
    public void reject(Long approvalId, String rejectReason){
        Admin admin = getLoginAdmin();
        Approval approval = findPendingApproval(approvalId);
        String beforeData = buildStatusJson(approval.getStatus().name());

        switch (approval.getApprovalType()){
            case VEHICLE -> rejectVehicle(approval);
            case RESERVATION -> rejectReservation(approval);
            case RESIDENT -> {}
        }
        approval.updateStatus(ApprovalStatus.REJECTED);
        approval.setProcessedAt(LocalDateTime.now());
        approval.setProcessedByAdminId(admin.getAdminId());
        approval.setRejectReason(rejectReason);

        saveActionLog(admin,approval,ActionType.REJECT, beforeData, String.format("{\"status\":\"REJECTED\",\"rejectReason\":\"%s\"}", rejectReason));

        switch (approval.getApprovalType()){
            case VEHICLE -> {
                Vehicle v = vehicleRepository.findById(approval.getTargetId()).orElse(null);
                if (v != null) notifyVehicleRejected(approval,v,rejectReason);
            }
            case RESERVATION -> {
                Reservation r = reservationRepository.findById(approval.getTargetId()).orElse(null);
                if (r != null) notifyReservationRejected(approval,r,rejectReason);
            }
        }
    }
}
