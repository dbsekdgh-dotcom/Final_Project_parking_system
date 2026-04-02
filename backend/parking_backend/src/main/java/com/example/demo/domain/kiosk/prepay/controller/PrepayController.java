package com.example.demo.domain.kiosk.prepay.controller;

import com.example.demo.domain.shared.parkinglog.dtos.response.VehicleSearchResponseDto;
import com.example.demo.domain.shared.parkinglog.service.ParkinglogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/prepays")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173") // cors잠시 해제, 시큐리티 완성시 삭제
public class PrepayController {
    private final ParkinglogService parkinglogService;

    //차량번호 4자리 입력 후 차량 조회 시 조회될 차량번호 목록
    @PostMapping("/search-car")
    public List<VehicleSearchResponseDto> searchPrepayCar(@RequestBody Map<String,String> request){
        String vehicleNumber =request.get("vehicleNumber");
        System.out.println(vehicleNumber);
        return parkinglogService.getActiveVehicleList(vehicleNumber);
    }
}
