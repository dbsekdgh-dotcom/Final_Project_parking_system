package com.example.demo.domain.kiosk.payment.controller;

import com.example.demo.domain.kiosk.payment.dtos.request.PaymentConfirmRequestDto;
import com.example.demo.domain.kiosk.payment.dtos.request.SettlementRequestDto;
import com.example.demo.domain.kiosk.payment.dtos.response.PaymentReadyResponseDto;
import com.example.demo.domain.kiosk.payment.dtos.response.SettlementResponseDto;
import com.example.demo.domain.kiosk.payment.dtos.response.VehiclePaymentResponseDto;
import com.example.demo.domain.kiosk.payment.facade.PaymentFacade;
import com.example.demo.domain.shared.activityLog.enums.ActivityType;
import com.example.demo.domain.shared.parkinglog.dtos.response.ParkingLogSettlementDto;
import com.example.demo.domain.shared.parkinglog.service.ParkingLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final ParkingLogService parkinglogService;
    private final PaymentFacade paymentFacade;


    //차량번호 4자리 입력 후 차량 조회 시 조회될 차량번호 목록
    @PostMapping("/search-car")
    public List<ParkingLogSettlementDto> searchPrepayCar(@RequestBody Map<String,String> request){
        //extieAt 값이 없는 parking_log 데이터 반환
        String vehicleNumber =request.get("vehicleNumber");
        log.info("검색한 차량번호==>{}",vehicleNumber);
        return parkinglogService.getActiveVehicleList(vehicleNumber);
    }

    //결제 정보 조회
    @PostMapping("/request-payment")
    public VehiclePaymentResponseDto requestPayment(@RequestBody ParkingLogSettlementDto parkingLogSettlementDto){
        VehiclePaymentResponseDto dto=paymentFacade.paymentProcess(parkingLogSettlementDto.getParkingLogId());
        log.info("조회된 차량 결제정보==>{}",dto);
        return dto;
    }

    //결제 전 사전 확인
    @PostMapping("/request-ready-payment")
    public PaymentReadyResponseDto requestReadyPayment(@RequestBody SettlementRequestDto settlementRequestDto){
        log.info("받은 정보 ==-==>{}",settlementRequestDto);
        PaymentReadyResponseDto paymentReadyResponseDto= paymentFacade.beforePayment(settlementRequestDto);
        log.info("결제 전 사전확인 요청 응답 ==>{}",paymentReadyResponseDto);
        return paymentReadyResponseDto;
    }

    //결제 후
    @PostMapping("/request-after-payment")
    public SettlementResponseDto requestAfterPayment(@RequestBody PaymentConfirmRequestDto paymentConfirmRequestDto){
        SettlementResponseDto dto= paymentFacade.afterPayment(paymentConfirmRequestDto, ActivityType.PAYMENT_PRE);
        log.info("결제 완료 후 응답 데이터==>{}",dto);
        return dto;
    }
}
