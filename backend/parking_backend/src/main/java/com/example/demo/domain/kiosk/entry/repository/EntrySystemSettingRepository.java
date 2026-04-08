package com.example.demo.domain.kiosk.entry.repository;

import com.example.demo.domain.shared.systemSetting.SystemSetting;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EntrySystemSettingRepository extends JpaRepository<SystemSetting,String> {
    //쓰기 락을 걸기위한 행 ( 점유 )
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SystemSetting s WHERE s.settingKey=:key")
    Optional<SystemSetting> findByIdWithLock(@Param("key")String key);
}
