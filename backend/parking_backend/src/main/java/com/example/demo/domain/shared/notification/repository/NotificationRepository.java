package com.example.demo.domain.shared.notification.repository;

import com.example.demo.domain.shared.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification,Long> {

}
