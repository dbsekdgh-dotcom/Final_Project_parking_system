package com.example.demo.api.admin.uservehicle;

import com.example.demo.domain.management.dtos.response.*;
import com.example.demo.domain.management.service.AdminManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "10. 사용자·차량 관리 (Management)", description = "사용자·차량·정기권·방문예약 목록 및 상세 조회 API")
@RestController
@RequestMapping("/api/admin/management")
@RequiredArgsConstructor
public class UserVehicleController {
    private final AdminManagementService adminManagementService;

    @Operation(summary = "사용자 목록 조회", description = "키워드·상태·거주자 여부 필터로 사용자 목록을 페이징 조회합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    // 유저
    @GetMapping("/users")
    public ResponseEntity<Page<AdminUserResponseDto>> getusers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean isResident,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return ResponseEntity.ok(
                adminManagementService.getUsers(keyword, status, isResident, PageRequest.of(page,size))
        );
    }
    @Operation(summary = "사용자 상세 조회", description = "특정 사용자의 기본 정보, 차량, 정기권, 예약 내역을 반환합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @GetMapping("/users/{userId}")
    public ResponseEntity<AdminUserDetailResponseDto> getUserDetail(@PathVariable Long userId){
        return ResponseEntity.ok(adminManagementService.getUserDetail(userId));
    }

    @Operation(summary = "차량 목록 조회", description = "키워드·상태 필터로 등록된 차량 목록을 페이징 조회합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    //차량
    @GetMapping("/vehicles")
    public ResponseEntity<Page<AdminVehicleResponseDto>> getVehicles(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return ResponseEntity.ok(
                adminManagementService.getVehicles(keyword,status,PageRequest.of(page,size))
        );
    }
    @Operation(summary = "차량 상세 조회", description = "특정 차량의 등록 정보, 소유자, 주차 이력을 반환합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @GetMapping("/vehicles/{vehicleId}")
    public ResponseEntity<AdminVehicleDetailResponseDto> getVehicleDetail(@PathVariable Long vehicleId){
        return ResponseEntity.ok(adminManagementService.getVehicleDetail(vehicleId));
    }

    @Operation(summary = "정기권 목록 조회", description = "키워드·상태 필터로 정기권 목록을 페이징 조회합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    // 정기권
    @GetMapping("/subscriptions")
    public ResponseEntity<Page<AdminSubscriptionResponseDto>> getSubscription(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return ResponseEntity.ok(adminManagementService.getSubscriptions(keyword,status,PageRequest.of(page,size)));
    }
    @Operation(summary = "정기권 상세 조회", description = "특정 정기권의 사용자, 차량, 유효 기간, 상태 정보를 반환합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @GetMapping("/subscriptions/{subscriptionId}")
    public ResponseEntity<AdminSubscriptionDetailResponseDto> getSubscriptionDetail(@PathVariable Long subscriptionId){
        return ResponseEntity.ok(adminManagementService.getSubscriptionDetail(subscriptionId));
    }
    @Operation(summary = "방문예약 목록 조회", description = "키워드·상태 필터로 방문 예약 목록을 페이징 조회합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    // 방문예약
    @GetMapping("/reservations")
    public ResponseEntity<Page<AdminReservationResponseDto>> getReservations(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return ResponseEntity.ok(adminManagementService.getReservations(keyword,status,PageRequest.of(page,size)));
    }
    @Operation(summary = "방문예약 상세 조회", description = "특정 방문 예약의 예약자, 차량번호, 방문 목적, 상태 등을 반환합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @GetMapping("/reservations/{reservationId}")
    public ResponseEntity<AdminReservationDetailResponseDto> getReservationDetail(@PathVariable Long reservationId){
        return ResponseEntity.ok(adminManagementService.getReservationDetail(reservationId));
    }
}
