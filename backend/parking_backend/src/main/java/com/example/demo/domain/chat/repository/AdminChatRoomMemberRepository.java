package com.example.demo.domain.chat.repository;

import com.example.demo.domain.chat.entity.AdminChatRoom;
import com.example.demo.domain.chat.entity.AdminChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AdminChatRoomMemberRepository extends JpaRepository<AdminChatRoomMember, Long> {

    @Query("""
    SELECT m FROM AdminChatRoomMember m
    JOIN FETCH m.room r
    JOIN FETCH m.admin a
    WHERE m.admin.adminId = :adminId
    ORDER BY r.createdAt DESC 
""")
    List<AdminChatRoomMember> findMyRooms(@Param("adminId") Long adminId);

    Optional<AdminChatRoomMember> findByRoomAndAdmin_AdminId(AdminChatRoom room, Long adminAdminId);
}
