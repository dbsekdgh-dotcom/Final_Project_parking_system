package com.example.demo.domain.notification.repository;

import com.example.demo.domain.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface NotificationRepository extends JpaRepository<Notification,Long> {

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notification n SET n.deletedAt = :now WHERE n.user.userId = :userId AND n.deletedAt IS NULL")
    void softDeleteByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}
