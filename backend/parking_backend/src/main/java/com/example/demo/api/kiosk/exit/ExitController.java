package com.example.demo.api.kiosk.exit;

import com.example.demo.domain.parking.exit.dtos.response.ExitPaymentResponseDto;
import com.example.demo.domain.parking.exit.service.ExitService;
import com.example.demo.domain.payment.dtos.response.VehiclePaymentResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Tag(name = "3. 출차 (Exit)", description = "출차 요청, 출차 확정, 출차 중 회차 API")
@Slf4j
@RestController
@RequestMapping("/api/exit")
@RequiredArgsConstructor
public class ExitController {

    private final ExitService exitService;

    @Operation(summary = "출차 요청", description = "parkingLogId·exitCameraId를 받아 출차 요금을 계산하고 EXIT_REQUESTED 상태로 변경합니다. 포인트 잔액도 함께 반환합니다.")
    @PostMapping("/request")
    public ExitPaymentResponseDto requestExit(
            @RequestParam Long parkingLogId,
            @RequestParam Long exitCameraId,
            @RequestParam(required = false,defaultValue = "") String imagePath
    ){
        log.info("출차 요청 - parkingLogId: {}, exitCameraId: {}",parkingLogId,exitCameraId);
       VehiclePaymentResponseDto result=exitService.requestExit(parkingLogId,exitCameraId,imagePath);
       int userPoint= exitService.getUserPoint(parkingLogId);
       return ExitPaymentResponseDto.builder().
               parkingLogId(result.getParkingLogId()).
               isFree(result.isFree()).
               message(result.getMessage()).
               vehicleNumber(result.getVehicleNumber()).
               parkingTime(result.getParkingTime()).
               rawFee(result.getRawFee()).
               calculatedFee(result.getCalculatedFee()).
               amountToPay(result.getAmountToPay()).
               userPoint(userPoint).
               build();
    }
    @Operation(summary = "출차 확정 (EXIT_REQUESTED → EXITED)", description = "결제 완료 후 출차를 최종 확정합니다. 주차 공간을 반납하고 상태를 EXITED로 변경합니다.")
    // 출차 확정 EXIT_REQUESTED -> EXITED
    @PostMapping("/confirm")
    public void confirmExit(@RequestParam Long parkingLogId){
        log.info("출차 확정 - parkingLogId: {}", parkingLogId);
        exitService.confirmExit(parkingLogId);
    }
    @Operation(summary = "출차 취소 (회차)", description = "출차 요청 중 회차 버튼을 눌렀을 때 EXIT_REQUESTED 상태를 ENTERED로 되돌립니다.")
    // 출차 중 회차 EXITED_REQUESTED -> ENTERED 복귀
    @PatchMapping("/cancel")
    public void cancelExit(@RequestParam Long parkingLogId){
        log.info("회차 - parkingLogId: {}",parkingLogId);
        exitService.cancelExit(parkingLogId);
    }
}
