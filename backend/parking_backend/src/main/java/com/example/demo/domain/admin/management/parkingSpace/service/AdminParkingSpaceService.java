package com.example.demo.domain.admin.management.parkingSpace.service;

import com.example.demo.domain.admin.management.parkingSpace.dtos.response.ParkingSpaceSummaryResponse;
import com.example.demo.domain.admin.management.parkingSpace.repository.ParkingSpaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminParkingSpaceService {
    private final ParkingSpaceRepository parkingSpaceRepository;

//    public ParkingSpaceSummaryResponse getParkingSpaceSummary(){
//        // 전체 주차자리 수 조회
//
//        // 상태별 카운트 조회
//
//        // 점유율 계산 (0으로 나누기 방지)
//
//        // 층별 현황 데이터 계산(우측 하단 게이지바용)
//    }
}
