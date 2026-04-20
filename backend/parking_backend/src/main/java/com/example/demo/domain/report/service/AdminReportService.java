package com.example.demo.domain.report.service;

import com.example.demo.domain.auth.admin.entity.Admin;
import com.example.demo.domain.auth.admin.entity.AdminActionLog;
import com.example.demo.domain.auth.admin.enums.ActionType;
import com.example.demo.domain.auth.admin.enums.AdminStatus;
import com.example.demo.domain.auth.admin.enums.TargetType;
import com.example.demo.domain.auth.admin.repository.AdminActionLogRepository;
import com.example.demo.domain.auth.admin.repository.AdminRepository;
import com.example.demo.domain.notification.Notification;
import com.example.demo.domain.notification.enums.Type;
import com.example.demo.domain.notification.repository.NotificationRepository;
import com.example.demo.domain.report.dto.response.AdminReportPageResponseDto;
import com.example.demo.domain.report.dto.response.AdminReportResponseDto;
import com.example.demo.domain.report.dto.response.AdminReportStatsResponseDto;
import com.example.demo.domain.report.entity.Report;
import com.example.demo.domain.report.entity.ReportStatus;
import com.example.demo.domain.report.entity.ReportType;
import com.example.demo.domain.report.entity.VehicleReportStat;
import com.example.demo.domain.report.repository.ReportRepository;
import com.example.demo.domain.report.repository.VehicleReportRepository;
import com.example.demo.domain.system.setting.SettingKey;
import com.example.demo.domain.system.setting.repository.SystemSettingRepository;
import com.example.demo.domain.vehicle.Vehicle;
import com.example.demo.domain.vehicle.VehicleRepository;
import com.example.demo.domain.vehicle.blacklist.VehicleBlacklist;
import com.example.demo.domain.vehicle.blacklist.enums.BlacklistReasonType;
import com.example.demo.domain.vehicle.blacklist.enums.BlacklistStatus;
import com.example.demo.domain.vehicle.blacklist.repository.VehicleBlacklistRepository;
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
public class AdminReportService {
    private final ReportRepository reportRepository;
    private final VehicleReportRepository vehicleReportRepository;
    private final VehicleBlacklistRepository blacklistRepository;
    private final VehicleRepository vehicleRepository;
    private final NotificationRepository notificationRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final AdminRepository adminRepository;
    private final AdminActionLogRepository adminActionLogRepository;

    // 헬퍼
    private Report findPendingReport(Long reportId){
        Report report = reportRepository.findById(reportId)
                .orElseThrow(()->new BusinessException(ErrorCode.INVALID_REQUEST));
        if (report.getStatus() != ReportStatus.PENDING){
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        return report;
    }

    private Admin getLoginAdmin(){
        AdminAuthDto principal = (AdminAuthDto)
                SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return adminRepository
                .findByLoginIdAndStatus(principal.getUsername(), AdminStatus.ACTIVE)
                .orElseThrow(()->new BusinessException(ErrorCode.ADMIN_NOT_FOUND));
    }
    private int getThreshold(){
        return systemSettingRepository
                .findBySettingKey(SettingKey.REPORT_BLACKLIST_THRESHOLD.getKey())
                .map(s -> Integer.parseInt(s.getSettingValue()))
                .orElse(SettingKey.REPORT_BLACKLIST_THRESHOLD.getDefaultIntValue());
    }
    private void sendWarningNotification(String carNumber, int current, int threshold) {
        vehicleRepository.findByCarNumber(carNumber).ifPresent(vehicle -> {
            if (vehicle.getUser() == null) return;
            notificationRepository.save(Notification.builder()
                    .user(vehicle.getUser())
                    .type(Type.WARNING)
                    .title("차량 신고 경고")
                    .content(String.format(
                            "[%s] 차량에 대한 유효 신고가 %d건 누적되었습니다. " +
                                    "임계값(%d건) 초과 시 블랙리스트에 등록됩니다.",
                            carNumber, current, threshold))
                    .build());
        });
    }
    private void autoBlacklist(String carNumber){
        if (blacklistRepository.existsByCarNumberAndStatus(carNumber, BlacklistStatus.ACTIVE)){
            return;
        }
        Vehicle vehicle = vehicleRepository.findByCarNumber(carNumber).orElse(null);
        blacklistRepository.save(VehicleBlacklist.builder()
                .vehicle(vehicle)
                .carNumber(carNumber)
                .reasonType(BlacklistReasonType.REPORT_ACCUMULATION)
                .reasonDetail("신고 누적으로 인한 자동 블랙리스트 등록")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.of(3000, 1, 1, 0, 0))
                .status(BlacklistStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build()
        );
    }
    private void saveActionLog(Admin admin, Report report,
                               ActionType actionType, String before, String after){
        adminActionLogRepository.save(AdminActionLog.builder()
                .admin(admin)
                .targetType(TargetType.REPORT)
                .actionType(actionType)
                .targetId(report.getId())
                .beforeData(before)
                .afterData(after)
                .build()
        );
    }
    //목록 조회
    @Transactional
    public AdminReportPageResponseDto getReports(
            ReportType type, ReportStatus status,
            String keyword, Pageable pageable
    ){
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword;
        Page<Report> page = reportRepository.findAllWithFilters(type, status, kw, pageable);

        List<AdminReportResponseDto> content = page.getContent().stream()
                .map(AdminReportResponseDto::from).toList();

        AdminReportStatsResponseDto stats= AdminReportStatsResponseDto.builder()
                .pendingCount(reportRepository.countByStatus(ReportStatus.PENDING))
                .approvedCount(reportRepository.countByStatus(ReportStatus.APPROVED))
                .rejectedCount(reportRepository.countByStatus(ReportStatus.REJECTED))
                .build();
        return AdminReportPageResponseDto.builder()
                .content(content)
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .number(page.getNumber())
                .stats(stats)
                .build();
    }
    // 승인
    public void approve(Long reportId){
        Admin admin = getLoginAdmin();
        Report report = findPendingReport(reportId);

        report.approve(admin.getAdminId());

        VehicleReportStat stat = vehicleReportRepository
                .findById(report.getCarNumber())
                .orElseGet(() -> vehicleReportRepository.save(
                        VehicleReportStat.create(report.getCarNumber())
                ));
        stat.increaseValid();

        int threshold = getThreshold();
        int valid = stat.getValidReportCount();

        if (valid == threshold / 2){
            sendWarningNotification(report.getCarNumber(),valid,threshold);
        }
        if (valid == threshold){
            autoBlacklist(report.getCarNumber());
        }
        saveActionLog(admin,report,ActionType.APPROVE,
                "{\"status\":\"PENDING\"}",
                String.format("{\"status\":\"APPROVED\",\"validReportCount\":%d}", valid)
                );
    }
    //거절
    public void reject(Long reportId, String rejectReason){
        Admin admin = getLoginAdmin();
        Report report = findPendingReport(reportId);

        report.reject(admin.getAdminId());

        saveActionLog(admin, report, ActionType.REJECT,
                "{\"status\":\"PENDING\"}",
                String.format("{\"status\":\"REJECTED\",\"rejectReason\":\"%s\"}", rejectReason)
                );
    }
}
