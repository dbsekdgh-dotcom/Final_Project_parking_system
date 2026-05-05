package com.example.demo.api.kiosk.chatbot;


import com.example.demo.domain.parking.chatbot.dtos.request.ChatbotRequestDto;
import com.example.demo.domain.parking.chatbot.dtos.response.ChatbotResponseDto;
import com.example.demo.domain.parking.chatbot.service.KioskChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kiosk/chatbot")
public class KioskChatbotController {
    private final KioskChatbotService kioskChatbotService;

    @PostMapping("/chat")
    public ChatbotResponseDto chat(@RequestBody ChatbotRequestDto dto){
        return kioskChatbotService.chat(dto);
    }

    @DeleteMapping("/end")
    public void chat(@RequestParam String sessionId){
        kioskChatbotService.deleteChat(sessionId);
    }

}
