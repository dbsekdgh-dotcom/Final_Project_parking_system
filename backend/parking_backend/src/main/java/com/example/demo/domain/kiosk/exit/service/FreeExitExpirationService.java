package com.example.demo.domain.kiosk.exit.service;

import com.example.demo.domain.kiosk.payment.dtos.response.FeeCalculationResponseDto;
import com.example.demo.domain.shared.parkinglog.ParkingLog;
import com.example.demo.domain.shared.parkinglog.enums.ParkingTypeSnapshot;
import com.example.demo.domain.shared.parkinglog.enums.PaymentStatus;
import com.example.demo.domain.shared.parkinglog.repository.ParkingLogRepository;
import com.example.demo.domain.shared.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FreeExitExpirationService {
    private final ParkingLogRepository parkingLogRepository;
    private final ReservationRepository reservationRepository;

    //무료시간 이후 요금 정산 업데이트
    @Transactional
    public void syncFreeExitStatus(ParkingLog parkingLog){
        //freeExitUntil이 null이거나 현재시각보다 이후면 리턴 -> 무료
        if (parkingLog.getFreeExitUntil()==null || !parkingLog.getFreeExitUntil().isBefore(LocalDateTime.now())) return;
        if (parkingLog.getPaymentStatus() == PaymentStatus.UNPAID) return;

        // 요금이 생성되면 상태값 업데이트 기납부 금액 + 추가 시간 요금 = 누적 청구 금액
        parkingLog.setPaymentStatus(PaymentStatus.UNPAID);
        parkingLog.setPaymentRequestedAt(null);
        parkingLogRepository.save(parkingLog);

        // RESERVATION 타입이면 Reservation 업데이트
        if (parkingLog.getParkingTypeSnapshot() == ParkingTypeSnapshot.RESERVATION){
            reservationRepository.updateIsFreeByCarNumber(parkingLog.getCarNumberSnapshot());
        }
    }

    @Scheduled(fixedDelay = 300000)
    @Transactional
    public void revertStaledExitRequested(){
        // 60초에 한번씩 EXIT_REQUSTED 상태에서 10분이 지난 차량 검색해서 ENTERED 상태로 변환
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(10);
        List<ParkingLog> logs= parkingLogRepository.findStaledExitRequestedLogs(cutoff);
        for (ParkingLog log : logs){
            log.revertToEntered();
            parkingLogRepository.save(log);
        }
    }
}
