package com.example.demo.domain.shared.parkinglog.service;

import com.example.demo.domain.admin.management.parking.service.AdminParkingService;
import com.example.demo.domain.shared.parkingTicket.ParkingTicket;
import com.example.demo.domain.shared.parkingTicket.repository.ParkingTicketRepository;
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
        //해당 로그에 쌓인 티켓들 전부 조회
        List<ParkingTicket> tickets = parkingTicketRepository.findAllByParkingLog(log);

        int storeSum=0;
        int adminSum=0;

        for (ParkingTicket ticket : tickets){
            int amount = adminParkingService.calculatedDiscountByPolicy(ticket.getTicketPolicy(),log);
            if(ticket.getTicketPolicy().getUseType() == UseType.STORE){
                storeSum +=amount;
            } else if (ticket.getTicketPolicy().getUseType()==UseType.ADMIN) {
                adminSum +=amount;
            }
        }
        //BusinessException : 내가 의도적으로 낸 에러 / RuntimeException : 시스템이 낸 에러
        return ParkingLogDetailResponse.toDetailDto(log,storeSum,adminSum); // 찾은 엔티티를 응답용 DTO로 반환
    }
}
