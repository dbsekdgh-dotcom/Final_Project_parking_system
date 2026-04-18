package com.example.demo.domain.payment.service;

import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

//외부 연동 모듈
@Slf4j
@Component
@RequiredArgsConstructor
public class AiServerClient {
    private final RestTemplate restTemplate;

    @Value("${AI_SERVER_URL}")
    private String aiServerUrl;

    //락
    public void lockPayment(String carNumber,String url){
        try {
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("car_number", carNumber);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestBody, Map.class);

            if(response.getStatusCode()== HttpStatus.OK){
                System.out.println("락 성공 응답"+response.getBody());
            }
        }catch (HttpStatusCodeException e){
            log.error("파이썬 응답 코드: {}", e.getStatusCode());
            log.error("파이썬 응답 내용: {}", e.getResponseBodyAsString());
            //파이썬에서 400번대 에러를 응답했을 때
            if(e.getStatusCode().value()==409){
                log.warn("이미 락이 걸려있는 차량입니다. {}",carNumber);
                // 이미 결제 진행 중인 경우(409)
                throw new BusinessException(ErrorCode.ALREADY_PROCESSING);
            }
            //그 외 잘못된 요청일 경우
            throw new BusinessException(ErrorCode.AI_SERVER_ERROR);
        }catch (ResourceAccessException e){
            //파이썬 서버가 꺼져있거나 네트워크 연결이 안될때
            throw new BusinessException(ErrorCode.AI_SERVER_ERROR);
        }catch (Exception e){
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
    public void requestPaymentLock(String carNumber){
        String lockUrl = aiServerUrl + "/api/v1/parking/payment/payment-start";
        lockPayment(carNumber,lockUrl);
    }
    public void requestPaymentLockRelease(String carNumber){
        String lockReleaseUrl =aiServerUrl +"/api/v1/parking/payment/payment-end";
        lockPayment(carNumber,lockReleaseUrl);
    }
}
