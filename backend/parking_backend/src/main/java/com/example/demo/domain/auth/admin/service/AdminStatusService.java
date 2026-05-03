package com.example.demo.domain.auth.admin.service;

import com.example.demo.domain.auth.admin.dtos.response.AdminListResponse;
import com.example.demo.domain.auth.admin.enums.AdminStatus;
import com.example.demo.domain.auth.admin.repository.AdminRepository;
import com.example.demo.global.redis.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminStatusService {
    private final AdminRepository adminRepository;
    private final RedisService redisService;

    public List<AdminListResponse> getAdminList(){
        return adminRepository.findAllByStatus(AdminStatus.ACTIVE).stream()
                .map(admin->AdminListResponse.of(
                        admin,
                        redisService.getRefreshToken(admin.getLoginId()) != null
                )).toList();
    }
}
