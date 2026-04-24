package com.example.demo.domain.auth.admin.repository;

import com.example.demo.domain.auth.admin.entity.AdminActionLog;
import com.example.demo.domain.auth.admin.enums.ActionType;
import com.example.demo.domain.auth.admin.enums.TargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface AdminActionLogRepository extends JpaRepository<AdminActionLog, Long> {

    @Query(
            value = """
              SELECT a FROM AdminActionLog a
              LEFT JOIN FETCH a.admin ad
              LEFT JOIN FETCH a.revertedByAdmin
              WHERE (:targetType IS NULL OR a.targetType = :targetType)
              AND (:actionType IS NULL OR a.actionType = :actionType)
              AND (:isReverted IS NULL OR a.isReverted = :isReverted)
              AND (:keyword IS NULL OR ad.name LIKE %:keyword%)
              AND (:startDate IS NULL OR a.createdAt >= :startDate)
              AND (:endDate IS NULL OR a.createdAt <= :endDate)
              ORDER BY a.createdAt DESC
              """,
            countQuery = """
              SELECT COUNT(a) FROM AdminActionLog a
              LEFT JOIN a.admin ad
              WHERE (:targetType IS NULL OR a.targetType = :targetType)
              AND (:actionType IS NULL OR a.actionType = :actionType)
              AND (:isReverted IS NULL OR a.isReverted = :isReverted)
              AND (:keyword IS NULL OR ad.name LIKE %:keyword%)
              AND (:startDate IS NULL OR a.createdAt >= :startDate)
              AND (:endDate IS NULL OR a.createdAt <= :endDate)
              """
    )
    Page<AdminActionLog> findAllWithFilters(
            @Param("targetType") TargetType targetType,
            @Param("actionType") ActionType actionType,
            @Param("isReverted") Boolean isReverted,
            @Param("keyword") String keyword,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
            );
}
