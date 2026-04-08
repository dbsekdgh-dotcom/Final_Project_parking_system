package com.example.demo.domain.kiosk.payment.service;

import com.example.demo.domain.kiosk.payment.dtos.request.SettlementRequestDto;
import com.example.demo.domain.kiosk.payment.dtos.response.SettlementResponseDto;
import com.example.demo.domain.shared.parkinglog.ParkingLog;
import com.example.demo.domain.shared.parkinglog.repository.ParkingLogRepository;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
public class SettlementService {
    private final ParkingLogRepository parkingLogRepository;

    //무료/유료 정산 대상 업데이트 (검증 =>userpoint&pointLog => payment => parking_log(fee ,calculated_fee,payment_status ,paid_at,free_exit_until)=>알람)
    // 1. user & parkingLog 검증(유저의 포인트가 요청된 포인트보다 많은지, 주차 로그가 결제 가능한 상태인지)/checkEligibility(결제 창 띄우기 전에)
    // 2. userPoint & PointLog update/processPointDeduction
    // 3. Payment insert/savePaymentReceipt
    // 4. parking Log 업데이트/updateParkingLogFinal
    // 5. Alram/sendSuccessAlarm

    public boolean checkEligibility(SettlementRequestDto settlementRequestDto){
        //결제 금액 검증
        ParkingLog parkingLog=parkingLogRepository.findByParkingLogId(settlementRequestDto.getParkingLogId()).orElse(null);
        if(parkingLog==null){
            throw new BusinessException(ErrorCode.VEHICLE_NOT_ENTERED);
        }
        boolean amountCheck=parkingLog.getCalculatedFee()==settlementRequestDto.getPaidAmount()+ settlementRequestDto.getUsedPoint();


        //결제 요청 시간 검증

        //유저 포인트 검증

        return  false;
    }
}
