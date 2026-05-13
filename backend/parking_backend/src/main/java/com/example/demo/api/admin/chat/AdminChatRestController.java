package com.example.demo.api.admin.chat;

import com.example.demo.domain.auth.admin.entity.Admin;
import com.example.demo.domain.auth.admin.repository.AdminRepository;
import com.example.demo.domain.chat.dtos.request.CreateGroupRoomRequest;
import com.example.demo.domain.chat.dtos.request.CreatedDirectRoomRequest;
import com.example.demo.domain.chat.dtos.response.ChatMessageResponse;
import com.example.demo.domain.chat.dtos.response.ChatRoomResponse;
import com.example.demo.domain.chat.service.AdminChatService;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import com.example.demo.global.security.admin.AdminAuthDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/chat")
public class AdminChatRestController {

    private final AdminChatService chatService;
    private final AdminRepository adminRepository;

    // 1:1 채팅방 생성 및 기존 방 조회
    @PostMapping("/rooms/direct")
    public ResponseEntity<ChatRoomResponse> getOrCreateDirect(
            @AuthenticationPrincipal AdminAuthDto principal,
            @RequestBody CreatedDirectRoomRequest request
            ){
        Long myId = getAdminId(principal.getUsername());
        return ResponseEntity.ok(chatService.getOrCreateDirectRoom(myId,request));
    }
    // 그룹 채팅방 생성
    @PostMapping("/rooms/group")
    public ResponseEntity<ChatRoomResponse> createGroup(
            @AuthenticationPrincipal AdminAuthDto principal,
            @RequestBody CreateGroupRoomRequest request
            ){
        Long myId = getAdminId(principal.getUsername());
        return ResponseEntity.ok(chatService.createGroupRoom(myId, request));
    }
    // 내 채팅방 목록
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomResponse>> getMyRooms(
            @AuthenticationPrincipal AdminAuthDto principal
    ){
        Long myId = getAdminId(principal.getUsername());
        return ResponseEntity.ok(chatService.getMyRooms(myId));
    }
    // 메세지 기록
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getMessages(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ){
        return ResponseEntity.ok(chatService.getMessages(roomId,page,size));
    }
    // 읽음 처리
    @PostMapping("/rooms/{roomId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long roomId,
            @AuthenticationPrincipal AdminAuthDto principal
    ){
        Long myId = getAdminId(principal.getUsername());
        chatService.markAsRead(roomId,myId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/rooms/{roomId}/leave")
    public ResponseEntity<Void> leaveRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal AdminAuthDto principal
    ) {
        Long myId = getAdminId(principal.getUsername());
        chatService.leaveRoom(roomId, myId);
        return ResponseEntity.ok().build();
    }

    private Long getAdminId(String loginId){
        return adminRepository.findByLoginId(loginId)
                .map(Admin::getAdminId)
                .orElseThrow(()-> new BusinessException(ErrorCode.ADMIN_NOT_FOUND));
    }
}
