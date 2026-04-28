package com.example.demo.api.admin.reservation;

import com.example.demo.domain.auth.admin.dtos.response.ReservationAdminDetailResponseDto;
import com.example.demo.domain.auth.admin.dtos.response.ReservationAdminListResponseDto;
import com.example.demo.domain.reservation.enums.Purpose;
import com.example.demo.domain.reservation.enums.Status;
import com.example.demo.domain.reservation.service.AdminReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@Tag(name = "11. 방문예약 관리 (Reservation)", description = "방문 예약 목록/상세 조회, 당일 통계, 예약 취소, 노쇼 처리 API")
@RestController
@RequestMapping("/api/admin/reservations")
@RequiredArgsConstructor
public class AdminReservationController {
    private final AdminReservationService adminReservationService;

    @Operation(summary = "예약 목록 조회", description = "키워드·상태·목적·날짜 범위 필터로 방문 예약 목록을 페이징 조회합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @GetMapping
    public ResponseEntity<Page<ReservationAdminListResponseDto>> getReserations(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) Purpose purpose,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
            ){
        return ResponseEntity.ok(
                adminReservationService.getReservations(
                        keyword, status, purpose, startDate, endDate,
                        PageRequest.of(page,size)
                )
        );
    }
    @Operation(summary = "예약 상세 조회", description = "특정 예약의 예약자·차량번호·방문 목적·시간·상태를 반환합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @GetMapping("/{reservationId}")
    public ResponseEntity<ReservationAdminDetailResponseDto> getReservation(@PathVariable Long reservationId){
        return ResponseEntity.ok(adminReservationService.getReservation(reservationId));
    }
    @Operation(summary = "오늘 예약 통계", description = "오늘 예약 건수, 방문 완료 수, 노쇼 수 등 당일 통계를 반환합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @GetMapping("/stats/today")
    public ResponseEntity<Map<String,Long>> getTodayStats(){
        return ResponseEntity.ok(adminReservationService.getTodayStats());
    }
    @Operation(summary = "예약 취소", description = "지정한 예약을 취소 처리합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @PostMapping("/{reservationId}/cancel")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long reservationId){
        adminReservationService.cancelReservation(reservationId);
        return ResponseEntity.ok().build();
    }
    @Operation(summary = "노쇼 처리", description = "지정한 예약을 노쇼(방문 불이행)로 처리합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @PostMapping("/{reservationId}/no-show")
    public ResponseEntity<Void> markNoShow(@PathVariable Long reservationId){
        adminReservationService.markNoshow(reservationId);
        return ResponseEntity.ok().build();
    }
}
