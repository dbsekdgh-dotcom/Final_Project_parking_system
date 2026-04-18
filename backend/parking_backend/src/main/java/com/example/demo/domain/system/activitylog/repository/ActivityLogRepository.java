package com.example.demo.domain.system.activitylog.repository;

import com.example.demo.domain.system.activitylog.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityLogRepository extends JpaRepository<ActivityLog,Long> {
}
