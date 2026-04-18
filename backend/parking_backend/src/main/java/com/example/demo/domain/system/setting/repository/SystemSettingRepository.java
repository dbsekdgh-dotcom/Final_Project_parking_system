package com.example.demo.domain.system.setting.repository;

import com.example.demo.domain.system.setting.SystemSetting;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SystemSettingRepository extends JpaRepository<SystemSetting, String> {
    Optional<SystemSetting> findBySettingKey(String settingKey);

    //어드민이 수정 할 수 있는 SystemSetting 값 불러오기
    List<SystemSetting> findByIsEditableTrue();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SystemSetting s WHERE s.settingKey = :key")
    Optional<SystemSetting> findByIdWithLock(@Param("key") String key);
}
