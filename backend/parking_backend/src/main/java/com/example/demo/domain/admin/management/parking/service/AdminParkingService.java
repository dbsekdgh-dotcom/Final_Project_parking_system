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
import com.example.demo.domain.shared.parkinglog.enums.PaymentStatus;
import com.example.demo.domain.shared.parkinglog.repository.ParkingLogRepository;
import com.example.demo.domain.shared.parkingspace.enums.SpaceStatus;
import com.example.demo.domain.shared.payment.Payment;
import com.example.demo.domain.shared.payment.repository.PaymentRepository;
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

    // 상가/관리자 타입별 할인 합계 계산
    private int calculateTotalDiscountByUseType(List<ParkingTicket> tickets, UseType useType, ParkingLog parkingLog){
        return tickets.stream()
                .filter(t->t.getTicketPolicy().getUseType()==useType)
                .mapToInt(t->calculatedDiscountByPolicy(t.getTicketPolicy(),parkingLog))
                .sum();
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
        //관리자용 가상 상점 조회
        Store adminStore = storeRepository.findAdminStoreByKeyword("관리", com.example.demo.domain.shared.store.enums.Status.ACTIVE)
                .stream()
                .findFirst()
                .orElseThrow(()->new EntityNotFoundException("'관리'키워드가 포함된 활성 관리자 상점이 존재하지 않습니다."));
        //관리자용 정책인지 검증(보안)
        if(policy.getUseType()!= UseType.ADMIN){
            throw new BusinessException("관리자 전용 할인권만 적용 가능합니다.",ErrorCode.INVALID_REQUEST);
        }
        // 감사로그에 기록할 Before 스냅샷 생성
        List<ParkingTicket> allTickets = parkingTicketRepository.findAllByParkingLog(parkingLog);
        //상가 할인 합계 계산 [Before]
        int beforeStoreTotal = calculateTotalDiscountByUseType(allTickets,UseType.STORE,parkingLog);
        //관리자 할인 합계 계산 [Before]
        int beforeAdminTotal = calculateTotalDiscountByUseType(allTickets,UseType.ADMIN,parkingLog);
        ParkingLogDetailResponse response = ParkingLogDetailResponse.toDetailDto(parkingLog,beforeStoreTotal,beforeAdminTotal);
        String beforeData = objectMapper.writeValueAsString(response);
        // 기존 관리자 할인 티켓(ADMIN타입) 삭제
        List<ParkingTicket> oldAdminTickets = allTickets.stream()
                .filter(t->t.getTicketPolicy().getUseType()==UseType.ADMIN)
                .collect(Collectors.toList());
        parkingTicketRepository.deleteAll(oldAdminTickets);
        //새로운 관리자 할인 정책 금액 계산
        int newAdminDiscountAmount = calculatedDiscountByPolicy(policy,parkingLog);
        //새 관리자 금액과 기존 상가 합계 전달
        parkingLog.updateAdminDiscount(newAdminDiscountAmount,beforeStoreTotal);
        //새 관리자 ParkingTicket 기록 저장
        ParkingTicket adminTicket = ParkingTicket.builder()
                .parkingLog(parkingLog)
                .ticketPolicy(policy)
                .store(adminStore)
                .status(com.example.demo.domain.shared.parkingTicket.Status.ADMIN)
                .appliedAmount(newAdminDiscountAmount)
                .build();
        parkingTicketRepository.save(adminTicket);

        // 기존 READY상태 결제 요청 무효화 처리
        paymentRepository.findAllByParkingLogAndPaymentStatus(parkingLog, com.example.demo.domain.shared.payment.enums.PaymentStatus.READY)
                .forEach(payment -> {
                    payment.setPaymentStatus(com.example.demo.domain.shared.payment.enums.PaymentStatus.CANCELLED);
                });
        // 감사로그에 기록할 After 스냅샷 생성
        saveAdminActionLog(currentAdmin,parkingLogId,beforeData,reason,policy,newAdminDiscountAmount,parkingLog,beforeStoreTotal);
        log.info("관리자[{}] 할인 수정 완료: 차량={}, 상가할인={}원, 기존관리자할인={}원 -> 새관리자할인={}원"
                ,currentAdmin.getLoginId(),parkingLog.getCarNumberSnapshot(),beforeStoreTotal,beforeAdminTotal,newAdminDiscountAmount);

    }

    //정책 타입에 따른 할인 금액 계산 로직
    public int calculatedDiscountByPolicy(TicketPolicy policy,ParkingLog parkingLog){
        //무료(FREE)타입일 경우: 현재 남은 금액(calculatedFee)만큼만 할인액으로 반환
        if(policy.getDiscountType()== DiscountType.FREE){
            return parkingLog.getCalculatedFee().intValue();
        }
        //시간(TIME) 또는 금액(AMOUNT)타입일 경우
        switch (policy.getDiscountType()){
            case AMOUNT : return policy.getDiscountValue(); //정액 할인
            case TIME : //시간당 요금 정책에 따라 계산
                ParkingFeePolicy feePolicy = parkingFeePolicyRepository.findById(parkingLog.getParkingFeePolicyId())
                        .orElseThrow(()->new BusinessException(ErrorCode.PARKING_POLICY_NOT_FOUND));
                if (feePolicy.getUnitMinutes()==null || feePolicy.getUnitMinutes()<=0){
                    log.error("잘못된 요금 정책 설정: 정책 ID {}의 unitMinutes가 0 이하입니다.", feePolicy.getId());
                    throw new BusinessException("시스템 요금 설정 오류: 단위 시간이 0분으로 설정되었습니다. 관리자에게 문의하세요.",
                            ErrorCode.INVALID_REQUEST);
                }
                // 단위 시간(unitMinutes)과 단위 요금(unitFee)을 사용하여 계산
                // 예: 10분당 500원 정책일 때, 60분 할인권이면 (60 / 10) * 500 = 3,000원
                int units = policy.getDiscountValue() / feePolicy.getUnitMinutes();
                return units * feePolicy.getUnitFee();
            default:return 0;
        }
    }

    //관리자 작업 감사로그 저장
    private void saveAdminActionLog(Admin admin,Long targetId,String beforeData, String reason,
                                    TicketPolicy policy,int newAdminDiscount, ParkingLog parkingLog, int storeTotal) throws Exception{
        String afterData=objectMapper.writeValueAsString(ParkingLogDetailResponse.toDetailDto(parkingLog,storeTotal,newAdminDiscount));
        Map<String, Object> diff=new HashMap<>();
        diff.put("reason",reason);
        diff.put("appliedPolicyName",policy.getName());
        diff.put("adminDiscountAmount",newAdminDiscount);
        diff.put("totalDiscount",parkingLog.getTotalDiscountAmount());
        diff.put("finalCalculatedFee",parkingLog.getCalculatedFee());

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
        //상태 검증 : 이미 나간 차량은 안됨
        if (log.getParkingStatus() == ParkingStatus.EXITED || log.getParkingStatus() == ParkingStatus.FORCE_EXITED){
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        //해당 주차기록에 연결된 모든 할인권 조회
        List<ParkingTicket> tickets = parkingTicketRepository.findAllByParkingLog(log);
        //상가 할인 합계 계산
        int storeSum = calculateTotalDiscountByUseType(tickets,UseType.STORE,log);
        //관리자 할인 합계 계산
        int adminSum = calculateTotalDiscountByUseType(tickets,UseType.ADMIN,log);

        //AdminActionLog를 위한 Before스냅샷: 변경 전 정보를 DTO로 변환 후 JSON 문자열로 저장
        ParkingLogDetailResponse beforeDto = ParkingLogDetailResponse.toDetailDto(log,storeSum,adminSum);
        String beforeData= objectMapper.writeValueAsString(beforeDto); //objectMapper: 객체(DTO,Mapper)를 문자열 형태로 변환
        LocalDateTime now = LocalDateTime.now();

        // 시나리오별 처리 (A:사전정산 완료 차량, B:미결제 차량)
        if(log.getPaymentStatus()==PaymentStatus.PAID){
            //case A: 사전정산 완료 차량
            log.updateStatusToForceExit(now);
        }else {
            //case B: 미결제 차량
            //취소해야 할 대상 상태 정의(READY,FAILED)
            List<com.example.demo.domain.shared.payment.enums.PaymentStatus> targetStatuses = List.of(
                    com.example.demo.domain.shared.payment.enums.PaymentStatus.READY,
                    com.example.demo.domain.shared.payment.enums.PaymentStatus.FAILED);
            //한번에 조회해서 모두 CANCELLED로 변경
            paymentRepository.findAllByParkingLogAndPaymentStatusIn(log,targetStatuses)
                    .forEach(p-> {
                        p.setPaymentStatus(com.example.demo.domain.shared.payment.enums.PaymentStatus.CANCELLED);
                    });

            long parkingTime = Math.max(0, Duration.between(log.getEnteredAt(),now).toMinutes());
            FeeCalculationResponseDto feeResult = paymentService.settlementFee(log,log.getParkingFeePolicyId(),parkingTime);

            log.updateForFreeForceExit(feeResult.getRawFee(),now);
        }

        //연관 데이터 정리: 주차 공간 해제
        if (log.getParkingSpace()!=null){
            log.getParkingSpace().setStatus(SpaceStatus.AVAILABLE);
            log.getParkingSpace().setLastStatusChangedAt(now);
        }

        //관리자 할인 합계 재계산
        int finalAdminSum = calculateTotalDiscountByUseType(tickets,UseType.ADMIN,log);

        //AdminActionLog를 위한 After스냅샷
        String afterData = objectMapper.writeValueAsString(ParkingLogDetailResponse.toDetailDto(log,storeSum,finalAdminSum));

        //바뀐값 요약 (Changed Fields)
        Map<String,Object> diff = new HashMap<>();
        diff.put("parkingStatus",log.getParkingStatus());
        diff.put("paymentStatus",log.getPaymentStatus());
        diff.put("reason",reason);

        //관리자 감사 로그 기록 (AdminActionLog)
        adminActionLogRepository.save(AdminActionLog.builder()
                .admin(currentAdmin)
                .targetType(TargetType.PARKING_LOG)
                .targetId(parkingLogId)
                .actionType(ActionType.UPDATE)
                .beforeData(beforeData)
                .afterData(afterData)
                .changedFields(objectMapper.writeValueAsString(diff))
                .build());

        //차량 활동 로그 기록 (ActivityLog)
        activityLogRepository.save(ActivityLog.builder()
                .parkingLog(log)
                .activityType(ActivityType.ADMIN_FORCE_EXIT)
                .carNumber(log.getCarNumberSnapshot())
                .message("관리자 강제출차:"+reason)
                .build());
    }
}
