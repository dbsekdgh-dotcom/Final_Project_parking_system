package com.example.demo.domain.shared.subscription.repository;

import com.example.demo.domain.shared.subscription.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription,Long> {
}
