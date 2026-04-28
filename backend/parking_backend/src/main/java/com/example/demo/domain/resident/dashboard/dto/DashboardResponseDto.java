package com.example.demo.domain.resident.dashboard.dto;

import lombok.*;
import org.springframework.data.domain.Page;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponseDto {

    //내 현황 섹션
    private int myPoint;
    private String myCarNumber;
    private String myCarLocation;
    private String parkingDuration;
    private Long subscriptionDDay; // null = 활성 정기권 없음

    //층별 상세 현황
    private FloorDetail b1Detail;
    private FloorDetail b2Detail;

    //최근 입출자 내역
    private Page<DashboardRecentLogDto> recentLogs;

    @Getter@Setter @AllArgsConstructor @Builder
    public static class FloorDetail{
        private int available;      //가용 주차면
        private int total;          //전체 주차면
        private int occupancyRate;  // 점유율(%)
        private String floorName;   // B1층 , B2층
        private String description; //외부차량, 입주민/상가전용
    }
}
