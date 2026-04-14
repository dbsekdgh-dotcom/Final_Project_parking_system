package com.example.demo.domain.user.report.repository;

import com.example.demo.domain.user.report.entity.Report;
import com.example.demo.domain.user.report.entity.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.domain.shared.user.User;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report,Long> {

    //내가 신고한 내역
    Page<Report> findByReporter_UserId(
            Long userId,
            Pageable pageable
    );

    //내가 받은 신고
    Page<Report> findByCarNumberIn(
            List<String> carNumbers,
            Pageable pageable
    );

    //기간 검색
    Page<Report> findByReporter_UserIdAndCreatedAtBetween(
            Long userId,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

}
