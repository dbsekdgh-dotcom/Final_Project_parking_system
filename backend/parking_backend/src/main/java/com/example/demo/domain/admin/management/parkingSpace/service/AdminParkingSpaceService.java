package com.example.demo.domain.admin.management.parkingSpace.service;

import com.example.demo.domain.admin.management.parkingSpace.dtos.response.ParkingSpaceListResponse;
import com.example.demo.domain.admin.management.parkingSpace.dtos.response.ParkingSpaceSummaryResponse;
import com.example.demo.domain.admin.management.parkingSpace.repository.ParkingSpaceRepository;
import com.example.demo.domain.shared.parkingspace.enums.Floor;
import com.example.demo.domain.shared.parkingspace.enums.SpaceStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminParkingSpaceService {
    private final ParkingSpaceRepository parkingSpaceRepository;

    //관리자 - 주차공간 상단 요약 모달, 하단 층별 주차 점유율
    public ParkingSpaceSummaryResponse getParkingSpaceSummary(){
        // 전체 주차자리 수 조회
        long totalSpaces = parkingSpaceRepository.count();
        // 상태별 카운트 조회
        long occupied = parkingSpaceRepository.countByStatus(SpaceStatus.OCCUPIED);
        long available = parkingSpaceRepository.countByStatus(SpaceStatus.AVAILABLE);
        // 점유율 계산 (0으로 나누기 방지)
        double occupancyRate = totalSpaces > 0 ?
                Math.round(((double) occupied / totalSpaces * 100) * 10) / 10.0
                : 0.0;
        // 층별 현황 데이터 계산(우측 하단 게이지바용)
        long b1Occupied = parkingSpaceRepository.countByFloorAndStatus(Floor.B1, SpaceStatus.OCCUPIED);
        long b1Total = parkingSpaceRepository.countByFloor(Floor.B1);

        long b2Occupied = parkingSpaceRepository.countByFloorAndStatus(Floor.B2,SpaceStatus.OCCUPIED);
        long b2Total = parkingSpaceRepository.countByFloor(Floor.B2);

        return ParkingSpaceSummaryResponse.builder()
                .totalSpaces(totalSpaces)
                .occupiedSpaces(occupied)
                .availableSpaces(available)
                .occupancyRate(occupancyRate)
                .b1Total(b1Total)
                .b2Total(b2Total)
                .b1Occupied(b1Occupied)
                .b2Occupied(b2Occupied)
                .build();
    }

    // 관리자 - 주차공간 층별 주차 구획 리스트 조회
    public List<ParkingSpaceListResponse> getFloorSpaces(Floor floor){
        return parkingSpaceRepository.findByFloorOrderBySpaceCodeAsc(floor)
                .stream()
                .map(space->ParkingSpaceListResponse.builder()
                        .id(space.getId())
                        .spaceCode(space.getSpaceCode())
                        .status(space.getStatus())
                        .isDisabled(space.getIsDisabled())
                        .isEvCharge(space.getIsEvCharge())
                        .build())
                .collect(Collectors.toList());
    }
}
