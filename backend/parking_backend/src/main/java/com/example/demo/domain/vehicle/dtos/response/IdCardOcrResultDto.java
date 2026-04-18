package com.example.demo.domain.vehicle.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
@Schema(description = "신분증 OCR 분석 결과 응답 — 이름(name)과 생년월일(birth, 6자리 정규화)을 반환합니다.")
public class IdCardOcrResultDto {

    private final String name;
    private final String birth;
}
