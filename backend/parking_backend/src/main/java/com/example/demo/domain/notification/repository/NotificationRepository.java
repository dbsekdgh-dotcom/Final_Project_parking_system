package com.example.demo.domain.notification.repository;

import com.example.demo.domain.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification,Long> {

}
