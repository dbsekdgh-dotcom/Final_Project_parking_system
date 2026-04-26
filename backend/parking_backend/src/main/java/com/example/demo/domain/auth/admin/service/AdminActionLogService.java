package com.example.demo.domain.auth.admin.service;

import com.example.demo.domain.auth.admin.entity.Admin;
import com.example.demo.domain.auth.admin.entity.AdminActionLog;
import com.example.demo.domain.auth.admin.enums.ActionType;
import com.example.demo.domain.auth.admin.enums.AdminStatus;
import com.example.demo.domain.auth.admin.enums.TargetType;
import com.example.demo.domain.auth.admin.repository.AdminActionLogRepository;
import com.example.demo.domain.auth.admin.repository.AdminRepository;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import com.example.demo.global.security.admin.AdminAuthDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AdminActionLogService {
    private final AdminActionLogRepository adminActionLogRepository;
    private final ObjectMapper objectMapper;
    private final AdminRepository adminRepository;

    //  admin_action_log insert
    public void insertPolicyAdminLog(Admin admin, ActionType actionType, long targetId, String beforeData, String afterData){
        AdminActionLog adminActionLog=AdminActionLog.builder().admin(admin).targetType(TargetType.POLICY).actionType(actionType).targetId(targetId)
                .beforeData(beforeData).afterData(afterData).isReverted(false).build();
        adminActionLogRepository.save(adminActionLog);
    }
    // reservation 용
    public void insertAdminlog(Admin admin, ActionType actionType, TargetType targetType, long targetId, String beforeData, String afterData){
        AdminActionLog log = AdminActionLog.builder()
                .admin(admin)
                .targetType(targetType)
                .actionType(actionType)
                .targetId(targetId)
                .beforeData(beforeData)
                .afterData(afterData)
                .isReverted(false)
                .build();
        adminActionLogRepository.save(log);
    }
    public String toJson(Object obj){
        try{
            return objectMapper.writeValueAsString(obj);
        }catch (JsonProcessingException e){
            log.error("actionLog 등록중 오류 발생");
        }
        return "{}";
    }
    //스프링시큐리티가 헤더에서 꺼내서 저장해둔 관리자 정보 가져오기
    public Admin getAdmin(){
        Object principal= SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginId=((AdminAuthDto)principal).getUsername();
        return  adminRepository.findByLoginIdAndStatus(loginId, AdminStatus.ACTIVE).orElseThrow(()->new BusinessException(ErrorCode.ADMIN_NOT_FOUND));
    }
}
