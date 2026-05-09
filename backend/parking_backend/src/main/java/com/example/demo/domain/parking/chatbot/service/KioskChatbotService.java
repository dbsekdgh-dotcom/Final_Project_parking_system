package com.example.demo.domain.parking.chatbot.service;

import com.example.demo.domain.parking.chatbot.dtos.request.ChatbotRequestDto;
import com.example.demo.domain.parking.chatbot.dtos.response.ChatbotResponseDto;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KioskChatbotService {
    private final RestTemplate restTemplate;

    @Value("${AI_SERVER_URL}")
    private String aiServerUrl;
    public ChatbotResponseDto chat(ChatbotRequestDto dto){
        // 질문이 없는 경우
        if(dto==null || !StringUtils.hasText(dto.getUserQuestion())) throw  new BusinessException(ErrorCode.INVALID_REQUEST);
        //session_id 없으면 생성
        String sessionId= StringUtils.hasText(dto.getSessionId()) ?dto.getSessionId():UUID.randomUUID().toString();
        // llm 요청
        String url=aiServerUrl+"/api/v1/kiosk/chat";
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("session_id", sessionId);
        requestBody.put("screen_id", StringUtils.hasText(dto.getScreenId())?dto.getScreenId():"");
        requestBody.put("user_question",dto.getUserQuestion());
        ResponseEntity<Map> response=kioskAiRequest(requestBody,url);
        // 응답
        Map body=response.getBody();
        if(body==null || body.get("answer")==null) throw new BusinessException(ErrorCode.AI_SERVER_ERROR);
        // 반환
        return  ChatbotResponseDto.builder().answer(body.get("answer").toString()).sessionId(sessionId).build();
    }
    public void deleteChat(String sessionId){
        //session_id가 없는 경우
        if (!StringUtils.hasText(sessionId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        String url= UriComponentsBuilder
                .fromUriString(aiServerUrl+"/api/v1/kiosk/end")
                .queryParam("session_id",sessionId)
                .toUriString();
        deleteChatRequest(url);
    }

    private ResponseEntity<Map> kioskAiRequest(Map<String, String> requestBody, String url){
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestBody, Map.class);
            return response;
        }catch (HttpStatusCodeException e){
            //잘못된 요청일 경우
            throw new BusinessException(ErrorCode.AI_SERVER_ERROR);
        }catch (ResourceAccessException e){
            //파이썬 서버가 꺼져있거나 네트워크 연결이 안될때
            throw new BusinessException(ErrorCode.AI_SERVER_ERROR);
        }catch (Exception e){
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
    private void deleteChatRequest(String url){
        try {
            restTemplate.delete(url);
        }catch (HttpStatusCodeException e){
            //잘못된 요청일 경우
            throw new BusinessException(ErrorCode.AI_SERVER_ERROR);
        }catch (ResourceAccessException e){
            //파이썬 서버가 꺼져있거나 네트워크 연결이 안될때
            throw new BusinessException(ErrorCode.AI_SERVER_ERROR);
        }catch (Exception e){
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
