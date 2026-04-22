package com.example.demo.domain.approval.repository;

import com.example.demo.domain.approval.Approval;
import com.example.demo.domain.approval.enums.ApprovalStatus;
import com.example.demo.domain.approval.enums.ApprovalType;
import com.example.demo.domain.resident.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApprovalRepository extends JpaRepository<Approval, Long> {

    // 특정 유저의 모든 신청 내역 조회
    List<Approval> findByRequestUserId_UserId(Long userId);

    // 상태별 신청 내역 조회 (승인 대기 목록 등)
    List<Approval> findByStatus(ApprovalStatus status);


    boolean existsByRequestUserIdAndApprovalTypeAndStatus(User requestUserId, ApprovalType approvalType, ApprovalStatus status);

    boolean existsByTargetIdAndApprovalTypeAndStatus(Long targetId, ApprovalType approvalType, ApprovalStatus status);

    Optional<Approval> findByApprovalIdAndRequestUserId_UserId(Long approvalId, Long userId);

    Optional<Approval> findTopByRequestUserIdAndApprovalTypeAndStatusOrderByCreatedAtDesc(
            User requestUserId, ApprovalType approvalType, ApprovalStatus status);

    Optional<Approval> findByTargetIdAndApprovalType(Long targetId, ApprovalType approvalType);

    // Admin 승인관리 부분
    @Query(
            value = """
                SELECT a FROM Approval a
                LEFT JOIN FETCH a.requestUserId u
                WHERE (:type IS NULL OR a.approvalType = :type)
                AND (:status IS NULL OR a.status = :status)
                AND (:keyword IS NULL OR u.name LIKE %:keyword%)
                ORDER BY 
                    CASE WHEN a.status = 'PENDING' THEN 0 ELSE 1 END ASC,
                    a.createdAt DESC 
                """,
            countQuery = """
                SELECT COUNT(a) FROM Approval a
                LEFT JOIN a.requestUserId u
                WHERE (:type IS NULL OR a.approvalType = :type)
                AND (:status IS NULL OR a.status = :status)
                AND (:keyword IS NULL OR u.name LIKE %:keyword%)
"""
    )
    Page<Approval> findAllWithFilters(
            @Param("type") ApprovalType type,
            @Param("status") ApprovalStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    long countByStatus(ApprovalStatus status);

    //Admin/UserVehicle Page 용
    @Query("SELECT a FROM Approval a LEFT JOIN FETCH a.requestUserId u WHERE a.targetId = :vehicleId AND a.approvalType = 'VEHICLE' ORDER BY a.createdAt ASC ")
    List<Approval> findVehicleRegistrationHistory(@Param("vehicleId") Long vehicleId);

}