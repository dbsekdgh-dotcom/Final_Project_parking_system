package com.example.demo.domain.kiosk.payment.service;

import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

//외부 연동 모듈
@Component
@RequiredArgsConstructor
public class AiServerClient {
    private final RestTemplate restTemplate;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    //중복된 결제 요청인지 확인
    public void checkPaymentLock(String carNumber){
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
}
