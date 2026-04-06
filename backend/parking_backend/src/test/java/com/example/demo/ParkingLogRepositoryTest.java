package com.example.demo;

import com.example.demo.domain.shared.parkinglog.ParkingLog;
import com.example.demo.domain.shared.parkinglog.dtos.response.ParkingLogSummaryResponse;
import com.example.demo.domain.shared.parkinglog.enums.ParkingTypeSnapshot;
import com.example.demo.domain.shared.parkinglog.repository.ParkingLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ParkingLogRepositoryTest {
    @Autowired
    private ParkingLogRepository parkingLogRepository;

    @org.springframework.boot.test.mock.mockito.MockBean
    private org.springframework.mail.javamail.JavaMailSender javaMailSender;

    @Test
    @DisplayName("관리자 페이지 상단 요약 통계 정보 조회 테스트")
    void getParkingSummaryTest(){
        ParkingLogSummaryResponse summary = parkingLogRepository.getParkingSummary();
        System.out.println("현재 주차 대수:"+summary.getCurrentParkingCount());
        System.out.println("오늘 출차 대수:"+summary.getTodayExitedCount());
        System.out.println("전체 미납 건수:"+summary.getUnpaidCount());
        System.out.println("오늘 발생 로그:"+summary.getTodayLogCount());

        assertThat(summary.getCurrentParkingCount()).isGreaterThanOrEqualTo(7);
        assertThat(summary.getTodayExitedCount()).isGreaterThanOrEqualTo(1);
        assertThat(summary.getUnpaidCount()).isGreaterThanOrEqualTo(4);
        assertThat(summary.getTodayLogCount()).isGreaterThanOrEqualTo(4);
    }

}
