package com.example.demo.domain.store.repository;

import com.example.demo.domain.store.StoreTicketConfig;
import com.example.demo.domain.system.store.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StoreTicketConfigRepository extends JpaRepository<StoreTicketConfig,Long> {

    int countStoreTicketConfigByTicketPolicyTicketPolicyId(Long ticketPolicyId);

    Optional<StoreTicketConfig> findByStore_StoreId(Long storeStoreId);

    void deleteByStore_StoreId(Long storeStoreId);

    @Query("""
        SELECT c FROM StoreTicketConfig c
        JOIN FETCH c.store s
        JOIN FETCH c.ticketPolicy tp
        WHERE s.status = :status
        AND c.monthlyQuota > 0
    """)
    List<StoreTicketConfig> findAllActiveWithQuota(Status status);
}
