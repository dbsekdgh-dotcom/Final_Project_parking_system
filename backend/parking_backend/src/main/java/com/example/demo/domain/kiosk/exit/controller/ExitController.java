package com.example.demo.domain.kiosk.exit.controller;

import com.example.demo.domain.kiosk.exit.service.ExitService;
import com.example.demo.domain.kiosk.payment.dtos.response.VehiclePaymentResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/exit")
@RequiredArgsConstructor
public class ExitController {

    private final ExitService exitService;

    @PostMapping("/request")
    public VehiclePaymentResponseDto requestExit(
            @RequestParam Long parkingLogId,
            @RequestParam Long exitCameraId,
            @RequestParam(required = false,defaultValue = "") String imagePath
    ){
        log.info("출차 요청 - parkingLogId: {}, exitCameraId: {}",parkingLogId,exitCameraId);
        return exitService.requestExit(parkingLogId,exitCameraId,imagePath);
    }
    // 출차 확정 EXIT_REQUESTED -> EXITED
    @PostMapping("/confirm")
    public void confirmExit(@RequestParam Long parkingLogId){
        log.info("출차 확정 - parkingLogId: {}", parkingLogId);
        exitService.confirmExit(parkingLogId);
    }
    // 출차 중 회차 EXITED_REQUESTED -> ENTERED 복귀
    @PatchMapping("/cancel")
    public void cancelExit(@RequestParam Long parkingLogId){
        log.info("회차 - parkingLogId: {}",parkingLogId);
        exitService.cancelExit(parkingLogId);
    }
}
