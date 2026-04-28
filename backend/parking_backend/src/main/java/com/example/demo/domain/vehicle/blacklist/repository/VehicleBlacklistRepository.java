package com.example.demo.domain.vehicle.blacklist.repository;

import com.example.demo.domain.vehicle.blacklist.VehicleBlacklist;
import com.example.demo.domain.vehicle.blacklist.enums.BlacklistStatus;
import com.example.demo.domain.vehicle.blacklist.enums.BlacklistReasonType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface VehicleBlacklistRepository extends JpaRepository<VehicleBlacklist,Long> {

    boolean existsByCarNumberAndStatus(String carNumber, BlacklistStatus status);

    @Query("SELECT COUNT(vb) > 0 FROM VehicleBlacklist vb " +
            "WHERE vb.carNumber = :carNumber " +
            "AND vb.status = 'ACTIVE' " +
            "AND :now BETWEEN vb.startDate AND vb.endDate")
    boolean isCurrentlyBlacklisted(@Param("carNumber") String carNumber, @Param("now") LocalDateTime now);

    @Query("""
        SELECT vb FROM VehicleBlacklist vb
        LEFT JOIN FETCH vb.vehicle v
        LEFT JOIN FETCH v.user u
        WHERE (:keyword IS NULL OR vb.carNumber LIKE %:keyword%)
        AND (:status IS NULL OR vb.status = :status)
        AND (:reasonType IS NULL OR vb.reasonType = :reasonType)
        ORDER BY vb.createdAt DESC
    """)
    Page<VehicleBlacklist> findAllWithFilters(
            @Param("keyword") String keyword,
            @Param("status") BlacklistStatus status,
            @Param("reasonType") BlacklistReasonType reasonType,
            Pageable pageable);
}
