package com.example.demo.domain.user.reservaion.controller;

import com.example.demo.domain.user.auth.principal.PrincipalDetails;
import com.example.demo.domain.user.reservaion.dtos.request.ReservationApplyRequestDto;
import com.example.demo.domain.user.reservaion.dtos.response.ReservationCancelResponseDto;
import com.example.demo.domain.user.reservaion.dtos.response.ReservationDetailResponseDto;
import com.example.demo.domain.user.reservaion.dtos.response.ReservationListResponseDto;
import com.example.demo.domain.user.reservaion.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 입주민용 방문 예약 API 컨트롤러입니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/reservations") // v1 제거 완료
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * [방문 예약 신청]
     * POST /api/user/reservations
     * @param principalDetails 로그인한 유저 정보
     * @param requestDto 차량 번호, 방문 시작 시간, 방문 목적 등
     */
    @PostMapping
    public ResponseEntity<ReservationDetailResponseDto> applyReservation(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody ReservationApplyRequestDto requestDto) {

        // 서비스의 신청 로직 호출 후 결과 반환
        return ResponseEntity.ok(reservationService.applyReservation(principalDetails, requestDto));
    }

    /**
     * [내 방문 예약 목록 조회]
     * GET /api/user/reservations
     * 본인이 신청한 모든 예약 리스트를 조회합니다.
     */
    @GetMapping
    public ResponseEntity<List<ReservationListResponseDto>> getMyReservations(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {

        // 서비스의 조회 로직 호출 후 결과 리스트 반환
        return ResponseEntity.ok(reservationService.getMyReservations(principalDetails));
    }

    /**
     * [방문 예약 취소]
     * PATCH /api/user/reservations/{reservationId}/cancel
     * 특정 예약을 취소 상태로 변경합니다. (데이터 무결성을 위해 PATCH 사용)
     * @param principalDetails 로그인한 유저 정보 (본인 확인용)
     * @param reservationId 취소할 예약의 고유 ID
     */
    @PatchMapping("/{reservationId}/cancel")
    public ResponseEntity<ReservationCancelResponseDto> cancelReservation(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable Long reservationId) {

        // 서비스의 취소 비즈니스 로직(검증, 상태 변경, 로그 등) 실행
        return ResponseEntity.ok(reservationService.cancelReservation(principalDetails, reservationId));
    }
}