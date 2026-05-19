package com.example.demo.domain.parking.chatbot.service;

import com.example.demo.domain.parking.chatbot.dtos.response.KioskFeePolicyResponseDto;
import com.example.demo.domain.parking.policy.ParkingFeePolicy;
import com.example.demo.domain.parking.policy.enums.ParkingType;
import com.example.demo.domain.parking.policy.repository.ParkingFeePolicyRepository;
import com.example.demo.domain.payment.ticketpolicy.enums.Status;
import com.example.demo.domain.payment.ticketpolicy.enums.UseType;
import com.example.demo.domain.payment.ticketpolicy.repository.TicketPolicyRepository;
import com.example.demo.domain.system.setting.SettingKey;
import com.example.demo.domain.system.setting.SystemSetting;
import com.example.demo.domain.system.setting.repository.SystemSettingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class KioskInfoService {
    private final ParkingFeePolicyRepository parkingFeePolicyRepository;
    private final TicketPolicyRepository ticketPolicyRepository;
    private final SystemSettingRepository systemSettingRepository;

    public KioskFeePolicyResponseDto getKioskInfo(){
        // 요금 정책
        List<ParkingFeePolicy> list=parkingFeePolicyRepository.findCurrentEffectivePolicy(LocalDateTime.now());
        List<KioskFeePolicyResponseDto.FeePolicyDto> policies=list.stream().map(p-> KioskFeePolicyResponseDto.FeePolicyDto.builder()
                .parkingType(p.getParkingType().name())
                .label(ParkingType.VISIT.equals(p.getParkingType())?"외부인":"방문객")
                .turnaroundGraceMinutes(p.getGraceMinutes())
                .baseFee(p.getBaseFee())
                .unitMinutes(p.getUnitMinutes())
                .unitFee(p.getUnitFee())
                .dailyMaxFee(p.getDailyMaxFee())
                .build()).toList();

        //systemSetting
        int postPaymentGraceMinutes=systemSettingRepository
                .findById(SettingKey.POST_PAYMENT_GRACE_MINUTES.getKey())
                .map(s->Integer.parseInt(s.getSettingValue()))
                .orElse(SettingKey.POST_PAYMENT_GRACE_MINUTES.getDefaultIntValue());

        //ticket policies
        List<KioskFeePolicyResponseDto.DiscountTicketDto> tickets=ticketPolicyRepository
                .findAllByUseTypeAndStatus(UseType.STORE, Status.ACTIVE)
                .stream().map(t-> KioskFeePolicyResponseDto.DiscountTicketDto.builder()
                        .name(t.getName())
                        .discountType(t.getDiscountType().name())
                        .discountAmount(t.getDiscountValue())
                        .price(t.getPrice())
                        .build()).toList();

        return KioskFeePolicyResponseDto.builder()
                .policies(policies)
                .postPaymentGraceMinutes(postPaymentGraceMinutes)
                .discountTickets(tickets)
                .build();
    }
}
