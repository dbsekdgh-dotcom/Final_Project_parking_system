package com.example.demo.domain.system.store.transaction.repository;

import com.example.demo.domain.system.store.transaction.StoreTicketTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreTicketTransactionRepository extends JpaRepository<StoreTicketTransaction, Long> {

    List<StoreTicketTransaction> findByStore_StoreIdOrderByCreatedAtDesc(Long storeId);
}
