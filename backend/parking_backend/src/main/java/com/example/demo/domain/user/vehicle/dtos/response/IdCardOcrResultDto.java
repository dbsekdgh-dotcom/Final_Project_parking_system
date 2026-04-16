package com.example.demo.domain.user.vehicle.dtos.response;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class IdCardOcrResultDto {

    private final String name;
    private final String birth;
}
