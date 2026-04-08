package com.example.demo.domain.shared.systemSetting.repository;

import com.example.demo.domain.shared.systemSetting.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemSettingRepository extends JpaRepository<SystemSetting, String> {
}
