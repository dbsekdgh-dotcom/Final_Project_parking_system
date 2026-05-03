package com.example.demo.api.admin.auth;

import com.example.demo.domain.auth.admin.dtos.response.AdminListResponse;
import com.example.demo.domain.auth.admin.service.AdminStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminManagmentController {
    private final AdminStatusService adminStatusService;

    @GetMapping("/admins")
    public List<AdminListResponse> getAdminList(){
        return adminStatusService.getAdminList();
    }
}
