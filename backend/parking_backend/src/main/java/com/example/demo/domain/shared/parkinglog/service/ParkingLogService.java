package com.example.demo.domain.shared.parkinglog.service;

import com.example.demo.domain.admin.management.parking.service.AdminParkingService;
import com.example.demo.domain.kiosk.payment.dtos.response.FeeCalculationResponseDto;
import com.example.demo.domain.kiosk.payment.service.PaymentService;
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
import com.example.demo.domain.shared.parkinglog.enums.ParkingTypeSnapshot;
import com.example.demo.domain.shared.parkinglog.enums.PaymentStatus;
import com.example.demo.domain.shared.parkinglog.repository.ParkingLogRepository;
import com.example.demo.domain.shared.reservation.repository.ReservationRepository;
import com.example.demo.domain.shared.ticketPolicy.enums.UseType;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
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
    private final PaymentService paymentService;
    private final ReservationRepository reservationRepository;

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

        LocalDateTime now = LocalDateTime.now();

        //과금 시작 시점 결정(방문 예약 차량 처리)
        LocalDateTime calculationStartTime = log.getEnteredAt();
        if(log.getParkingTypeSnapshot().equals(ParkingTypeSnapshot.RESERVATION)){
            //예약 차량이고, ENTERED상태가 더이상 유효하지 않으면 입차시간이 아닌 '무료 만료 시간'부터 과금 시작
            if(reservationRepository.getCountbyCarNumber(log.getCarNumberSnapshot(), com.example.demo.domain.shared.reservation.enums.Status.ENTERED,true)<=0){
                calculationStartTime = log.getFreeExitUntil();
            }
        }

        //실시간 요금 및 최종 요금 계산
        Long realTimeRawFee;
        Long finalPrice;

        //A.이미 출차 완료된 차량: DB 스냅샷 사용
        if (log.getParkingStatus() == ParkingStatus.EXITED || log.getExitedAt() != null) {
            realTimeRawFee = (long) log.getRawFee();
            finalPrice = (long) log.getFee();
        }
        //B.주차 중인 차량: 실시간 계산 메서드 사용
        else {
            //보정된 시작 시간부터 현재까지의 주차 분(minutes)계산
            long totalDurationForCalculation = Duration.between(calculationStartTime,now).toMinutes();
            //요금계산 메서드(할인권 조회 및 log.getFee() 차감 처리됨)
            FeeCalculationResponseDto calculation = paymentService.settlementFee(
                    log,
                    log.getParkingFeePolicyId(),
                    totalDurationForCalculation
            );
            realTimeRawFee = (long) calculation.getRawFee(); // 할인 전 원금(또는 시간할인만 적용된 원금)
            finalPrice = calculation.getAmountToPay(); //추가결제 해야 할 최종 금액
        }

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

        //DTO 변환 및 반환
        return ParkingLogDetailResponse.toDetailDto(log,storeSum,adminSum,realTimeRawFee,finalPrice);
    }
}
