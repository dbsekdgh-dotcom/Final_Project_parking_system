package com.example.demo.api.admin.systemsetting;

import com.example.demo.domain.system.setting.SystemSetting;
import com.example.demo.domain.system.setting.repository.SystemSettingRepository;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "14. 시스템 설정 (System Setting)", description = "수정 가능한 시스템 설정 조회 및 값 변경 API")
@RestController
@RequestMapping("/api/admin/system-setting/status")
@RequiredArgsConstructor
public class AdminSystemSettingController {

    private final SystemSettingRepository systemSettingRepository;

    @Operation(summary = "시스템 설정 목록 조회", description = "관리자가 수정 가능한 시스템 설정 항목 목록을 반환합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @GetMapping
    public ResponseEntity<List<SystemSetting>> getSettings(){
        return ResponseEntity.ok(systemSettingRepository.findByIsEditableTrue());
    }
    @Operation(summary = "시스템 설정 값 변경", description = "settingKey로 지정한 설정 항목의 값을 수정합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @PatchMapping("/{settingKey}")
    public ResponseEntity<Void> updateSetting(
            @PathVariable String settingKey,
            @RequestBody Map<String,String> body
            ){
        SystemSetting setting = systemSettingRepository.findBySettingKey(settingKey)
                .orElseThrow(()->new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        setting.updateValue(body.get("settingValue"));
        systemSettingRepository.save(setting);
        return ResponseEntity.ok().build();
    }
}
