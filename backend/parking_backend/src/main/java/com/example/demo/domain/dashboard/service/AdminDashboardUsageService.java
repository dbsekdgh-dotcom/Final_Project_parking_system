package com.example.demo.domain.dashboard.service;

import com.example.demo.domain.dashboard.dtos.response.*;
import com.example.demo.domain.parking.log.repository.ParkingLogRepository;
import com.example.demo.domain.parking.space.repository.ParkingSpaceRepository;
import com.example.demo.domain.payment.enums.PaymentStatus;
import com.example.demo.domain.payment.enums.PaymentType;
import com.example.demo.domain.payment.point.repository.PointLogRepository;
import com.example.demo.domain.payment.repository.PaymentRepository;
import com.example.demo.domain.payment.subscription.repository.SubscriptionRepository;
import com.example.demo.domain.reservation.repository.ReservationRepository;
import com.example.demo.domain.resident.household.repository.HouseholdRepository;
import com.example.demo.domain.system.store.transaction.repository.StoreTicketTransactionRepository;
import com.example.demo.domain.vehicle.VehicleRepository;
import com.example.demo.domain.vehicle.enums.VehicleStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardUsageService {

    private final HouseholdRepository householdRepository;
    private final VehicleRepository vehicleRepository;
    private final ParkingSpaceRepository parkingSpaceRepository;
    private final PaymentRepository paymentRepository;
    private final ParkingLogRepository parkingLogRepository;
    private final StoreTicketTransactionRepository storeTicketTransactionRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ReservationRepository reservationRepository;
    private final PointLogRepository pointLogRepository;

    private List<MonthlyCountDto> buildMonthlyCount(String type, LocalDateTime now){
        List<MonthlyCountDto> result = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("M월");

        for (int i = 5; i >= 0; i--){
            LocalDateTime monthStart = now.minusMonths(1)
                    .withDayOfMonth(1)
                    .withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime monthEnd = monthStart.plusMonths(1);
            String label = monthStart.format(fmt);

            long count = getMonthCount(type, monthStart, monthEnd);
            result.add(new MonthlyCountDto(label, count));
        }
        return result;
    }

    private long getMonthCount(String type, LocalDateTime start, LocalDateTime end){
        return switch (type){
            case "PARKING" -> parkingLogRepository.countExitedBetween(start,end);
            case "TICKET" -> storeTicketTransactionRepository.countUsedBetween(start,end);
            case "SUBSCRIPTION" -> subscriptionRepository.countActivatedBetween(start, end);
            case "RESERVATION" -> reservationRepository.countCompletedBetween(start, end);
            case "POINT" -> pointLogRepository.countUsedBetween(start, end);
            default -> 0L;
        };
    }
    private List<DashboardUsageDetailResponseDto.DetailRow> buildDetailRows(String type, LocalDateTime from, LocalDateTime to){
        List<UsageDailyProjection> projections = switch (type){
            case "PARKING" -> parkingLogRepository.findDailyExitedRows(from,to);
            case "TICKET" -> storeTicketTransactionRepository.findDailyUsedRows(from,to);
            case "SUBSCRIPTION" -> subscriptionRepository.findDailyActivatedRows(from,to);
            case "RESERVATION" -> reservationRepository.findDailyCompletedRows(from,to);
            case "POINT" -> pointLogRepository.findDailyUsedRows(from,to);
            default -> new ArrayList<>();
        };
        return projections.stream()
                .map(p -> new DashboardUsageDetailResponseDto.DetailRow(
                        p.getDate(),
                        p.getCategory(),
                        p.getUsageCount(),
                        p.getTracsactionCount()
                ))
                .collect(Collectors.toList());
    }
    public DashboardSummaryResponseDto getSummary(){
        long housegoldCount = householdRepository.count();
        long vehicleCount = vehicleRepository.countByStatus(VehicleStatus.ACTIVE);
        long parkingSpaceCount = parkingSpaceRepository.count();
        long totalRevenue = paymentRepository
                .sumAmountByPaymentTypeAndStatus(PaymentType.PARKING, PaymentStatus.SUCCESS);

        return new DashboardSummaryResponseDto(housegoldCount,vehicleCount,parkingSpaceCount,totalRevenue);
    }

    public DashboardUsageResponseDto getUsage(String type){
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime sixMonthsAgo = now.minusMonths(5)
                .withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);
        List<MonthlyCountDto> monthly = buildMonthlyCount(type,now);

        long thisMonth = monthly.isEmpty() ? 0 : monthly.get(monthly.size() - 1).getCount();

        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime startOfLastMonth = startOfMonth.minusMonths(1);
        long lastMonth = getMonthCount(type,startOfLastMonth,startOfMonth);

        double changePercent = lastMonth == 0 ? 100.0 : Math.round(((double)(thisMonth - lastMonth) / lastMonth) * 1000) / 10.0;
        return new DashboardUsageResponseDto(thisMonth, changePercent, monthly);
    }

    public DashboardUsageDetailResponseDto getUsageDetail(String type, int page, int size){
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sixMonthsAgo = now.minusMonths(6);

        List<DashboardUsageDetailResponseDto.DetailRow> allRows =
                buildDetailRows(type, sixMonthsAgo, now);

        int totalElements = allRows.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int from = page * size;
        int to = Math.min(from + size, totalElements);

        List<DashboardUsageDetailResponseDto.DetailRow> content =
                from >= totalElements ? new ArrayList<>() : allRows.subList(from, to);

        return new DashboardUsageDetailResponseDto(content, totalPages, totalElements);
    }
}
