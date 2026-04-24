# AdminActionLog 현황 (2026-04-24)

## 완료된 백엔드 구현

### 구조
- **Entity**: `AdminActionLog` — admin_action_log 테이블
- **Repository**: `AdminActionLogRepository` — 복합 필터링 + 페이징
- **Service**:
  - `AdminActionLogService` — 로그 기록 헬퍼 (insertPolicyAdminLog, toJson, getAdmin)
  - `AdminActionLogQueryService` — 조회(getActionLogs) + 되돌리기(revert)
- **DTO**: `ActionLogResponseDto`
- **Controller**: `AdminActionLogController`

### API 엔드포인트
| 메서드 | 경로 | 기능 |
|--------|------|------|
| GET | `/api/admin/action-logs` | 로그 목록 조회 (필터: targetType, actionType, isReverted, keyword, startDate, endDate, page, size) |
| POST | `/api/admin/action-logs/{actionId}/revert` | 로그 되돌리기 |

### Enum 정의
- **ActionType**: CREATE, UPDATE, DELETE, APPROVE, REJECT, REFUND, REPORT, BLACKLIST, ACTIVE, INACTIVE
- **TargetType**: USER, VEHICLE, PAYMENT, POLICY, RESERVATION, SYSTEM_SETTING, STORE, PARKING_LOG, REPORT

### revert 가능 대상 (UPDATE 타입만)
- **POLICY** → ParkingFeePolicy.effectiveTo 복원
- **PARKING_SPACE** → ParkingSpace.status / isDisabled / isEvCharge 복원
- **PARKING_LOG** → ParkingLog.parkingStatus 복원

### 로그 기록 중인 서비스
- ParkingFeePolicyService (요금정책 CREATE/UPDATE)
- TicketPolicyService (할인권 CREATE/UPDATE/DELETE/INACTIVE/ACTIVE)
- AdminApprovalService (승인/거부 APPROVE/REJECT)
- AdminParkingService (주차 로그 수정 UPDATE)
- AdminParkingSpaceService (주차공간 제어)
- AdminReportService (신고 처리)

## 남은 작업 / 주의사항
- `changedFields` 필드: 엔티티에 있지만 실제 저장 로직 없음
- CREATE/DELETE 타입은 revert 미지원 (UPDATE만 가능)
