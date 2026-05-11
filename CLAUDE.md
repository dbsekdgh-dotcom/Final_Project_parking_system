# 프로젝트 전체 현황 (2026-04-24)

## 기술 스택
- **백엔드**: Spring Boot 3.x, JPA/Hibernate, QueryDSL, Spring Security + JWT, Redis, MySQL, AWS S3, Naver CLOVA OCR, Gradle
- **프론트엔드**: React 18+, React Router v6, Redux, Axios, Vite
- **AI 서비스**: FastAPI, LangChain, LangGraph, OpenAI GPT-4o-mini (Vision), EasyOCR, YOLO (ultralytics), ChromaDB, Redis, openpyxl

### AI 기능 목록

| 기능 | 경로 | 사용 기술 | 설명 |
|------|------|-----------|------|
| 키오스크 챗봇 | `ai-service/app/domain/kioskChatbot/` | LangChain, ChatOpenAI, ChromaDB, Redis | RAG 기반 키오스크 안내 챗봇. 화면 ID별 컨텍스트 + 매뉴얼 벡터 검색 + Redis 대화 히스토리 |
| 사용자 챗봇 | `ai-service/app/domain/userchatbot/` | LangGraph, LangChain Tool Use | 예약 조회/생성/취소, 차량 등록, 정기권, 입주민 신청 등 Spring API 연동 Tool Use 에이전트 |
| 엑셀 보고서 생성 | `ai-service/app/domain/report/` | openpyxl, ChatOpenAI | 주간/월간 운영 보고서 엑셀 자동 생성. 요약·매출·사용량 3개 시트 + AI 분석 코멘트 삽입 |
| OCR 정확도 향상 | `ai-service/app/domain/entryexitocr/` | YOLO, EasyOCR, GPT-4o-mini Vision | 번호판 인식: YOLO 크롭 → EasyOCR → 저신뢰도 시 GPT-4o-mini Vision LLM fallback |
| 자동 알림 문구 생성 | `ai-service/app/domain/notification/` | LangGraph, ChatOpenAI | 승인/거절 이벤트 타입별 LangGraph 분기로 개인화 알림 제목·내용 자동 생성 |

---

## 백엔드 패키지 구조

```
com.example.demo
├── api/                        # REST Controllers
│   ├── admin/                  # 관리자 API (/api/admin/**)
│   ├── kiosk/                  # 키오스크 API (/api/kiosk/**)
│   └── user/                   # 사용자 API (/api/user/**)
├── domain/                     # 비즈니스 로직
│   ├── approval/               # 승인
│   ├── auth/                   # 인증 (Admin, User)
│   ├── notification/           # 알림
│   ├── parking/                # 주차 (log, policy, space, entry, exit)
│   ├── payment/                # 결제 (point, subscription, ticket, ticketpolicy)
│   ├── report/                 # 신고
│   ├── reservation/            # 예약
│   ├── resident/               # 거주자 (household, mypage)
│   ├── store/                  # 상가 할인권 설정 (StoreTicketConfig)
│   ├── system/store/           # 상가 (Store, Wallet, Transaction)
│   └── vehicle/                # 차량, 블랙리스트
└── global/                     # 공통 인프라
    ├── config/                 # Security, QueryDSL, S3, Swagger
    ├── exception/              # ErrorCode, GlobalExceptionHandler
    ├── redis/                  # Redis 서비스
    └── security/               # JWT, AuthDto, Filter
```

---

## 구현된 관리자 API 목록

| 경로 | 메서드 | 기능 |
|------|--------|------|
| `/admin/refresh` | POST | 토큰 갱신 |
| `/admin/logout` | POST | 로그아웃 |
| `/admin/parking-space/summary` | GET | 주차공간 요약 |
| `/admin/parking-space` | GET | 층별 주차공간 조회 |
| `/admin/parking-space/{id}/control` | PATCH | 주차공간 상태 변경 |
| `/admin/parking/summary` | GET | 입출차 요약 |
| `/admin/parking/logs` | GET | 입출차 목록 |
| `/admin/parking/logs/{id}` | GET | 입출차 상세 |
| `/admin/parking/logs/{id}/force-exit` | POST | 강제 출차 |
| `/admin/parking/logs/{id}/discount` | PATCH | 할인 수정 |
| `/admin/fee-policy` | GET | 요금 정책 조회 |
| `/admin/fee-policy/change` | POST | 요금 정책 변경 |
| `/admin/fee-policy/history` | GET | 요금 정책 이력 |
| `/admin/ticket-policy` | POST/DELETE/PUT | 할인권 등록/삭제/비활성화 |
| `/admin/approvals` | GET | 승인 목록 |
| `/admin/approvals/{id}/approve` | POST | 승인 |
| `/admin/approvals/{id}/reject` | POST | 거절 |
| `/admin/reports` | GET | 신고 목록 |
| `/admin/reports/{id}/approve` | POST | 신고 승인 |
| `/admin/reports/{id}/reject` | POST | 신고 거절 |
| `/admin/management/users` | GET | 사용자 목록 |
| `/admin/management/users/{id}` | GET | 사용자 상세 |
| `/admin/management/vehicles` | GET | 차량 목록 |
| `/admin/management/vehicles/{id}` | GET | 차량 상세 |
| `/admin/management/subscriptions` | GET | 정기권 목록 |
| `/admin/management/reservations` | GET | 예약 목록 |
| `/admin/system-setting` | GET/PUT | 시스템 설정 조회/수정 |
| `/admin/action-logs` | GET | 액션 로그 목록 |
| `/admin/action-logs/{id}/revert` | POST | 액션 되돌리기 |

---

## AdminActionLog 현황

### 구조
- **Entity**: `AdminActionLog` — admin_action_log 테이블
- **Repository**: `AdminActionLogRepository` — 복합 필터링 + 페이징
- **Service**:
  - `AdminActionLogService` — 로그 기록 헬퍼 (insertPolicyAdminLog, toJson, getAdmin)
  - `AdminActionLogQueryService` — 조회(getActionLogs) + 되돌리기(revert)
- **DTO**: `ActionLogResponseDto`
- **Controller**: `AdminActionLogController`

### Enum
- **ActionType**: CREATE, UPDATE, DELETE, APPROVE, REJECT, REFUND, REPORT, BLACKLIST, ACTIVE, INACTIVE
- **TargetType**: USER, VEHICLE, PAYMENT, POLICY, RESERVATION, SYSTEM_SETTING, STORE, PARKING_LOG, REPORT

### revert 가능 대상 (UPDATE 타입만)
- **POLICY** → ParkingFeePolicy.effectiveTo 복원
- **PARKING_SPACE** → ParkingSpace.status / isDisabled / isEvCharge 복원
- **PARKING_LOG** → ParkingLog.parkingStatus 복원
- **STORE** → Store.name / terminalPassword / status 복원 (추가 예정)

### 로그 기록 중인 서비스
- ParkingFeePolicyService, TicketPolicyService, AdminApprovalService
- AdminParkingService, AdminParkingSpaceService, AdminReportService

### 남은 작업
- `changedFields` 필드 저장 로직 없음
- CREATE/DELETE 타입 revert 미지원

---

## 프론트엔드 구조 (parking_frontend_admin)

```
src/
├── features/
│   ├── auth/               # 로그인
│   ├── dashboard/          # 대시보드 (미구현)
│   ├── parkingspace/       # 주차공간 관리
│   ├── parking-management/ # 입출차 관리
│   ├── fee/                # 요금 관리 (정책/이력/통계 탭)
│   ├── user-vehicle/       # 사용자/차량/예약/정기권/블랙리스트
│   ├── approval/           # 승인관리 + 신고관리 (탭)
│   ├── action-log/         # 관리자 활동내역 ✅ 구현 중
│   └── systemsetting/      # 시스템 설정
└── shared/
    ├── api/adminApi.js     # Axios 인스턴스 (토큰 자동 갱신)
    ├── components/         # Header, Sidebar, Pagination
    └── layouts/Mainlayout.jsx
```

### 라우팅
| 경로 | 컴포넌트 |
|------|---------|
| `/admin` | LoginPage |
| `/admin/dashboard` | DashBoard |
| `/admin/parking-space` | ParkingSpacePage |
| `/admin/entry-exit` | ParkingLogPage |
| `/admin/fee` | Fee |
| `/admin/system-setting` | SystemSettingStatusPage |
| `/admin/action-log` | ActionLogPage |
| `/admin/approval/approval-request` | ApprovalRequestPage |
| `/admin/approval/report` | ReportPage |
| `/admin/user-vehicle/user` | UserVehicleUser |
| `/admin/user-vehicle/vehicle` | UserVehicleVehicle |
| `/admin/user-vehicle/blacklist` | UserVehicleBlacklist |
| `/admin/user-vehicle/reservation` | UserVehicleReservation |
| `/admin/user-vehicle/subscription` | UservehicleSubscription |

### 미구현 페이지
- Dashboard (기본 UI만)
- **상가 관리** (현재 작업 중)

---

## Store(상가) 관련 테이블

| 테이블 | 설명 |
|--------|------|
| `store` | 상가 정보 (name, location, status, terminalPassword) |
| `store_ticket_config` | 상가별 사용 가능 할인권 정책 + monthly_quota |
| `store_ticket_wallet` | 상가별 정책별 잔량 (issuedCount, usedCount, remainingCount) |
| `store_ticket_transaction` | 지갑 변동 로그 (PURCHASE, USE 등) |

### Wallet 로직
- 지갑은 첫 구매 시 자동 생성 (lazy)
- remaining=0인 지갑은 목록에서 숨김 (수정 완료)
- 정책 비활성화 후에도 기존 잔량 사용 가능 (applyTicket에서 정책 status 미검증 — 의도적)

### 상가 관리 페이지 처리 흐름 (작업 예정)
- **입주 (INACTIVE→ACTIVE)**: status=ACTIVE, 새 비밀번호 설정, deleted_at=null
- **퇴거 (ACTIVE→INACTIVE)**: status=INACTIVE, deleted_at=now(), wallet 카운트 전부 0 리셋
- **수정 가능**: name, terminalPassword
- **수정 불가**: location, createdAt

### 필요 API (미구현)
| 경로 | 메서드 | 기능 |
|------|--------|------|
| `/admin/stores` | GET | 전체 상가 목록 |
| `/admin/stores/{id}` | GET | 상가 상세 |
| `/admin/stores/{id}` | PATCH | 이름/비밀번호 수정 |
| `/admin/stores/{id}/activate` | POST | 입주 처리 |
| `/admin/stores/{id}/deactivate` | POST | 퇴거 처리 |
