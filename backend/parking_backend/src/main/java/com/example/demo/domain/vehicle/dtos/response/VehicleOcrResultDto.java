package com.example.demo.domain.vehicle.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
@Schema(description = "차량등록증 OCR 분석 결과 응답 — 표시용 파싱값(carNumber, vehicleName, name, birth)과 자동승인 비교용 OCR 원본값(ocrRawName, orcRawBirth)을 함께 반환합니다.")
public class VehicleOcrResultDto {

    private final String carNumber;
    private final String vehicleName;
    private final String name;
    private final String birth;

    private final String ocrRawName;
    private final String orcRawBirth;
}
