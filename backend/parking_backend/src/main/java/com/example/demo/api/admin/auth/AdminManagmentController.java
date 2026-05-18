package com.example.demo.api.admin.auth;

import com.example.demo.domain.auth.admin.dtos.response.AdminListResponse;
import com.example.demo.domain.auth.admin.service.AdminStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "19. 관리자 계정 관리 (Admin Management)", description = "관리자 계정 목록 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminManagmentController {
    private final AdminStatusService adminStatusService;

    @Operation(summary = "관리자 목록 조회", description = "등록된 전체 관리자 계정 목록을 반환합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @GetMapping("/admins")
    public List<AdminListResponse> getAdminList(){
        return adminStatusService.getAdminList();
    }
}
