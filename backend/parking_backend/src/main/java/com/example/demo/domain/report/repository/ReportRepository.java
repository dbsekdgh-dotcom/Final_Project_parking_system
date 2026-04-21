package com.example.demo.domain.report.repository;

import com.example.demo.domain.report.entity.Report;
import com.example.demo.domain.report.entity.ReportStatus;
import com.example.demo.domain.report.entity.ReportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    // 1. 내가 신고한 내역 (직접 쿼리 작성)
    @Query("SELECT r FROM Report r WHERE r.reporter.userId = :userId ORDER BY r.createdAt DESC")
    Page<Report> findMyReports(@Param("userId") Long userId, Pageable pageable);

    // 2. 내가 받은 신고 (직접 쿼리 작성)
    @Query("SELECT r FROM Report r WHERE r.carNumber IN :carNumbers ORDER BY r.createdAt DESC")
    Page<Report> findReceivedReports(@Param("carNumbers") List<String> carNumbers, Pageable pageable);

    // 3. 기간 검색
    @Query("""
            SELECT r FROM Report r WHERE r.reporter.userId = :userId AND r.createdAt BETWEEN :start AND :end ORDER BY r.createdAt DESC""")
    Page<Report> findByPeriod(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );
    //----------------- 관리자용 ------------
    @Query(
           value = """
                SELECT r FROM Report r
                LEFT JOIN r.reporter u
                WHERE (:type IS NULL OR r.reportType = :type)
                AND (:status IS NULL OR r.status = :status)
                AND (:keyword IS NULL OR r.carNumber LIKE %:keyword% OR u.name LIKE %:keyword%)
                ORDER BY 
                    CASE WHEN r.status = 'PENDING' THEN 0 ELSE 1 END ASC ,
                    r.createdAt DESC 
                """,
            countQuery = """
                SELECT COUNT(r) FROM Report r
                LEFT JOIN r.reporter u
                WHERE (:type IS NULL OR r.reportType = :type)
                AND (:status IS NULL OR r.status = :status)
                AND (:keyword IS NULL OR r.carNumber LIKE %:keyword% OR u.name LIKE %:keyword%)
                """
    )
    Page<Report> findAllWithFilters(
            @Param("type") ReportType type,
            @Param("status") ReportStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
            );
    long countByStatus(ReportStatus status);
}