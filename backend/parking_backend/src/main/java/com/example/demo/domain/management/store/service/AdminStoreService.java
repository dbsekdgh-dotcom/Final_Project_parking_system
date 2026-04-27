package com.example.demo.domain.management.store.service;

import com.example.demo.domain.auth.admin.entity.Admin;
import com.example.demo.domain.auth.admin.entity.AdminActionLog;
import com.example.demo.domain.auth.admin.enums.ActionType;
import com.example.demo.domain.auth.admin.enums.AdminStatus;
import com.example.demo.domain.auth.admin.enums.TargetType;
import com.example.demo.domain.auth.admin.repository.AdminActionLogRepository;
import com.example.demo.domain.auth.admin.repository.AdminRepository;
import com.example.demo.domain.management.store.dtos.request.StoreUpdateRequestDto;
import com.example.demo.domain.management.store.dtos.response.StoreDetailResponseDto;
import com.example.demo.domain.management.store.dtos.response.StoreListResponseDto;
import com.example.demo.domain.management.store.dtos.response.StoreLogSnapshot;
import com.example.demo.domain.system.store.Store;
import com.example.demo.domain.system.store.enums.Status;
import com.example.demo.domain.system.store.repository.StoreRepository;
import com.example.demo.domain.system.store.wallet.repository.StoreTicketWalletRepository;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import com.example.demo.global.security.admin.AdminAuthDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminStoreService {
    private final StoreRepository storeRepository;
    private final AdminRepository adminRepository;
    private final AdminActionLogRepository adminActionLogRepository;
    private final ObjectMapper objectMapper;
    private final StoreTicketWalletRepository storeTicketWalletRepository;

    //헬퍼
    private Store findStore(Long storeId){
        return storeRepository.findByStoreId(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
    }
    private Admin getLoginAdmin(){
        AdminAuthDto principal = (AdminAuthDto)
                SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return adminRepository
                .findByLoginIdAndStatus(principal.getUsername(),AdminStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_NOT_FOUND));
    }
    private void saveActionLog(Admin admin, Long targetId, ActionType actionType, Object before, Object after){
        try{
            adminActionLogRepository.save(AdminActionLog.builder()
                    .admin(admin)
                    .targetType(TargetType.STORE)
                    .actionType(actionType)
                    .targetId(targetId)
                    .beforeData(objectMapper.writeValueAsString(before))
                    .afterData(objectMapper.writeValueAsString(after))
                    .isReverted(false)
                    .build());
        }catch (Exception e){
            log.error("Store ActionLog 저장실패 storeId={}",targetId, e);
        }
    }

    @Transactional(readOnly = true)
    public Page<StoreListResponseDto> getStores(String keyword, Status status, Pageable pageable){
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword;

        return storeRepository
                .findAllWithFilters(kw, status, pageable)
                .map(StoreListResponseDto::from);
    }
    @Transactional(readOnly = true)
    public StoreDetailResponseDto getStore(Long storeId){
        Store store = findStore(storeId);
        return StoreDetailResponseDto.from(store);
    }
    public void updateStore(Long storeId, StoreUpdateRequestDto dto){
        Store store = findStore(storeId);
        Admin admin = getLoginAdmin();

        StoreLogSnapshot before = StoreLogSnapshot.from(store);

        if (dto.getName() != null) store.updateName(dto.getName());
        if (dto.getTerminalPassword() != null) store.updateTerminalPassword(dto.getTerminalPassword());
        store.updateUpdatedBy(admin);

        saveActionLog(admin,storeId, ActionType.UPDATE, before, StoreLogSnapshot.from(store));
    }
    public void activate(Long storeId){
        Admin admin = getLoginAdmin();
        Store store = findStore(storeId);
        if (store.getStatus() == Status.ACTIVE){
            throw new BusinessException(ErrorCode.STORE_ALREADY_ACTIVE);
        }
        StoreLogSnapshot before =StoreLogSnapshot.from(store);

        store.updateStatus(Status.ACTIVE);
        store.setDeletedAt(null);
        store.updateUpdatedBy(admin);

        saveActionLog(admin, storeId, ActionType.ACTIVE, before, StoreLogSnapshot.from(store));
    }
    public void deactivate(Long storeId){
        Store store = findStore(storeId);
        Admin admin = getLoginAdmin();
        if (store.getStatus() == Status.INACTIVE){
            throw  new BusinessException(ErrorCode.STORE_ALREADY_INACTIVE);
        }
        StoreLogSnapshot before =StoreLogSnapshot.from(store);

        store.updateStatus(Status.INACTIVE);
        store.setDeletedAt(LocalDateTime.now());
        store.updateUpdatedBy(admin);

        storeTicketWalletRepository.findByStore_StoreId(storeId)
                .forEach(wallet -> wallet.reset());
        saveActionLog(admin, storeId, ActionType.INACTIVE, before, StoreLogSnapshot.from(store));
    }
}
