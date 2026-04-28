package com.example.demo.domain.payment.ticket.service;

import com.example.demo.domain.payment.ticketpolicy.TicketPolicy;
import com.example.demo.domain.store.StoreTicketConfig;
import com.example.demo.domain.store.repository.StoreTicketConfigRepository;
import com.example.demo.domain.system.store.Store;
import com.example.demo.domain.system.store.enums.Status;
import com.example.demo.domain.system.store.transaction.StoreTicketTransaction;
import com.example.demo.domain.system.store.transaction.enums.CreatedByType;
import com.example.demo.domain.system.store.transaction.enums.ReferenceType;
import com.example.demo.domain.system.store.transaction.enums.TransactionType;
import com.example.demo.domain.system.store.transaction.repository.StoreTicketTransactionRepository;
import com.example.demo.domain.system.store.wallet.StoreTicketWallet;
import com.example.demo.domain.system.store.wallet.repository.StoreTicketWalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FreeTicketProvideService {

    private final StoreTicketConfigRepository configRepository;
    private final StoreTicketWalletRepository walletRepository;
    private final StoreTicketTransactionRepository transactionRepository;
    private final FreeTicketRedisService redisService;

    // 설정 저장 시 즉시 지급 후 30일 Redis 키 등록
    @Transactional
    public void provideAndSchedule(Long storeId) {
        provide(storeId);
    }

    // Redis 만료 이벤트 또는 startup recovery 시 호출
    @Transactional
    public void provide(Long storeId) {
        StoreTicketConfig config = configRepository.findByStore_StoreId(storeId).orElse(null);
        if (config == null) {
            log.warn("free-ticket 지급 스킵 - config 없음, storeId={}", storeId);
            return;
        }

        Store store = config.getStore();
        if (store.getStatus() != Status.ACTIVE) {
            log.warn("free-ticket 지급 스킵 - 비활성 상가, storeId={}", storeId);
            return;
        }

        TicketPolicy policy = config.getTicketPolicy();
        int quota = config.getMonthlyQuota();

        StoreTicketWallet wallet = walletRepository
                .findByStore_StoreIdAndTicketPolicy_TicketPolicyId(storeId, policy.getTicketPolicyId())
                .orElseGet(() -> walletRepository.save(StoreTicketWallet.builder()
                        .store(store)
                        .ticketPolicy(policy)
                        .build()));

        int before = wallet.getRemainingCount();
        wallet.purchase(quota);

        transactionRepository.save(StoreTicketTransaction.builder()
                .wallet(wallet)
                .store(store)
                .ticketPolicy(policy)
                .transactionType(TransactionType.ISSUE)
                .quantity(quota)
                .beforeBalance(before)
                .afterBalance(wallet.getRemainingCount())
                .referenceType(ReferenceType.SYSTEM)
                .createdByType(CreatedByType.SYSTEM)
                .build());

        config.markIssued();
        redisService.register(storeId);

        log.info("free-ticket 지급 완료, storeId={}, policy={}, qty={}", storeId, policy.getName(), quota);
    }
    public void cancel(Long storeId){
        redisService.delete(storeId);
        log.info("free-ticket Redis 키 삭제, storeId={}",storeId);
    }
}
