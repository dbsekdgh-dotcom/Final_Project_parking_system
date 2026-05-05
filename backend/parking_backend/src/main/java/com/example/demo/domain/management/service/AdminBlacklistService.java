package com.example.demo.domain.management.service;

import com.example.demo.domain.management.dtos.request.AdminBlacklistRequestDto;
import com.example.demo.domain.management.dtos.response.AdminBlacklistDetailResponseDto;
import com.example.demo.domain.management.dtos.response.AdminBlacklistResponseDto;
import com.example.demo.domain.management.dtos.response.AdminReservationDetailResponseDto;
import com.example.demo.domain.vehicle.blacklist.VehicleBlacklistEntity;
import com.example.demo.domain.vehicle.blacklist.enums.BlacklistReasonType;
import com.example.demo.domain.vehicle.blacklist.enums.BlacklistStatus;
import com.example.demo.domain.vehicle.blacklist.repository.VehicleBlacklistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminBlacklistService {

    private  final VehicleBlacklistRepository blacklistRepository;

    //블랙리스트 등록(관리자 수동)
    @Transactional
    public Long register(AdminBlacklistRequestDto requestDto) {

        // 이미 차단된 차량인지 확인
        if (blacklistRepository.existsByCarNumberAndStatus(requestDto.getCarNumber(), BlacklistStatus.ACTIVE)) {
            throw new IllegalArgumentException("이미 차단된 차량입니다.");
        }

        //종료일 설정
        LocalDateTime endDate = requestDto.getEndDate() != null
                ? requestDto.getEndDate() : LocalDateTime.of(3000, 12, 31, 23, 59, 59);

        VehicleBlacklistEntity blacklist = VehicleBlacklistEntity.builder()
                .carNumber(requestDto.getCarNumber())
                .reasonType(requestDto.getReasonType())
                .reasonDetail((requestDto.getReasonDetail()))
                .startDate(LocalDateTime.now())
                .endDate(endDate)
                .status(BlacklistStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
        return blacklistRepository.save(blacklist).getId();
    }

    //블랙리스크 목록 조회
    @Transactional(readOnly = true)
    public Page<AdminBlacklistResponseDto> getBlackList(
            String carNumber,
            BlacklistStatus status,
            BlacklistReasonType reasonType,
            Pageable pageable){


        Page<VehicleBlacklistEntity> result;

        if((carNumber == null || carNumber.isEmpty()) && status == null && reasonType == null) {
            result = blacklistRepository.findAll(pageable);
        }else {
            result = blacklistRepository.findByCarNumberContainingAndStatusAndReasonType(
                    carNumber != null ?carNumber :"",
                    status,
                    reasonType,
                    pageable);
        }

        return result.map(entity -> AdminBlacklistResponseDto.builder()
                .id(entity.getId())
                .carNumber(entity.getCarNumber())
                .reasonType(entity.getReasonType())
                .reasonDetail(entity.getReasonDetail())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .status(entity.getStatus())
                .build());

        }

        //블랙리스트 상세 조회
    @Transactional(readOnly = true)
    public AdminBlacklistDetailResponseDto getDetail(Long id){
        VehicleBlacklistEntity entity = blacklistRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 블랙리스트 기록이 없습니다. ID" + id));

        return AdminBlacklistDetailResponseDto.builder()
                .id(entity.getId())
                .carNumber(entity.getCarNumber())
                .reasonType(entity.getReasonType())
                .reasonDetail(entity.getReasonDetail())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .releasedAt(entity.getReleasedAt())
                .vehicleId(null)
                .vehicleName(null)
                .ownerName(null)
                .ownerEmail(null)
                .build();
        }
    }

