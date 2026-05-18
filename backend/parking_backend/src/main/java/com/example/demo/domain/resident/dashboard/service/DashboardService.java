package com.example.demo.domain.resident.dashboard.service;

import com.example.demo.domain.parking.space.repository.ParkingSpaceRepository;
import com.example.demo.domain.parking.log.ParkingLog;
import com.example.demo.domain.parking.log.enums.ParkingStatus;
import com.example.demo.domain.parking.log.repository.ParkingLogRepository;
import com.example.demo.domain.parking.space.enums.Floor;
import com.example.demo.domain.parking.space.enums.SpaceStatus;
import com.example.demo.domain.payment.subscription.repository.SubscriptionRepository;
import com.example.demo.domain.resident.dashboard.dto.DashboardRecentLogDto;
import com.example.demo.domain.system.activitylog.ActivityLog;
import com.example.demo.domain.system.activitylog.enums.ActivityType;
import com.example.demo.domain.system.activitylog.repository.ActivityLogRepository;
import com.example.demo.domain.vehicle.Vehicle;
import com.example.demo.domain.vehicle.VehicleRepository;
import com.example.demo.domain.vehicle.enums.VehicleStatus;
import com.example.demo.domain.resident.dashboard.dto.DashboardResponseDto;
import com.example.demo.domain.payment.point.entity.UserPoint;
import com.example.demo.domain.payment.point.repository.UserPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    //필요한 레파지토리 주입받기
    private final UserPointRepository userPointRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ParkingSpaceRepository parkingSpaceRepository;
    private final VehicleRepository vehicleRepository;
    private final ParkingLogRepository parkingLogRepository;
    private final ActivityLogRepository activityLogRepository;

    public DashboardResponseDto getUserDashboardData(Long userId, int page) {

        //활성 차량 조회( 최근 등록 순)
        Vehicle myVehicle = vehicleRepository.findMainVehicle(userId, VehicleStatus.ACTIVE)
                .orElse(null);
        String carNumber = (myVehicle != null) ? myVehicle.getCarNumber() : "등록 차량 없음";
        String carLocation = "주차 정보 없음";
        String durationStr = "0분";
        // 주차 위치 계산(myVehicle있을 때만)
        if (myVehicle != null) {
            //현재 주차 중인 최신 로그 확인( 상태가 ENTERED이고 아직 출차 안함)
            Optional<ParkingLog> activeLog = parkingLogRepository.findFirstByCarNumberSnapshotAndParkingStatus(carNumber, ParkingStatus.ENTERED);

            if (activeLog.isPresent() && activeLog.get().getExitedAt() == null) {
                ParkingLog log = activeLog.get();
                //위치정보 (ParkingSpace 엔티티가 ParkingLog안에 연관관계로 있어야 함)
                if (log.getParkingSpace() != null) {
                    carLocation = log.getParkingSpace().getFloor() + "/" + log.getParkingSpace().getSpaceCode();
                }
                //주차시간 계산
                long diffMinutes = ChronoUnit.MINUTES.between(log.getEntryTime(), LocalDateTime.now());
                durationStr = (diffMinutes >= 60) ? (diffMinutes / 60) + "시간" + (diffMinutes % 60) + "분" : diffMinutes + "분";
            }
        }

        //최근 입출자 내역 리스트 (최신 5개)
        //필터링 할 타입의 정의(ENTRY, EXIT만)
        //최근 입출차 내역 리스트 (최신 5개) - activity_log 기반 누적 이력으로 변경!
        List<ActivityLog> recentActivities = activityLogRepository.findRecentActivities(userId);

        List<DashboardRecentLogDto> dtoList = recentActivities.stream()
                .filter(activity -> activity.getActivityType() == ActivityType.ENTRY || activity.getActivityType() == ActivityType.EXIT)
                .limit(5)
                .map(activity -> {
                    Long ownerId = (activity.getUser() != null) ? activity.getUser().getUserId() : null;
                    String message = (ownerId != null && ownerId.equals(userId)) ? "내 차량" : "방문 예약 차량";
                    String statusDescription = (activity.getActivityType() == ActivityType.ENTRY) ? "입차완료" : "출차완료";

                    return DashboardRecentLogDto.builder()
                            .parkingLogId(activity.getActivityId())
                            .status(statusDescription)
                            .carNumber(activity.getCarNumber())
                            .message(message)
                            .createdAt(activity.getCreatedAt())
                            .build();
                })
                .toList();

        // 기존 return 규격에 맞추기 위해 PageImpl로 감싸서 logPage 변수에 담아줍니다.
        Page<DashboardRecentLogDto> logPage = new org.springframework.data.domain.PageImpl<>(dtoList);

        //포인트 조회
        int myPoint = userPointRepository.findById(userId)
                .map(UserPoint::getCurrentPoint)
                .orElse(0);

        //정기권 D-Day 계산 (ACTIVE + 만료 안 된 정기권만, 없으면 null)
        Long dDay = subscriptionRepository.findMyActiveSubscription(userId, LocalDateTime.now())
                .map(sub -> ChronoUnit.DAYS.between(LocalDateTime.now(), sub.getEndDate()))
                .orElse(null);

        //층별 주차 현황 조회
        int b1Total     = (int) parkingSpaceRepository.countByFloor(Floor.B1);
        int b2Total     = (int) parkingSpaceRepository.countByFloor(Floor.B2);
        int b1Available = (int) parkingSpaceRepository.countByFloorAndStatus(Floor.B1, SpaceStatus.AVAILABLE);
        int b2Available = (int) parkingSpaceRepository.countByFloorAndStatus(Floor.B2, SpaceStatus.AVAILABLE);

        return DashboardResponseDto.builder()
                .myPoint(myPoint)
                .subscriptionDDay(dDay)
                .myCarNumber(carNumber)
                .myCarLocation(carLocation)
                .parkingDuration(durationStr)
                .b1Detail(buildFloorDetail(Floor.B1, b1Available, b1Total))
                .b2Detail(buildFloorDetail(Floor.B2, b2Available, b2Total))
                .recentLogs(logPage)
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
        int occupied = total - available;
        int occupancyRate = total > 0 ? (int)((double) occupied / total * 100) : 0;
        return DashboardResponseDto.FloorDetail.builder()
                .available(available)
                .total(total)
                .occupancyRate(occupancyRate)
                .floorName(floor.name() + "층")
                .description(floor == Floor.B1 ? "외부 차량 가능" : "입주민 전용")
                .build();
    }
}
