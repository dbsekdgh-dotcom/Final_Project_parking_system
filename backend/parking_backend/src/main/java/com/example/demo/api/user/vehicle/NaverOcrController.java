package com.example.demo.api.user.vehicle;

import com.example.demo.domain.vehicle.dtos.response.VehicleOcrResultDto;
import com.example.demo.domain.vehicle.dtos.response.IdCardOcrResultDto;
import com.example.demo.domain.vehicle.service.NaverOcrService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "6. NAVER CLOVA OCR", description = "차량등록증·신분증 이미지를 S3에 임시 업로드 후 네이버 CLOVA OCR로 분석하는 API. 잘못된 서류 첨부 시 자동으로 차단됩니다.")
@Slf4j
@RestController
@RequestMapping("/api/user/ai/naver")
@RequiredArgsConstructor
public class NaverOcrController {

    private final NaverOcrService naverOcrService;

    @Operation(summary = "차량등록증 OCR 분석", description = "차량등록증 이미지를 업로드하면 차량번호·차종·소유자 이름·생년월일을 추출합니다. 신분증 등 잘못된 서류 첨부 시 400 에러를 반환합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @PostMapping("/upload-registration")
    public ResponseEntity<VehicleOcrResultDto> uploadRegistration(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return ResponseEntity.badRequest().build();

        VehicleOcrResultDto result = naverOcrService.executeCarRegistrationOcr(file);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "신분증 OCR 분석", description = "신분증 이미지를 업로드하면 이름·생년월일을 추출합니다. 차량등록증 등 잘못된 서류 첨부 시 400 에러를 반환합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @PostMapping("/upload-idcard")
    public ResponseEntity<IdCardOcrResultDto> uploadIdCard(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return ResponseEntity.badRequest().build();

        IdCardOcrResultDto result = naverOcrService.executeIdCardOcr(file);
        return ResponseEntity.ok(result);
    }

}
