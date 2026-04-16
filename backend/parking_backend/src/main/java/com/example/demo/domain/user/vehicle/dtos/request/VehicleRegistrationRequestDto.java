package com.example.demo.domain.user.vehicle.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class VehicleRegistrationRequestDto {
    @NotBlank(message = "차량 번호는 필수 입력 값입니다.")
    @Size(max = 25, message = "차량 번호는 25자를 초과할 수 없습니다.")
    private String carNumber;

    @NotBlank(message = "차종 정보는 필수입니다.")
    @Size(max = 100, message = "차종 명칭은 100자를 초과할 수 없습니다.")
    private String vehicleName;

    @NotBlank(message = "이름을 확인해주세요.")
    private String name;

    @NotBlank(message = "생년월일을 확인해주세요.")
    @Pattern(regexp = "\\d{6}", message = "생년월일은 6자리 숫자여야 합니다.")
    private String birth;

    @NotBlank(message = "OCR 원본 데이터가 유실되었습니다.")
    private String ocrRawName;

    @NotBlank(message = "OCR 원본 데이터가 유실되었습니다.")
    private String ocrRawBirth;
}
