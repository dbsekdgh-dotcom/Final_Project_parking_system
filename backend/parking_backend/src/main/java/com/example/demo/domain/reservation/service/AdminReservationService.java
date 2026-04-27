package com.example.demo.domain.reservation.service;

import com.example.demo.domain.auth.admin.dtos.response.ReservationAdminDetailResponseDto;
import com.example.demo.domain.auth.admin.dtos.response.ReservationAdminListResponseDto;
import com.example.demo.domain.auth.admin.entity.Admin;
import com.example.demo.domain.auth.admin.enums.ActionType;
import com.example.demo.domain.auth.admin.enums.TargetType;
import com.example.demo.domain.auth.admin.service.AdminActionLogService;
import com.example.demo.domain.reservation.Reservation;
import com.example.demo.domain.reservation.enums.Purpose;
import com.example.demo.domain.reservation.enums.Status;
import com.example.demo.domain.reservation.repository.ReservationRepository;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReservationService {
    private final ReservationRepository reservationRepository;
    private final AdminActionLogService adminActionLogService;

    public Page<ReservationAdminListResponseDto> getReservations(String keyword, Status status, Purpose purpose,
                                                                 LocalDateTime startDate, LocalDateTime endDate, Pageable pageable){
        return reservationRepository
                .findAllForAdminFull(keyword, status, purpose, startDate, endDate, pageable)
                .map(ReservationAdminListResponseDto::new);
    }
    public ReservationAdminDetailResponseDto getReservation(Long reservationId){
        Reservation r =reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
        return new ReservationAdminDetailResponseDto(r);
    }
    public Map<String, Long>getTodayStats(){
        LocalDateTime start = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime end = LocalDateTime.now().with(LocalTime.MAX);
        List<Object[]> rows = reservationRepository.countTodayByStatus(start,end);

        Map<String,Long> stats = new LinkedHashMap<>();
        for (Status s : Status.values()) stats.put(s.name(),0L);

        long total = 0L;
        for (Object[] row : rows){
            String key = ((Status) row[0]).name();
            long count = (Long) row[1];
            stats.put(key,count);
            total += count;
        }
        stats.put("TOTAL",total);
        return stats;
    }
    @Transactional
    public void cancelReservation(Long reservationId){
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(()->new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));

        if (r.getStatus() == Status.COMPLETED
            || r.getStatus() == Status.CANCELLED
            || r.getStatus() == Status.NO_SHOW ){
            throw new BusinessException(ErrorCode.RESERVATION_ALREADY_COMPLETED);
        }
        String before = adminActionLogService.toJson(new ReservationAdminDetailResponseDto(r));
        r.cancel(Status.CANCELLED);
        String after = adminActionLogService.toJson(new ReservationAdminDetailResponseDto(r));

        Admin admin = adminActionLogService.getAdmin();
        adminActionLogService.insertAdminlog(
                admin, ActionType.UPDATE, TargetType.RESERVATION,reservationId,before,after
        );
    }
    @Transactional
    public void markNoshow(Long reservationId){
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(()->new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));

        if (r.getStatus() != Status.PENDING && r.getStatus() != Status.RESERVED){
            throw new BusinessException(ErrorCode.RESERVATION_ALREADY_CANCELLED);
        }
        String before = adminActionLogService.toJson(new ReservationAdminDetailResponseDto(r));
        r.updateStatus(Status.NO_SHOW);
        String after = adminActionLogService.toJson(new ReservationAdminDetailResponseDto(r));

        Admin admin = adminActionLogService.getAdmin();
        adminActionLogService.insertAdminlog(
                admin, ActionType.UPDATE, TargetType.RESERVATION,
                reservationId, before, after);
    }
}
