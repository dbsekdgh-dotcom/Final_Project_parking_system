package com.example.demo.domain.shared.activityLog.repository;

import com.example.demo.domain.shared.activityLog.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityLogRepository extends JpaRepository<ActivityLog,Long> {
}
