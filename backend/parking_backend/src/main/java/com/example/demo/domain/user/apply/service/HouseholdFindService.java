package com.example.demo.domain.user.apply.service;

import com.example.demo.domain.shared.household.enums.IsActive;
import com.example.demo.domain.shared.household.repository.HouseholdRepository;
import com.example.demo.domain.user.apply.dtos.response.AvailableUnitResponseDto;
import com.example.demo.domain.user.apply.dtos.response.HouseholdListResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HouseholdFindService {

    private final HouseholdRepository householdRepository;

    @Transactional(readOnly = true)
    public List<HouseholdListResponseDto> findAllActiveHouseholdes() {
        return householdRepository.findEmptyHouseholds().stream()
                .map(HouseholdListResponseDto::new)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public AvailableUnitResponseDto getAvailableUnitNos() {
        List<Integer> availableUnits = householdRepository.findAvailableUnitNos();

        return new AvailableUnitResponseDto(availableUnits);
    }
}
