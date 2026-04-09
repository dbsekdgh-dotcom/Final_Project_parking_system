package com.example.demo.domain.user.apply.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AvailableUnitResponseDto {
    private List<Integer> availableUnits;
}
