package com.example.demo.api.kiosk.entry;

import com.example.demo.domain.parking.entry.dtos.response.CameraResponse;
import com.example.demo.domain.parking.entry.dtos.response.ParkingLogTypeResponse;
import com.example.demo.domain.parking.entry.dtos.response.ParkingSpaceResponse;
import com.example.demo.domain.parking.entry.service.EntryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Tag(name = "2. 입차 (Entry)", description = "차량 감지, 입차 확정, 자리 선택, 카메라 목록 조회 API")
@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/entry")
public class EntryController {
    private final EntryService entryService;

    @Operation(summary = "차량 감지 등록 (DETECTED)", description = "카메라가 차량번호를 인식하면 plateNumber·s3path·cameraId를 받아 주차 로그를 DETECTED 상태로 생성합니다.")
    // 차량번호, s3path camera id를 받아 DETECTED ( insert )
    @PostMapping
    public ResponseEntity<Map<String, Long>> entry(
            @RequestParam String plateNumber,
            @RequestParam String s3path,
            @RequestParam Long cameraId) {
        Long parkingLogId = entryService.detectedEntry(plateNumber,s3path, cameraId);
        return ResponseEntity.ok(Map.of("parkingLogId", parkingLogId));
    }

    @Operation(summary = "입차 여부 확인", description = "차량번호로 현재 ENTERED 상태인지 조회합니다. 키오스크 입차/출차 버튼 분기에 사용됩니다.")
    // 차번호로 현재 ENTERED 상태인지 조회 (입차/출차 버튼 분기용)
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkEntered(@RequestParam String carNumber) {
        return ResponseEntity.ok(entryService.checkEntered(carNumber));
    }

    @Operation(summary = "입차 카메라 목록 조회", description = "등록된 ENTRY 카메라 목록을 반환합니다.")
    // ENTRY 카메라 목록 조회
    @GetMapping("/cameras")
    public ResponseEntity<List<CameraResponse>> getEntryCameras() {
        return ResponseEntity.ok(entryService.getEntryCameras());
    }

    @Operation(summary = "출차 카메라 목록 조회", description = "등록된 EXIT 카메라 목록을 반환합니다.")
    // EXIT 카메라 목록 조회
    @GetMapping("/cameras/exit")
    public ResponseEntity<List<CameraResponse>> getExitCameras() {
        return ResponseEntity.ok(entryService.getExitCameras());
    }

    @Operation(summary = "입차 확정 (DETECTED → ENTERED)", description = "자리를 선택하고 입차를 확정합니다. DETECTED 상태를 ENTERED로 변경하고 주차 공간을 배정합니다.")
    // 자리 선택 + 입차 확정: DETECTED → ENTERED, 자리 배정 (하나의 트랜잭션)
    @PatchMapping("/{parkingLogId}/enter")
    public ResponseEntity<Void> enterWithCamera(
            @PathVariable Long parkingLogId,
            @RequestParam Long spaceId) {
        entryService.enterWithCamera(parkingLogId, spaceId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "주차 타입 조회", description = "parkingLog의 parkingTypeSnapshot을 반환합니다. 어떤 층을 배정할지 결정하는 데 사용됩니다.")
    // parkingLog의 parkingTypeSnapshot 조회 (층 결정용)
    @GetMapping("/{parkingLogId}/type")
    public ResponseEntity<ParkingLogTypeResponse> getParkingType(@PathVariable Long parkingLogId) {
        return ResponseEntity.ok(entryService.getParkingType(parkingLogId));
    }

    @Operation(summary = "층별 주차 공간 목록 조회", description = "floor 파라미터로 해당 층의 주차 공간 목록과 사용 가능 여부를 반환합니다.")
    // 층별 주차 공간 목록 조회
    @GetMapping("/space")
    public ResponseEntity<List<ParkingSpaceResponse>> getSpaces(@RequestParam String floor) {
        return ResponseEntity.ok(entryService.getSpacesByFloor(floor));
    }

    @Operation(summary = "입차 취소 (회차)", description = "DETECTED 상태의 주차 로그를 ENTRY_CANCELLED로 변경합니다. 키오스크에서 회차 버튼을 눌렀을 때 호출됩니다.")
    // DETECTED → ENTRY_CANCELLED (회차 버튼)
    @PatchMapping("/{parkingLogId}/cancel")
    public ResponseEntity<Void> cancelEntry(@PathVariable Long parkingLogId) {
        entryService.cancelEntry(parkingLogId);
        return ResponseEntity.ok().build();
    }
}
