package com.example.demo.api.admin.uservehicle;

import com.example.demo.domain.management.dtos.response.*;
import com.example.demo.domain.management.service.AdminManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/management")
@RequiredArgsConstructor
public class UserVehicleController {
    private final AdminManagementService adminManagementService;

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
    @GetMapping("/users/{userId}")
    public ResponseEntity<AdminUserDetailResponseDto> getUserDetail(@PathVariable Long userId){
        return ResponseEntity.ok(adminManagementService.getUserDetail(userId));
    }

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
    @GetMapping("/vehicles/{vehicleId}")
    public ResponseEntity<AdminVehicleDetailResponseDto> getVehicleDetail(@PathVariable Long vehicleId){
        return ResponseEntity.ok(adminManagementService.getVehicleDetail(vehicleId));
    }

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
    @GetMapping("/subscriptions/{subscriptionId}")
    public ResponseEntity<AdminSubscriptionDetailResponseDto> getSubscriptionDetail(@PathVariable Long subscriptionId){
        return ResponseEntity.ok(adminManagementService.getSubscriptionDetail(subscriptionId));
    }
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
    @GetMapping("/reservations/{reservationId}")
    public ResponseEntity<AdminReservationDetailResponseDto> getReservationDetail(@PathVariable Long reservationId){
        return ResponseEntity.ok(adminManagementService.getReservationDetail(reservationId));
    }
}
