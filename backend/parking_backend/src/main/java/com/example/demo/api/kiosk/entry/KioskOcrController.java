package com.example.demo.api.kiosk.entry;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Tag(name = "6. 키오스크 OCR (Kiosk OCR)", description = "번호판 이미지를 AI 서버로 전달해 차량 번호를 인식하는 OCR API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kiosk/ocr")
public class KioskOcrController {

    private final RestTemplate restTemplate;

    @Value("${AI_SERVER_URL}")
    private String aiServerUrl;

    @Operation(summary = "번호판 OCR 인식", description = "번호판 이미지를 AI 서버로 전송해 차량 번호를 추출합니다. 입차 시 키오스크에서 사용됩니다.")
    @PostMapping("/plate")
    public ResponseEntity<Map> plateOcr(@RequestParam("file") MultipartFile file) throws Exception {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() { return file.getOriginalFilename(); }
        };
        body.add("file", resource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
        return restTemplate.postForEntity(aiServerUrl + "/api/v1/parking/entryexit", request, Map.class);
    }
}
