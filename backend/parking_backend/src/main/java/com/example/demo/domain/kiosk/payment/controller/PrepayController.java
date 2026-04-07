package com.example.demo.domain.kiosk.payment.controller;

import com.example.demo.domain.shared.parkinglog.dtos.response.VehicleSearchResponseDto;
import com.example.demo.domain.shared.parkinglog.service.ParkingLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/prepays")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5203") // cors잠시 해제, 시큐리티 완성시 삭제
public class PrepayController {
    private final ParkingLogService parkinglogService;

    //차량번호 4자리 입력 후 차량 조회 시 조회될 차량번호 목록
    @PostMapping("/search-car")
    public List<VehicleSearchResponseDto> searchPrepayCar(@RequestBody Map<String,String> request){
        //extieAt 값이 없는 parking_log 데이터 반환
        String vehicleNumber =request.get("vehicleNumber");
        log.info("검색한 차량번호==>",vehicleNumber);
        return parkinglogService.getActiveVehicleList(vehicleNumber);
    }
}
