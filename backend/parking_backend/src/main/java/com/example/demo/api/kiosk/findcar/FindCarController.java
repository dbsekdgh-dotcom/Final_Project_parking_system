package com.example.demo.api.kiosk.findcar;

import com.example.demo.domain.parking.log.dtos.response.FindCarResponseDto;
import com.example.demo.domain.parking.log.repository.ParkingLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "4. 내차찾기 (Find Car)", description = "현재 주차 중인 차량 위치 검색 API")
@RestController
@RequestMapping("/api/kiosk")
@RequiredArgsConstructor
public class FindCarController {

    private final ParkingLogRepository parkingLogRepository;

    @Operation(summary = "차량 위치 검색", description = "차량번호 일부(query)로 현재 주차 중인 차량의 층·구획 위치를 반환합니다.")
    @GetMapping("/find-car")
    public ResponseEntity<List<FindCarResponseDto>> findcar(@RequestParam String query){
        List<FindCarResponseDto> result = parkingLogRepository
                .findActiveWithSpaceByCarNumber(query)
                .stream()
                .map(FindCarResponseDto::from)
                .toList();
        return ResponseEntity.ok(result);
    }
}
