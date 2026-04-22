package com.example.demo.domain.system.activitylog.repository;

import com.example.demo.domain.system.activitylog.ActivityLog;
import com.example.demo.domain.system.activitylog.enums.ActivityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog,Long> {

    @Query("SELECT a FROM ActivityLog a WHERE a.user.userId = :userId ORDER BY a.createdAt DESC")
    List<ActivityLog> findRecentActivities(@Param("userId")Long userId);


}
