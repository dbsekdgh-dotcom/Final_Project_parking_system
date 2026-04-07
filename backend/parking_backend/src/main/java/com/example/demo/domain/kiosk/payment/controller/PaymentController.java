package com.example.demo.domain.kiosk.payment.controller;

import com.example.demo.domain.kiosk.payment.dtos.response.VehiclePaymentResponseDto;
import com.example.demo.domain.kiosk.payment.facade.PaymentFacade;
import com.example.demo.domain.kiosk.payment.service.AiServerClient;
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
    private final AiServerClient aiServerClient;

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
        //락 걸기
        //aiServerClient.checkPaymentLock(parkingLogSettlementDto.getVehicleNumber());
        VehiclePaymentResponseDto dto=paymentFacade.paymentProcess(parkingLogSettlementDto.getParkingLogId());
        log.info("조회된 차량 결제정보==>{}",dto);
        return dto;
    }
}
