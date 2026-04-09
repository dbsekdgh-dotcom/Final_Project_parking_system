package com.example.demo.domain.shared.approval.repository;

import com.example.demo.domain.shared.approval.Approval;
import com.example.demo.domain.shared.approval.enums.ApprovalStatus;
import com.example.demo.domain.shared.approval.enums.ApprovalType;
import com.example.demo.domain.shared.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalRepository extends JpaRepository<Approval, Long> {

    List<Approval> findByRequestUserId_UserId(Long userId);

    List<Approval> findByStatus(ApprovalStatus status);

    boolean existsByRequestUserIdAndApprovalType(User requestUserId, ApprovalType approvalType);
}
