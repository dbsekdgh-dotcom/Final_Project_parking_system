package com.example.demo.domain.store.repository;

import com.example.demo.domain.store.StoreTicketConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreTicketConfigRepository extends JpaRepository<StoreTicketConfig,Long> {

    int countStoreTicketConfigByTicketPolicyTicketPolicyId(Long ticketPolicyId);
}
