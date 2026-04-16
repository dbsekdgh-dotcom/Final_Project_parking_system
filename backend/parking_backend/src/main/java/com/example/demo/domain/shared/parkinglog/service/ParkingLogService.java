package com.example.demo.domain.shared.parkinglog.service;

import com.example.demo.domain.admin.management.parking.service.AdminParkingService;
import com.example.demo.domain.shared.parkingTicket.ParkingTicket;
import com.example.demo.domain.shared.parkingTicket.Status;
import com.example.demo.domain.shared.parkingTicket.repository.ParkingTicketRepository;
import com.example.demo.domain.shared.parkingfeepolicy.ParkingFeePolicy;
import com.example.demo.domain.shared.parkingfeepolicy.repository.ParkingFeePolicyRepository;
import com.example.demo.domain.shared.parkinglog.ParkingLog;
import com.example.demo.domain.shared.parkinglog.dtos.response.ParkingLogDetailResponse;
import com.example.demo.domain.shared.parkinglog.dtos.response.ParkingLogListResponse;
import com.example.demo.domain.shared.parkinglog.dtos.response.ParkingLogSettlementDto;
import com.example.demo.domain.shared.parkinglog.dtos.response.ParkingLogSummaryResponse;
import com.example.demo.domain.shared.parkinglog.enums.ParkingStatus;
import com.example.demo.domain.shared.parkinglog.enums.PaymentStatus;
import com.example.demo.domain.shared.parkinglog.repository.ParkingLogRepository;
import com.example.demo.domain.shared.ticketPolicy.enums.UseType;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본을 읽기전용으로 설정 - 추후 db 저장/수정 하는 메서드에만 일반 @Transactional을 붙여주면 됨
public class ParkingLogService {
    private final ParkingLogRepository parkinglogRepository;
    private final ParkingTicketRepository parkingTicketRepository;
    private final AdminParkingService adminParkingService;
    private final ParkingFeeCalculator parkingFeeCalculator;
    private final ParkingFeePolicyRepository parkingFeePolicyRepository;

    //차량번호 4자리 입력 후 차량 조회 시 조회될 차량번호 목록
    public List<ParkingLogSettlementDto> getActiveVehicleList(String vehicleNumber){
        List<ParkingLogSettlementDto> list=parkinglogRepository.getActiveVehicleList(vehicleNumber.trim(),PaymentStatus.NONE);
        if(list==null || list.isEmpty()){
            throw new BusinessException(ErrorCode.VEHICLE_NOT_ENTERED);
        }
        return list;
    }

    //관리자 입출차 기록 페이지 상단 요약정보 4가지 정보 조회(현재주차,금일출차완료,미납,금일로그)
    public ParkingLogSummaryResponse getMainSummary(){
        return parkinglogRepository.getParkingSummary();
    }

    //관리자 입출차 기록 페이지 하단 내역테이블 정보 조회 + 페이징
    public Page<ParkingLogListResponse> getParkingLogList(String keyword,String status, Pageable pageable){
        Page<ParkingLog> logPage;

        //오늘 시간 범위 설정
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime endOfToday = LocalDate.now().atTime(LocalTime.MAX);

        // 키워드 존재 여부에 따른 조회 분기처리
        if(keyword!=null && !keyword.isBlank()){
            logPage = parkinglogRepository.findByCarNumberSnapshotContaining(keyword,pageable);
        }else if (status!=null && !status.equals("ALL")){
            logPage=switch (status){
                case "CURRENT" -> parkinglogRepository.findByParkingStatusAndExitedAtIsNull(ParkingStatus.ENTERED,pageable); //현재 주차중
                case "UNPAID" -> parkinglogRepository.findByPaymentStatus(PaymentStatus.UNPAID,pageable); //미납
                case "EXITED" -> parkinglogRepository.findByExitedAtBetween(startOfToday,endOfToday, pageable); //출차완료
                case "LOG" -> parkinglogRepository.findByEntryTimeBetween(startOfToday,endOfToday,pageable); //금일 로그
                default -> parkinglogRepository.findAll(pageable);
            };
        }else {
            logPage = parkinglogRepository.findAll(pageable);
        }
        return logPage.map(ParkingLogListResponse::toListDto);
    }

    //관리자 입출차 기록 페이지 - 상세보기 정보 조회
    public ParkingLogDetailResponse getParkingLogDetail(Long parkingLogId){
        ParkingLog log = parkinglogRepository.findById(parkingLogId)
                .orElseThrow(()->new BusinessException(ErrorCode.PARKING_LOG_NOT_FOUND));
        ParkingFeePolicy policy = parkingFeePolicyRepository.findById(log.getParkingFeePolicyId())
                .orElseThrow(()->new BusinessException(ErrorCode.PARKING_POLICY_NOT_FOUND));
        //할인티켓 합산 로직
        List<ParkingTicket> tickets = parkingTicketRepository.findAllByParkingLog(log);
        int storeSum=0;
        int adminSum=0;
        //티켓 리스트 돌며 status에 따라 금액 분류 합산
        if(tickets != null && !tickets.isEmpty()){
            for (ParkingTicket ticket:tickets) {
                if(ticket.getStatus() == Status.STORE){
                    storeSum += ticket.getAppliedAmount();
                } else if (ticket.getStatus() == Status.ADMIN) {
                    adminSum += ticket.getAppliedAmount();
                }
            }
        }
        //실시간 요금 계산
        Long realTimeRawFee;
        //이미 출차완료된 차량이면 DB에 저장된 rawFee 사용, 주차중이면 실시간 계산
        if(log.getParkingStatus()==ParkingStatus.EXITED || log.getExitedAt()!=null){
            realTimeRawFee = (long)log.getRawFee();
        } else {
            //아직 주차중인 경우, 조회한 policy 객체를 계산기에 전달
            realTimeRawFee = parkingFeeCalculator.calculateRawFee(
                    log.getEnteredAt(),
                    log.getExitedAt(),
                    policy
            );
        }
        //최종 결제 예정 금액 계산 (0원 이하 방지)
        Long finalPrice = Math.max(0L, realTimeRawFee - (storeSum+adminSum));

        //DTO 변환 및 반환
        return ParkingLogDetailResponse.toDetailDto(log,storeSum,adminSum,realTimeRawFee,finalPrice);
    }
}
