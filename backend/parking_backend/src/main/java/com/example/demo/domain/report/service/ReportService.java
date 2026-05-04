package com.example.demo.domain.report.service;

import com.example.demo.domain.notification.enums.Type;
import com.example.demo.domain.notification.service.NotificationService;
import com.example.demo.domain.resident.User;
import com.example.demo.domain.resident.UserRepository;
import com.example.demo.domain.vehicle.Vehicle;
import com.example.demo.domain.vehicle.enums.VehicleStatus;
import com.example.demo.domain.vehicle.VehicleRepository;
import com.example.demo.domain.report.dto.ReportResponseDto;
import com.example.demo.domain.report.entity.Report;
import com.example.demo.domain.report.entity.ReportStatus;
import com.example.demo.domain.report.entity.ReportType;
import com.example.demo.domain.report.entity.VehicleReportStat;
import com.example.demo.domain.report.repository.ReportRepository;
import com.example.demo.domain.report.repository.VehicleReportRepository;

import com.example.demo.global.exception.CustomException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {

    private final ReportRepository reportRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleReportRepository vehicleReportRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /**
     * 신고 생성 + 통계 증가
     * 파라미터 Long userId -> String email 변경
     */
    public void createReport(String email, String carNumber, ReportType type, String description, String report_s3path){

        // 이메일로 유저 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND_REPORT));

            Report report = Report.builder()
                .reporter(user)
                .carNumber(carNumber)
                .reportType(type)
                .description(description)
                .imageUrl(report_s3path)
                .build();

        reportRepository.save(report);


        //5월2일 수정해야될 것-신고 시 알림 생성 로직 수정해야됨

        vehicleRepository.findByCarNumber(carNumber.replace(" ","")).ifPresent(vehicle ->{
            User targetUser = vehicle.getUser();  //차주 찾기
            if (targetUser !=null){
                String title ="차량 신고 접수 알림";
                String content = switch (type){
                    case ILLEGAL_PARKING -> String.format("[%s] 차량이 '일반 불법 주차'로 신고되었습니다.",carNumber);
                    case BLOCKING -> String.format("[%s] 차량이 '통로 막음'으로 신고되었습니다", carNumber);
                    case DOUBLE_PARK -> String.format("[%s] 차량이 '이중 주차'로 신고되었습니다.", carNumber);
                    case NOISE -> String.format("[%s] 차량에 대해 '소음공해' 신고가 접수되었습니다.", carNumber);
                    case OTHER -> String.format("[%s] 차량에 대해 '기타' 신고가 접수되었습니다: %s", carNumber, description);
                    default ->String.format("[%s] 차량에 새로운 신고가 접수되었습니다.", carNumber);
                };

                //알림 서비스 호출
                notificationService.createNotification(
                        targetUser.getUserId(),
                        title,
                        content,
                        Type.WARNING
                );
            }
        });

        // 통계 업데이트
        VehicleReportStat stat = vehicleReportRepository.findById(carNumber)
                .orElseGet(() -> VehicleReportStat.create(carNumber));

        stat.increaseTotal();
        vehicleReportRepository.save(stat);
    }

    /**
     * 내가 신고한 내역
     * 파라미터 Long userId -> String email 변경
     */
    @Transactional(readOnly = true)
    public Page<ReportResponseDto> getMyReports(String email, Pageable pageable) {
        // 이메일로 유저 조회 후 해당 유저의 ID로 검색
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND_REPORT));

        Page<Report> reports = reportRepository
                .findMyReports(user.getUserId(), pageable);

        return reports.map(ReportResponseDto::from);
    }

    /**
     * 내가 받은 신고
     * 파라미터 Long userId -> String email 변경
     */
    @Transactional(readOnly = true)
    public Page<ReportResponseDto> getReceivedReports(String email, Pageable pageable) {
        // 이메일로 유저 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND_REPORT));

        // 해당 유저가 소유한 활성 차량 리스트 조회
        List<Vehicle> vehicles = vehicleRepository.findByUser_UserIdAndStatus(user.getUserId(), VehicleStatus.ACTIVE);

        List<String> carNumbers = vehicles.stream()
                .map(v -> v.getCarNumber().replace(" ",""))
                .toList();

        if (carNumbers.isEmpty()) {
            return Page.empty();
        }
        return reportRepository.findReceivedReports(carNumbers, pageable)
                .map(ReportResponseDto::from);
    }

    /**
     * 신고 취소
     * 파라미터 Long userId -> String email 변경
     */
    public void cancelReport(Long reportId, String email){

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));

        // 이메일로 유저 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND_REPORT));

        // 본인이 작성한 신고인지 확인
        if (!report.getReporter().getUserId().equals(user.getUserId())){
            throw new CustomException(ErrorCode.REPORT_CANNOT_CANCEL);
        }
        report.cancel();
        reportRepository.save(report);  //명시적으로 저장 호출
    }

    /**
     * 기간 검색
     * 파라미터 Long userId -> String email 변경
     */
    @Transactional(readOnly = true)
    public Page<ReportResponseDto> searchReports(String email, LocalDateTime start, LocalDateTime end, Pageable pageable){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND_REPORT));

        return reportRepository.findByPeriod(user.getUserId(), start, end, pageable)
                .map(ReportResponseDto::from);
    }

    /**
     * 신고 상태 변경 (기존 유지)
     */
    public void updateReportStatus(Long reportId, ReportStatus newStatus, Long adminId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));

        switch (newStatus) {
            case APPROVED -> {
                report.approve(adminId);
                //관리자가 승인하면 유효 신고 통계(Valid) 1증가
                VehicleReportStat stat = vehicleReportRepository.findById(report.getCarNumber())
                        .orElseGet(() -> VehicleReportStat.create(report.getCarNumber()));
                stat.increaseValid();
                vehicleReportRepository.save(stat);
            }

            case REJECTED -> report.reject(adminId);
            case CANCELLED -> report.cancel();
            case PENDING -> {}
            }
        }
    }