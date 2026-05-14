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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "20. 관리자 채팅 (Admin Chat)", description = "1:1·그룹 채팅방 생성·조회, 메시지 기록, 읽음 처리, 방 나가기 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/chat")
public class AdminChatRestController {

    private final AdminChatService chatService;
    private final AdminRepository adminRepository;

    @Operation(summary = "1:1 채팅방 생성 또는 조회", description = "상대 관리자와의 1:1 채팅방이 없으면 생성하고, 있으면 기존 방을 반환합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @PostMapping("/rooms/direct")
    public ResponseEntity<ChatRoomResponse> getOrCreateDirect(
            @AuthenticationPrincipal AdminAuthDto principal,
            @RequestBody CreatedDirectRoomRequest request
            ){
        Long myId = getAdminId(principal.getUsername());
        return ResponseEntity.ok(chatService.getOrCreateDirectRoom(myId,request));
    }
    @Operation(summary = "그룹 채팅방 생성", description = "여러 관리자를 초대하여 그룹 채팅방을 생성합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @PostMapping("/rooms/group")
    public ResponseEntity<ChatRoomResponse> createGroup(
            @AuthenticationPrincipal AdminAuthDto principal,
            @RequestBody CreateGroupRoomRequest request
            ){
        Long myId = getAdminId(principal.getUsername());
        return ResponseEntity.ok(chatService.createGroupRoom(myId, request));
    }
    @Operation(summary = "내 채팅방 목록 조회", description = "현재 로그인한 관리자가 참여 중인 채팅방 목록과 마지막 메시지, 안읽은 수를 반환합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomResponse>> getMyRooms(
            @AuthenticationPrincipal AdminAuthDto principal
    ){
        Long myId = getAdminId(principal.getUsername());
        return ResponseEntity.ok(chatService.getMyRooms(myId));
    }
    @Operation(summary = "채팅 메시지 기록 조회", description = "특정 채팅방의 메시지를 페이징으로 반환합니다. 최신 메시지 순으로 정렬됩니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getMessages(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ){
        return ResponseEntity.ok(chatService.getMessages(roomId,page,size));
    }
    @Operation(summary = "채팅방 읽음 처리", description = "특정 채팅방의 모든 메시지를 읽음 처리합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @PostMapping("/rooms/{roomId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long roomId,
            @AuthenticationPrincipal AdminAuthDto principal
    ){
        Long myId = getAdminId(principal.getUsername());
        chatService.markAsRead(roomId,myId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "채팅방 나가기", description = "지정한 채팅방에서 나갑니다. 1:1 방은 소프트 삭제, 그룹 방은 참여자 목록에서 제거됩니다.", security = @SecurityRequirement(name = "jwtAuth"))
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
