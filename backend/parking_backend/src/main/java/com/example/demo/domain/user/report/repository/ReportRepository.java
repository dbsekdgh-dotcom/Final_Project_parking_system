package com.example.demo.domain.user.report.repository;

import com.example.demo.domain.user.report.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    // 1. 내가 신고한 내역 (직접 쿼리 작성)
    @Query("SELECT r FROM Report r WHERE r.reporter.id = :userId ORDER BY r.createdAt DESC")
    Page<Report> findMyReports(@Param("userId") Long userId, Pageable pageable);

    // 2. 내가 받은 신고 (직접 쿼리 작성)
    @Query("SELECT r FROM Report r WHERE r.carNumber IN :carNumbers ORDER BY r.createdAt DESC")
    Page<Report> findReceivedReports(@Param("carNumbers") List<String> carNumbers, Pageable pageable);

    // 3. 기간 검색
    @Query("SELECT r FROM Report r WHERE r.reporter.id = :userId AND r.createdAt BETWEEN :start AND :end ORDER BY r.createdAt DESC")
    Page<Report> findByPeriod(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );
}