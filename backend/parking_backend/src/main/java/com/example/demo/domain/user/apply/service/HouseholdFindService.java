package com.example.demo.domain.user.apply.service;

import com.example.demo.domain.shared.approval.enums.ApprovalStatus; // 추가
import com.example.demo.domain.shared.approval.enums.ApprovalType;   // 추가
import com.example.demo.domain.shared.household.enums.IsActive;
import com.example.demo.domain.shared.household.repository.HouseholdRepository;
import com.example.demo.domain.user.apply.dtos.response.AvailableUnitResponseDto;
import com.example.demo.domain.user.apply.dtos.response.HouseholdListResponseDto;
import com.example.demo.domain.user.apply.dtos.response.UnitStatusResponseDto;
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
    public List<HouseholdListResponseDto> findAllActiveHouseholds() {
        return householdRepository.findEmptyHouseholds().stream()
                .map(HouseholdListResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AvailableUnitResponseDto getAvailableUnitNos() {
        // [수정] 파라미터 3개를 전달해야 함
        List<Integer> availableUnits = householdRepository.findAvailableUnitNos(
                ApprovalType.RESIDENT,
                ApprovalStatus.PENDING,
                ApprovalStatus.APPROVED
        );

        return new AvailableUnitResponseDto(availableUnits);
    }

    @Transactional(readOnly = true)
    public List<UnitStatusResponseDto> getAllUnitStatuses() {
        // [수정] 파라미터 3개를 전달해야 함
        return householdRepository.findAllUnitStatus(
                        IsActive.ACTIVE,
                        ApprovalType.RESIDENT,
                        ApprovalStatus.PENDING
                ).stream()
                .map(p -> new UnitStatusResponseDto(
                        p.getHouseholdId(),
                        p.getUnitNo(),
                        p.getStatus()
                ))
                .collect(Collectors.toList());
    }
}