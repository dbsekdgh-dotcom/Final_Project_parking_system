package com.example.demo.domain.shared.systemSetting.repository;

import com.example.demo.domain.shared.systemSetting.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SystemSettingRepository extends JpaRepository<SystemSetting, String> {
    Optional<SystemSetting> findBySettingKey(String settingKey);

    //어드민이 수정 할 수 있는 SystemSetting 값 불러오기
    List<SystemSetting> findByIsEditableTrue();
}
