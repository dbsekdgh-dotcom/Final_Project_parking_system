package com.example.demo.domain.system.activitylog.service;

import com.example.demo.domain.system.activitylog.dtos.response.ActivityLogDetailDto;
import com.example.demo.domain.system.activitylog.dtos.response.ActivityLogListDto;
import com.example.demo.domain.system.activitylog.enums.ActivityType;
import com.example.demo.domain.system.activitylog.repository.ActivityLogRepository;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    public Page<ActivityLogListDto> getActivityLogs(
            String keyword,
            ActivityType activityType,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ){
        return activityLogRepository
                .findAllForAdmin(keyword, activityType, startDate, endDate, pageable)
                .map(ActivityLogListDto::from);
    }
    public ActivityLogDetailDto getActivityLogDetail(Long id){
        return activityLogRepository.findDetailById(id)
                .map(ActivityLogDetailDto::from)
                .orElseThrow(()-> new BusinessException(ErrorCode.ACTIVITY_LOG_NOT_FOUND));
    }
}
