package com.example.demo.domain.chat.repository;

import com.example.demo.domain.chat.entity.AdminChatMessage;
import com.example.demo.domain.chat.entity.AdminChatRoom;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AdminChatMessageRepository extends JpaRepository<AdminChatMessage,Long> {
    @Query("""
    SELECT m FROM AdminChatMessage m
    JOIN FETCH m.sender
    WHERE m.room = :room
    ORDER BY m.messageId DESC 
""")
    Page<AdminChatMessage> findByRoom(@Param("room") AdminChatRoom room, Pageable pageable);

    long countByRoom_RoomIdAndMessageIdGreaterThan(Long roomId,Long messageIdIsGreaterThan);

    Optional<AdminChatMessage> findTopByRoom_RoomIdOrderByMessageIdDesc(Long roomRoomId);
}
