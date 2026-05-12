package com.example.demo.domain.chat.repository;

import com.example.demo.domain.chat.entity.AdminChatRoom;
import com.example.demo.domain.chat.enums.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AdminChatRoomRepository extends JpaRepository<AdminChatRoom, Long> {

    @Query("""
    SELECT r FROM AdminChatRoom r
    WHERE r.roomType = :type
    AND  EXISTS (
    SELECT m FROM AdminChatRoomMember m WHERE m.room = r AND m.admin.adminId = :adminId1
    ) AND EXISTS (
    SELECT m FROM AdminChatRoomMember m WHERE m.room = r AND m.admin.adminId = :adminId2
    )
    AND (SELECT COUNT(m) FROM AdminChatRoomMember m WHERE m.room = r) = 2
""")
    Optional<AdminChatRoom> findDirectRoom(
            @Param("adminId1") Long adminId1,
            @Param("adminId2") Long adminId2,
            @Param("type")RoomType type
            );
}
