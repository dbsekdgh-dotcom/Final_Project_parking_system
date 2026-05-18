package com.example.demo.api.admin.chat;

import com.example.demo.domain.auth.admin.entity.Admin;
import com.example.demo.domain.auth.admin.repository.AdminRepository;
import com.example.demo.domain.chat.dtos.request.SendMessageRequest;
import com.example.demo.domain.chat.dtos.response.ChatMessageResponse;
import com.example.demo.domain.chat.dtos.response.UnreadNotificationResponse;
import com.example.demo.domain.chat.service.AdminChatService;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import com.example.demo.global.security.admin.AdminAuthDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class AdminChatWebSocketController {

    private final AdminChatService chatService;
    private final AdminRepository adminRepository;
    private final SimpMessagingTemplate messagingTemplate;

    //클라이언트가 /app/chat/room/{roomId} 로 메시지 전송 실시간 채팅만 ws
    @MessageMapping("/chat/room/{roomId}")
    public void handleMessage(
            @DestinationVariable Long roomId,
            @Payload SendMessageRequest request,
            Principal principal
            ){
        // Principal에서 loginId 추출 -> adminId 조회
        String loginId = extractLoginId(principal);
        Admin sender = adminRepository.findByLoginId(loginId)
                .orElseThrow(()-> new BusinessException(ErrorCode.ADMIN_NOT_FOUND));
        // DB 저장
        ChatMessageResponse response = chatService.saveMessage(roomId,sender.getAdminId(),request.getContent());
        // 해당 방 구독자 전체에게 브로드캐스트
        messagingTemplate.convertAndSend("/topic/chat/room/" + roomId, response);

        // 방 멤버 개인에게 unread 알림 ( 보낸 사람 제외 )
        List<String> memberLoginIds = chatService.getRoomMemberLoginIds(roomId, sender.getAdminId());
        UnreadNotificationResponse notification = UnreadNotificationResponse.of(roomId,response);
        for (String memberLoginId : memberLoginIds){
            messagingTemplate.convertAndSendToUser(memberLoginId,"/queue/unread", notification);
        }
    }

    private String extractLoginId(Principal principal){
        if (principal instanceof UsernamePasswordAuthenticationToken auth){
            if (auth.getPrincipal() instanceof AdminAuthDto adminAuthDto){
                return adminAuthDto.getUsername();
            }
        }
        throw new BusinessException(ErrorCode.AUTH_INFO_NOT_FOUND);
    }
}
