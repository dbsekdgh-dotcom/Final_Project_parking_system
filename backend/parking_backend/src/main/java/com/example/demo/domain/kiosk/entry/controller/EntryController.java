package com.example.demo.domain.kiosk.entry.controller;

import com.example.demo.domain.kiosk.entry.dtos.response.CameraResponse;
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

    // 입차 감지: OCR + S3 → ParkingLog(DETECTED) → parkingLogId 반환
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Long>> entry(@RequestPart("file") MultipartFile file) {
        Long parkingLogId = entryService.detectedEntry(file);
        return ResponseEntity.ok(Map.of("parkingLogId", parkingLogId));
    }

    // ENTRY 카메라 목록 조회
    @GetMapping("/cameras")
    public ResponseEntity<List<CameraResponse>> getEntryCameras() {
        return ResponseEntity.ok(entryService.getEntryCameras());
    }

    // 입구 카메라 선택 → ParkingLog(ENTERED) 확정
    @PatchMapping("/{parkingLogId}/enter")
    public ResponseEntity<Void> enterWithCamera(
            @PathVariable Long parkingLogId,
            @RequestParam Long cameraId) {
        entryService.enterWithCamera(parkingLogId, cameraId);
        return ResponseEntity.ok().build();
    }
}
