package com.example.demo.domain.system.store.service;

import com.example.demo.domain.parking.log.ParkingLog;
import com.example.demo.domain.parking.log.repository.ParkingLogRepository;
import com.example.demo.domain.payment.ticketpolicy.TicketPolicy;
import com.example.demo.domain.payment.ticketpolicy.enums.UseType;
import com.example.demo.domain.payment.ticketpolicy.repository.TicketPolicyRepository;
import com.example.demo.domain.system.store.Store;
import com.example.demo.domain.system.store.dtos.request.StoreLoginRequestDto;
import com.example.demo.domain.system.store.dtos.request.TicketApplyRequestDto;
import com.example.demo.domain.system.store.dtos.response.*;
import com.example.demo.domain.system.store.enums.Status;
import com.example.demo.domain.system.store.repository.StoreRepository;
import com.example.demo.domain.system.store.transaction.StoreTicketTransaction;
import com.example.demo.domain.system.store.transaction.enums.CreatedByType;
import com.example.demo.domain.system.store.transaction.enums.ReferenceType;
import com.example.demo.domain.system.store.transaction.enums.TransactionType;
import com.example.demo.domain.system.store.transaction.repository.StoreTicketTransactionRepository;
import com.example.demo.domain.system.store.wallet.StoreTicketWallet;
import com.example.demo.domain.system.store.wallet.repository.StoreTicketWalletRepository;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import com.example.demo.global.security.store.StoreAuthDto;
import com.example.demo.global.util.admin.AdminJWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class KioskStoreService {
    private final StoreRepository storeRepository;
    private final StoreTicketWalletRepository walletRepository;
    private final StoreTicketTransactionRepository storeTicketTransactionRepository;
    private final TicketPolicyRepository ticketPolicyRepository;
    private final ParkingLogRepository parkingLogRepository;
    private final AdminJWTUtil adminJWTUtil;


    @Transactional(readOnly = true)
    public StoreLoginResponseDto login(StoreLoginRequestDto dto) {
        Store store = storeRepository.findByTerminalPassword(dto.getTerminalPassword())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST));

        if (store.getStatus() != Status.ACTIVE) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        String token = adminJWTUtil.generateKioskToken(
                store.getStoreId(), store.getName(), 60 * 8);

        return new StoreLoginResponseDto(store.getStoreId(), store.getName(), token);
    }

    @Transactional(readOnly = true)
    public StoreInfoResponseDto getMyInfo() {
        Store store = getLoginStore();
        return StoreInfoResponseDto.from(store);
    }

    @Transactional(readOnly = true)
    public List<StoreWalletResponseDto> getWallets() {
        Store store = getLoginStore();
        return walletRepository.findByStore_StoreId(store.getStoreId())
                .stream()
                .map(StoreWalletResponseDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TicketPolicyResponseDto> getStorePolicies() {
        return ticketPolicyRepository
                .findAllByUseTypeAndStatus(
                        UseType.STORE,
                        com.example.demo.domain.payment.ticketpolicy.enums.Status.ACTIVE)
                .stream()
                .map(TicketPolicyResponseDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public StorePurchaseReadyResponseDto purchaseReady(Long ticketPolicyId, int quantity) {
        TicketPolicy policy = ticketPolicyRepository.findById(ticketPolicyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST));
        return new StorePurchaseReadyResponseDto(
                UUID.randomUUID().toString(),
                policy.getName(),
                policy.getPrice() * quantity);
    }

    public void purchaseConfirm(Long ticketPolicyId, int quantity) {
        Store store = getLoginStore();
        TicketPolicy policy = ticketPolicyRepository.findById(ticketPolicyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST));

        StoreTicketWallet wallet = walletRepository
                .findByStore_StoreIdAndTicketPolicy_TicketPolicyId(store.getStoreId(), ticketPolicyId)
                .orElseGet(() -> walletRepository.save(
                        StoreTicketWallet.builder()
                                .store(store)
                                .ticketPolicy(policy)
                                .build()));

        int before = wallet.getRemainingCount();
        wallet.purchase(quantity);

        storeTicketTransactionRepository.save(StoreTicketTransaction.builder()
                .wallet(wallet)
                .store(store)
                .ticketPolicy(policy)
                .transactionType(TransactionType.PURCHASE)
                .quantity(quantity)
                .beforeBalance(before)
                .afterBalance(wallet.getRemainingCount())
                .referenceType(ReferenceType.PAYMENT)
                .createdByType(CreatedByType.STORE)
                .createdById(store.getStoreId())
                .build());
    }

    @Transactional(readOnly = true)
    public List<StoreCarSearchResponseDto> searchCar(String query) {
        return parkingLogRepository.getActiveVehicleList(query)
                .stream()
                .map(dto -> {
                    ParkingLog log = parkingLogRepository.findByParkingLogId(dto.getParkingLogId())
                            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST));
                    return StoreCarSearchResponseDto.from(log);
                })
                .toList();
    }

    public void applyTicket(TicketApplyRequestDto dto) {
        Store store = getLoginStore();

        ParkingLog parkingLog = parkingLogRepository.findByParkingLogId(dto.getParkingLogId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST));

        StoreTicketWallet wallet = walletRepository
                .findByStore_StoreIdAndTicketPolicy_TicketPolicyId(
                        store.getStoreId(), dto.getTicketPolicyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST));
        int quantity = dto.getQuantity();

        if (wallet.getRemainingCount() < quantity) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        TicketPolicy policy = wallet.getTicketPolicy();
        for (int i= 0; i<quantity; i++) {
            parkingLog.applyStoreTicket(policy.getDiscountType(), policy.getDiscountValue());
        }

        int before = wallet.getRemainingCount();
        wallet.use(quantity);

        storeTicketTransactionRepository.save(StoreTicketTransaction.builder()
                .wallet(wallet)
                .store(store)
                .ticketPolicy(policy)
                .transactionType(TransactionType.USE)
                .quantity(quantity)
                .beforeBalance(before)
                .afterBalance(wallet.getRemainingCount())
                .referenceType(ReferenceType.PARKING_LOG)
                .referenceId(dto.getParkingLogId())
                .createdByType(CreatedByType.STORE)
                .createdById(store.getStoreId())
                .build());
    }

    private Store getLoginStore() {
        StoreAuthDto principal = (StoreAuthDto)
                SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return storeRepository.findByStoreId(principal.getStoreId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST));
    }
}