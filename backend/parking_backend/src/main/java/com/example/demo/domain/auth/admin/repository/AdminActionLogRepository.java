package com.example.demo.domain.auth.admin.repository;

import com.example.demo.domain.auth.admin.entity.AdminActionLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminActionLogRepository extends JpaRepository<AdminActionLog, Long> {
}
