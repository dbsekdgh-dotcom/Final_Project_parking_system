package com.example.demo.domain.vehicle.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "차량 등록 신청 요청 데이터 — 사용자가 최종 확인한 차량 정보와 OCR 원본 데이터를 함께 전달합니다. " +
        "4가지 수정 여부 + 이름 유사도 + 명의 유사도 조건을 모두 충족하면 자동 승인(ACTIVE), 하나라도 미달 시 관리자 검토 대기(PENDING)로 처리됩니다.")
public class VehicleRegistrationRequestDto {

    @NotBlank(message = "차량 번호는 필수 입력 값입니다.")
    @Size(max = 25, message = "차량 번호는 25자를 초과할 수 없습니다.")
    private String carNumber; // 사용자가 최종 확인/수정한 차번호

    @NotBlank(message = "차종 정보는 필수입니다.")
    @Size(max = 100, message = "차종 명칭은 100자를 초과할 수 없습니다.")
    private String vehicleName; // 사용자가 최종 확인/수정한 차종

    @NotBlank(message = "이름을 확인해주세요.")
    private String name; // 사용자가 최종 확인/수정한 이름

    @NotBlank(message = "생년월일을 확인해주세요.")
    @Pattern(regexp = "\\d{6}", message = "생년월일은 6자리 숫자여야 합니다.")
    private String birth; // 사용자가 최종 확인/수정한 생년월일

    // --- 자동차 등록증 OCR 원본 데이터 ---
    @NotBlank(message = "등록증 OCR 이름 원본 데이터가 유실되었습니다.")
    private String ocrRawName;

    @NotBlank(message = "등록증 OCR 생년월일 원본 데이터가 유실되었습니다.")
    private String ocrRawBirth;

    @NotBlank(message = "등록증 OCR 차번호 원본 데이터가 유실되었습니다.")
    private String ocrRawCarNumber;

    @NotBlank(message = "등록증 OCR 차종 원본 데이터가 유실되었습니다.")
    private String ocrRawVehicleName;

    // --- 신분증 OCR 원본 데이터 ---
    @NotBlank(message = "신분증 OCR 이름 원본 데이터가 유실되었습니다.")
    private String idCardRawName;

    @NotBlank(message = "신분증 OCR 생년월일 원본 데이터가 유실되었습니다.")
    private String idCardRawBirth;
}