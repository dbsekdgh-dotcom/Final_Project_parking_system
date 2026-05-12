package com.example.demo.domain.chat.entity;

import com.example.demo.domain.auth.admin.entity.Admin;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "admin_chat_room_member",
        uniqueConstraints = @UniqueConstraint(columnNames = {"room_id","admin_id"})
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminChatRoomMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private AdminChatRoom room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    private Long lastReadMessageId;

    @CreationTimestamp
    @Column(nullable = false,updatable = false)
    private LocalDateTime joinedAt;

    public void updateLastRead(Long messageId){
        this.lastReadMessageId = messageId;
    }

}
