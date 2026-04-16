package com.example.demo.domain.user.home.dashboard.service;

import com.example.demo.domain.kiosk.entry.repository.EntryParkingSpaceRepository;
import com.example.demo.domain.shared.parkinglog.ParkingLog;
import com.example.demo.domain.shared.parkinglog.enums.ParkingStatus;
import com.example.demo.domain.shared.parkinglog.repository.ParkingLogRepository;
import com.example.demo.domain.shared.parkingspace.enums.Floor;
import com.example.demo.domain.shared.parkingspace.enums.SpaceStatus;
import com.example.demo.domain.shared.subscription.repository.SubscriptionRepository;
import com.example.demo.domain.shared.vehicle.Vehicle;
import com.example.demo.domain.shared.vehicle.VehicleRepository;
import com.example.demo.domain.shared.vehicle.enums.VehicleStatus;
import com.example.demo.domain.user.home.dashboard.dto.DashboardResponseDto;
import com.example.demo.domain.user.mypage.point.entity.UserPoint;
import com.example.demo.domain.user.mypage.point.repository.UserPointRepository;
import com.example.demo.domain.user.report.repository.VehicleReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    //필요한 레파지토리 주입받기
    private final UserPointRepository userPointRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final EntryParkingSpaceRepository entryParkingSpaceRepository;
    private final VehicleRepository vehicleRepository;
    private final ParkingLogRepository parkingLogRepository;

    public DashboardResponseDto getUserDashboardData(Long userId){

        //활성 차량 조회( 최근 등록 순)
        Vehicle myVehicle = vehicleRepository.findMainVehicle(userId, VehicleStatus.ACTIVE)
                .orElse(null);

        String carNumber = (myVehicle != null) ? myVehicle.getCarNumber(): "등록 차량 없음";
        String carLocation = "주차 정보 없음";
        String durationStr = "0분";
        List<DashboardResponseDto.RecentLog> logs = new ArrayList<>();

        if(myVehicle != null){
            //현재 주차 중인 최신 로그 확인( 상태가 ENTERED이고 아직 출차 안함)
            Optional<ParkingLog> activeLog = parkingLogRepository.findFirstByCarNumberSnapshotAndParkingStatus(carNumber, ParkingStatus.ENTERED);

            if (activeLog.isPresent() && activeLog.get().getExitedAt() == null){
                ParkingLog log = activeLog.get();
                //위치정보 (ParkingSpace 엔티티가 ParkingLog안에 연관관계로 있어야 함)
                if (log.getParkingSpace() != null){
                    carLocation =log.getParkingSpace().getFloor() + "/" + log.getParkingSpace().getSpaceCode();
                }
                //주차시간 계산
                long diffMinutes = ChronoUnit.MINUTES.between(log.getEntryTime(),LocalDateTime.now());
                durationStr = (diffMinutes >= 60) ? (diffMinutes / 60) + "시간" + (diffMinutes % 60) + "분" : diffMinutes + "분";
            }

            //최근 입출자 내역 리스트 (최신 5개)
            logs = parkingLogRepository.findTop5ByCarNumberSnapshotOrderByEntryTimeDesc(carNumber).stream()
                    .map(l -> DashboardResponseDto.RecentLog.builder()
                            .type(l.getExitedAt() == null ? "입" : "출")
                            .carNumber(carNumber)
                            .location(l.getParkingSpace() != null ? l.getParkingSpace().getSpaceCode() : "알 수 없음")
                            .status(l.getExitedAt() == null ? "입차" : "출차")
                            .timeAgo(calculateTimeAgo(l.getEntryTime()))
                            .build())
                        .toList();

        }


        //포인트 조회
        int myPoint = userPointRepository.findById(userId)
                .map(UserPoint::getCurrentPoint)
                .orElse(0);

        //정기권 D-Day 계산
        long dDay = subscriptionRepository.findLatestSubscription(userId)
                .map(sub -> ChronoUnit.DAYS.between(LocalDateTime.now(),sub.getEndDate()))
                .orElse(0L);

        //층별 주차 현황 조회
        int b1Available = entryParkingSpaceRepository.countByFloorAndStatus(Floor.B1, SpaceStatus.AVAILABLE);
        int b2Available = entryParkingSpaceRepository.countByFloorAndStatus(Floor.B2, SpaceStatus.AVAILABLE);

        int floorTotal =30;

        return DashboardResponseDto.builder()
                .myPoint(myPoint)
                .subscriptionDDay(dDay)
                .myCarNumber(carNumber)
                .myCarLocation(carLocation)
                .parkingDuration(durationStr)
                .b1Detail(buildFloorDetail(Floor.B1,b1Available,30))
                .b2Detail(buildFloorDetail(Floor.B2,b2Available,30))
                .recentLogs(logs)
                .build();
    }

    //점유율 계산을 위한 간단한 헬퍼 메서드
    private String calculateTimeAgo(LocalDateTime time){
        long minutes = ChronoUnit.MINUTES.between(time, LocalDateTime.now());
        if (minutes < 60)return minutes + "분 전";
        if (minutes <1440)return (minutes /60) + "시간 전";
        return (minutes / 1440) + "일 전";
    }

    private DashboardResponseDto.FloorDetail buildFloorDetail(Floor floor, int available, int total){
        return DashboardResponseDto.FloorDetail.builder()
                .available(available)
                .total(total)
                .occupancyRate((int)(((double)(total - available)/ total)*100))
                .floorName(floor.name() + "층")
                .description(floor == Floor.B1 ? "외부 차량 가능" : "입주민 전용")
                .build();
    }

}
