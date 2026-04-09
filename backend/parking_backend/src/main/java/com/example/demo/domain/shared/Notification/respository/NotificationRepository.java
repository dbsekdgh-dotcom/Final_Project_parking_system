package com.example.demo.domain.shared.Notification.respository;

import com.example.demo.domain.shared.Notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification,Long> {

}
