package com.example.demo.domain.user.vehicle.service;

import com.example.demo.domain.user.vehicle.dtos.response.VehicleOcrResultDto;
import com.example.demo.domain.user.vehicle.dtos.response.IdCardOcrResultDto;
import com.example.demo.domain.user.vehicle.dtos.response.NaverOcrResponse;
import com.example.demo.global.exception.CustomException;
import com.example.demo.global.exception.ErrorCode;
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
     * [NAVER CLOVA OCR] 자동차 등록증 인증 메인 로직
     * 이미지 분석 후 사용자 수정용 데이터와 보안 검증용 원본 데이터를 함께 반환합니다.
     */
    public VehicleOcrResultDto executeCarRegistrationOcr(MultipartFile file) {
        NaverOcrResponse response = executeOcrWithFile(file);
        return extractCarInfo(response);
    }

    /**
     * [NAVER CLOVA OCR] 신분증 인증 메인 로직
     * 이미지 분석 후 성명과 생년월일을 추출하여 반환합니다.
     */
    public IdCardOcrResultDto executeIdCardOcr(MultipartFile file) {
        NaverOcrResponse response = executeOcrWithFile(file);
        return extractIdCardInfo(response);
    }

    /**
     * [NAVER CLOVA OCR] API 실제 통신 로직
     * S3 URL을 통해 네이버 클로바 OCR 서버와 통신합니다.
     */
    public NaverOcrResponse callOcrApi(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        String ext = extractExtension(imageUrl);
        List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png", "pdf", "tiff");
        if (!allowedExtensions.contains(ext)) {
            log.error("지원하지 않는 확장자: {}", ext);
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        // 네이버 API 규격에 맞게 jpeg를 jpg로 정규화
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
            log.info("--- [NaverOcrService] CLOVA OCR API 요청 시작 ---");

            return restTemplate.postForObject(invokeUrl, request, NaverOcrResponse.class);

        } catch (HttpStatusCodeException e) {
            log.error("네이버 API 서버 에러: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new CustomException(ErrorCode.AI_SERVER_ERROR);
        } catch (Exception e) {
            log.error("OCR 통신 중 예상치 못한 에러: {}", e.getMessage());
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * [S3 연동 및 임시 파일 관리]
     * 파일을 S3에 업로드하고 OCR 분석이 끝나면 즉시 삭제합니다.
     */
    public NaverOcrResponse executeOcrWithFile(MultipartFile file) {
        String uploadedUrl = "";
        String fileName = "";

        try {
            uploadedUrl = s3Service.uploadFile(file);
            fileName = uploadedUrl.substring(uploadedUrl.lastIndexOf("/") + 1);
            log.info("--- [S3 Uploaded] 분석 시작: {} ---", uploadedUrl);
            return callOcrApi(uploadedUrl);
        } finally {
            if (!fileName.isEmpty()) {
                try {
                    s3Service.deleteFile(fileName);
                    log.info("--- [S3 Cleanup] 임시 파일 삭제 완료 ---");
                } catch (Exception e) {
                    log.warn("S3 임시 파일 삭제 실패: {}", fileName);
                }
            }
        }
    }

    /**
     * [Data Parsing] 자동차 등록증 정보 추출 + 서류 종류 검증
     *
     * ※ 서류 교차 업로드 방지 원리:
     *   네이버 OCR이 이미지를 분석하면 해당 서류에 맞는 필드명을 응답에 포함시킵니다.
     *   차량등록증에는 반드시 "car_number" 필드가 존재합니다.
     *   따라서 파싱 후 carNumber가 비어있다면 → 신분증 등 다른 서류가 올라온 것으로 판단합니다.
     *   이 검증은 백엔드 OCR 파싱 단계에서 이루어지므로, 프론트 조작과 무관하게 항상 동작합니다.
     */
    private VehicleOcrResultDto extractCarInfo(NaverOcrResponse response) {
        String carNumber = "";
        String vehicleName = "";
        String name = "";
        String birth = "";

        // 1. 응답 유효성 검사 (NPE 방지)
        if (response == null || response.getImages() == null || response.getImages().isEmpty()) {
            throw new CustomException(ErrorCode.OCR_DATA_MISSING);
        }

        List<NaverOcrResponse.FieldResponse> fields = response.getImages().get(0).getFields();

        // 2. 텍스트 인식 여부 검사 (화질 저하 대응)
        if (fields == null || fields.isEmpty()) {
            log.warn("--- [OCR Fail] 이미지에서 텍스트를 인식할 수 없음 ---");
            throw new CustomException(ErrorCode.OCR_NO_TEXT_DETECTED);
        }

        // 3. 필드 데이터 매핑
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

        // 4. 서류 종류 검증: 차량등록증 슬롯에 신분증 등 다른 서류가 올라왔는지 확인
        //    차량등록증이라면 OCR 응답에 반드시 "car_number" 필드가 있어야 합니다.
        //    carNumber가 비어있다는 것 = 이 서류는 차량등록증이 아님 → 즉시 차단
        if (carNumber.isEmpty()) {
            log.warn("--- [서류 오류] 차량등록증 슬롯에 잘못된 서류 업로드 감지 (car_number 필드 없음) ---");
            throw new CustomException(ErrorCode.WRONG_DOCUMENT_TYPE);
        }

        // 5. DTO 생성: 인풋창용 데이터와 보안 검증용(ocrRaw) 데이터를 동일하게 초기화하여 반환
        return VehicleOcrResultDto.builder()
                .carNumber(carNumber)
                .vehicleName(vehicleName)
                .name(name)             // 사용자가 인풋창에서 수정할 값
                .birth(birth)
                .ocrRawName(name)       // ⭐ 절대 수정되지 않는 AI 원본 (비교용)
                .orcRawBirth(birth)     // ⭐ 절대 수정되지 않는 AI 원본 (비교용)
                .build();
    }

    /**
     * [Data Parsing] 신분증 정보 추출 + 서류 종류 검증
     *
     * ※ 서류 교차 업로드 방지 원리:
     *   차량등록증에는 "car_number" 필드가 포함되어 있습니다.
     *   신분증 슬롯에 차량등록증을 올리면 OCR 응답에 "car_number" 필드가 나타납니다.
     *   이를 감지해 즉시 예외를 던집니다.
     *   마찬가지로 프론트 조작과 무관하게 서버 파싱 단계에서 차단됩니다.
     */
    private IdCardOcrResultDto extractIdCardInfo(NaverOcrResponse response) {
        String name = "";
        String birth = "";

        if (response == null || response.getImages() == null || response.getImages().isEmpty()) {
            throw new CustomException(ErrorCode.OCR_DATA_MISSING);
        }

        List<NaverOcrResponse.FieldResponse> fields = response.getImages().get(0).getFields();

        if (fields == null || fields.isEmpty()) {
            throw new CustomException(ErrorCode.OCR_NO_TEXT_DETECTED);
        }

        for (NaverOcrResponse.FieldResponse field : fields) {
            String fieldName = field.getName();
            String text = field.getInferText();

            // 서류 종류 검증: 신분증 슬롯에 차량등록증이 올라왔는지 확인
            // "car_number" 필드는 차량등록증에서만 나타나는 고유 필드입니다.
            // 신분증 파싱 중에 이 필드가 발견되면 → 잘못된 서류 → 즉시 차단
            if ("car_number".equals(fieldName)) {
                log.warn("--- [서류 오류] 신분증 슬롯에 차량등록증 업로드 감지 (car_number 필드 발견) ---");
                throw new CustomException(ErrorCode.WRONG_DOCUMENT_TYPE);
            }

            switch (fieldName) {
                case "name": name = cleanName(text); break;
                case "birth": birth = text; break;
            }
        }

        return IdCardOcrResultDto.builder()
                .name(name)
                .birth(birth)
                .build();
    }

    /**
     * [Data Cleansing] 이름 정제 로직
     * 괄호와 영문을 제거하고 순수 한글 이름만 추출합니다.
     */
    private String cleanName(String raw) {
        if (raw == null || raw.isBlank()) return "";
        String cleaned = raw.replaceAll("\\(.*?\\)", "").trim();
        cleaned = cleaned.replaceAll("[^가-힣\\s]", "").trim();
        return cleaned;
    }

    /**
     * 이미지 URL에서 확장자 추출
     */
    private String extractExtension(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) return "jpeg";
        String path = imageUrl.contains("?") ? imageUrl.substring(0, imageUrl.indexOf("?")) : imageUrl;
        int dotIdx = path.lastIndexOf(".");
        if (dotIdx < 0 || dotIdx == path.length() - 1) return "jpeg";
        return path.substring(dotIdx + 1).toLowerCase();
    }

    /**
     * 생년월일 6자리 정규화
     */
    private String normalizeTo6Digits(String rawBirth) {
        if (rawBirth == null || rawBirth.isEmpty()) return "";
        String digits = rawBirth.replaceAll("[^0-9]", "");
        if (digits.length() == 8) {
            return digits.substring(2);
        }
        return digits;
    }
}