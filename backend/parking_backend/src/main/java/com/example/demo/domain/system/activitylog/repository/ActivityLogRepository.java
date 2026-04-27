package com.example.demo.domain.system.activitylog.repository;

import com.example.demo.domain.system.activitylog.ActivityLog;
import com.example.demo.domain.system.activitylog.enums.ActivityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ActivityLogRepository extends JpaRepository<ActivityLog,Long> {

    @Query("SELECT a FROM ActivityLog a WHERE a.user.userId = :userId ORDER BY a.createdAt DESC")
    List<ActivityLog> findRecentActivities(@Param("userId")Long userId);

    //관리자 목록 조회 keyword: 차량번호 or 사용자 이름
    @Query(value = """
          SELECT a FROM ActivityLog a
          LEFT JOIN FETCH a.user u
          LEFT JOIN FETCH a.household h
          WHERE (:keyword IS NULL
                 OR a.carNumber LIKE %:keyword%
                 OR (u IS NOT NULL AND u.name LIKE %:keyword%))
          AND (:activityType IS NULL OR a.activityType = :activityType)
          AND (:startDate IS NULL OR a.createdAt >= :startDate)
          AND (:endDate IS NULL OR a.createdAt <= :endDate)
          ORDER BY a.createdAt DESC
          """,
            countQuery = """
          SELECT COUNT(a) FROM ActivityLog a
          LEFT JOIN a.user u
          LEFT JOIN a.household h
          WHERE (:keyword IS NULL
                 OR a.carNumber LIKE %:keyword%
                 OR (u IS NOT NULL AND u.name LIKE %:keyword%))
          AND (:activityType IS NULL OR a.activityType = :activityType)
          AND (:startDate IS NULL OR a.createdAt >= :startDate)
          AND (:endDate IS NULL OR a.createdAt <= :endDate)
          """)
    Page<ActivityLog> findAllForAdmin(
            @Param("keyword") String keyword,
            @Param("activityType") ActivityType activityType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
    // 상세 조회
    @Query("""
          SELECT a FROM ActivityLog a
          LEFT JOIN FETCH a.user u
          LEFT JOIN FETCH a.household h
          LEFT JOIN FETCH a.parkingLog pl
          LEFT JOIN FETCH a.payment py
          LEFT JOIN FETCH a.reservation r
          WHERE a.activityId = :id
          """)
    Optional<ActivityLog> findDetailById(@Param("id") Long id);
}


