package com.example.demo.domain.user.reservation.controller;

import com.example.demo.domain.user.auth.principal.PrincipalDetails;
import com.example.demo.domain.user.reservation.dtos.request.ReservationApplyRequestDto;
import com.example.demo.domain.user.reservation.dtos.response.ReservationCancelResponseDto;
import com.example.demo.domain.user.reservation.dtos.response.ReservationDetailResponseDto;
import com.example.demo.domain.user.reservation.dtos.response.ReservationEventPolicyResponseDto;
import com.example.demo.domain.user.reservation.dtos.response.ReservationListResponseDto;
import com.example.demo.domain.user.reservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;

@Tag(name = "5. 방문 예약 (Reservation)", description = "입주민 방문 차량 예약 신청, 조회, 수정, 취소 및 예약 정책 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(summary = "방문 예약 신청", description = "방문 차량 번호와 방문 일시를 입력해 예약을 신청합니다. 시스템 정책(일별/월별 한도)에 따라 신청이 제한될 수 있습니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @PostMapping
    public ResponseEntity<ReservationDetailResponseDto> applyReservation(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Valid @RequestBody ReservationApplyRequestDto requestDto) {

        // 서비스의 신청 로직 호출 후 결과 반환
        return ResponseEntity.ok(reservationService.applyReservation(principalDetails, requestDto));
    }

    @Operation(summary = "내 방문 예약 목록 조회", description = "본인이 신청한 전체 방문 예약 목록을 반환합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @GetMapping
    public ResponseEntity<List<ReservationListResponseDto>> getMyReservations(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {

        // 서비스의 조회 로직 호출 후 결과 리스트 반환
        return ResponseEntity.ok(reservationService.getMyReservations(principalDetails));
    }

    @Operation(summary = "방문 예약 취소", description = "특정 예약을 취소합니다. 방문 당일 또는 이미 입차된 예약은 취소가 불가합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @PatchMapping("/{reservationId}/cancel")
    public ResponseEntity<ReservationCancelResponseDto> cancelReservation(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable Long reservationId) {

        // 서비스의 취소 비즈니스 로직(검증, 상태 변경, 로그 등) 실행
        return ResponseEntity.ok(reservationService.cancelReservation(principalDetails, reservationId));
    }

    @Operation(summary = "예약 정책 및 날짜별 잔여 현황 조회", description = "달력에서 날짜 선택 시 해당 날짜의 예약 가능 여부, 잔여 슬롯, 단지 정책(일/월 한도)을 반환합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @GetMapping("/policy")
    public ResponseEntity<ReservationEventPolicyResponseDto> getReservationPolicy(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestParam("targetDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetDate) {
        return ResponseEntity.ok(reservationService.getReservationPolicyInfo(principalDetails, targetDate));
    }

    @Operation(summary = "방문 예약 수정", description = "대기(PENDING) 상태인 예약의 차량번호·방문 일시를 수정합니다. 이미 승인되거나 처리 중인 예약은 수정 불가합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @PutMapping("/{reservationId}")
    public ResponseEntity<ReservationDetailResponseDto> updateReservation(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable Long reservationId,
            @Valid @RequestBody ReservationApplyRequestDto reservationApplyRequestDto) {

        return ResponseEntity.ok(reservationService.updateReservation(principalDetails, reservationId, reservationApplyRequestDto));
    }

}