package com.example.demo.domain.management.service;

import com.example.demo.domain.approval.Approval;
import com.example.demo.domain.approval.repository.ApprovalRepository;
import com.example.demo.domain.management.dtos.response.*;
import com.example.demo.domain.parking.log.ParkingLog;
import com.example.demo.domain.parking.log.repository.ParkingLogRepository;
import com.example.demo.domain.payment.subscription.Subscription;
import com.example.demo.domain.payment.subscription.repository.SubscriptionRepository;
import com.example.demo.domain.reservation.Reservation;
import com.example.demo.domain.reservation.repository.ReservationRepository;
import com.example.demo.domain.resident.User;
import com.example.demo.domain.resident.UserRepository;
import com.example.demo.domain.vehicle.Vehicle;
import com.example.demo.domain.vehicle.VehicleRepository;
import com.example.demo.domain.vehicle.enums.VehicleStatus;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminManagementService {
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final ApprovalRepository approvalRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ReservationRepository reservationRepository;
    private final ParkingLogRepository parkingLogRepository;

    //헬퍼
    private String blankToNull(String s){
        return (s == null || s.isBlank() ? null : s);
    }
    //사용자 목록
    public Page<AdminUserResponseDto> getUsers(
            String keyword, String status, Boolean isResident, Pageable pageable) {

        com.example.demo.domain.resident.enums.Status userStatus = status != null
                ? com.example.demo.domain.resident.enums.Status.valueOf(status) : null;

        return userRepository.findAllForAdmin(blankToNull(keyword), userStatus, isResident, pageable)
                .map(AdminUserResponseDto::from);
    }
    //사용자 상세
    public AdminUserDetailResponseDto getUserDetail(Long userId){
        User user = userRepository.findById(userId).orElseThrow(()-> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<AdminVehicleResponseDto> vehicles = vehicleRepository.findByUser_UserId(userId)
                .stream()
                .map(AdminVehicleResponseDto::from)
                .toList();
        return AdminUserDetailResponseDto.from(user, vehicles);
    }
    //차량 목록
    public Page<AdminVehicleResponseDto> getVehicles(String keyword, String status, Pageable pageable){
        VehicleStatus vehicleStatus = status != null ? VehicleStatus.valueOf(status) : null;

        return vehicleRepository.findAllForAdmin(blankToNull(keyword), vehicleStatus, pageable)
                .map(AdminVehicleResponseDto::from);
    }
    //차량 상세
    public AdminVehicleDetailResponseDto getVehicleDetail(Long vehicleId){
        Vehicle vehicle = vehicleRepository.findByIdWithUser(vehicleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.VEHICLE_NOT_FOUND));

        List<Approval> history = approvalRepository.findVehicleRegistrationHistory(vehicleId);
        List<AdminVehicleDetailResponseDto.RegistrationHistoryDto> historyDtos =
                history.stream().map(AdminVehicleDetailResponseDto.RegistrationHistoryDto::from).toList();

        Optional<ParkingLog> currentParking = parkingLogRepository.findCurrentParkingByCarNumber(vehicle.getCarNumber());

        Optional<LocalDateTime> subEndDate = subscriptionRepository.findActiveSubscriptionEndDate(vehicleId);

        return AdminVehicleDetailResponseDto.builder()
                .vehicleId(vehicle.getId())
                .carNumber(vehicle.getCarNumber())
                .vehicleName(vehicle.getVehicleName())
                .status(vehicle.getStatus().name())
                .createdAt(vehicle.getCreatedAt())
                .deletedAt(vehicle.getDeletedAt())
                .ownerName(vehicle.getUser() != null ? vehicle.getUser().getName() : "소유자 없음")
                .ownerEmail(vehicle.getUser() != null ? vehicle.getUser().getEmail() : null)
                .ownerPhone(vehicle.getUser() != null ? vehicle.getUser().getPhone() : null)
                .registrationHistory(historyDtos)
                .hasReRegistration(history.size() > 1)
                .currentlyParked(currentParking.isPresent())
                .parkedSince(currentParking.map(ParkingLog::getEnteredAt).orElse(null))
                .spaceCode(currentParking
                        .map(p -> p.getParkingSpace() != null ? p.getParkingSpace().getSpaceCode() : null)
                        .orElse(null))
                .hasActiveSubscription(subEndDate.isPresent())
                .subscriptionEndDate(subEndDate.orElse(null))
                .build();
    }
    // 정기권 목록
    public Page<AdminSubscriptionResponseDto> getSubscriptions( String keyword, String status, Pageable pageable){
        com.example.demo.domain.payment.subscription.enums.Status subStatus = status != null ? com.example.demo.domain.payment.subscription.enums.Status.valueOf(status) : null;

        return subscriptionRepository.findAllForAdmin(blankToNull(keyword), subStatus, pageable).map(AdminSubscriptionResponseDto::from);
    }
    // 정기권 상세
    public AdminSubscriptionDetailResponseDto getSubscriptionDetail(Long subscriptionId){
        Subscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(()->new BusinessException(ErrorCode.SUBSCRIPTION_NOT_FOUND));
        return AdminSubscriptionDetailResponseDto.from(sub);
    }
    // 방문예약 목록
    public Page<AdminReservationResponseDto> getReservations( String keyword, String status, Pageable pageable){
        com.example.demo.domain.reservation.enums.Status resStatus = status != null ? com.example.demo.domain.reservation.enums.Status.valueOf(status) : null;

        return reservationRepository.findAllForAdmin(blankToNull(keyword),resStatus,pageable)
                .map(AdminReservationResponseDto::from);
    }
    // 방문예약 상세
    public AdminReservationDetailResponseDto getReservationDetail(Long reservationId){
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(()->new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));

        return AdminReservationDetailResponseDto.from(reservation);
    }
}
