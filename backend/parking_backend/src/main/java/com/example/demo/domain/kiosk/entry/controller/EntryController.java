package com.example.demo.domain.kiosk.entry.controller;

import com.example.demo.domain.kiosk.entry.dtos.response.CameraResponse;
import com.example.demo.domain.kiosk.entry.dtos.response.ParkingLogTypeResponse;
import com.example.demo.domain.kiosk.entry.dtos.response.ParkingSpaceResponse;
import com.example.demo.domain.kiosk.entry.service.EntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/entry")
public class EntryController {
    private final EntryService entryService;

    // 입차 감지: OCR + S3 → 블랙리스트면 BLACKLIST_REJECTED 저장 후 403, 정상이면 DETECTED 저장 → parkingLogId 반환
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Long>> entry(
            @RequestPart("file") MultipartFile file,
            @RequestParam Long cameraId) {
        Long parkingLogId = entryService.detectedEntry(file, cameraId);
        return ResponseEntity.ok(Map.of("parkingLogId", parkingLogId));
    }

    // 차번호로 현재 ENTERED 상태인지 조회 (입차/출차 버튼 분기용)
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkEntered(@RequestParam String carNumber) {
        return ResponseEntity.ok(entryService.checkEntered(carNumber));
    }

    // ENTRY 카메라 목록 조회
    @GetMapping("/cameras")
    public ResponseEntity<List<CameraResponse>> getEntryCameras() {
        return ResponseEntity.ok(entryService.getEntryCameras());
    }

    // EXIT 카메라 목록 조회
    @GetMapping("/cameras/exit")
    public ResponseEntity<List<CameraResponse>> getExitCameras() {
        return ResponseEntity.ok(entryService.getExitCameras());
    }

    // 자리 선택 + 입차 확정: DETECTED → ENTERED, 자리 배정 (하나의 트랜잭션)
    @PatchMapping("/{parkingLogId}/enter")
    public ResponseEntity<Void> enterWithCamera(
            @PathVariable Long parkingLogId,
            @RequestParam Long spaceId) {
        entryService.enterWithCamera(parkingLogId, spaceId);
        return ResponseEntity.ok().build();
    }

    // parkingLog의 parkingTypeSnapshot 조회 (층 결정용)
    @GetMapping("/{parkingLogId}/type")
    public ResponseEntity<ParkingLogTypeResponse> getParkingType(@PathVariable Long parkingLogId) {
        return ResponseEntity.ok(entryService.getParkingType(parkingLogId));
    }

    // 층별 주차 공간 목록 조회
    @GetMapping("/space")
    public ResponseEntity<List<ParkingSpaceResponse>> getSpaces(@RequestParam String floor) {
        return ResponseEntity.ok(entryService.getSpacesByFloor(floor));
    }

    // DETECTED → ENTRY_CANCELLED (회차 버튼)
    @PatchMapping("/{parkingLogId}/cancel")
    public ResponseEntity<Void> cancelEntry(@PathVariable Long parkingLogId) {
        entryService.cancelEntry(parkingLogId);
        return ResponseEntity.ok().build();
    }
}
