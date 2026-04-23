package com.example.demo.domain.management.dtos.response;

import com.example.demo.domain.resident.User;
import com.example.demo.domain.resident.household.Household;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class AdminUserDetailResponseDto {
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private LocalDate birth;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;

    private Long householdId;
    private Integer unitNo;
    private String householdStatus;

    private List<AdminVehicleResponseDto> vehicles;

    public static AdminUserDetailResponseDto from(User user, List<AdminVehicleResponseDto> vehicles){
        Household h = user.getHousehold();
        return AdminUserDetailResponseDto.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .birth(user.getBirth())
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .deletedAt(user.getDeletedAt())
                .householdId(h != null ? h.getHouseholdId() : null)
                .unitNo(h != null ? h.getUnitNo() : null)
                .householdStatus( h != null ? h.getIsActive().name() : null)
                .vehicles(vehicles)
                .build();
    }
}
