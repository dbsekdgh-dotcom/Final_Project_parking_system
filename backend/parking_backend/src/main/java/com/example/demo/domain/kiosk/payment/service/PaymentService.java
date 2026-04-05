package com.example.demo.domain.kiosk.payment.service;

import com.example.demo.domain.kiosk.payment.dtos.request.VehicleExitRequestDto;
import com.example.demo.domain.kiosk.payment.dtos.response.VehicleExitResponseDto;
import com.example.demo.domain.shared.household.repository.HouseholdRepository;
import com.example.demo.domain.shared.parkingfeepolicy.ParkingFeePolicy;
import com.example.demo.domain.shared.parkingfeepolicy.repository.ParkingFeePolicyRepository;
import com.example.demo.domain.shared.parkinglog.ParkingLog;
import com.example.demo.domain.shared.parkinglog.enums.ParkingTypeSnapshot;
import com.example.demo.domain.shared.parkinglog.enums.PaymentStatus;
import com.example.demo.domain.shared.parkinglog.repository.ParkinglogRepository;
import com.example.demo.domain.shared.reservation.enums.Status;
import com.example.demo.domain.shared.reservation.repository.ReservationRepository;
import com.example.demo.domain.shared.subscription.repository.SubscriptionRepository;
import com.example.demo.domain.shared.vehicle.VehicleRepository;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {
    private final RestTemplate restTemplate;
    private final ParkinglogRepository parkinglogRepository;
    private final ReservationRepository reservationRepository;
    private final ParkingFeePolicyRepository parkingFeePolicyRepository;
    private final VehicleRepository vehicleRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final HouseholdRepository householdRepository;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    //중복된 결제 요청인지 확인
    public void startPayment(String carNumber){
        try {
            String url = aiServerUrl + "/api/v1/parking/payment/payment-start";
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("car_number", carNumber);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestBody, Map.class);

            if(response.getStatusCode()== HttpStatus.OK){
                System.out.println("락 성공 응답"+response.getBody());
            }
        }catch (HttpClientErrorException e){
            //파이썬에서 400번대 에러를 응답했을 때
            if(e.getStatusCode()==HttpStatus.CONFLICT){
                // 이미 결제 진행 중인 경우(409)
                throw new BusinessException(ErrorCode.ALREADY_PROCESSING);
            }
            //그 외 잘못된 요청일 경우
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }catch (ResourceAccessException e){
            //파이썬 서버가 꺼져있거나 네트워크 연결이 안될때
            throw new BusinessException(ErrorCode.AI_SERVER_ERROR);
        }catch (Exception e){
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    //타입이 방문인 경우 입차확인&무료요금&방문예약일이 경과했는지 체크
    public boolean isValidReservation(String carNumber){
        int isFree=reservationRepository.getCountbyCarNumber(carNumber, Status.ENTERED, true);
        if(isFree>0){
            return true;
        }
        return false;
    }

    //요금 계산  ///사전정산,할인권은 일요일에...
    public Integer calculateFee(ParkingLog parkingLog,Long parkingFeePolicyId){
        int rawFee;
        int totalDiscountMinutes;
        int totalDiscountAmount;
        int calculatedFee;
        int fee;  //사전정산시 결제한 금액

        //요금 정책이 없는 경우 오류 처리
        ParkingFeePolicy parkingFeePolicy=parkingFeePolicyRepository.findById(parkingFeePolicyId).orElse(null);
        if(parkingFeePolicy==null)throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);

        //현재 주차시간 계산
        LocalDateTime enteredAt=parkingLog.getEnteredAt();
        LocalDateTime exitTime=LocalDateTime.now();
        Long parkingTime=Duration.between(enteredAt,exitTime).toMinutes();

        //요금 계산
        int baseTime=parkingFeePolicy.getGraceMinutes();
        int baseFee=parkingFeePolicy.getBaseFee();
        int unitMinutes=parkingFeePolicy.getUnitMinutes();
        int extraTime=Math.max(0,(int)(parkingTime-baseTime));
        int tempRawFee;
        if(extraTime>0){
            int extraUnit=(int)Math.ceil(extraTime/(double)unitMinutes);
            tempRawFee=extraUnit*parkingFeePolicy.getUnitFee()+baseFee;
        }else{
            tempRawFee=baseFee;
        }

        //일 최대요금 비교
        rawFee=Math.min(tempRawFee,parkingFeePolicy.getDailyMaxFee());

        //사전정산 금액(있는 경우)


        return 0;
    }


    //EXIT_REQUESTED
    public VehicleExitResponseDto requestPayment(VehicleExitRequestDto vehicleExitRequestDto) {

        ParkingLog parkingLog = (ParkingLog) parkinglogRepository.findById(vehicleExitRequestDto.getParkingLogId()).orElseThrow(() -> {
            throw new BusinessException(ErrorCode.VEHICLE_NOT_ENTERED);
        });
        String carNumber = parkingLog.getCarNumberSnapshot();
        PaymentStatus status = parkingLog.getPaymentStatus();
        Long parkingFeePolicyId = parkingLog.getParkingFeePolicyId();
        LocalDateTime freeExitTime = parkingLog.getFreeExitUntil();
        ParkingTypeSnapshot parkingTypeSnapshot = parkingLog.getParkingTypeSnapshot();

        // parking_fee_policy_id가 NULL인 경우 (db가 not null 이지만 한번 더 체크)
        if (parkingFeePolicyId == null) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        //payment_status가 none인 경우(입주민, 정기권 구매자로 입차시 판단)
        if (PaymentStatus.NONE.equals(status)) {
            //데이터 무결성 오류(출차시간을 update하지 않음)
            if(freeExitTime==null)throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
            //회차 차량 처리(입차 후 아주 짧은 시간(예: 10분 내)에 나가는 '회차 차량'의 경우) && 입주민 또는 정기권 차량인 경우
            if (freeExitTime.isAfter(LocalDateTime.now())) {
                return VehicleExitResponseDto.builder().isFree(true).fee(0).message("무료 출차 대상입니다.").build();
            }
        }

        //사전정산 한 경우
        if (PaymentStatus.PAID.equals(status)) {
            //데이터 무결성 오류(출차시간을 update하지 않음)
            if (freeExitTime == null) throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
            //사전정산 후 출차시간 내에 출차하는 경우
            if (freeExitTime.isAfter(LocalDateTime.now())) {
                return VehicleExitResponseDto.builder().isFree(true).fee(0).message("사전정산 완료된 차량입니다.").build();
            }
            //사전정산 후 추가요금 계산

        }

        //중복 정산 요청 방지(사용자가 결제 버튼을 여러 번 누를 경우, 첫 번째 요청이 처리 중일 때 두 번째 요청을 막는 로직)
        startPayment(carNumber);

        //타입이 방문인 경우 입차확인&무료요금&방문예약일이 경과했는지 체크
        if (parkingTypeSnapshot.equals(ParkingTypeSnapshot.RESERVATION)) {
            if (isValidReservation(carNumber)){
                return VehicleExitResponseDto.builder().isFree(true).fee(0).message("방문 예약 차량입니다.").build();
            }
            //방문 요금 적용 계산

        }

        //사전정산 안한 경우 결제금액 계산



        return null;
    }

}

