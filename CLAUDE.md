
  ---
  주요 변경점 요약

  ┌────────────────┬─────────────────────────┬──────────────────────────────────────────────────────┐
  │      항목      │          이전           │                         이후                         │
  ├────────────────┼─────────────────────────┼──────────────────────────────────────────────────────┤
  │ 데이터 소스    │ 더미 INITIAL_DATA       │ /api/admin/approvals GET                             │
  ├────────────────┼─────────────────────────┼──────────────────────────────────────────────────────┤
  │ 필터링         │ 클라이언트 useMemo      │ 서버 파라미터로 전달                                 │
  ├────────────────┼─────────────────────────┼──────────────────────────────────────────────────────┤
  │ 페이지네이션   │ 없음                    │ Pagination 컴포넌트 사용                             │
  ├────────────────┼─────────────────────────┼──────────────────────────────────────────────────────┤
  │ 필드명         │ row.id, row.type 소문자 │ row.approvalId, row.approvalType 대문자              │
  ├────────────────┼─────────────────────────┼──────────────────────────────────────────────────────┤
  │ 처리 완료 표시 │ "처리 완료" 고정        │ processedBySystem 따라 "시스템 처리" / "관리자 처리" │
  ├────────────────┼─────────────────────────┼──────────────────────────────────────────────────────┤
  │ 방문 예약 일시 │ 없음                    │ RESERVATION 타입에 visitStartAt~visitEndAt 표시      │
  ├────────────────┼─────────────────────────┼──────────────────────────────────────────────────────┤
  │ 통계 카드      │ 현재 페이지 rows 기준   │ totalElements + 백엔드 stats 기준                    │
  └────────────────┴─────────────────────────┴──────────────────────────────────────────────────────┘

  ---
  통계 카드 관련해서 백엔드 응답에 stats 객체를 포함시킬지, 별도 API를 쓸지는 팀원과 맞춰야 합니다. 위 코드는 data.stats가 있으면 쓰고 없으면
  totalElements만 쓰는 방어 로직으로 작성했습니다.

✻ Crunched for 44s

❯ 다음꺼                                                                                                                                           
         
● 어느 쪽 "다음"인지 확인이 필요합니다.                                                                                                            
                                                                                                                                                 
  1. 백엔드 API — GET /api/admin/approvals, POST approve/reject, 서비스 로직                                                                       
  2. 다른 프론트 컴포넌트 — ReportPage나 다른 화면                                                                                                 
                                                                                                                
  어느 쪽인가요?                                                                                                                                   
                                                                                                                                                   
❯ 백엔드 쪽                                                                                                                                        
                                                                                                                                                   
● 기존 백엔드 구조 먼저 파악하겠습니다.                                                                                                            
                                                                                                                                                   
● Explore(Explore backend admin approval structure)                                                                                                
  ⎿  Done (27 tool uses · 51.6k tokens · 1m 6s)                                                                                                    
  (ctrl+o to expand)                                                                                                                               
                                                                                                                                                   
● 구조 파악됐습니다. 생성해야 할 파일 목록부터 보여드리겠습니다.                
                                                                                                                                                   
  ---                                                                                                                                              
  생성할 파일 구조                                                                                                                                 
                                                                                                                                                   
  admin/management/approval/                                                                                                                       
  ├── controller/AdminApprovalController.java                                                                                                      
  ├── service/AdminApprovalService.java                                                                                                            
  └── dtos/                                                                                                                                        
      ├── request/ApprovalRejectRequestDto.java                                                                                                    
      └── response/
          ├── ApprovalResponseDto.java
          ├── ApprovalStatsDto.java
          └── ApprovalPageResponseDto.java

  shared/approval/repository/ApprovalRepository.java  ← 신규

  ---
  1. ApprovalRepository.java

  package com.example.demo.domain.shared.approval.repository;

  import com.example.demo.domain.shared.approval.Approval;
  import com.example.demo.domain.shared.approval.enums.ApprovalStatus;
  import com.example.demo.domain.shared.approval.enums.ApprovalType;
  import org.springframework.data.domain.Page;
  import org.springframework.data.domain.Pageable;
  import org.springframework.data.jpa.repository.JpaRepository;
  import org.springframework.data.jpa.repository.Query;
  import org.springframework.data.repository.query.Param;

  public interface ApprovalRepository extends JpaRepository<Approval, Long> {

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
          @Param("type")    ApprovalType    type,
          @Param("status")  ApprovalStatus  status,
          @Param("keyword") String          keyword,
          Pageable pageable
      );

      long countByStatus(ApprovalStatus status);
  }

  FETCH JOIN과 Page를 같이 쓸 때 countQuery를 분리해야 Hibernate가 에러 없이 페이징합니다. PENDING을 항상 위로 정렬해서 처리할 것들이 먼저 보이도록
   했습니다.

  ---
  2. ApprovalRejectRequestDto.java

  package com.example.demo.domain.admin.management.approval.dtos.request;

  import lombok.Getter;
  import lombok.NoArgsConstructor;

  @Getter
  @NoArgsConstructor
  public class ApprovalRejectRequestDto {
      private String rejectReason;
  }

  ---
  3. ApprovalStatsDto.java

  package com.example.demo.domain.admin.management.approval.dtos.response;

  import lombok.Builder;
  import lombok.Getter;

  @Getter
  @Builder
  public class ApprovalStatsDto {
      private long pendingCount;
      private long approvedCount;
      private long rejectedCount;
  }

  ---
  4. ApprovalResponseDto.java

  package com.example.demo.domain.admin.management.approval.dtos.response;

  import com.example.demo.domain.shared.approval.Approval;
  import com.example.demo.domain.shared.approval.enums.ApprovalStatus;
  import com.example.demo.domain.shared.approval.enums.ApprovalType;
  import lombok.Builder;
  import lombok.Getter;

  import java.time.LocalDateTime;

  @Getter
  @Builder
  public class ApprovalResponseDto {

      private Long          approvalId;
      private ApprovalType  approvalType;
      private Long          targetId;
      private String        requestUserName;
      private String        content;
      private ApprovalStatus status;
      private LocalDateTime createdAt;
      private LocalDateTime processedAt;
      private boolean       processedBySystem;
      private String        rejectReason;
      private LocalDateTime visitStartAt;   // RESERVATION 타입만 사용
      private LocalDateTime visitEndAt;     // RESERVATION 타입만 사용

      public static ApprovalResponseDto from(
              Approval approval,
              String content,
              LocalDateTime visitStartAt,
              LocalDateTime visitEndAt) {

          boolean processedBySystem =
                  approval.getProcessedByAdminId() == null
                  && approval.getStatus() != ApprovalStatus.PENDING;

          String requesterName = (approval.getRequestUserId() != null)
                  ? approval.getRequestUserId().getName()
                  : "알 수 없음";

          return ApprovalResponseDto.builder()
                  .approvalId(approval.getApprovalId())
                  .approvalType(approval.getApprovalType())
                  .targetId(approval.getTargetId())
                  .requestUserName(requesterName)
                  .content(content)
                  .status(approval.getStatus())
                  .createdAt(approval.getCreatedAt())
                  .processedAt(approval.getProcessedAt())
                  .processedBySystem(processedBySystem)
                  .rejectReason(approval.getRejectReason())
                  .visitStartAt(visitStartAt)
                  .visitEndAt(visitEndAt)
                  .build();
      }
  }

  ---
  5. ApprovalPageResponseDto.java

  package com.example.demo.domain.admin.management.approval.dtos.response;

  import lombok.Builder;
  import lombok.Getter;

  import java.util.List;

  @Getter
  @Builder
  public class ApprovalPageResponseDto {
      private List<ApprovalResponseDto> content;
      private int  totalPages;
      private long totalElements;
      private int  number;
      private ApprovalStatsDto stats;
  }

  ---
  6. AdminApprovalService.java

  package com.example.demo.domain.admin.management.approval.service;

  import com.example.demo.domain.admin.entity.Admin;
  import com.example.demo.domain.admin.entity.AdminActionLog;
  import com.example.demo.domain.admin.enums.ActionType;
  import com.example.demo.domain.admin.enums.AdminStatus;
  import com.example.demo.domain.admin.enums.TargetType;
  import com.example.demo.domain.admin.management.approval.dtos.response.ApprovalPageResponseDto;
  import com.example.demo.domain.admin.management.approval.dtos.response.ApprovalResponseDto;
  import com.example.demo.domain.admin.management.approval.dtos.response.ApprovalStatsDto;
  import com.example.demo.domain.admin.repository.AdminActionLogRepository;
  import com.example.demo.domain.admin.repository.AdminRepository;
  import com.example.demo.domain.shared.approval.Approval;
  import com.example.demo.domain.shared.approval.enums.ApprovalStatus;
  import com.example.demo.domain.shared.approval.enums.ApprovalType;
  import com.example.demo.domain.shared.approval.repository.ApprovalRepository;
  import com.example.demo.domain.shared.reservation.Reservation;
  import com.example.demo.domain.shared.reservation.enums.Status;
  import com.example.demo.domain.shared.reservation.repository.ReservationRepository;
  import com.example.demo.domain.shared.vehicle.Vehicle;
  import com.example.demo.domain.shared.vehicle.enums.VehicleStatus;
  import com.example.demo.domain.shared.vehicle.repository.VehicleRepository;
  import com.example.demo.domain.shared.household.Household;
  import com.example.demo.domain.shared.household.repository.HouseholdRepository;
  import com.example.demo.domain.shared.user.User;
  import com.example.demo.domain.shared.user.repository.UserRepository;
  import com.example.demo.global.exception.BusinessException;
  import com.example.demo.global.exception.ErrorCode;
  import com.example.demo.global.security.admin.AdminAuthDto;
  import lombok.RequiredArgsConstructor;
  import org.springframework.data.domain.Page;
  import org.springframework.data.domain.Pageable;
  import org.springframework.security.core.context.SecurityContextHolder;
  import org.springframework.stereotype.Service;
  import org.springframework.transaction.annotation.Transactional;

  import java.time.LocalDateTime;
  import java.util.List;

  @Service
  @RequiredArgsConstructor
  @Transactional
  public class AdminApprovalService {

      private final ApprovalRepository      approvalRepository;
      private final AdminRepository         adminRepository;
      private final AdminActionLogRepository adminActionLogRepository;
      private final VehicleRepository       vehicleRepository;
      private final ReservationRepository   reservationRepository;
      private final UserRepository          userRepository;
      private final HouseholdRepository     householdRepository;

      // ── 목록 조회 ──────────────────────────────────────────────────────
      @Transactional(readOnly = true)
      public ApprovalPageResponseDto getApprovals(
              ApprovalType type, ApprovalStatus status,
              String keyword, Pageable pageable) {

          String kw = (keyword == null || keyword.isBlank()) ? null : keyword;
          Page<Approval> page = approvalRepository.findAllWithFilters(type, status, kw, pageable);

          List<ApprovalResponseDto> content = page.getContent().stream()
                  .map(this::toResponseDto)
                  .toList();

          ApprovalStatsDto stats = ApprovalStatsDto.builder()
                  .pendingCount(approvalRepository.countByStatus(ApprovalStatus.PENDING))
                  .approvedCount(approvalRepository.countByStatus(ApprovalStatus.APPROVED))
                  .rejectedCount(approvalRepository.countByStatus(ApprovalStatus.REJECTED))
                  .build();

          return ApprovalPageResponseDto.builder()
                  .content(content)
                  .totalPages(page.getTotalPages())
                  .totalElements(page.getTotalElements())
                  .number(page.getNumber())
                  .stats(stats)
                  .build();
      }

      // ── 승인 ───────────────────────────────────────────────────────────
      public void approve(Long approvalId) {
          Admin admin       = getLoginAdmin();
          Approval approval = findPendingApproval(approvalId);
          String beforeData = buildStatusJson(approval.getStatus().name());

          switch (approval.getApprovalType()) {
              case RESIDENT    -> approveResident(approval);
              case VEHICLE     -> approveVehicle(approval);
              case RESERVATION -> approveReservation(approval);
          }

          approval.updateStatus(ApprovalStatus.APPROVED);
          approval.setProcessedAt(LocalDateTime.now());
          approval.setProcessedByAdminId(admin.getAdminId());

          saveActionLog(admin, approval, ActionType.APPROVE,
                  beforeData, buildStatusJson("APPROVED"));
      }

      // ── 거절 ───────────────────────────────────────────────────────────
      public void reject(Long approvalId, String rejectReason) {
          Admin admin       = getLoginAdmin();
          Approval approval = findPendingApproval(approvalId);
          String beforeData = buildStatusJson(approval.getStatus().name());

          switch (approval.getApprovalType()) {
              case VEHICLE     -> rejectVehicle(approval);
              case RESERVATION -> rejectReservation(approval);
              case RESIDENT    -> {} // 별도 처리 없음
          }

          approval.updateStatus(ApprovalStatus.REJECTED);
          approval.setProcessedAt(LocalDateTime.now());
          approval.setProcessedByAdminId(admin.getAdminId());
          approval.setRejectReason(rejectReason);

          saveActionLog(admin, approval, ActionType.REJECT,
                  beforeData,
                  String.format("{\"status\":\"REJECTED\",\"rejectReason\":\"%s\"}", rejectReason));
      }

      // ── 유형별 승인 처리 ────────────────────────────────────────────────
      private void approveResident(Approval approval) {
          User user = userRepository.findById(approval.getTargetId())
                  .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST));

          Household household = householdRepository.findById(
                  user.getHouseholdId())  // 등록 신청 시 이미 세팅된 household_id
                  .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST));

          household.activate();   // is_active=ACTIVE, 카운트 0 초기화 (아래 참고)
      }

      private void approveVehicle(Approval approval) {
          Vehicle vehicle = vehicleRepository.findById(approval.getTargetId())
                  .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST));
          vehicle.setStatus(VehicleStatus.ACTIVE);
      }

      private void approveReservation(Approval approval) {
          Reservation reservation = reservationRepository.findById(approval.getTargetId())
                  .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST));
          reservation.setStatus(Status.RESERVED);
      }

      // ── 유형별 거절 처리 ────────────────────────────────────────────────
      private void rejectVehicle(Approval approval) {
          Vehicle vehicle = vehicleRepository.findById(approval.getTargetId())
                  .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST));
          vehicle.setStatus(VehicleStatus.DELETED);
      }

      private void rejectReservation(Approval approval) {
          Reservation reservation = reservationRepository.findById(approval.getTargetId())
                  .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST));
          reservation.setStatus(Status.REJECTED);
      }

      // ── 내부 헬퍼 ──────────────────────────────────────────────────────
      private Approval findPendingApproval(Long approvalId) {
          Approval approval = approvalRepository.findById(approvalId)
                  .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST));
          if (approval.getStatus() != ApprovalStatus.PENDING) {
              throw new BusinessException(ErrorCode.INVALID_REQUEST);
          }
          return approval;
      }

      private Admin getLoginAdmin() {
          AdminAuthDto principal = (AdminAuthDto)
                  SecurityContextHolder.getContext().getAuthentication().getPrincipal();
          return adminRepository
                  .findByLoginIdAndStatus(principal.getUsername(), String.valueOf(AdminStatus.ACTIVE))
                  .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_NOT_FOUND));
      }

      private void saveActionLog(Admin admin, Approval approval,
                                  ActionType actionType, String before, String after) {
          TargetType targetType = switch (approval.getApprovalType()) {
              case RESIDENT    -> TargetType.USER;
              case VEHICLE     -> TargetType.VEHICLE;
              case RESERVATION -> TargetType.RESERVATION;
              default          -> TargetType.USER;
          };

          adminActionLogRepository.save(AdminActionLog.builder()
                  .admin(admin)
                  .targetType(targetType)
                  .actionType(actionType)
                  .targetId(approval.getApprovalId())
                  .beforeData(before)
                  .afterData(after)
                  .build());
      }

      private ApprovalResponseDto toResponseDto(Approval approval) {
          String content           = "";
          LocalDateTime visitStart = null;
          LocalDateTime visitEnd   = null;

          switch (approval.getApprovalType()) {
              case RESIDENT -> content = "입주민 등록 신청";
              case VEHICLE -> {
                  Vehicle v = vehicleRepository.findById(approval.getTargetId()).orElse(null);
                  content = (v != null) ? v.getCarNumber() + " 차량 등록 신청" : "차량 등록 신청";
              }
              case RESERVATION -> {
                  Reservation r = reservationRepository.findById(approval.getTargetId()).orElse(null);
                  if (r != null) {
                      content    = r.getCarNumber() + " 방문 예약 신청";
                      visitStart = r.getVisitStartAt();
                      visitEnd   = r.getVisitEndAt();
                  } else {
                      content = "방문 예약 신청";
                  }
              }
          }

          return ApprovalResponseDto.from(approval, content, visitStart, visitEnd);
      }

      private String buildStatusJson(String status) {
          return String.format("{\"status\":\"%s\"}", status);
      }
  }

  ---
  7. AdminApprovalController.java

  package com.example.demo.domain.admin.management.approval.controller;

  import com.example.demo.domain.admin.management.approval.dtos.request.ApprovalRejectRequestDto;
  import com.example.demo.domain.admin.management.approval.dtos.response.ApprovalPageResponseDto;
  import com.example.demo.domain.admin.management.approval.service.AdminApprovalService;
  import com.example.demo.domain.shared.approval.enums.ApprovalStatus;
  import com.example.demo.domain.shared.approval.enums.ApprovalType;
  import lombok.RequiredArgsConstructor;
  import org.springframework.data.domain.PageRequest;
  import org.springframework.http.ResponseEntity;
  import org.springframework.web.bind.annotation.*;

  @RestController
  @RequestMapping("/api/admin/approvals")
  @RequiredArgsConstructor
  public class AdminApprovalController {

      private final AdminApprovalService adminApprovalService;

      @GetMapping
      public ResponseEntity<ApprovalPageResponseDto> getApprovals(
              @RequestParam(required = false) ApprovalType   type,
              @RequestParam(required = false) ApprovalStatus status,
              @RequestParam(required = false) String         keyword,
              @RequestParam(defaultValue = "0")  int page,
              @RequestParam(defaultValue = "10") int size
      ) {
          return ResponseEntity.ok(
                  adminApprovalService.getApprovals(type, status, keyword, PageRequest.of(page, size))
          );
      }

      @PostMapping("/{approvalId}/approve")
      public ResponseEntity<Void> approve(@PathVariable Long approvalId) {
          adminApprovalService.approve(approvalId);
          return ResponseEntity.ok().build();
      }

      @PostMapping("/{approvalId}/reject")
      public ResponseEntity<Void> reject(
              @PathVariable Long approvalId,
              @RequestBody ApprovalRejectRequestDto dto
      ) {
          adminApprovalService.reject(approvalId, dto.getRejectReason());
          return ResponseEntity.ok().build();
      }
  }

  ---
  주의사항 2가지

  ① Household.activate() 메서드 추가 필요

  Household 엔티티에 아래 메서드가 없으면 추가해야 합니다:

  public void activate() {
      this.isActive = HouseholdStatus.ACTIVE;  // 또는 "ACTIVE" Enum에 맞게
      this.totalVisitCount     = 0;
      this.todayVisitCount     = 0;
      this.monthlyVisitCount   = 0;
      this.activeReservationCount = 0;
  }
   
  Householdn엔티티에N아래메서드가 없으면 추가해야 합니다:
                            
  public void activate()c{e/reservation 조회 쿼리가 건별로 추가 실행됩니다. 당장 문제는 없지만 데이터가 많아지면 별도 JOIN 쿼리로 개선이
      this.isActive = Hous holdStatus.ACTIVE;  // 또는 "ACTIVE" Enum에 맞게                                                             
      this.totalVisitCount     = 0;
✻ Brewthis.todayVisitCount     = 0;      
      this.monthlyVisitCount   = 0;                                             
──────this.activeReservationCount─=─0;─────────────────────────────────────────────────────────────────────────────────────────────────────────────
  }                                                                                                                                                
   ────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
  ② toResponseDto의 N+1 주의                                                                                                                       
                                                                                                                                                   
  목록 10건 조회 시 vehicle/reservation 조회 쿼리가 건별로 추가 실행됩니다. 당장 문제는 없지만 데이터가 많아지면 별도 JOIN 쿼리로 개선이
  필요합니다.

✻ Brewed for 3m 32s
