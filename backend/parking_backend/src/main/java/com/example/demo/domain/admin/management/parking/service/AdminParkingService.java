package com.example.demo.domain.admin.management.parking.service;

import com.example.demo.domain.admin.entity.Admin;
import com.example.demo.domain.admin.entity.AdminActionLog;
import com.example.demo.domain.admin.enums.ActionType;
import com.example.demo.domain.admin.enums.TargetType;
import com.example.demo.domain.admin.repository.AdminActionLogRepository;
import com.example.demo.domain.admin.repository.AdminRepository;
import com.example.demo.domain.kiosk.payment.dtos.response.FeeCalculationResponseDto;
import com.example.demo.domain.kiosk.payment.service.PaymentService;
import com.example.demo.domain.shared.activityLog.ActivityLog;
import com.example.demo.domain.shared.activityLog.enums.ActivityType;
import com.example.demo.domain.shared.activityLog.repository.ActivityLogRepository;
import com.example.demo.domain.shared.parkinglog.ParkingLog;
import com.example.demo.domain.shared.parkinglog.dtos.response.ParkingLogDetailResponse;
import com.example.demo.domain.shared.parkinglog.enums.ParkingStatus;
import com.example.demo.domain.shared.parkinglog.enums.PaymentStatus;
import com.example.demo.domain.shared.parkinglog.repository.ParkingLogRepository;
import com.example.demo.domain.shared.parkingspace.enums.SpaceStatus;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import com.example.demo.global.security.admin.AdminAuthDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class AdminParkingService {
    private final ParkingLogRepository parkingLogRepository;
    private final AdminRepository adminRepository;
    private final AdminActionLogRepository adminActionLogRepository;
    private final ActivityLogRepository activityLogRepository;
    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    public void processForceExit(Long parkingLogId, AdminAuthDto adminAuthDto, String reason) throws Exception{
        log.error("강제 출차 서비스 실패..");

        Admin currentAdmin = adminRepository.findByLoginId(adminAuthDto.getUsername())
                .orElseThrow(()->new BusinessException(ErrorCode.ADMIN_NOT_FOUND));

        //주차 로그 조회
        ParkingLog log = parkingLogRepository.findById(parkingLogId)
                .orElseThrow(()->new BusinessException(ErrorCode.VEHICLE_NOT_ENTERED));

        //상태 검증 : 이미 나간 차량은 안됨
        if (log.getParkingStatus() == ParkingStatus.EXITED || log.getParkingStatus() == ParkingStatus.FORCE_EXITED){
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        //AdminActionLog를 위한 Before스냅샷: 변경 전 정보를 DTO로 변환 후 JSON 문자열로 저장
        ParkingLogDetailResponse beforeDto = ParkingLogDetailResponse.toDetailDto(log);
        String beforeData= objectMapper.writeValueAsString(beforeDto); //objectMapper: 객체(DTO,Mapper)를 문자열 형태로 변환
        LocalDateTime now = LocalDateTime.now();

        // 시나리오별 처리 (A:사전정산 완료 차량, B:미결제 차량)
        if(log.getPaymentStatus()==PaymentStatus.PAID){
            //case A: 사전정산 완료 차량
            log.updateStatusToForceExit(now);
        }else {
            //case B: 미결제 차량
            long parkingTime = Math.max(0, Duration.between(log.getEnteredAt(),now).toMinutes());
            FeeCalculationResponseDto feeResult = paymentService.settlementFee(log,log.getParkingFeePolicyId(),parkingTime);

            log.updateForFreeForceExit(feeResult.getRawFee(),now);
        }

        //연관 데이터 정리: 주차 공간 해제
        if (log.getParkingSpace()!=null){
            log.getParkingSpace().setStatus(SpaceStatus.AVAILABLE);
            log.getParkingSpace().setLastStatusChangedAt(now);
        }

        //관리자 감사 로그 기록 (AdminActionLog)
        String afterData = objectMapper.writeValueAsString(ParkingLogDetailResponse.toDetailDto(log));

        //바뀐값 요약 (Changed Fields)
        Map<String,Object> diff = new HashMap<>();
        diff.put("parkingStatus",log.getParkingStatus());
        diff.put("paymentStatus",log.getPaymentStatus());
        diff.put("reason",reason);

        if(currentAdmin==null){
            System.out.println("현재 관리자 정보가 없어 로그의 admin_id가 null로 세팅될 수 있습니다.");
        }

        adminActionLogRepository.save(AdminActionLog.builder()
                .admin(currentAdmin)
                .targetType(TargetType.PARKING_LOG)
                .targetId(parkingLogId)
                .actionType(ActionType.UPDATE)
                .beforeData(beforeData)
                .afterData(afterData)
                .changedFields(objectMapper.writeValueAsString(diff))
                .build());

        //차량 활동 로그 기록 (ActivityLog)
        activityLogRepository.save(ActivityLog.builder()
                .parkingLog(log)
                .activityType(ActivityType.ADMIN_FORCE_EXIT)
                .carNumber(log.getCarNumberSnapshot())
                .message("관리자 강제출차:"+reason)
                .build());
    }
}
