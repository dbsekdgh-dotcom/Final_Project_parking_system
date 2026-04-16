package com.example.demo.domain.admin.management.parking.service;

import com.example.demo.domain.admin.entity.Admin;
import com.example.demo.domain.admin.entity.AdminActionLog;
import com.example.demo.domain.admin.enums.ActionType;
import com.example.demo.domain.admin.enums.TargetType;
import com.example.demo.domain.admin.management.parking.dtos.request.DiscountModifyRequest;
import com.example.demo.domain.admin.management.parking.dtos.response.AdminTicketPolicyResponse;
import com.example.demo.domain.admin.repository.AdminActionLogRepository;
import com.example.demo.domain.admin.repository.AdminRepository;
import com.example.demo.domain.kiosk.payment.dtos.response.FeeCalculationResponseDto;
import com.example.demo.domain.kiosk.payment.service.PaymentService;
import com.example.demo.domain.shared.activityLog.ActivityLog;
import com.example.demo.domain.shared.activityLog.enums.ActivityType;
import com.example.demo.domain.shared.activityLog.repository.ActivityLogRepository;
import com.example.demo.domain.shared.parkingTicket.ParkingTicket;
import com.example.demo.domain.shared.parkingTicket.repository.ParkingTicketRepository;
import com.example.demo.domain.shared.parkingfeepolicy.ParkingFeePolicy;
import com.example.demo.domain.shared.parkingfeepolicy.repository.ParkingFeePolicyRepository;
import com.example.demo.domain.shared.parkinglog.ParkingLog;
import com.example.demo.domain.shared.parkinglog.dtos.response.ParkingLogDetailResponse;
import com.example.demo.domain.shared.parkinglog.enums.ParkingStatus;
import com.example.demo.domain.shared.parkinglog.enums.ParkingTypeSnapshot;
import com.example.demo.domain.shared.parkinglog.enums.PaymentStatus;
import com.example.demo.domain.shared.parkinglog.repository.ParkingLogRepository;
import com.example.demo.domain.shared.parkinglog.service.ParkingFeeCalculator;
import com.example.demo.domain.shared.parkingspace.enums.SpaceStatus;
import com.example.demo.domain.shared.payment.Payment;
import com.example.demo.domain.shared.payment.repository.PaymentRepository;
import com.example.demo.domain.shared.reservation.repository.ReservationRepository;
import com.example.demo.domain.shared.store.Store;
import com.example.demo.domain.shared.store.repository.StoreRepository;
import com.example.demo.domain.shared.ticketPolicy.TicketPolicy;
import com.example.demo.domain.shared.ticketPolicy.enums.DiscountType;
import com.example.demo.domain.shared.ticketPolicy.enums.Status;
import com.example.demo.domain.shared.ticketPolicy.enums.UseType;
import com.example.demo.domain.shared.ticketPolicy.respository.TicketPolicyRepository;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import com.example.demo.global.security.admin.AdminAuthDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class AdminParkingService {
    private final ParkingLogRepository parkingLogRepository;
    private final AdminRepository adminRepository;
    private final AdminActionLogRepository adminActionLogRepository;
    private final ActivityLogRepository activityLogRepository;
    private final PaymentRepository paymentRepository;
    private final TicketPolicyRepository ticketPolicyRepository;
    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;
    private final ParkingTicketRepository parkingTicketRepository;
    private final ParkingFeePolicyRepository parkingFeePolicyRepository;
    private final StoreRepository storeRepository;
    private final ParkingFeeCalculator parkingFeeCalculator;
    private final ReservationRepository reservationRepository;

    // 관리자용 이면서 현재 활성화된 정책만 가져옴
    public List<AdminTicketPolicyResponse> getAdminTicketPolicies(){
        // UseType이 ADMIN이고 Status가 ACTIVE인 정책만 조회
        return ticketPolicyRepository.findAllByUseTypeAndStatus(UseType.ADMIN, Status.ACTIVE)
                .stream()
                .map(policy -> AdminTicketPolicyResponse.builder()
                        .id(policy.getTicketPolicyId())
                        .name(policy.getName())
                        .discountType(policy.getDiscountType())
                        .discountValue(policy.getDiscountValue())
                        .build())
                .collect(Collectors.toList());
    }

    // 관리자 - 입출차 상세정보 - 할인수정 기능 (할인권 기반)
    public void modifyParkingDiscount(Long parkingLogId, Long ticketPolicyId, String reason, AdminAuthDto adminAuthDto) throws Exception{
        // 관리자,주차로그,할인정책 조회
        Admin currentAdmin = adminRepository.findByLoginId(adminAuthDto.getUsername())
                .orElseThrow(()->new BusinessException(ErrorCode.ADMIN_NOT_FOUND));
        ParkingLog parkingLog = parkingLogRepository.findById(parkingLogId)
                .orElseThrow(()->new BusinessException(ErrorCode.PARKING_LOG_NOT_FOUND));
        TicketPolicy policy = ticketPolicyRepository.findById(ticketPolicyId)
                .orElseThrow(()->new BusinessException(ErrorCode.PARKING_POLICY_NOT_FOUND));
        ParkingFeePolicy feePolicy = parkingFeePolicyRepository.findById(parkingLog.getParkingFeePolicyId())
                .orElseThrow(()->new BusinessException(ErrorCode.PARKING_POLICY_NOT_FOUND));

        //관리자용 정책인지 검증(보안)
        if(policy.getUseType()!= UseType.ADMIN){
            throw new BusinessException("관리자 전용 할인권만 적용 가능합니다.",ErrorCode.INVALID_REQUEST);
        }

        //관리자용 가상 상점 조회
        Store adminStore = storeRepository.findAdminStoreByKeyword("관리", com.example.demo.domain.shared.store.enums.Status.ACTIVE)
                .stream()
                .findFirst()
                .orElseThrow(()->new EntityNotFoundException("'관리'키워드가 포함된 활성 관리자 상점이 존재하지 않습니다."));

        //계산 로직(payment)과 동일하게 과금 시작 시간 보정
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime calculationStartTime = parkingLog.getEnteredAt();
        if(parkingLog.getParkingTypeSnapshot().equals(ParkingTypeSnapshot.RESERVATION)){
            if(reservationRepository.getCountbyCarNumber(parkingLog.getCarNumberSnapshot(), com.example.demo.domain.shared.reservation.enums.Status.ENTERED,true)<=0){
                calculationStartTime = parkingLog.getFreeExitUntil();
            }
        }
        long totalMinutes = Duration.between(calculationStartTime,now).toMinutes();

        //결제 메서드를 사용해 현재 요금 스냅샷 가져오기 [Before]
        FeeCalculationResponseDto calculation = paymentService.settlementFee(parkingLog,parkingLog.getParkingFeePolicyId(),totalMinutes);
        int currentRawFee = calculation.getRawFee();

        // 감사로그에 기록할 Before 스냅샷 생성
        List<ParkingTicket> allTickets = parkingTicketRepository.findAllByParkingLog(parkingLog);
        //상가 할인 합계 계산 [Before]
        int beforeStoreTotal = calculateDiscountSum(allTickets, com.example.demo.domain.shared.parkingTicket.Status.STORE);
        //관리자 할인 합계 계산 [Before]
        int beforeAdminTotal = calculateDiscountSum(allTickets, com.example.demo.domain.shared.parkingTicket.Status.ADMIN);
        //Before 스냅샷 생성
        String beforeData = objectMapper.writeValueAsString(ParkingLogDetailResponse.toDetailDto(parkingLog,beforeStoreTotal,beforeAdminTotal,(long)currentRawFee,calculation.getAmountToPay()));

        // 기존 관리자 할인 티켓(ADMIN타입) 삭제
        parkingTicketRepository.deleteAll(
                allTickets.stream()
                        .filter(t->t.getStatus() == com.example.demo.domain.shared.parkingTicket.Status.ADMIN)
                        .collect(Collectors.toList())
        );
        //새 관리자 할인액 계산(원금 currentRawFee 기준)
        int newAdminDiscountAmount = calculateDiscountByPolicy(policy,feePolicy,currentRawFee,beforeStoreTotal);
        //DB업데이트 및 새 티켓 저장
        parkingLog.setRawFee(currentRawFee);
        parkingLog.updateAdminDiscount(newAdminDiscountAmount,beforeStoreTotal);

        //새 관리자 티켓 저장 (조회해둔 adminStore 사용)
        ParkingTicket adminTicket = ParkingTicket.builder()
                .parkingLog(parkingLog)
                .ticketPolicy(policy)
                .store(adminStore)
                .status(com.example.demo.domain.shared.parkingTicket.Status.ADMIN)
                .appliedAmount(newAdminDiscountAmount)
                .build();
        parkingTicketRepository.save(adminTicket);
        //결제 취소 및 감사로그 저장
        cancelReadyPayments(parkingLog);
        saveAdminActionLog(currentAdmin,parkingLogId,beforeData,reason,policy,newAdminDiscountAmount,parkingLog,beforeStoreTotal,(long)currentRawFee);
        log.info("관리자[{}] 할인 수정 완료: 차량={}, 새관리자할인={}원",currentAdmin.getLoginId(),parkingLog.getCarNumberSnapshot(),newAdminDiscountAmount);
    }

    //DB에 이미 기록된 티켓들의 금액을 상태별로 단순 합산 - 스냅샷 생성 및 기존 할인내역 확인용(단순 합산용)
    private int calculateDiscountSum(List<ParkingTicket> tickets, com.example.demo.domain.shared.parkingTicket.Status status){
        if(tickets == null || tickets.isEmpty()) return 0;

        return tickets.stream()
                .filter(t->t.getStatus() ==status)
                .mapToInt(ParkingTicket::getAppliedAmount)
                .sum();
    }

    //신규 할인액 계산용 - 현재 시점의 요금을 기준으로 계산
    private int calculateDiscountByPolicy(TicketPolicy policy,ParkingFeePolicy feePolicy, int rawFee, int storeTotal){
        //전액 무료(FREE) 정책일 경우: (원금 - 상가할인) 전체를 할인액으로 반환
        if(policy.getDiscountType() == DiscountType.FREE){
            return Math.max(0,rawFee-storeTotal);
        }
        //그 외 타입별 계산
        return switch (policy.getDiscountType()){
            case AMOUNT -> policy.getDiscountValue(); //정액 할인
            case TIME -> {
                //시간 할인
                int units = policy.getDiscountValue() / feePolicy.getUnitMinutes();
                yield units * feePolicy.getUnitFee();
            }
            default -> 0;
        };
    }

    private void cancelReadyPayments(ParkingLog parkingLog) {
        //해당 주차 로그와 연결된 결제 데이터 중 상태가 Ready,Failed인 것들을 모두 찾음
        List<com.example.demo.domain.shared.payment.enums.PaymentStatus> targetStatuses =
                List.of(com.example.demo.domain.shared.payment.enums.PaymentStatus.READY,
                        com.example.demo.domain.shared.payment.enums.PaymentStatus.FAILED);
        List<Payment> targets = paymentRepository.findAllByParkingLogAndPaymentStatusIn(
                parkingLog, targetStatuses
        );
        //루프를 돌며 상태를 CANCELLED로 변경
        targets.forEach(payment -> {
            payment.setPaymentStatus(com.example.demo.domain.shared.payment.enums.PaymentStatus.CANCELLED);
            log.info("기존 결제 요청 무효화 완료 (ID: {})",payment.getPaymentId());
        });
    }

    //관리자 작업 감사로그 저장
    private void saveAdminActionLog(Admin admin,Long targetId,String beforeData, String reason,
                                    TicketPolicy policy,int newAdminDiscount, ParkingLog parkingLog,
                                    int storeTotal, Long currentRawFee) throws Exception{
        //수정 후의 최종 금액 계산
        Long finalPrice = Math.max(0L,currentRawFee-(storeTotal+newAdminDiscount));

        String afterData=objectMapper.writeValueAsString(ParkingLogDetailResponse.toDetailDto(parkingLog,storeTotal,newAdminDiscount,
                currentRawFee,finalPrice));
        Map<String, Object> diff=new HashMap<>();
        diff.put("reason",reason);
        diff.put("appliedPolicyName",policy.getName());
        diff.put("adminDiscountAmount",newAdminDiscount);
        diff.put("totalDiscount",storeTotal+newAdminDiscount);
        diff.put("finalCalculatedFee",finalPrice);

        adminActionLogRepository.save(AdminActionLog.builder()
                .admin(admin)
                .targetType(TargetType.PARKING_LOG)
                .targetId(targetId)
                .actionType(ActionType.UPDATE)
                .beforeData(beforeData)
                .afterData(afterData)
                .changedFields(objectMapper.writeValueAsString(diff))
                .build());
    }

    // 관리자 - 입출차 상세정보 - 강제출차 기능(상태변경)
    public void processForceExit(Long parkingLogId, AdminAuthDto adminAuthDto, String reason) throws Exception{
        //관리자, 주차 로그 조회
        Admin currentAdmin = adminRepository.findByLoginId(adminAuthDto.getUsername())
                .orElseThrow(()->new BusinessException(ErrorCode.ADMIN_NOT_FOUND));
        ParkingLog log = parkingLogRepository.findById(parkingLogId)
                .orElseThrow(()->new BusinessException(ErrorCode.VEHICLE_NOT_ENTERED));
        ParkingFeePolicy feePolicy = parkingFeePolicyRepository.findById(log.getParkingFeePolicyId())
                .orElseThrow(()->new BusinessException(ErrorCode.PARKING_POLICY_NOT_FOUND));
        //상태 검증 : 이미 나간 차량은 안됨
        if (log.getParkingStatus() == ParkingStatus.EXITED || log.getParkingStatus() == ParkingStatus.FORCE_EXITED){
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        LocalDateTime now = LocalDateTime.now();
//      LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        //강제 출차 시점의 요금을 payment메서드로 산출
        long totalMinutes = Duration.between(log.getEnteredAt(),now).toMinutes();
        FeeCalculationResponseDto calculation = paymentService.settlementFee(log,log.getParkingFeePolicyId(),totalMinutes);

        //Before데이터 생성을 위한 기존 정보 합산
        //해당 주차기록에 연결된 모든 할인권 조회
        List<ParkingTicket> tickets = parkingTicketRepository.findAllByParkingLog(log);
        //상가 할인 합계 계산
        int storeSum = calculateDiscountSum(tickets, com.example.demo.domain.shared.parkingTicket.Status.STORE);
        //관리자 할인 합계 계산
        int adminSum = calculateDiscountSum(tickets, com.example.demo.domain.shared.parkingTicket.Status.ADMIN);
        //AdminActionLog를 위한 Before스냅샷: 변경 전 정보를 DTO로 변환 후 JSON 문자열로 저장
        ParkingLogDetailResponse beforeDto = ParkingLogDetailResponse.toDetailDto(log,storeSum,adminSum,(long)calculation.getRawFee(),calculation.getAmountToPay());
        String beforeData= objectMapper.writeValueAsString(beforeDto); //objectMapper: 객체(DTO,Mapper)를 문자열 형태로 변환

        // 시나리오별 처리 (A:사전정산 완료 차량, B:미결제 차량)
        if(log.getPaymentStatus()==PaymentStatus.PAID){
            //case A: 사전정산 완료 차량
            log.updateStatusToForceExit(now);
        }else {
            //case B: 미결제 차량
            //취소해야 할 대상 상태 정의(READY,FAILED)
            cancelReadyPayments(log);
            //강제 출차 시점의 원금을 계산기로 산출
            log.updateForFreeForceExit(calculation.getRawFee(),now);
        }

        //연관 데이터 정리: 주차 공간 해제
        if (log.getParkingSpace()!=null){
            log.getParkingSpace().setStatus(SpaceStatus.AVAILABLE);
            log.getParkingSpace().setLastStatusChangedAt(now);
        }
        //After 스냅샷 생성
        Long afterRawFee = (long) log.getRawFee();
        Long afterFinalPrice = Math.max(0L,afterRawFee - (storeSum + adminSum));
        String afterData = objectMapper.writeValueAsString(ParkingLogDetailResponse.toDetailDto(log,storeSum,adminSum,(long)log.getRawFee(),(long)log.getFee()));

        //관리자 감사 로그 기록 (AdminActionLog)
        saveForceExitActionLog(currentAdmin,parkingLogId,beforeData,afterData,reason,log);

        //차량 활동 로그 기록 (ActivityLog)
        activityLogRepository.save(ActivityLog.builder()
                .parkingLog(log)
                .activityType(ActivityType.ADMIN_FORCE_EXIT)
                .carNumber(log.getCarNumberSnapshot())
                .message("관리자 강제출차:"+reason)
                .build());
    }

    private void saveForceExitActionLog(Admin admin,Long targetId, String beforeData, String afterData, String reason, ParkingLog log) throws Exception{
        Map<String, Object> diff = new HashMap<>();
        diff.put("parkingStatus",log.getParkingStatus());
        diff.put("paymentStatus",log.getPaymentStatus());
        diff.put("reason",reason);
        adminActionLogRepository.save(AdminActionLog.builder()
                .admin(admin)
                .targetType(TargetType.PARKING_LOG)
                .targetId(targetId)
                .actionType(ActionType.UPDATE)
                .beforeData(beforeData)
                .afterData(afterData)
                .changedFields(objectMapper.writeValueAsString(diff))
                .build());
    }
}
