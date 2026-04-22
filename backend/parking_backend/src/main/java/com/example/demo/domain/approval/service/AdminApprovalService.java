package com.example.demo.domain.approval.service;

import com.example.demo.domain.approval.Approval;
import com.example.demo.domain.approval.dtos.response.AppprovalPageResponseDto;
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
import com.example.demo.domain.reservation.Reservation;
import com.example.demo.domain.reservation.enums.Status;
import com.example.demo.domain.reservation.repository.ReservationRepository;
import com.example.demo.domain.resident.User;
import com.example.demo.domain.resident.UserRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminApprovalService {

    private final ApprovalRepository approvalRepository;
    private final AdminRepository adminRepository;
    private final AdminActionLogRepository adminActionLogRepository;
    private final VehicleRepository vehicleRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final HouseholdRepository householdRepository;

    //헬퍼
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
    public AppprovalPageResponseDto getApprovals(
            ApprovalType type,
            ApprovalStatus status,
            String keyword,
            Pageable pageable
    ){
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword;
        Page<Approval> page = approvalRepository.findAllWithFilters(type,status,kw,pageable);

        List<ApprovalResponseDto> content = page.getContent().stream()
                .map(this::toResponseDto)
                .toList();
        ApprovalStatsDto stats = ApprovalStatsDto.builder()
                .pendingCount(approvalRepository.countByStatus(ApprovalStatus.PENDING))
                .approvedCount(approvalRepository.countByStatus(ApprovalStatus.APPROVED))
                .rejectedCount(approvalRepository.countByStatus(ApprovalStatus.REJECTED))
                .build();
        return AppprovalPageResponseDto.builder()
                .content(content)
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .number(page.getNumber())
                .stats(stats)
                .build();
    }

    // 유형별 승인 처리
    private void approveResident(Approval approval){
        User user = userRepository.findById(approval.getTargetId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST));
        Household household = householdRepository.findById(user.getHousehold().getHouseholdId())
                .orElseThrow(()->new BusinessException(ErrorCode.INVALID_REQUEST));
        household.setIsActive(IsActive.ACTIVE);
        household.setTotalVisitCount(0);
        household.setTodayVisitCount(0);
        household.setMonthlyVisitCount(0);
        household.setActiveReservationCount(0);
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
    }
}
