package com.example.demo.domain.notification.repository;

import com.example.demo.domain.notification.Notification;
import com.example.demo.domain.notification.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification,Long> {

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notification n SET n.deletedAt = :now WHERE n.user.userId = :userId AND n.deletedAt IS NULL")
    void softDeleteByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    //알림 목록을 최신순으로 가져오는 기능
    List<Notification> findByUser_UserIdAndStatusOrderByCreatedAtDesc(Long userUserId, Status status);

    //읽지 않은 알림이 몇 개인지 알려주는 기능
    long countByUser_UserIdAndReadAtIsNullAndStatus(Long userId, Status status);

    //안 읽은 데이터만 카운트하는 기능
    @Query("SELECT COUNT (n) FROM Notification n WHERE n.user.userId = :userId AND n.readAt IS NULL AND n.status = 'ACTIVE'")
    Long countUnreadNotifications(@Param("userId")Long userId);

}
