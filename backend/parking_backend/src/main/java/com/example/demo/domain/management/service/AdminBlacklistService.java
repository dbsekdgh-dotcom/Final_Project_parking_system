package com.example.demo.domain.management.service;

import com.example.demo.domain.auth.admin.entity.Admin;
import com.example.demo.domain.auth.admin.enums.ActionType;
import com.example.demo.domain.auth.admin.enums.AdminStatus;
import com.example.demo.domain.auth.admin.enums.TargetType;
import com.example.demo.domain.auth.admin.entity.AdminActionLog;
import com.example.demo.domain.auth.admin.repository.AdminActionLogRepository;
import com.example.demo.domain.auth.admin.repository.AdminRepository;
import com.example.demo.domain.management.dtos.request.AdminBlacklistRequestDto;
import com.example.demo.domain.management.dtos.response.AdminBlacklistDetailResponseDto;
import com.example.demo.domain.management.dtos.response.AdminBlacklistResponseDto;
import com.example.demo.domain.vehicle.Vehicle;
import com.example.demo.domain.vehicle.VehicleRepository;
import com.example.demo.domain.vehicle.blacklist.VehicleBlacklist;
import com.example.demo.domain.vehicle.blacklist.enums.BlacklistReasonType;
import com.example.demo.domain.vehicle.blacklist.enums.BlacklistStatus;
import com.example.demo.domain.vehicle.blacklist.repository.VehicleBlacklistRepository;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import com.example.demo.global.security.admin.AdminAuthDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminBlacklistService {

    private final VehicleBlacklistRepository blacklistRepository;
    private final VehicleRepository vehicleRepository;
    private final AdminRepository adminRepository;
    private final AdminActionLogRepository adminActionLogRepository;
    private final ObjectMapper objectMapper;

    private Admin getLoginAdmin() {
        AdminAuthDto principal = (AdminAuthDto)
                SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return adminRepository.findByLoginIdAndStatus(principal.getUsername(), AdminStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_NOT_FOUND));
    }

    private void saveActionLog(Admin admin, Long targetId, ActionType actionType, Object before, Object after) {
        try {
            adminActionLogRepository.save(AdminActionLog.builder()
                    .admin(admin)
                    .targetType(TargetType.VEHICLE)
                    .actionType(actionType)
                    .targetId(targetId)
                    .beforeData(objectMapper.writeValueAsString(before))
                    .afterData(objectMapper.writeValueAsString(after))
                    .isReverted(false)
                    .build());
        } catch (Exception e) {
            log.error("Blacklist ActionLog 저장 실패 id={}", targetId, e);
        }
    }

    @Transactional(readOnly = true)
    public Page<AdminBlacklistResponseDto> getBlacklist(String keyword, BlacklistStatus status,
                                                        BlacklistReasonType reasonType, Pageable pageable) {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword;
        return blacklistRepository.findAllWithFilters(kw, status, reasonType, pageable)
                .map(AdminBlacklistResponseDto::from);
    }

    @Transactional(readOnly = true)
    public AdminBlacklistDetailResponseDto getDetail(Long id) {
        VehicleBlacklist bl = blacklistRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.BLACKLIST_NOT_FOUND));
        return AdminBlacklistDetailResponseDto.from(bl);
    }

    public void register(AdminBlacklistRequestDto dto) {
        if (blacklistRepository.existsByCarNumberAndStatus(dto.getCarNumber(), BlacklistStatus.ACTIVE)) {
            throw new BusinessException(ErrorCode.BLACKLIST_ALREADY_ACTIVE);
        }

        Admin admin = getLoginAdmin();

        // 차량 번호로 등록 차량 조회 (없으면 null — 비회원 차량)
        Vehicle vehicle = vehicleRepository.findByCarNumber(dto.getCarNumber()).orElse(null);

        LocalDateTime endDate = (dto.getEndDate() != null)
                ? dto.getEndDate()
                : LocalDateTime.of(3000, 1, 1, 0, 0);

        VehicleBlacklist bl = blacklistRepository.save(VehicleBlacklist.builder()
                .vehicle(vehicle)
                .carNumber(dto.getCarNumber())
                .reasonType(dto.getReasonType())
                .reasonDetail(dto.getReasonDetail())
                .startDate(LocalDateTime.now())
                .endDate(endDate)
                .status(BlacklistStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build());

        saveActionLog(admin, bl.getId(), ActionType.BLACKLIST, null, AdminBlacklistResponseDto.from(bl));
    }

    public void release(Long id) {
        VehicleBlacklist bl = blacklistRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.BLACKLIST_NOT_FOUND));

        if (bl.getStatus() == BlacklistStatus.RELEASED) {
            throw new BusinessException(ErrorCode.BLACKLIST_ALREADY_RELEASED);
        }

        Admin admin = getLoginAdmin();
        AdminBlacklistResponseDto before = AdminBlacklistResponseDto.from(bl);

        bl.release();

        saveActionLog(admin, id, ActionType.BLACKLIST, before, AdminBlacklistResponseDto.from(bl));
    }
}
