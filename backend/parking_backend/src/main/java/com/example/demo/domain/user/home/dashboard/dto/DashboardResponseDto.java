package com.example.demo.domain.user.home.dashboard.dto;

import com.example.demo.domain.shared.parkingspace.enums.Floor;
import lombok.*;

import java.util.List;

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
    private long subscriptionDDay;

    //층별 상세 현황
    private FloorDetail b1Detail;
    private FloorDetail b2Detail;

    //최근 입출자 내역
    private List<RecentLog> recentLogs;

    @Getter@Setter @AllArgsConstructor @Builder
    public static class FloorDetail{
        private int available;      //가용 주차면
        private int total;          //전체 주차면
        private int occupancyRate;  // 점유율(%)
        private String floorName;   // B1층 , B2층
        private String description; //외부차량, 입주민/상가전용
    }

    @Getter @Setter @AllArgsConstructor @Builder
    public static class RecentLog{
        private String type;
        private String carNumber;
        private String location;
        private String status;
        private String timeAgo;
    }

}
