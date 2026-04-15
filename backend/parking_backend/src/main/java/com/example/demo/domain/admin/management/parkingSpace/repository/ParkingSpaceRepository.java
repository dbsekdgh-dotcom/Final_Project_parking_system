package com.example.demo.domain.admin.management.parkingSpace.repository;

import com.example.demo.domain.shared.parkingspace.ParkingSpace;
import com.example.demo.domain.shared.parkingspace.enums.Floor;
import com.example.demo.domain.shared.parkingspace.enums.SpaceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParkingSpaceRepository extends JpaRepository<ParkingSpace, Long> {
    // 층별 주차공간 목록 조회
    List<ParkingSpace> findByFloorOrderBySpaceCodeAsc(Floor floor);

    //상태별 주차공간 수 카운트 (전체/점유/가용 요약용)
    long countByStatus(SpaceStatus status);

    //특정 층의 상태별 카운트 (구역별 현황 UI용)
    long countByFloorAndStatus(Floor floor, SpaceStatus status);
}
