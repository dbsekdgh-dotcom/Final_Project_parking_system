package com.example.demo.domain.parking.space.service;

import com.example.demo.domain.auth.admin.entity.Admin;
import com.example.demo.domain.auth.admin.entity.AdminActionLog;
import com.example.demo.domain.auth.admin.enums.ActionType;
import com.example.demo.domain.auth.admin.enums.TargetType;
import com.example.demo.domain.auth.admin.repository.AdminActionLogRepository;
import com.example.demo.domain.auth.admin.repository.AdminRepository;
import com.example.demo.domain.parking.space.dtos.request.SpaceControlRequest;
import com.example.demo.domain.parking.space.dtos.response.ParkingSpaceListResponse;
import com.example.demo.domain.parking.space.dtos.response.ParkingSpaceLogSnapShot;
import com.example.demo.domain.parking.space.dtos.response.ParkingSpaceSummaryResponse;
import com.example.demo.domain.parking.space.repository.ParkingSpaceRepository;
import com.example.demo.domain.parking.log.ParkingLog;
import com.example.demo.domain.parking.log.repository.ParkingLogRepository;
import com.example.demo.domain.parking.space.ParkingSpace;
import com.example.demo.domain.parking.space.enums.Floor;
import com.example.demo.domain.parking.space.enums.SpaceStatus;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import com.example.demo.global.security.admin.AdminAuthDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminParkingSpaceService {
    private final ParkingSpaceRepository parkingSpaceRepository;
    private final ParkingLogRepository parkingLogRepository;
    private final AdminRepository adminRepository;
    private final AdminActionLogRepository adminActionLogRepository;
    private final ObjectMapper objectMapper;

    //관리자 - 주차공간 상단 요약 모달, 하단 층별 주차 점유율
    public ParkingSpaceSummaryResponse getParkingSpaceSummary(){
        // 전체 주차자리 수 조회
        long totalSpaces = parkingSpaceRepository.count();
        // 상태별 카운트 조회
        long occupied = parkingSpaceRepository.countByStatus(SpaceStatus.OCCUPIED);
        long available = parkingSpaceRepository.countByStatus(SpaceStatus.AVAILABLE);
        // 점유율 계산 (0으로 나누기 방지)
        double occupancyRate = totalSpaces > 0 ?
                Math.round(((double) occupied / totalSpaces * 100) * 10) / 10.0
                : 0.0;
        // 층별 현황 데이터 계산(우측 하단 게이지바용)
        long b1Occupied = parkingSpaceRepository.countByFloorAndStatus(Floor.B1, SpaceStatus.OCCUPIED);
        long b1Total = parkingSpaceRepository.countByFloor(Floor.B1);

        long b2Occupied = parkingSpaceRepository.countByFloorAndStatus(Floor.B2,SpaceStatus.OCCUPIED);
        long b2Total = parkingSpaceRepository.countByFloor(Floor.B2);

        return ParkingSpaceSummaryResponse.builder()
                .totalSpaces(totalSpaces)
                .occupiedSpaces(occupied)
                .availableSpaces(available)
                .occupancyRate(occupancyRate)
                .b1Total(b1Total)
                .b2Total(b2Total)
                .b1Occupied(b1Occupied)
                .b2Occupied(b2Occupied)
                .build();
    }

    // 관리자 - 주차공간 층별 주차 구획 리스트 조회
    public List<ParkingSpaceListResponse> getFloorSpaces(Floor floor){
        //해당층의 모든 주차칸 조회
        List<ParkingSpace> allSpaces = parkingSpaceRepository.findByFloorOrderBySpaceCodeAsc(floor);
        //해당 층에서 현재 주차중인 로그들만 조회
        List<ParkingLog> activeLogs = parkingLogRepository.findActiveLogsByFloor(floor);
        //Map으로 변환 (Key: ParkingSpaceId, Value: CarNumber)
        Map<Long,String> activeVehicleMap = activeLogs.stream()
                .collect(Collectors.toMap(
                        log -> log.getParkingSpace().getId(),
                        ParkingLog::getCarNumberSnapshot
                ));

        return allSpaces.stream()
                .map(space->ParkingSpaceListResponse.builder()
                        .id(space.getId())
                        .spaceCode(space.getSpaceCode())
                        .status(space.getStatus())
                        .isDisabled(space.getIsDisabled())
                        .isEvCharge(space.getIsEvCharge())
                        .carNumber(activeVehicleMap.get(space.getId()))
                        .build())
                .collect(Collectors.toList());
    }

    // 관리자 - 주차공간 구획별 통합 제어
    public void controlParkingSpace(Long spaceId, SpaceControlRequest request, AdminAuthDto adminAuthDto) throws Exception{
        //관리자 조회
        Admin currentAdmin = adminRepository.findByLoginId(adminAuthDto.getUsername())
                .orElseThrow(()->new BusinessException(ErrorCode.ADMIN_NOT_FOUND));

        //주차공간 조회
        ParkingSpace space = parkingSpaceRepository.findById(spaceId)
                .orElseThrow(()->new EntityNotFoundException("해당 주차 공간을 찾을 수 없습니다."));
        //변경 전 데이터 스냅샷 (AdminActionLog)
        String beforeData = objectMapper.writeValueAsString(ParkingSpaceLogSnapShot.from(space));
        //요청된 액션에 따른 상태/구역 변경 (1구획 1타입 원칙 반영)
        switch (request.getAction()){
            case BLOCK -> handleBlockAction(space); //차단
            case UNBLOCK -> space.setStatus(SpaceStatus.AVAILABLE); //차단해제
            case SET_DISABLED -> updateZone(space, true, false); //장애인석 설정
            case SET_EV -> updateZone(space, false, true); //전기차석 설정
            case SET_GENERAL -> updateZone(space, false, false); //일반석 설정
        }
        //변경 후 데이터 스냅샷
        String afterData=objectMapper.writeValueAsString(ParkingSpaceLogSnapShot.from(space));
        //감사로그 기록
        AdminActionLog actionLog = AdminActionLog.builder()
                .admin(currentAdmin)
                .targetType(TargetType.PARKING_SPACE)
                .actionType(ActionType.UPDATE)
                .targetId(spaceId)
                .beforeData(beforeData)
                .afterData(afterData)
                .isReverted(false)
                .build();
        adminActionLogRepository.save(actionLog);
    }

    //차단 시 주차 상태 확인
    private void handleBlockAction(ParkingSpace space){
        if(space.getStatus()==SpaceStatus.OCCUPIED){
            throw new BusinessException(ErrorCode.CANNOT_BLOCK_OCCUPIED_SPACE);
        }
        space.setStatus(SpaceStatus.BLOCKED);
    }

    //주차구역 타입 업데이트 (장애인/전기차 중복 설정 불가)
    private void updateZone(ParkingSpace space, boolean isDisabled, boolean isEvCharge){
        space.setIsDisabled(isDisabled);
        space.setIsEvCharge(isEvCharge);
    }
}
