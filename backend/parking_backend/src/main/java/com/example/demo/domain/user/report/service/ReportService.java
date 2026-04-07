package com.example.demo.domain.user.report.service;

import com.example.demo.domain.shared.user.User;
import com.example.demo.domain.shared.user.UserRepository;
import com.example.demo.domain.shared.vehicle.Vehicle;
import com.example.demo.domain.shared.vehicle.enums.VehicleStatus;
import com.example.demo.domain.shared.vehicle.VehicleRepository;
import com.example.demo.domain.user.report.entity.Report;
import com.example.demo.domain.user.report.entity.ReportStatus;
import com.example.demo.domain.user.report.entity.ReportType;
import com.example.demo.domain.user.report.entity.VehicleReportStat;
import com.example.demo.domain.user.report.repository.ReportRepository;
import com.example.demo.domain.user.report.repository.VehicleReportRepository;

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

    //신고 생성 + 통계 증가
    public void createReport(Long userId, String carNumber, ReportType type, String description){

        User user =userRepository.findById(userId)
                .orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_FOUND_REPORT));

        Report report = Report.builder()
                .reporter(user) //핵심
                .carNumber(carNumber)
                .reportType(type)
                .description(description)
                .build();

        reportRepository.save(report);

        //통계 업데이트
        VehicleReportStat stat = vehicleReportRepository.findById(carNumber)
                .orElseGet(() -> VehicleReportStat.create(carNumber));

        stat.increaseTotal();
        vehicleReportRepository.save(stat);
    }

    //내가 신고한 내역
    @Transactional(readOnly = true)
    public Page<Report> getMyReports(Long userId, Pageable pageable) {
        return reportRepository.findByReporter_UserIdAndStatusNot(
                userId, ReportStatus.CANCELLED, pageable
        );
    }

    //내가 받은 신고
    @Transactional(readOnly = true)
    public Page<Report> getReceivedReports(Long userId, Pageable pageable) {

        List<Vehicle> vehicles = vehicleRepository.findByUser_UserIdAndStatus(userId, VehicleStatus.ACTIVE);

        List<String> carNumbers = vehicles.stream()
                .map(Vehicle::getCarNumber)
                .toList();

        if (carNumbers.isEmpty()) {
            return Page.empty();
        }
        return reportRepository.findByCarNumberInAndStatusNot(carNumbers, ReportStatus.CANCELLED, pageable);
    }

    //신고 취소
    public void cancelReport(Long reportId, Long userId){

        Report report = reportRepository.findById(reportId)
                .orElseThrow(()-> new CustomException(ErrorCode.REPORT_NOT_FOUND));

        if (!report.getReporter().getUserId().equals(userId)){
            throw  new CustomException(ErrorCode.REPORT_CANNOT_CANCEL);
        }
        report.cancel();
    }

    //기간 검색
    @Transactional(readOnly = true)
    public Page<Report> searchReports(Long userId,LocalDateTime start,LocalDateTime end,Pageable pageable){
        return reportRepository.findByReporter_UserIdAndCreatedAtBetweenAndStatusNot(
                userId,start,end, ReportStatus.CANCELLED,pageable
        );
    }
}


