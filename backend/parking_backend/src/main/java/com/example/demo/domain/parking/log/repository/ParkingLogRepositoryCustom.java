package com.example.demo.domain.parking.log.repository;

import com.example.demo.domain.parking.log.dtos.response.ParkingLogSummaryResponse;

public interface ParkingLogRepositoryCustom {
    //입출차 기록 페이지 상단 4가지 요약 데이터를 가져오는 메서드
    ParkingLogSummaryResponse getParkingSummary();

}
