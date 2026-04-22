package com.example.demo.domain.system.store.wallet.repository;

import com.example.demo.domain.system.store.wallet.StoreTicketWallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoreTicketWalletRepository extends JpaRepository<StoreTicketWallet, Long> {

    List<StoreTicketWallet> findByStore_StoreId(Long storeId);

    Optional<StoreTicketWallet> findByStore_StoreIdAndTicketPolicy_TicketPolicyId(Long storeId, Long ticketPolicyId);
}
