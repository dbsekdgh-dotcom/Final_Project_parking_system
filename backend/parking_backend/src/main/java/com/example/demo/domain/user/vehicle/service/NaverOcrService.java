package com.example.demo.domain.user.vehicle.service;

import com.example.demo.domain.user.vehicle.dtos.response.VehicleOcrResultDto;
import com.example.demo.domain.user.vehicle.dtos.response.IdCardOcrResultDto;
import com.example.demo.domain.user.vehicle.dtos.response.NaverOcrResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverOcrService {

    @Value("${naver.clova.ocr.invoke-url}")
    private String invokeUrl;

    @Value("${naver.clova.ocr.secret-key}")
    private String secretKey;

    private final S3Service s3Service;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * [자동차 등록증 인증 메인 로직]
     */
    public VehicleOcrResultDto executeCarRegistrationOcr(MultipartFile file) {
        NaverOcrResponse response = executeOcrWithFile(file);
        return extractCarInfo(response);
    }

    /**
     * [신분증 인증 메인 로직]
     */
    public IdCardOcrResultDto executeIdCardOcr(MultipartFile file) {
        NaverOcrResponse response = executeOcrWithFile(file);
        return extractIdCardInfo(response);
    }

    /**
     * [네이버 OCR API 실제 통신]
     */
    public NaverOcrResponse callOcrApi(String imageUrl) {

        if (imageUrl == null || imageUrl.isEmpty()) {
            throw new RuntimeException("이미지 URL이 비어있습니다.");
        }

        String ext = extractExtension(imageUrl);

        List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png", "pdf", "tiff");
        if (!allowedExtensions.contains(ext)) {
            log.error("지원하지 않는 확장자 요청: {}", ext);
            throw new RuntimeException("지원하지 않는 파일 형식입니다 (" + ext + ").");
        }
        // Naver OCR API는 "jpg" 만 처리하므로 "jpeg" → "jpg" 정규화
        if (ext.equals("jpeg")) ext = "jpg";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-OCR-SECRET", secretKey);

            Map<String, Object> body = new HashMap<>();
            body.put("version", "V2");
            body.put("requestId", UUID.randomUUID().toString());
            body.put("timestamp", System.currentTimeMillis());

            Map<String, String> imageInfo = new HashMap<>();
            imageInfo.put("format", ext);
            imageInfo.put("name", "ocr_request_image");
            imageInfo.put("url", imageUrl);

            body.put("images", Collections.singletonList(imageInfo));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            log.info("--- [NaverOcrService] 네이버 API 호출 시작 (URL 방식) ---");

            return restTemplate.postForObject(invokeUrl, request, NaverOcrResponse.class);

        } catch (HttpStatusCodeException e) {
            log.error("네이버 API 서버 에러: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("네이버 OCR 서비스 오류");
        } catch (Exception e) {
            log.error("OCR 통신 중 예상치 못한 에러: {}", e.getMessage());
            throw new RuntimeException("OCR 통신 실패");
        }
    }

    /**
     * [S3 연동 및 파일 관리 로직]
     */
    public NaverOcrResponse executeOcrWithFile(MultipartFile file) {

        String uploadedUrl = "";
        String fileName = "";

        try {
            uploadedUrl = s3Service.uploadFile(file);
            fileName = uploadedUrl.substring(uploadedUrl.lastIndexOf("/") + 1);
            log.info("--- [S3 Upload Success] URL: {} ---", uploadedUrl);

            return callOcrApi(uploadedUrl);

        } finally {
            if (!fileName.isEmpty()) {
                try {
                    s3Service.deleteFile(fileName);
                    log.info("--- [S3 Temporary File Cleanup] File: {} 삭제 완료 ---", fileName);
                } catch (Exception e) {
                    log.warn("S3 임시 파일 삭제 실패 (수동 확인 필요): {}", fileName);
                }
            }
        }
    }

    /**
     * [데이터 파싱] 자동차 등록증용
     */
    private VehicleOcrResultDto extractCarInfo(NaverOcrResponse response) {
        String carNumber = "";
        String vehicleName = "";
        String name = "";
        String birth = "";

//        if (response.getImages() != null || response.getImages().isEmpty()) {
//            throw
//        }

        if (response.getImages() != null && !response.getImages().isEmpty()) {
            List<NaverOcrResponse.FieldResponse> fields = response.getImages().get(0).getFields();
            log.info("--- [OCR 차량등록증] inferResult: {}, fields: {} ---",
                    response.getImages().get(0).getInferResult(), fields);

            if (fields != null) {
                for (NaverOcrResponse.FieldResponse field : fields) {
                    String fieldName = field.getName();
                    String text = field.getInferText();

                    switch (fieldName) {
                        case "car_number": carNumber = text; break;
                        case "vehicle_name": vehicleName = text; break;
                        case "name": name = cleanName(text); break;
                        case "birth": birth = text; break;
                    }
                }
            }
        }
        return VehicleOcrResultDto.builder()
                .carNumber(carNumber)
                .vehicleName(vehicleName)
                .name(name)
                .birth(birth)
                .build();
    }

    /**
     * [데이터 파싱] 신분증용
     */
    private IdCardOcrResultDto extractIdCardInfo(NaverOcrResponse response) {
        String name = "";
        String birth = "";

        if (response.getImages() != null && !response.getImages().isEmpty()) {
            List<NaverOcrResponse.FieldResponse> fields = response.getImages().get(0).getFields();
            log.info("--- [OCR 신분증] inferResult: {}, fields: {} ---",
                    response.getImages().get(0).getInferResult(), fields);

            if (fields != null) {
                for (NaverOcrResponse.FieldResponse field : fields) {
                    String fieldName = field.getName();
                    String text = field.getInferText();

                    switch (fieldName) {
                        case "name": name = cleanName(text); break;
                        case "birth": birth = text; break;
                    }
                }
            }
        }

        return IdCardOcrResultDto.builder()
                .name(name)
                .birth(birth)
                .build();
    }

    /**
     * OCR 이름에서 괄호 및 영문 제거 후 한글 이름만 추출
     * 예: "홍길동(Hong Gil Dong)" → "홍길동"
     */
    private String cleanName(String raw) {
        if (raw == null || raw.isBlank()) return "";
        // 괄호와 그 안의 내용 제거
        String cleaned = raw.replaceAll("\\(.*?\\)", "").trim();
        // 한글, 공백만 남기기 (영문·숫자·특수문자 제거)
        cleaned = cleaned.replaceAll("[^가-힣\\s]", "").trim();
        return cleaned;
    }

    private String extractExtension(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) return "jpeg";
        // 쿼리스트링 제거 후 경로에서 파일명 추출
        String path = imageUrl.contains("?") ? imageUrl.substring(0, imageUrl.indexOf("?")) : imageUrl;
        int dotIdx = path.lastIndexOf(".");
        if (dotIdx < 0 || dotIdx == path.length() - 1) return "jpeg";
        return path.substring(dotIdx + 1).toLowerCase();
    }

    private String normalizeTo6Digits(String rawBirth) {
        if (rawBirth == null || rawBirth.isEmpty()) return "";

        String digits = rawBirth.replaceAll("[^0-9]", "");

        if (digits.length() == 8) {
            return digits.substring(2);
        }
        return digits;
    }

}
