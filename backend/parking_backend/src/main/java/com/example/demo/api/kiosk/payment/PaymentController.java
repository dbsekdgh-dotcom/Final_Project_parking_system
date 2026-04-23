package com.example.demo.api.kiosk.payment;

import com.example.demo.domain.payment.dtos.request.PaymentConfirmRequestDto;
import com.example.demo.domain.payment.dtos.request.SettlementRequestDto;
import com.example.demo.domain.payment.dtos.response.PaymentReadyResponseDto;
import com.example.demo.domain.payment.dtos.response.SettlementResponseDto;
import com.example.demo.domain.payment.dtos.response.VehiclePaymentResponseDto;
import com.example.demo.domain.payment.service.facade.PaymentFacade;
import com.example.demo.domain.system.activitylog.enums.ActivityType;
import com.example.demo.domain.parking.log.dtos.response.ParkingLogSettlementDto;
import com.example.demo.domain.parking.log.service.ParkingLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Tag(name = "01. 결제 (Payment)", description = "키오스크 사전 정산 관련 API — 차량 조회·요금 계산·결제 처리")
public class PaymentController {
    private final ParkingLogService parkinglogService;
    private final PaymentFacade paymentFacade;


    //차량번호 4자리 입력 후 차량 조회 시 조회될 차량번호 목록
    @Operation(summary = "차량 번호 검색",
        description = "뒤 4자리 입력 시 현재 주차 중인(exitAt 없는) 차량 목록을 반환합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "차량 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = ParkingLogSettlementDto.class))),
        @ApiResponse(responseCode = "400", description = "vehicleNumber 누락", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
    })
    @RequestBody(description = "검색할 차량 번호 (뒤 4자리 또는 전체)", required = true,
        content = @Content(schema = @Schema(example = "{\"vehicleNumber\": \"3456\"}")))
    @PostMapping("/search-car")
    public List<ParkingLogSettlementDto> searchPrepayCar(@RequestBody Map<String,String> request){
        //extieAt 값이 없는 parking_log 데이터 반환
        String vehicleNumber =request.get("vehicleNumber");
        log.info("검색한 차량번호==>{}",vehicleNumber);
        return parkinglogService.getActiveVehicleList(vehicleNumber);
    }

    //결제 정보 조회
    @Operation(summary = "결제 정보 조회",
        description = "주차 로그 ID 기준으로 할인 적용 후 요금, 포인트 등 결제 정보를 반환합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "결제 정보 조회 성공",
            content = @Content(schema = @Schema(implementation = VehiclePaymentResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "요청 데이터 형식 오류", content = @Content),
        @ApiResponse(responseCode = "404", description = "주차 로그를 찾을 수 없음", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
    })
    @PostMapping("/request-payment")
    public VehiclePaymentResponseDto requestPayment(@RequestBody ParkingLogSettlementDto parkingLogSettlementDto){
        VehiclePaymentResponseDto dto=paymentFacade.paymentProcess(parkingLogSettlementDto.getParkingLogId());
        log.info("조회된 차량 결제정보==>{}",dto);
        return dto;
    }

    //결제 전 사전 확인
    @Operation(summary = "결제 사전 확인 (결제 준비)",
        description = "포인트·할인권 적용 후 최종 결제 금액을 확인하고 orderId를 발급합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "결제 준비 성공 — orderId 및 최종 금액 반환",
            content = @Content(schema = @Schema(implementation = PaymentReadyResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "요청 데이터 형식 오류", content = @Content),
        @ApiResponse(responseCode = "404", description = "주차 로그 또는 사용자를 찾을 수 없음", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
    })
    @PostMapping("/request-ready-payment")
    public PaymentReadyResponseDto requestReadyPayment(@RequestBody SettlementRequestDto settlementRequestDto){
        log.info("받은 정보 ==-==>{}",settlementRequestDto);
        PaymentReadyResponseDto paymentReadyResponseDto= paymentFacade.beforePayment(settlementRequestDto);
        return paymentReadyResponseDto;
    }

    //결제 후
    @Operation(summary = "결제 확정 처리",
        description = "토스페이먼츠 paymentKey·orderId·amount를 검증하고 결제를 최종 확정합니다. 성공 시 출차 가능 시각(exitDeadline)을 반환합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "결제 확정 성공 — 출차 가능 시각 반환",
            content = @Content(schema = @Schema(implementation = SettlementResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "결제 금액 불일치 또는 요청 데이터 오류", content = @Content),
        @ApiResponse(responseCode = "404", description = "주차 로그를 찾을 수 없음", content = @Content),
        @ApiResponse(responseCode = "409", description = "이미 결제 완료된 주차 건", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류 또는 토스 결제 승인 실패", content = @Content)
    })
    @PostMapping("/request-after-payment")
    public SettlementResponseDto requestAfterPayment(@RequestBody PaymentConfirmRequestDto paymentConfirmRequestDto){
        SettlementResponseDto dto= paymentFacade.afterPayment(paymentConfirmRequestDto, ActivityType.PAYMENT_PRE);
        log.info("결제 완료 후 응답 데이터==>{}",dto);
        return dto;
    }
}
