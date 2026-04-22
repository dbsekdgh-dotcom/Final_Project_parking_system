package com.example.demo.domain.resident.dashboard.service;

import com.example.demo.domain.parking.space.repository.ParkingSpaceRepository;
import com.example.demo.domain.parking.log.ParkingLog;
import com.example.demo.domain.parking.log.enums.ParkingStatus;
import com.example.demo.domain.parking.log.repository.ParkingLogRepository;
import com.example.demo.domain.parking.space.enums.Floor;
import com.example.demo.domain.parking.space.enums.SpaceStatus;
import com.example.demo.domain.payment.subscription.repository.SubscriptionRepository;
import com.example.demo.domain.resident.dashboard.dto.DashboardRecentLogDto;
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
        List<ActivityType> types = Arrays.asList(ActivityType.ENTRY, ActivityType.EXIT);

        // 1. 페이지 설정 (EntryTime 기준 역순 정렬)
        Pageable pageable = PageRequest.of(page, 5, Sort.by("entryTime").descending());

        // ParkingLog에서 데이터 가져오기
        Page<DashboardRecentLogDto> logPage = parkingLogRepository.findMyAndReservedLogs(userId, pageable)
                .map(log -> {
                    // 소유주 ID를 안전하게 가져오기 (소유주가 없으면 null)
                    Long ownerId = (log.getVehicle() != null && log.getVehicle().getUser() != null)
                            ? log.getVehicle().getUser().getUserId()
                            : null;

                    // 소유주 ID가 로그인한 유저(userId)와 같으면 "내 차량", 아니면 "방문 예약 차량"
                    String message = (ownerId != null && ownerId.equals(userId)) ? "내 차량" : "방문 예약 차량";

                    return DashboardRecentLogDto.builder()
                            .parkingLogId(log.getParkingLogId())
                            .status(log.getParkingStatus().getDescription())
                            .carNumber(log.getCarNumberSnapshot())
                            .message(message)
                            .createdAt(log.getEntryTime())
                            .build();
                });

        //포인트 조회
        int myPoint = userPointRepository.findById(userId)
                .map(UserPoint::getCurrentPoint)
                .orElse(0);

        //정기권 D-Day 계산
        long dDay = subscriptionRepository.findLatestSubscription(userId)
                .map(sub -> ChronoUnit.DAYS.between(LocalDateTime.now(), sub.getEndDate()))
                .orElse(0L);

        //층별 주차 현황 조회
        int b1Available = (int) parkingSpaceRepository.countByFloorAndStatus(Floor.B1, SpaceStatus.AVAILABLE);
        int b2Available = (int) parkingSpaceRepository.countByFloorAndStatus(Floor.B2, SpaceStatus.AVAILABLE);

        int floorTotal = 30;

        return DashboardResponseDto.builder()
                .myPoint(myPoint)
                .subscriptionDDay(dDay)
                .myCarNumber(carNumber)
                .myCarLocation(carLocation)
                .parkingDuration(durationStr)
                .b1Detail(buildFloorDetail(Floor.B1, b1Available, 30))
                .b2Detail(buildFloorDetail(Floor.B2, b2Available, 30))
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
        return DashboardResponseDto.FloorDetail.builder()
                .available(available)
                .total(total)
                .occupancyRate((int)(((double)(total - available)/ total)*100))
                .floorName(floor.name() + "층")
                .description(floor == Floor.B1 ? "외부 차량 가능" : "입주민 전용")
                .build();
    }

}
