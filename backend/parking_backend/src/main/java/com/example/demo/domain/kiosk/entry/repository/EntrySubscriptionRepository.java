package com.example.demo.domain.kiosk.entry.repository;

import com.example.demo.domain.shared.subscription.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntrySubscriptionRepository extends JpaRepository<Subscription,Long> {

}
