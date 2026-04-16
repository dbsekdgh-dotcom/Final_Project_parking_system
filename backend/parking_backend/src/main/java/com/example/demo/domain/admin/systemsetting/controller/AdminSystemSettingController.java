package com.example.demo.domain.admin.systemsetting.controller;

import com.example.demo.domain.shared.systemSetting.SystemSetting;
import com.example.demo.domain.shared.systemSetting.repository.SystemSettingRepository;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/system-setting/status")
@RequiredArgsConstructor
public class AdminSystemSettingController {

    private final SystemSettingRepository systemSettingRepository;

    @GetMapping
    public ResponseEntity<List<SystemSetting>> getSettings(){
        return ResponseEntity.ok(systemSettingRepository.findByIsEditableTrue());
    }
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
