package com.example.demo.domain.shared.vehicle.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Service
public class OcrService {
    /* 파일에서 켁스트 추출(OCR)
     * 현재는 샘플로 그냥 빈 문자열 반환
     *  나중에 실제 OCR 라이브러리와 연동
     */
    public String extractText(MultipartFile file){
        // TODO:  실제 OCR 라이브러리로 이미지에서 텍스트 추출
        return "";
    }
    /* 차량 등록증에서 필요한 정보 파싱
        예: 차량번호 추출
     */
    public Map<String ,String> parseVehicleReg(String ocrText){
        Map<String,String> vehicleInfo = new HashMap<>();
        
        //간다한 정규식 예: 숫자2~3자리 + 한글 1자리 + 숫자 4자리
        //실제 수현 시에는 ocrText에서 이 패턴을 찾아내는 로직이 들어갑니다.
        vehicleInfo.put("vehicleNumber","12가3546"); //임시값
        return vehicleInfo;
    }
}
//extractText
//-> MultipartFile로 들어온 신분증 / 차량등록증 이미지에서 텍스트 추출
//-> 지금은 샘플로 빈 문자열 반환, 나중에 OCR 라이브러리 연동
//parseVehicleReg
//-> OCR로 추출된 문자열에서 차량 번호, 모델 등 필요한 정보만 파싱
//-> 지금은 임시 값 "12가3456" 반환, 나중에 실제 로직 구현