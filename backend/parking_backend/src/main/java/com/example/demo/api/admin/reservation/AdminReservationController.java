package com.example.demo.api.admin.reservation;

import com.example.demo.domain.auth.admin.dtos.response.ReservationAdminDetailResponseDto;
import com.example.demo.domain.auth.admin.dtos.response.ReservationAdminListResponseDto;
import com.example.demo.domain.reservation.enums.Purpose;
import com.example.demo.domain.reservation.enums.Status;
import com.example.demo.domain.reservation.service.AdminReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/reservations")
@RequiredArgsConstructor
public class AdminReservationController {
    private final AdminReservationService adminReservationService;

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
    @GetMapping("/{reservationId}")
    public ResponseEntity<ReservationAdminDetailResponseDto> getReservation(@PathVariable Long reservationId){
        return ResponseEntity.ok(adminReservationService.getReservation(reservationId));
    }
    @GetMapping("/stats/today")
    public ResponseEntity<Map<String,Long>> getTodayStats(){
        return ResponseEntity.ok(adminReservationService.getTodayStats());
    }
    @PostMapping("/{reservationId}/cancel")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long reservationId){
        adminReservationService.cancelReservation(reservationId);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/{reservationId}/no-show")
    public ResponseEntity<Void> markNoShow(@PathVariable Long reservationId){
        adminReservationService.markNoshow(reservationId);
        return ResponseEntity.ok().build();
    }
}
