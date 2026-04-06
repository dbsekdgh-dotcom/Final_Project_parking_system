package com.example.demo.domain.shared.parkinglog.dtos.response;

import com.example.demo.domain.shared.parkinglog.ParkingLog;
import com.example.demo.domain.shared.parkinglog.enums.ParkingStatus;
import com.example.demo.domain.shared.parkinglog.enums.ParkingTypeSnapshot;
import com.example.demo.domain.shared.parkinglog.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@Builder
public class ParkingLogListResponse { // 관리자페이지 - 입출차 기록 하단 테이블 리스트용
    private Long parkingLogId;
    private String carNumber;
    private ParkingTypeSnapshot type; // 유형(입주민,외부인,예약 등)
    private ParkingStatus parkingStatus; // 주차상태 (입차완료, 출차완료 등)
    private PaymentStatus paymentStatus; //결제상태 (미납,완납 등)
    @JsonFormat(pattern = "yy/MM/dd HH:mm:ss")
    private LocalDateTime entryTime; // 입차완료 우선, 없으면 감지 시간
    @JsonFormat(pattern = "yy/MM/dd HH:mm:ss")
    private LocalDateTime exitTime; // 출차완료 시점
    private String parkingDuration; //계산된 주차 시간 (예: "1시간10분")
    private String parkingSpaceCode; //주차 위치


//    엔티티를 DTO로 변환하는 정적 메소드
    public static ParkingLogListResponse toListDto(ParkingLog entity){
        // 입차시간 우선순위: 입차완료(enteredAt) > 감지시간(entryTime)
        LocalDateTime displayEntryTime = (entity.getEnteredAt()!=null)
                ? entity.getEnteredAt()
                : entity.getEntryTime();
        //출차시간 우선순위: 실제출차(exitedAt) > 출차요청시간(exitTime)
        LocalDateTime displayExitTime = (entity.getExitedAt()!=null)
                ? entity.getExitedAt()
                : entity.getExitTime();

        return ParkingLogListResponse.builder()
                .parkingLogId(entity.getParkingLogId())
                .carNumber(entity.getCarNumberSnapshot())
                .type(entity.getParkingTypeSnapshot())
                .parkingStatus(entity.getParkingStatus())
                .paymentStatus(entity.getPaymentStatus())
                .entryTime(displayEntryTime)
                .exitTime(displayExitTime)
                //주차 시간 계산 시에도 이 우선순위 시간을 사용함
                .parkingDuration(calculateDuration(displayEntryTime, displayExitTime))
                .parkingSpaceCode(entity.getParkingSpace()!=null
                ? entity.getParkingSpace().getSpaceCode() : "미지정")
                .build();
    }

    //주차시간 계산 로직
    private static String calculateDuration(LocalDateTime start, LocalDateTime end) {
        if(start==null) return "-";
        //출차 전이면 현재 시간까지, 출차했으면 출차 시점까지 계산
        LocalDateTime endPoint = (end!=null)?end:LocalDateTime.now();
        Duration duration = Duration.between(start,endPoint);

        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();

        if(hours>0){
            return String.format("%d시간 %d분",hours,minutes);
        }
        return String.format("%d분",minutes);
    }
}

