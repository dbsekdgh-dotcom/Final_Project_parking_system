package com.example.demo.api.kiosk.chatbot;


import com.example.demo.domain.parking.chatbot.dtos.request.ChatbotRequestDto;
import com.example.demo.domain.parking.chatbot.dtos.response.ChatbotResponseDto;
import com.example.demo.domain.parking.chatbot.dtos.response.KioskFeePolicyResponseDto;
import com.example.demo.domain.parking.chatbot.service.KioskChatbotService;
import com.example.demo.domain.parking.chatbot.service.KioskInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "7. 키오스크 챗봇 (Kiosk Chatbot)", description = "키오스크 AI 챗봇 질의 및 세션 종료 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kiosk/chatbot")
public class KioskChatbotController {
    private final KioskChatbotService kioskChatbotService;
    private final KioskInfoService kioskInfoService;

    @Operation(summary = "챗봇 질의", description = "키오스크에서 사용자의 질문을 AI 서버로 전달하고 응답을 반환합니다.")
    @PostMapping("/chat")
    public ChatbotResponseDto chat(@RequestBody ChatbotRequestDto dto){
        return kioskChatbotService.chat(dto);
    }

    @Operation(summary = "챗봇 세션 종료", description = "세션 ID에 해당하는 챗봇 대화 기록을 삭제합니다.")
    @DeleteMapping("/end")
    public void chat(@RequestParam String sessionId){
        kioskChatbotService.deleteChat(sessionId);
    }

    @Operation(summary = "챗봇 요금 정책 및 할인권 정책 조회", description = "주차요금 정책 및 할인권 정책을 조회하여 반환합니다.")
    @GetMapping("/fee_policy")
    public KioskFeePolicyResponseDto getFeePolicy(){
        return kioskInfoService.getKioskInfo();
    }

}
