package com.example.demo.domain.shared.parkinglog.dtos.response;

import com.example.demo.domain.shared.parkinglog.ParkingLog;
import com.example.demo.domain.shared.parkinglog.enums.ParkingStatus;
import com.example.demo.domain.shared.parkinglog.enums.ParkingTypeSnapshot;
import com.example.demo.domain.shared.parkinglog.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ParkingLogDetailResponse { // 관리자 - 입출차 기록 - 차량 상세보기 Dto
    private Long parkingLogId;
    private String carNumber;
    private boolean isBlacklist;

    //입차,출차 이미지 경로
    private String entryPlateImage; //입차 이미지
    private String exitPlateImage; //출차 이미지

    //상태정보
    private ParkingTypeSnapshot userType; //입차 시점 권한(입주민,외부,회원,예약 방문)
    private ParkingStatus parkingStatus; //주차 상태
    private PaymentStatus paymentStatus; //결제 상태

    //입출차 시간
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime entryTime; //입차 감지 시점
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime exitTime; //출차 감지 시점
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime enteredAt; //실제 입차 완료 시점
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime exitedAt; //실제 출차 완료 시점

    private String parkingDuration; // 주차 이용 시간(예-30분/1시간 20분)

    //요금 관련 정보
    private Integer fee; //실제 db에 기록된 결제 금액
    private Long calculatedFee; //현재 시간 기준 시스템이 계산한 예상 금액

    //위치 정보
    private String spaceCode; //주차 공간 번호
    private String floor;

    //상세조회 변환 메서드
    public static ParkingLogDetailResponse toDetailDto(ParkingLog entity){
        String duration = "-";
        if(entity.getEnteredAt() != null){
            LocalDateTime end = (entity.getExitedAt() != null) ? entity.getExitedAt():LocalDateTime.now();
            long totalMinutes = java.time.Duration.between(entity.getEnteredAt(),end).toMinutes();
            long hours = totalMinutes / 60;
            long mins = totalMinutes % 60;
            if(hours>0){
                duration = String.format("%d시간 %d분",hours,mins);
            }else {
                duration = mins + "분";
            }
        }

        return ParkingLogDetailResponse.builder()
                .parkingLogId(entity.getParkingLogId())
                .carNumber(entity.getCarNumberSnapshot())
                .entryPlateImage(entity.getEntryPlateImage())
                .exitPlateImage(entity.getExitPlateImage())
                .userType(entity.getParkingTypeSnapshot())
                .parkingStatus(entity.getParkingStatus())
                .paymentStatus(entity.getPaymentStatus())
                .entryTime(entity.getEntryTime())
                .exitTime(entity.getExitTime())
                .enteredAt(entity.getEnteredAt())
                .exitedAt(entity.getExitedAt())
                .fee(entity.getFee())
                .calculatedFee(entity.getCalculatedFee())
                .parkingDuration(duration)
                .isBlacklist(entity.getIsBlacklist())
                .spaceCode(entity.getParkingSpace() != null ?
                        entity.getParkingSpace().getSpaceCode() : "-")
                .floor(entity.getParkingSpace() != null ?
                        entity.getParkingSpace().getFloor().name() : "-")
                .build();
    }
}
