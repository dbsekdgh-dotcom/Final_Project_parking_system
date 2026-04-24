package com.example.demo.domain.parking.space.service;

import com.example.demo.domain.parking.space.dtos.response.UserParkingSummaryDto;
import com.example.demo.domain.parking.space.enums.Floor;
import com.example.demo.domain.parking.space.enums.SpaceStatus;
import com.example.demo.domain.parking.space.repository.ParkingSpaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserParkingSpaceService {

    private final ParkingSpaceRepository repository;

    public UserParkingSummaryDto getMyFloorSummary(String userType) {
        Floor targetFloor = "RESIDENT".equals(userType) ? Floor.B2 : Floor.B1;

        long total    = repository.countByFloor(targetFloor);
        long occupied = repository.countByFloorAndStatus(targetFloor, SpaceStatus.OCCUPIED);
        double occupancyRate = total > 0 ? (double) occupied / total * 100 : 0;

        return UserParkingSummaryDto.builder()
                .totalSpaces(total)
                .occupiedSpaces(occupied)
                .availableSpaces(total - occupied)
                .occupancyRate(Math.round(occupancyRate * 10) / 10.0)
                .targetFloor(targetFloor.name())
                .build();
    }
}
