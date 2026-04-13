package com.example.demo.domain.kiosk.payment.service;

import com.example.demo.domain.kiosk.payment.dtos.request.PaymentConfirmRequestDto;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Service
public class TossPaymentService {
    @Value("${TOSS_SECRET_KEY}")
    private String toss;

    //toss secret key 변환
    private String authorization(){
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] encodedBytes = encoder.encode((toss + ":").getBytes(StandardCharsets.UTF_8));
        return  "Basic " + new String(encodedBytes);
    }

    //결제 승인 요청
    public ResponseEntity<JSONObject> confirmPayment(@RequestBody PaymentConfirmRequestDto dto) {
        try {
            //1. 승인시도 : post요청
            String urlStr="https://api.tosspayments.com/v1/payments/confirm";
            JSONObject body = new JSONObject();
            body.put("orderId", dto.getOrderId());
            body.put("amount", dto.getAmount());
            body.put("paymentKey", dto.getPaymentKey());

            return sendRequest(urlStr,"POST",body);

        }catch(IOException e){
            //2. 통신장애 발생이 요청상태 재확인 :get요청
            try {
                String urlStr = "https://api.tosspayments.com/v1/payments/orders/" + dto.getOrderId();
                ResponseEntity<JSONObject> response= sendRequest(urlStr, "GET", null);
                if(response.getBody()!=null && "DONE".equals(response.getBody().get("status"))){
                    return response;
                }
            }catch (Exception ex){
                log.error("최종 조회마저 실패 :{}",dto.getOrderId());
                throw new BusinessException(ErrorCode.PAYMENT_NETWORK_ERROR);
            }
        }catch (ParseException e){
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
    public ResponseEntity<JSONObject> sendRequest(String urlStr, String method,JSONObject body) throws IOException,ParseException{

            URL url=new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Authorization", authorization());
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestMethod(method);
            connection.setConnectTimeout(5000); // 서버 연결 대기 시간 (5초)
            connection.setReadTimeout(10000);    // 응답 읽기 대기 시간 (10초)

            if("POST".equals(method)&& body!=null){
                connection.setDoOutput(true);
                //post형식이면 body보내기
                try(OutputStream outputStream = connection.getOutputStream()){
                    outputStream.write(body.toString().getBytes("UTF-8"));
                }
            }
            int code = connection.getResponseCode();
            boolean isSuccess = (code >= 200 && code < 300);

            try(InputStream responseStream = isSuccess ? connection.getInputStream() : connection.getErrorStream()){
                // 결제 성공 및 실패 비즈니스 로직(try-with-resources로 스트림 자동 종료 보장)
                Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8);
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = (JSONObject) parser.parse(reader);
                return ResponseEntity.status(code).body(jsonObject);
            }
    }


    public ResponseEntity<JSONObject> cancelPayment(String paymentKey,String cancelReason){
        try {
            String urlStr = "https://api.tosspayments.com/v1/payments/" + paymentKey + "/cancel";
            JSONObject body = new JSONObject();
            body.put("cancelReason", cancelReason);
            return sendRequest(urlStr, "POST", body);
        }catch (IOException e){
            throw new BusinessException(ErrorCode.PAYMENT_NETWORK_ERROR);
        }catch (ParseException e){
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

}