# 🅿️ Seamless Unified Smart Complex Parking Web Service
### 무중단 통합 스마트 복합 주차관제 웹서비스

<p align="center">
  <img src="https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=flat-square&logo=springboot&logoColor=white"/>
  <img src="https://img.shields.io/badge/Java-21-007396?style=flat-square&logo=openjdk&logoColor=white"/>
  <img src="https://img.shields.io/badge/React-19-61DAFB?style=flat-square&logo=react&logoColor=black"/>
  <img src="https://img.shields.io/badge/FastAPI-0.111-009688?style=flat-square&logo=fastapi&logoColor=white"/>
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql&logoColor=white"/>
  <img src="https://img.shields.io/badge/Redis-7.x-DC382D?style=flat-square&logo=redis&logoColor=white"/>
  <img src="https://img.shields.io/badge/AWS-EC2_·_S3_·_CloudFront-FF9900?style=flat-square&logo=amazonaws&logoColor=white"/>
  <img src="https://img.shields.io/badge/Docker-26.x-2496ED?style=flat-square&logo=docker&logoColor=white"/>
  <img src="https://img.shields.io/badge/Jenkins-CI/CD-D24939?style=flat-square&logo=jenkins&logoColor=white"/>
</p>

---

## 📋 프로젝트 개요

복합 주차 시설을 위한 **통합 관제 웹 서비스**입니다.
- **관리자**: 주차공간 현황, 입출차 관리, 요금·할인권 정책, 승인/신고 처리, 사용자·차량·예약 관리, 엑셀 보고서, 실시간 채팅
- **사용자**: 주차 예약, 정기권 구매, 방문예약 신청, 입주민 등록, AI 챗봇 연동, Toss 결제
- **키오스크**: OCR 번호판 인식 자동 입출차, 상가 할인권 적용, 사전 정산, RAG 기반 안내 챗봇
- **AI 서비스**: LangGraph 사용자 챗봇, RAG 키오스크 챗봇, YOLO+EasyOCR+GPT-4o-mini OCR 파이프라인, 엑셀 자동 보고서, 알림 문구 생성

---

## 📅 개발 기간

**2026.04.02 ~ 2026.05.06** (약 5주)

---

## 👥 팀 구성

| 이름 | GitHub | 담당 역할 |
|------|--------|-----------|
| 윤상호 | [@dbsekdgh](https://github.com/dbsekdgh) | 팀장 · CI/CD (Jenkins, Docker, ECR, Blue-Green) · 키오스크 프론트 · 관리자 채팅 · OCR LLM 파이프라인 · 상가 관리 · 보안/JWT 설정 |
| 김보경 | [@KimBoKyung07](https://github.com/KimBoKyung07) | 관리자 JWT인증 및 로그인 · 입출차관리/강제출차/할인수정/주차공간 제어 · LLM기반 Excel보고서 생성 · CI/CD 및 배포 |
| 유승원 | [@ehrbs56](https://github.com/ehrbs56) | 사용자 프론트 · 사용자 AI 챗봇 (LangGraph) · 알림 · 사용자 차량등록OCR · Swagger · 사용자 통합로그인 (Naver/Kakao/local)|
| 최주연 | [@juyeon](https://github.com/juyeon) | 키오스크 챗봇 (RAG) · 결제 (Toss, Redis 락) · Jenkins CI/CD · 키오스크 결제 흐름 · 관리자 요금 정책/조회|
| 이윤진 | [@greathera](https://github.com/greathera) | 사용자 대시보드 · 알림 백엔드/프론트 · 홈 화면 · 블랙리스트 · 사용자 신고(S3 Image저장)|

---

## 🔗 링크

| 항목 | 링크 |
|------|------|
| 배포 주소 (관리자) | https://admin.parking-system.shop |
| 배포 주소 (사용자) | https://user.parking-system.shop |
| 배포 주소 (키오스크) | https://kiosk.parking-system.shop |
| API 문서 (Swagger) | https://parking-system.shop/swagger-ui/index.html |
| ERD | https://drive.google.com/file/d/1sQ_QIhS0_YNGRT3F-SSONS2SX23Wq-9l/view?usp=sharing |
| 노션 | <!-- 링크 추가 예정 --> |

---

## ✨ 주요 기능

### 🚗 입출차 & OCR
- YOLO v8로 번호판 영역 크롭 → EasyOCR 인식 → 저신뢰도 시 GPT-4o-mini Vision fallback
- Naver CLOVA OCR 병행 지원
- 입차·출차 시 자동 번호판 인식 및 주차 로그 생성

### 💰 요금 & 결제
- 시간대별 요금 정책 설정 (관리자)
- Toss Payments SDK 연동 (사용자 결제)
- Redis 분산 락으로 동시 결제 방지
- 사전 정산, 정기권, 포인트, 할인권 통합 정산

### 🏪 상가 할인권
- 상가별 할인권 정책 구매 및 지갑(Wallet) 관리
- 주차 차량 검색 후 할인권 즉시 적용
- 지갑 변동 트랜잭션 이력 관리

### 📅 예약 & 입주민
- 방문 예약 신청 / 관리자 승인·거절
- 입주민 등록 신청 및 정기권 구매
- 예약·입주민 상태별 알림 자동 발송

### 🤖 AI 기능

| 기능 | 기술 | 설명 |
|------|------|------|
| 키오스크 안내 챗봇 | LangChain, ChromaDB, Redis | 화면 컨텍스트 + 매뉴얼 RAG + 대화 히스토리 |
| 사용자 AI 챗봇 | LangGraph, Tool Use | 예약/차량/정기권/입주민 Spring API 연동 에이전트 |
| OCR 정확도 향상 | YOLO, EasyOCR, GPT-4o-mini | 저신뢰도 번호판 LLM Vision fallback |
| 엑셀 보고서 자동 생성 | openpyxl, ChatOpenAI | 주간/월간 운영 보고서 + AI 분석 코멘트 |
| 알림 문구 자동 생성 | LangGraph, ChatOpenAI | 이벤트 타입별 분기로 개인화 알림 생성 |

### 🗨️ 관리자 채팅
- WebSocket STOMP + SockJS 실시간 1:1 / 그룹 채팅
- 셀프 채팅(메모), 그룹 나가기, 안 읽은 메시지 카운트
- 채팅 이력 DB 영구 저장

### 📊 관리자 대시보드
- 주차공간 실시간 현황 (층별 / 상태별)
- 입출차 통계, 매출 분석
- 관리자 활동 로그 및 되돌리기(revert)
- LangChain 기반 엑셀 보고서 다운로드

---

## 🛠️ 기술 스택

### Backend
| 분류 | 기술 |
|------|------|
| Language / Framework | Java 21, Spring Boot 3.x |
| ORM | JPA/Hibernate, QueryDSL |
| Security | Spring Security 6, JWT (Access + Refresh), Redis 세션 |
| Database | MySQL 8.0, Redis 7.x |
| Storage | AWS S3 |
| Messaging | WebSocket STOMP, SockJS |
| External API | Toss Payments, Naver CLOVA OCR |
| Docs | Swagger (SpringDoc OpenAPI 3) |
| Build | Gradle |

### Frontend (3개 분리)
| 앱 | 기술 |
|----|------|
| 관리자 (parking_frontend_admin) | React 19, Vite, Redux Toolkit, Axios, STOMP WebSocket |
| 사용자 (parking_frontend_user) | React 19, Vite, Redux Toolkit, React Query, Toss Payments SDK |
| 키오스크 (parking_frontend_kiosk) | React 18, Vite, Zustand, Axios |

### AI Service
| 분류 | 기술 |
|------|------|
| Language / Framework | Python 3.12, FastAPI |
| LLM / Agent | LangChain, LangGraph, ChatOpenAI (GPT-4o-mini) |
| Vector DB | ChromaDB, FAISS |
| OCR | YOLO v8 (ultralytics), EasyOCR |
| Storage | Redis (대화 히스토리), openpyxl |

### Infra / DevOps
| 분류 | 기술 |
|------|------|
| Cloud | AWS EC2, S3, CloudFront, ALB, ECR |
| Containerization | Docker, Docker Compose |
| CI/CD | Jenkins (Blue-Green 무중단 배포) |

---

## 🏗️ 시스템 아키텍처

```
[사용자/관리자/키오스크 브라우저]
        │  HTTPS / WSS
        ▼
[AWS CloudFront]
   /api/* → ALB → EC2 (Spring Boot)
   /ws/*  → ALB → EC2 (WebSocket)
   /*     → S3  (React Static)
        │
        ▼
[Spring Boot 3.x]  ←→  [MySQL 8.0]
        │           ←→  [Redis 7.x]
        │           ←→  [AWS S3]
        │
        ▼
[FastAPI AI Service]
   ├── LangGraph / LangChain
   ├── ChromaDB / FAISS
   ├── YOLO + EasyOCR
   └── GPT-4o-mini (OpenAI)
```

---

## 📁 프로젝트 구조

```
Final_Project_parking_system/
├── backend/
│   └── parking_backend/
│       └── src/main/java/com/example/demo/
│           ├── api/          # REST Controllers (admin, kiosk, user)
│           ├── domain/       # 비즈니스 로직 (parking, payment, reservation, store ...)
│           └── global/       # Security, JWT, Exception, Redis
├── frontend/
│   ├── parking_frontend_admin/   # 관리자 React 앱
│   ├── parking_frontend_user/    # 사용자 React 앱
│   └── parking_frontend_kiosk/   # 키오스크 React 앱
├── ai-service/
│   └── app/domain/
│       ├── kioskChatbot/    # RAG 키오스크 챗봇
│       ├── userchatbot/     # LangGraph 사용자 챗봇
│       ├── report/          # 엑셀 보고서 생성
│       ├── entryexitocr/    # YOLO + EasyOCR + GPT fallback
│       └── notification/    # 알림 문구 자동 생성
└── ai/
    ├── config/              # AI 서비스 설정
    └── prompts/             # 프롬프트 템플릿
```

---

## 🚀 로컬 실행 방법

### 1. 환경 변수 설정

`backend/parking_backend/src/main/resources/application.yml`에 아래 항목을 설정합니다:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/parking
    username: <DB_USER>
    password: <DB_PASSWORD>
  redis:
    host: localhost
    port: 6379
jwt:
  secret: <JWT_SECRET>
aws:
  s3:
    bucket: <BUCKET_NAME>
    access-key: <ACCESS_KEY>
    secret-key: <SECRET_KEY>
```

### 2. 백엔드 실행

```bash
cd backend/parking_backend
./gradlew bootRun
```

### 3. 프론트엔드 실행

```bash
# 관리자
cd frontend/parking_frontend_admin
npm install && npm run dev

# 사용자
cd frontend/parking_frontend_user
npm install && npm run dev

# 키오스크
cd frontend/parking_frontend_kiosk
npm install && npm run dev
```

### 4. AI 서비스 실행

```bash
cd ai-service
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8000
```

---

## 📄 API 문서

Swagger UI: `{서버주소}/swagger-ui/index.html`

주요 API 그룹:
- `1. 관리자 (Admin)` — 주차·요금·승인·신고·사용자·시스템 관리
- `2. 사용자 (User)` — 예약·결제·정기권·입주민·챗봇
- `3. 키오스크 (Kiosk)` — OCR 입출차·사전정산·결제
- `4. AI` — 챗봇·OCR·보고서
- `5. 상가 (Store)` — 상가 로그인·지갑·할인권 구매·적용

---

## 📝 ERD

<!-- ERD 이미지 또는 링크 추가 예정 -->

---

<p align="center">
  <sub>ⓒ 2026 Seamless Parking Team. All rights reserved.</sub>
</p>
