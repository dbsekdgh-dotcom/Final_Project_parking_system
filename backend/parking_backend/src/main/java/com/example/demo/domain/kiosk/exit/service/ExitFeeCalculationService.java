package com.example.demo.domain.kiosk.exit.service;

import com.example.demo.domain.kiosk.payment.dtos.request.FeeCalculationRequestDto;
import com.example.demo.domain.kiosk.payment.dtos.response.FeeCalculationResponseDto;
import com.example.demo.domain.kiosk.payment.service.PaymentService;
import com.example.demo.domain.shared.parkingTicket.ParkingTicket;
import com.example.demo.domain.shared.parkingTicket.repository.ParkingTicketRepository;
import com.example.demo.domain.shared.parkingfeepolicy.ParkingFeePolicy;
import com.example.demo.domain.shared.parkingfeepolicy.repository.ParkingFeePolicyRepository;
import com.example.demo.domain.shared.parkinglog.ParkingLog;
import com.example.demo.domain.shared.ticketPolicy.enums.Status;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExitFeeCalculationService {
    private final ParkingFeePolicyRepository parkingFeePolicyRepository;
    private final ParkingTicketRepository parkingTicketRepository;
    private final PaymentService paymentService;

    private static final int MINUTRES_PER_DAY = 1440;

    public FeeCalculationResponseDto settlemnetFee(ParkingLog parkingLog, Long parkingFeePolicyId, Long parkingTime){
        ParkingFeePolicy parkingFeePolicy = parkingFeePolicyRepository.findById(parkingFeePolicyId).orElse(null);
        if (parkingFeePolicy == null) throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);

        List<ParkingTicket> list = parkingTicketRepository.getValidTickets(parkingLog.getParkingLogId(), Status.ACTIVE);
        List<ParkingTicket> discountTickets = list.stream().filter(l->{
            Long totalMinutes = l.getTicketPolicy().getValidDays() * (long) MINUTRES_PER_DAY + l.getTicketPolicy().getValidMinutes();
            LocalDateTime expiryDate = l.getTicketPolicy().getCreatedAt().plusMinutes(totalMinutes);
            return expiryDate.isAfter(LocalDateTime.now());
        }).toList();

        FeeCalculationRequestDto request = FeeCalculationRequestDto.builder()
                .parkingTime(parkingTime)
                .policy(parkingFeePolicy)
                .discountTicketRequestDtos(discountTickets)
                .vehicleNumber(parkingLog.getCarNumberSnapshot())
                .build();
        return paymentService.calculateBaseFee(request);
    }
}
