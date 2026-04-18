package com.example.demo.domain.parking.policy.service;

import com.example.demo.domain.parking.policy.ParkingFeePolicy;
import com.example.demo.domain.parking.policy.enums.ParkingType;
import com.example.demo.domain.parking.policy.repository.ParkingFeePolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class PolicyScheduler {
    private final ParkingFeePolicyRepository parkingFeePolicyRepository;

    @Scheduled(cron = "0 0 0 * * *") //내일 policy id 2번 변경되었는지 확인하기
    @Transactional
    public void updatePolicyStatus(){
        log.info("---[scheduler] 요금 정책 상태 업데이트 시작---");
        LocalDateTime now=LocalDateTime.now();
        //현재 활성화된 정책 비활성화
        parkingFeePolicyRepository.findPoliciesToInactivateByType(ParkingType.VISIT).forEach(p-> p.setIsActive(false));
        parkingFeePolicyRepository.findPoliciesToInactivateByType(ParkingType.RESERVATION).forEach(p->p.setIsActive(false));

        //적용 시작일이 지난 정책 중 가장 버전이 높은것 활성화
        parkingFeePolicyRepository.findPoliciesToActivate(now,ParkingType.VISIT).forEach(p->p.setIsActive(true));
        parkingFeePolicyRepository.findPoliciesToActivate(now,ParkingType.RESERVATION).forEach(p->p.setIsActive(true));

        //만료된 정책 비활성화
        parkingFeePolicyRepository.findPoliciesToInActivate(now).forEach(p->p.setIsActive(false));
        log.info("---[scheduler] 요금 정책 상태 업데이트 종료---");
    }
}
