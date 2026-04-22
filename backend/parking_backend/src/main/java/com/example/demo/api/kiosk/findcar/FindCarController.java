package com.example.demo.api.kiosk.findcar;

import com.example.demo.domain.parking.log.dtos.response.FindCarResponseDto;
import com.example.demo.domain.parking.log.repository.ParkingLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/kiosk")
@RequiredArgsConstructor
public class FindCarController {

    private final ParkingLogRepository parkingLogRepository;

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
