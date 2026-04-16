package com.example.demo.domain.kiosk.exit.service;

import com.example.demo.domain.shared.parkinglog.repository.ParkingLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class FreeExitKeyExpirationListener implements MessageListener {

    private final ParkingLogRepository parkingLogRepository;
    private final FreeExitExpirationService freeExitExpirationService;
    private static final String KEY_PREFIX = "freeExit";

    @Override
    @Transactional
    public void onMessage(Message message, byte[] pattern){
        String expiredKey = message.toString();
        if (!expiredKey.startsWith(KEY_PREFIX)) return;

        try{
            Long parkingLogId = Long.parseLong(expiredKey.substring(KEY_PREFIX.length()));
            parkingLogRepository.findById(parkingLogId).ifPresent(
                    freeExitExpirationService::syncFreeExitStatus
            );
        }catch (Exception e){
            log.error("freeExit 만료 처리 실패={}:{}",expiredKey,e.getMessage());
        }
    }
}
