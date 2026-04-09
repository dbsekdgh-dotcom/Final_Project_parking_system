package com.example.demo.domain.user.apply.dtos.response;

import com.example.demo.domain.shared.household.Household;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public class HouseholdListResponseDto {

    private final Long householdId;
    private final Integer unitNo;

    public HouseholdListResponseDto(Household household){
        this.householdId=household.getHouseholdId();
        this.unitNo=household.getUnitNo();
    }
}
