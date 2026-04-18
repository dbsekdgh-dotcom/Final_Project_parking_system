package com.example.demo.domain.kiosk.entry;

import com.example.demo.domain.shared.systemSetting.repository.SystemSettingRepository;
import com.example.demo.domain.shared.parkinglog.repository.ParkingLogRepository;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class EntryScheduler {

    private final ParkingLogRepository parkingLogRepository;
    private final SystemSettingRepository systemSettingRepository;

    // 30초마다 실행 → 차량별 entryTime 기준으로 만료된 DETECTED 로그 일괄 취소
    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void cancelExpiredDetected() {
        int minutes = Integer.parseInt(
                systemSettingRepository.findById("DETECTED_CANCEL_MINUTES")
                        .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND))
                        .getSettingValue()
        );
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(minutes);
        parkingLogRepository.cancelExpiredDetected(cutoff);
    }
}
