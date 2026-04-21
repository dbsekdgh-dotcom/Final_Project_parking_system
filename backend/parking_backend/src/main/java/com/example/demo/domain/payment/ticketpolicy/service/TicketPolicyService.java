package com.example.demo.domain.payment.ticketpolicy.service;

import com.example.demo.domain.auth.admin.enums.ActionType;
import com.example.demo.domain.auth.admin.service.AdminActionLogService;
import com.example.demo.domain.payment.ticketpolicy.dtos.request.TicketPolicyInsertRequestDto;
import com.example.demo.domain.payment.ticketpolicy.TicketPolicy;
import com.example.demo.domain.payment.ticketpolicy.enums.Status;
import com.example.demo.domain.payment.ticketpolicy.repository.TicketPolicyRepository;
import com.example.demo.domain.store.repository.StoreTicketConfigRepository;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TicketPolicyService {
    private final TicketPolicyRepository ticketPolicyRepository;
    private final StoreTicketConfigRepository storeTicketConfigRepository;
    private final AdminActionLogService adminActionLogService;

    //할인권 삭제
    public void deleteTicketPolicy(long parkingTicketId){
        //적용중인 할인권인지 확인
        checkStoreConfig(parkingTicketId);
        //기존 정책
        TicketPolicy ticketPolicy=ticketPolicyRepository.findById(parkingTicketId).orElseThrow(()->new BusinessException(ErrorCode.INVALID_REQUEST));
        String oldPolicyStr=adminActionLogService.toJson(ticketPolicy);
        //정책 수정
        ticketPolicy.setStatus(Status.DELETED);
        TicketPolicy updatedPolicy=ticketPolicyRepository.saveAndFlush(ticketPolicy);
        String updatedPolicyStr= adminActionLogService.toJson(updatedPolicy);
        //admin_action_log
        adminActionLogService.insertPolicyAdminLog(adminActionLogService.getAdmin(), ActionType.DELETE,parkingTicketId,oldPolicyStr,updatedPolicyStr);
    }
    //할인권 등록
    public long insertTicketPolicy(TicketPolicyInsertRequestDto dto){
        TicketPolicy ticketPolicy=TicketPolicy.builder()
                                    .name(dto.getName())
                                    .description(dto.getDescription())
                                    .price(dto.getPrice())
                                    .discountType(dto.getDiscountType())
                                    .discountValue(dto.getDiscountValue())
                                    .useType(dto.getUseType())
                                    .validMinutes(dto.getValidMinutes())
                                    .validDays(dto.getValidDays())
                                    .stackable(dto.isStackable())
                                    .status(Status.ACTIVE)
                                    .isFreeTicket(dto.isFreeTicket())
                                    .build();

        TicketPolicy newTicketPolicy=ticketPolicyRepository.save(ticketPolicy);
        String policyStr= adminActionLogService.toJson(newTicketPolicy);
        adminActionLogService.insertPolicyAdminLog(adminActionLogService.getAdmin(),ActionType.CREATE,newTicketPolicy.getTicketPolicyId(),policyStr,policyStr);
        return newTicketPolicy.getTicketPolicyId();
    }
    //할인권 비활성화
    public void inactivatePolicy(long ticketPolicyId){
        //사용중인 정책인지 조회
        checkStoreConfig(ticketPolicyId);
        //기존 정책 조회
        TicketPolicy ticketPolicy=ticketPolicyRepository.findById(ticketPolicyId).orElseThrow(()->new BusinessException(ErrorCode.INVALID_REQUEST));
        String oldPolicyStr=adminActionLogService.toJson(ticketPolicy);
        //수정
        ActionType actionType=null;
        if(Status.ACTIVE.equals(ticketPolicy.getStatus())){
            ticketPolicy.setStatus(Status.INACTIVE);
            actionType=ActionType.INACTIVE;
        }else if(Status.INACTIVE.equals(ticketPolicy.getStatus())){
            ticketPolicy.setStatus(Status.ACTIVE);
            actionType=ActionType.ACTIVE;
        }
        TicketPolicy updatedPolicy=ticketPolicyRepository.saveAndFlush(ticketPolicy);
        String updatedPolicyStr= adminActionLogService.toJson(updatedPolicy);
        //admin_action_log
        adminActionLogService.insertPolicyAdminLog(adminActionLogService.getAdmin(),actionType, updatedPolicy.getTicketPolicyId(),oldPolicyStr,updatedPolicyStr);
    }
    private void checkStoreConfig(long ticketPolicyId){
        int activeTicket=storeTicketConfigRepository.countStoreTicketConfigByTicketPolicyTicketPolicyId(ticketPolicyId);
        if(activeTicket>0){
            throw new BusinessException(ErrorCode.POLICY_IN_USE);
        }
    }
}

