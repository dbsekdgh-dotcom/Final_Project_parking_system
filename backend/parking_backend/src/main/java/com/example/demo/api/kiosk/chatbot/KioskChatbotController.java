package com.example.demo.api.kiosk.chatbot;


import com.example.demo.domain.parking.chatbot.dtos.request.ChatbotRequestDto;
import com.example.demo.domain.parking.chatbot.dtos.response.ChatbotResponseDto;
import com.example.demo.domain.parking.chatbot.service.KioskChatbotService;
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
    public Map<String,Object> getFeePolicy(){
        Map<String, Object> result = new HashMap<>();

        List<Map<String, Object>> policies = new ArrayList<>();

        Map<String, Object> visitPolicy = new HashMap<>();
        visitPolicy.put("parking_type", "VISIT");
        visitPolicy.put("label", "외부인");
        visitPolicy.put("turnaround_grace_minutes", 10);
        visitPolicy.put("base_fee", 1000);
        visitPolicy.put("unit_minutes", 10);
        visitPolicy.put("unit_fee", 500);
        visitPolicy.put("daily_max_fee", 20000);

        Map<String, Object> reservationPolicy = new HashMap<>();
        reservationPolicy.put("parking_type", "RESERVATION");
        reservationPolicy.put("label", "방문객");
        reservationPolicy.put("turnaround_grace_minutes", 10);
        reservationPolicy.put("base_fee", 2000);
        reservationPolicy.put("unit_minutes", 10);
        reservationPolicy.put("unit_fee", 500);
        reservationPolicy.put("daily_max_fee", 20000);

        policies.add(visitPolicy);
        policies.add(reservationPolicy);

        Map<String, Object> systemSettings = new HashMap<>();
        systemSettings.put("post_payment_grace_minutes", 15);

        List<Map<String, Object>> discountTickets = new ArrayList<>();

        Map<String, Object> ticket1 = new HashMap<>();
        ticket1.put("name", "1시간 할인권");
        ticket1.put("discount_minutes", 60);
        ticket1.put("price", 1000);

        Map<String, Object> ticket2 = new HashMap<>();
        ticket2.put("name", "2시간 할인권");
        ticket2.put("discount_minutes", 120);
        ticket2.put("price", 2000);

        discountTickets.add(ticket1);
        discountTickets.add(ticket2);

        result.put("policies", policies);
        result.put("system_settings", systemSettings);
        result.put("discount_tickets", discountTickets);
        return result;
    }

}
