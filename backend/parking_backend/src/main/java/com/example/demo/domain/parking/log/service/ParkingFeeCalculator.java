package com.example.demo.domain.parking.log.service;

import com.example.demo.domain.parking.policy.ParkingFeePolicy;
import com.example.demo.domain.parking.policy.repository.ParkingFeePolicyRepository;
import com.example.demo.domain.parking.log.enums.ParkingTypeSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ParkingFeeCalculator {
    private final ParkingFeePolicyRepository parkingFeePolicyRepository;

    //상세 모달에서 실시간 계산할 때 사용할 로직
    public Long calculateRawFee(LocalDateTime entryAt, LocalDateTime exitAt, ParkingFeePolicy policy){
        if(entryAt ==null || policy == null) return 0L;

        //계산 기준 종료 시간 결정(출차 전이면 현재시간)
        LocalDateTime end = (exitAt != null) ? exitAt : LocalDateTime.now();
        //총 주차 시간(분) 계산
        long totalMinutes = Duration.between(entryAt, end).toMinutes();
        //회차 인정 시간(Grace Minutes) 체크
        if (totalMinutes <= policy.getGraceMinutes()) {
            return 0L;
        }
        //요금 계산(기본요금 + 추가 단위 요금)
        long units = (long) Math.ceil((double) totalMinutes / policy.getUnitMinutes());
        long fee = policy.getBaseFee() + (units * policy.getUnitFee());
        //일일 최대 요금 제한 적용
        return Math.min(fee,(long) policy.getDailyMaxFee());
    }
}
