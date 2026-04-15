package com.example.demo.domain.shared.approval.repository;

import com.example.demo.domain.shared.approval.Approval;
import com.example.demo.domain.shared.approval.enums.ApprovalStatus;
import com.example.demo.domain.shared.approval.enums.ApprovalType;
import com.example.demo.domain.shared.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
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

}