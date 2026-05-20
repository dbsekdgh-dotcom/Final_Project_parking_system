package com.example.demo.domain.chat.service;

import com.example.demo.domain.auth.admin.entity.Admin;
import com.example.demo.domain.auth.admin.repository.AdminRepository;
import com.example.demo.domain.chat.dtos.request.CreateGroupRoomRequest;
import com.example.demo.domain.chat.dtos.request.CreatedDirectRoomRequest;
import com.example.demo.domain.chat.dtos.response.ChatMessageResponse;
import com.example.demo.domain.chat.dtos.response.ChatRoomResponse;
import com.example.demo.domain.chat.entity.AdminChatMessage;
import com.example.demo.domain.chat.entity.AdminChatRoom;
import com.example.demo.domain.chat.entity.AdminChatRoomMember;
import com.example.demo.domain.chat.enums.RoomType;
import com.example.demo.domain.chat.repository.AdminChatMessageRepository;
import com.example.demo.domain.chat.repository.AdminChatRoomMemberRepository;
import com.example.demo.domain.chat.repository.AdminChatRoomRepository;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminChatService {

    private final AdminChatRoomRepository roomRepository;
    private final AdminChatRoomMemberRepository memberRepository;
    private final AdminChatMessageRepository messageRepository;
    private final AdminRepository adminRepository;

    // 헬퍼
    private ChatRoomResponse buildRoomResponse(AdminChatRoom room, Long myAdminId){
        List<AdminChatRoomMember> members = memberRepository
                .findAll().stream()
                .filter(m->m.getRoom().getRoomId().equals(room.getRoomId()))
                .collect(Collectors.toList());
        String displayName = room.getRoomName();
        if (room.getRoomType() == RoomType.DIRECT){
            displayName = members.stream()
                    .filter(m -> !m.getAdmin().getAdminId().equals(myAdminId))
                    .findFirst()
                    .map(m -> m.getAdmin().getName())
                    .orElseGet(() -> members.stream()
                            .filter(m -> m.getAdmin().getAdminId().equals(myAdminId))
                            .findFirst()
                            .map(m -> m.getAdmin().getName() + " (나)")
                            .orElse("알 수 없음"));
        }
        long unread = memberRepository.findByRoomAndAdmin_AdminId(room,myAdminId)
                .map(m -> m.getLastReadMessageId() == null
                    ? messageRepository.countByRoom_RoomIdAndMessageIdGreaterThan(room.getRoomId(), 0L)
                        : messageRepository.countByRoom_RoomIdAndMessageIdGreaterThan(room.getRoomId(), m.getLastReadMessageId())
                ).orElse(0L);

        var lastMsg = messageRepository.findTopByRoom_RoomIdOrderByMessageIdDesc(room.getRoomId());

        List<ChatRoomResponse.MemberInfo> memberInfos = members.stream()
                .map(m->ChatRoomResponse.MemberInfo.builder()
                        .adminId(m.getAdmin().getAdminId())
                        .name(m.getAdmin().getName())
                        .loginId(m.getAdmin().getLoginId())
                        .build())
                .collect(Collectors.toList());

        return ChatRoomResponse.builder()
                .roomId(room.getRoomId())
                .roomType(room.getRoomType())
                .roomName(displayName)
                .unreadCount(unread)
                .lastMessage(lastMsg.map(AdminChatMessage::getContent).orElse(null))
                .lastMessageAt(lastMsg.map(AdminChatMessage::getCreatedAt).orElse(room.getCreatedAt()))
                .members(memberInfos)
                .build();
    }
    private Admin getAdmin(Long adminId){
        return adminRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_NOT_FOUND));
    }
    private AdminChatRoom getRoom(Long roomId){
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_CHAT_ROOM_NOT_FOUND));
    }

    public ChatRoomResponse getOrCreateDirectRoom(Long myAdminId, CreatedDirectRoomRequest req){
        Long targetId = req.getTargetAdminId();

        if (myAdminId.equals(targetId)) {
            return roomRepository.findSelfRoom(myAdminId, RoomType.DIRECT)
                    .map(room -> buildRoomResponse(room, myAdminId))
                    .orElseGet(() -> {
                        Admin me = getAdmin(myAdminId);
                        AdminChatRoom room = AdminChatRoom.builder()
                                .roomType(RoomType.DIRECT)
                                .createdBy(me)
                                .build();
                        roomRepository.save(room);
                        memberRepository.save(AdminChatRoomMember.builder()
                                .room(room).admin(me).build());
                        return buildRoomResponse(room, myAdminId);
                    });
        }
        // 1:1 — 기존 방 있으면 반환, 없으면 생성
        return roomRepository.findDirectRoom(myAdminId, targetId, RoomType.DIRECT)
                .map(room -> buildRoomResponse(room, myAdminId))
                .orElseGet(() -> {
                    Admin me = getAdmin(myAdminId);
                    Admin target = getAdmin(targetId);

                    AdminChatRoom room = AdminChatRoom.builder()
                            .roomType(RoomType.DIRECT)
                            .createdBy(me)
                            .build();
                    roomRepository.save(room);

                    memberRepository.save(AdminChatRoomMember.builder()
                            .room(room).admin(me).build());
                    memberRepository.save(AdminChatRoomMember.builder()
                            .room(room).admin(target).build());

                    return buildRoomResponse(room, myAdminId);
                });
    }
    public ChatRoomResponse createGroupRoom(Long myAdminId, CreateGroupRoomRequest req){
        Admin me = getAdmin(myAdminId);

        AdminChatRoom room = AdminChatRoom.builder()
                .roomType(RoomType.GROUP)
                .roomName(req.getRoomName())
                .createdBy(me)
                .build();
        roomRepository.save(room);

        memberRepository.save(AdminChatRoomMember.builder().room(room).admin(me).build());

        for (Long memberId : req.getMemberAdminIds()){
            if (!memberId.equals(myAdminId)){
                memberRepository.save(AdminChatRoomMember.builder()
                        .room(room)
                        .admin(getAdmin(memberId))
                        .build());
            }
        }
        return buildRoomResponse(room,myAdminId);
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getMyRooms(Long myAdminId){
        return memberRepository.findMyRooms(myAdminId).stream()
                .map(m->buildRoomResponse(m.getRoom(),myAdminId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getMessages(Long roomId, int page, int size){
        AdminChatRoom room = getRoom(roomId);
        Page<AdminChatMessage> messages = messageRepository.findByRoom(
                room, PageRequest.of(page,size));
        return messages.getContent().stream()
                .map(ChatMessageResponse::from)
                .collect(Collectors.toList());
    }
    public void leaveRoom(Long roomId, Long adminId) {
        AdminChatRoom room = getRoom(roomId);
        if (room.getRoomType() != RoomType.GROUP) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        memberRepository.findByRoomAndAdmin_AdminId(room, adminId)
                .ifPresent(memberRepository::delete);
    }

    //읽음 처리
    public void markAsRead(Long roomId, Long adminId){
        AdminChatRoom room = getRoom(roomId);
        messageRepository.findTopByRoom_RoomIdOrderByMessageIdDesc(roomId)
                .ifPresent(lastMsg ->
                        memberRepository.findByRoomAndAdmin_AdminId(room,adminId)
                                .ifPresent(m->m.updateLastRead(lastMsg.getMessageId())));
    }
    //웹소켓 메시지 저장 후 응답 반환
    public ChatMessageResponse saveMessage(Long roomId, Long senderAdminId, String content){
        AdminChatRoom room = getRoom(roomId);
        Admin sender = getAdmin(senderAdminId);

        AdminChatMessage message = AdminChatMessage.builder()
                .room(room)
                .sender(sender)
                .content(content)
                .build();

        return ChatMessageResponse.from(messageRepository.save(message));
    }
    //방 멤버 loginId 목록 조회 ( 특정 관리자 id 제외 )
    @Transactional
    public List<String> getRoomMemberLoginIds(Long roomId, Long excludeAdminId){
        AdminChatRoom room = getRoom(roomId);
        return memberRepository.findAll().stream()
                .filter(m->m.getRoom().getRoomId().equals(roomId))
                .filter(m -> !m.getAdmin().getAdminId().equals(excludeAdminId))
                .map(m->m.getAdmin().getLoginId())
                .collect(Collectors.toList());
    }
}
