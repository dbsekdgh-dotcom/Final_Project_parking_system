-- 1. 세대 정보 (HouseHold)
CREATE TABLE household (
household_id BIGINT AUTO_INCREMENT PRIMARY KEY,
unit_no INT NOT NULL UNIQUE,
is_active ENUM('ACTIVE', 'INACTIVE') DEFAULT 'INACTIVE' NOT NULL,
total_visit_count INT DEFAULT 0 NOT NULL,
today_visit_count  INT DEFAULT 0 NOT NULL,
monthly_visit_count INT DEFAULT 0 NOT NULL,
active_reservation_count INT DEFAULT 0 NOT NULL,
created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 2. 사용자 (User)
CREATE TABLE user (
user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
household_id BIGINT NULL,
password VARCHAR(255) NULL,
email VARCHAR(255) NOT NULL UNIQUE,
name VARCHAR(100) NOT NULL,
birth DATE NOT NULL,
phone VARCHAR(30) NOT NULL UNIQUE, -- UNIQUE와 NOT NULL 추가
created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
status ENUM('ACTIVE', 'DELETED') DEFAULT 'ACTIVE' NOT NULL,
deleted_at DATETIME NULL,
CONSTRAINT fk_user_household FOREIGN KEY (household_id) REFERENCES household(household_id)
);
-- 3. 로그인 수단
CREATE TABLE social_account (
social_account_id BIGINT AUTO_INCREMENT PRIMARY KEY,
user_id BIGINT NOT NULL,
provider ENUM('LOCAL','KAKAO','NAVER') NOT NULL,
provider_id VARCHAR(150) NOT NULL,
connected_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
UNIQUE KEY uq_provider (provider, provider_id),
FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
);

-- 4. 차량 (Vehicle)
CREATE TABLE vehicle (
vehicle_id BIGINT AUTO_INCREMENT PRIMARY KEY,
user_id BIGINT NULL,
vehicle_name VARCHAR(100),
car_number VARCHAR(25) NOT NULL UNIQUE,
created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
deleted_at DATETIME NULL,
status ENUM('ACTIVE', 'DELETED') DEFAULT 'ACTIVE' NOT NULL,
CONSTRAINT fk_vehicle_user FOREIGN KEY (user_id) REFERENCES user(user_id)
);
-- 5. 주차 요금 정책 (Parking_Fee_Policy)
CREATE TABLE parking_fee_policy (
parking_fee_policy_id BIGINT AUTO_INCREMENT PRIMARY KEY,
admin_id BIGINT NOT NULL COMMENT '등록한 관리자 ID',
parking_type ENUM('VISIT','RESERVATION') NOT NULL COMMENT '정책 구분',
grace_minutes INT DEFAULT 0 NOT NULL COMMENT '회차 인정 시간 (분)',
base_fee INT DEFAULT 0 NOT NULL,
unit_minutes INT DEFAULT 0 NOT NULL COMMENT '추가 단위 시간 (분)',
unit_fee INT DEFAULT 0 NOT NULL COMMENT '추가 단위 요금',
daily_max_fee INT DEFAULT 0 NOT NULL COMMENT '일 최대 요금',
is_active BOOLEAN DEFAULT TRUE NOT NULL COMMENT '현재 활성화 여부',
effective_from DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '적용 시작 시점',
effective_to DATETIME NOT NULL DEFAULT '3000-01-01 00:00:00' COMMENT '적용 종료 시점',
version BIGINT DEFAULT 1 COMMENT '정책 버전(수정 시 증가)',
created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록한 시간',
CONSTRAINT fk_parking_policy_admin_id FOREIGN KEY (admin_id) REFERENCES admin(admin_id)
);
-- 6. 카메라 (Camera)
CREATE TABLE camera (
camera_id BIGINT AUTO_INCREMENT PRIMARY KEY,
camera_code VARCHAR(100) UNIQUE NOT NULL,
description TEXT,
location VARCHAR(200) NOT NULL,
floor ENUM('B1', 'B2') NOT NULL,
camera_type ENUM('ENTRY', 'EXIT', 'AREA') NOT NULL,
rtsp_url VARCHAR(500) -- 더미용 데이터 삽입가능성 때문에
);
-- 7. 주차 공간 (Parking_Space)
CREATE TABLE parking_space (
parking_space_id BIGINT AUTO_INCREMENT PRIMARY KEY,
space_code VARCHAR(50) UNIQUE NOT NULL,
floor ENUM('B1', 'B2') NOT NULL,
is_reservation BOOLEAN DEFAULT FALSE NOT NULL,
status ENUM('AVAILABLE', 'OCCUPIED', 'BLOCKED') DEFAULT 'AVAILABLE' NOT NULL,
last_status_changed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
is_disabled BOOLEAN DEFAULT FALSE NOT NULL COMMENT '장애인 전용자리',
is_ev_charge BOOLEAN DEFAULT FALSE NOT NULL COMMENT '전기차 전용자리'
);
-- 8. 주차 로그 (Parking_Log)
CREATE TABLE parking_log (
parking_log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
vehicle_id BIGINT NULL,
parking_space_id BIGINT NULL,
car_number_snapshot VARCHAR(25) NOT NULL COMMENT '인식된 번호판 번호',
entry_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '입차 감지 시점',
exit_time DATETIME NULL COMMENT '출차 감지 시점',
entry_camera_id BIGINT NULL COMMENT '입차 인식 카메라',
exit_camera_id BIGINT NULL COMMENT '출차 인식 카메라',
parking_type_snapshot ENUM('RESIDENT','VISIT','USER','RESERVATION',’SUBSCRIPTION’) NOT NULL COMMENT '입차 시점 권한',
is_blacklist BOOLEAN DEFAULT FALSE NOT NULL,
fee INT DEFAULT 0 NOT NULL COMMENT '최종 결제 금액',
payment_status ENUM('NONE', 'UNPAID', 'PAID',’REFUNDED’) DEFAULT 'NONE' NOT NULL,
parking_status ENUM('DETECTED', 'ENTRY_CANCELLED', 'ENTERED', 'EXIT_REQUESTED', 'EXITED', 'FORCE_EXITED') DEFAULT 'DETECTED',
parking_fee_policy_id BIGINT NOT NULL COMMENT '적용된 요금 정책 ID',
calculated_fee BIGINT DEFAULT 0 NOT NULL COMMENT '계산된 발생 요금',
entered_at DATETIME NULL COMMENT '실제 입차 완료(게이트 통과)',
exited_at DATETIME NULL COMMENT '실제 출차 완료(세션 종료)',
paid_at DATETIME NULL COMMENT '결제 완료 시점',
free_exit_until DATETIME NULL COMMENT '무료 출차 가능 데드라인',
grace_minutes_snapshot BIGINT NOT NULL COMMENT '입차 시점 회차 시간(분) 실제 계산되는 값',
entry_plate_image VARCHAR(512) NOT NULL,
exit_plate_image VARCHAR(512) NULL,
row_fee INT DEFAULT 0 NOT NULL,
total_discount_minutes INT DEFAULT 0 NOT NULL,
total_discount_amount INT DEFAULT 0 NOT NULL,
payment_requested_at DATETIME,
CONSTRAINT fk_log_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicle(vehicle_id),
CONSTRAINT fk_log_space FOREIGN KEY (parking_space_id) REFERENCES parking_Space(parking_space_id),
CONSTRAINT fk_log_policy FOREIGN KEY (parking_fee_policy_id) REFERENCES parking_Fee_Policy(parking_fee_policy_id),
CONSTRAINT fk_log_entry_camera FOREIGN KEY (entry_camera_id) REFERENCES camera(camera_id),
CONSTRAINT fk_log_exit_camera FOREIGN KEY (exit_camera_id) REFERENCES camera(camera_id)
);

- - 1. 누락된 계산 관련 컬럼 추가
ALTER TABLE parking_log
ADD COLUMN total_discount_minutes INT DEFAULT 0 ,
ADD COLUMN total_discount_amount INT DEFAULT 0 ,
ADD COLUMN raw_fee INT DEFAULT 0 COMMENT '할인 받기 전 순수요금 ENTERED ~ 결제까지';
- - 2. 결제 요청 시간(스냅샷 유효성 검증용) 추가
ALTER TABLE parking_log
ADD COLUMN payment_requested_at DATETIME NULL COMMENT '요금 조회 및 결제 요청 시점 검증';

-- 9. 방문 예약 (Reservation)
CREATE TABLE reservation (
reservation_id BIGINT AUTO_INCREMENT PRIMARY KEY,
vehicle_id BIGINT NULL COMMENT '방문 차량 ID (등록된 차량일 경우)',
user_id BIGINT NOT NULL COMMENT '신청한 입주민 ID',
car_number VARCHAR(25) NOT NULL COMMENT '방문 차량 번호 (필수)',
visit_start_at DATETIME NOT NULL COMMENT '입차 허용 시작 시간',
visit_end_at DATETIME NOT NULL COMMENT '예약 종료(출차 권장) 시간',
status ENUM(’PENDING’,’REJECTED’,'RESERVED', 'ENTERED', 'NO_SHOW', 'CANCELLED', 'COMPLETED') DEFAULT 'PENDING' NOT NULL,
purpose ENUM('FAMILY', 'FRIEND', 'BUSINESS', 'DELIVERY', 'OTHER') NOT NULL,
actual_entry_at DATETIME ,
is_free BOOLEAN DEFAULT ‘TRUE’ NOT NULL,
created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
cancelled_at DATETIME NULL COMMENT '취소 버튼을 누른 시각',
CONSTRAINT fk_res_host FOREIGN KEY (user_id) REFERENCES user(user_id),
CONSTRAINT fk_res_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicle(vehicle_id)
);

-- 11. 상가 (Store)
CREATE TABLE store (
store_id BIGINT AUTO_INCREMENT PRIMARY KEY,
name VARCHAR(150) NOT NULL,
location VARCHAR(255) NULL,
status ENUM('ACTIVE','INACTIVE') DEFAULT 'ACTIVE' NOT NULL,
terminal_password VARCHAR(255) NOT NULL,
created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
deleted_at DATETIME NULL,
created_by BIGINT NULL,
updated_by BIGINT NULL
);
-- 12. 상가 할인권 설정 (Store_Ticket_Config)
CREATE TABLE store_ticket_config (
store_ticket_config_id BIGINT AUTO_INCREMENT PRIMARY KEY,
store_id BIGINT NOT NULL,
ticket_policy_id BIGINT NOT NULL,
monthly_quota INT DEFAULT 0 NOT NULL,
CONSTRAINT fk_stc_store FOREIGN KEY (store_id) REFERENCES Store(store_id),
CONSTRAINT fk_stc_ticket_policy FOREIGN KEY (ticket_policy_id) REFERENCES ticket_policy(ticket_policy_id ),

UNIQUE KEY uq_store_policy (store_id, ticket_policy_id)
);

-- 13. 할인권 정책 (Ticket_policy)
CREATE TABLE ticket_policy (
ticket_policy_id BIGINT AUTO_INCREMENT PRIMARY KEY,
name VARCHAR(100) NOT NULL,
description TEXT,
price INT DEFAULT 0 NOT NULL,
discount_type ENUM('TIME', 'AMOUNT', 'FREE', 'RATE') NOT NULL,
discount_value INT NOT NULL,
**use_type ENUM(’STORE’,’ADMIN’) DEFAULT ‘STORE’ NOT NULL,**
max_discount_amount INT,
valid_minutes INT,
valid_days INT,
stackable BOOLEAN DEFAULT TRUE NOT NULL,
status ENUM('ACTIVE', 'INACTIVE', 'DELETED') DEFAULT 'ACTIVE' NOT NULL,
is_free_ticket BOOLEAN DEFAULT FALSE NOT NULL,
created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL
);

- ALTER TABLE ticket_policy
ADD COLUMN use_type ENUM('STORE', 'ADMIN') DEFAULT 'STORE' NOT NULL
COMMENT '정책 사용 주체 (상가용 또는 관리자 직접 할인용)';

-- 14. 할인권 지갑 (store_ticket_wallet)
CREATE TABLE store_ticket_wallet (
wallet_id BIGINT AUTO_INCREMENT PRIMARY KEY,
store_id BIGINT NOT NULL,
ticket_policy_id BIGINT NOT NULL,
created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
issued_count INT DEFAULT 0 NOT NULL,
used_count INT DEFAULT 0 NOT NULL,
remaining_count INT DEFAULT 0 NOT NULL,
CONSTRAINT fk_stw_store FOREIGN KEY (store_id) REFERENCES store(store_id),
CONSTRAINT fk_stw_policy FOREIGN KEY (ticket_policy_id) REFERENCES ticket_policy(ticket_policy_id)
);
-- 15. 할인권 수량 변동 로그 (store_ticket_transaction)
CREATE TABLE store_ticket_transaction (
transaction_id BIGINT AUTO_INCREMENT PRIMARY KEY,
wallet_id BIGINT NOT NULL,
store_id BIGINT NOT NULL,
ticket_policy_id BIGINT NOT NULL,
transaction_type ENUM('ISSUE', 'PURCHASE', 'USE', 'EXPIRE', 'ADJUST', 'CANCEL') NOT NULL,
quantity INT NOT NULL,
before_balance INT NOT NULL,
after_balance INT NOT NULL,
reference_type ENUM('PARKING_LOG', 'PAYMENT', 'ADMIN', 'SYSTEM') NOT NULL,
reference_id BIGINT NULL,
created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
created_by_type ENUM('ADMIN', 'STORE', 'SYSTEM') NOT NULL,
created_by_id BIGINT NULL,
CONSTRAINT fk_stt_wallet FOREIGN KEY (wallet_id) REFERENCES store_ticket_wallet(wallet_id),
CONSTRAINT fk_stt_store FOREIGN KEY (store_id) REFERENCES store(store_id),
CONSTRAINT fk_stt_policy FOREIGN KEY (ticket_policy_id) REFERENCES ticket_policy(ticket_policy_id)
);
-- 16. 주차 할인권 매핑 (Parking_ticket)
CREATE TABLE parking_ticket (
parking_ticket_id BIGINT AUTO_INCREMENT PRIMARY KEY,
store_id BIGINT NOT NULL,
parking_log_id BIGINT NOT NULL,
ticket_policy_id BIGINT NOT NULL,
status ENUM (’ADMIN’,’STORE’), DEFAULT ‘STORE’ NOT NULL,
applied_amount INT NOT NULL,
CONSTRAINT fk_pt_store FOREIGN KEY (store_id) REFERENCES store(store_id),
CONSTRAINT fk_pt_log FOREIGN KEY (parking_log_id) REFERENCES parking_log(parking_log_id),
CONSTRAINT fk_pt_policy FOREIGN KEY (ticket_policy_id) REFERENCES ticket_policy(ticket_policy_id)
);
-- 17. 결제 (PAYMENT)
CREATE TABLE payment (
payment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
parking_log_id BIGINT NULL COMMENT '출차 결제 시 참조',
store_id BIGINT NULL COMMENT '할인권 구매 시 참조',
vehicle_id BIGINT NULL COMMENT '정기권 결제 시 참조',
amount BIGINT NOT NULL DEFAULT 0 COMMENT '실제로 사용자가 지불(승인)한 금액 (SUCCESS 시 snapshot과 일치해야 함)',
price_snapshot BIGINT NOT NULL COMMENT '할인이 적용된 후 사용자가 최종적으로 내야 할 청구 금액',
payment_method ENUM('PAY', 'POINT', 'FREE_POLICY') NOT NULL,
payment_status ENUM('READY', 'SUCCESS', 'FAILED', 'CANCELLED'**,’REFUNDED’**) DEFAULT 'READY' NOT NULL,
payment_type ENUM('PARKING', 'SUBSCRIPTION', 'TICKET') NOT NULL,
ticket_quantity INT NULL COMMENT '할인권 구매 시 수량',
external_payment_id VARCHAR(255) NULL COMMENT '결제 후 받는 고유 id',
paid_at DATETIME NULL COMMENT '실제 결제가 성공한 시각',
created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

**refunded_amount INT NOT NULL DEFAULT 0 COMMENT ‘환불 처리된 누적 금액’,**
CONSTRAINT fk_pay_log FOREIGN KEY (parking_log_id) REFERENCES parking_log(parking_log_id),
CONSTRAINT fk_pay_store FOREIGN KEY (store_id) REFERENCES store(store_id),
CONSTRAINT fk_pay_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicle(vehicle_id)
);
-- 18. 정기권 (SUBSCRIPTION)
CREATE TABLE subscription (
subscription_id BIGINT AUTO_INCREMENT PRIMARY KEY,
user_id BIGINT NOT NULL,
vehicle_id BIGINT NULL,
start_date DATETIME NOT NULL,
end_date DATETIME NOT NULL,
status ENUM('ACTIVE', 'EXPIRED', 'CANCELLED', 'REFUNDED') NOT NULL,
payment_id BIGINT NOT NULL,
price INT NOT NULL,
created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
activated_at DATETIME NULL,
cancelled_at DATETIME NULL,
CONSTRAINT fk_sub_user FOREIGN KEY (user_id) REFERENCES user(user_id),
CONSTRAINT fk_sub_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicle(vehicle_id),
CONSTRAINT fk_sub_payment FOREIGN KEY (payment_id) REFERENCES payment(payment_id)

);
-- 19. 관리자 계정 (Admin)
CREATE TABLE admin (
admin_id BIGINT AUTO_INCREMENT PRIMARY KEY,
login_id VARCHAR(100) UNIQUE NOT NULL,
password VARCHAR(255) NOT NULL,
name VARCHAR(100) NOT NULL,
status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE' NOT NULL,
created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
last_login_at DATETIME NULL,
deleted_at DATETIME NULL
);
-- 20. 유저 포인트 (User_Point)
CREATE TABLE user_Point (
user_id BIGINT PRIMARY KEY,
current_point INT DEFAULT 0 NOT NULL,
CONSTRAINT fk_up_user FOREIGN KEY (user_id) REFERENCES user(user_id)
);
-- 21. 포인트 로그 (Point_Log)
CREATE TABLE point_log (
point_log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
payment_id BIGINT NULL,
user_id BIGINT NOT NULL,
change_amount INT NOT NULL,
before_point INT NOT NULL,
after_point INT NOT NULL,
reason ENUM('PAYMENT_EARN', 'PAYMENT_USE', 'REFUND', 'ADMIN_GRANT',’ADMIN_REVOKE’) NOT NULL,
created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
CONSTRAINT fk_pl_user FOREIGN KEY (user_id) REFERENCES user(user_id),
CONSTRAINT fk_pl_payment FOREIGN KEY (payment_id) REFERENCES payment(payment_id)
);

-- 22. 시스템 설정 (System_Setting)
CREATE TABLE system_setting (
setting_key VARCHAR(150) PRIMARY KEY NOT NULL,
setting_value TEXT NOT NULL,
description VARCHAR(255) NULL,

is_editable BOOLEAN NOT NULL DEFAULT TRUE
);

— 행의 모든 이름들은 ENUM으로 관리하겠습니다.

```jsx
INSERT INTO system_setting (settting_key,setting_value)
VALUES ('POST_PAYMENT_GRACE_MINUTES','5') -- 정산 후 회차시간(5= 5분)
```

```jsx

INSERT INTO system_setting (setting_key, setting_value)
VALUES ('PAYMENT_VALID_MINUTES', '5');
```

```jsx
 INSERT INTO system_setting (setting_key, setting_value)
  VALUES ('ENTRY_LOCK', 'mutex'); -- 트랜잭션 LOCK을 걸어두기만을 위한 행
																		  
```

```jsx
  INSERT INTO system_setting (setting_key, setting_value)
  VALUES ('DETECTED_CANCEL_MINUTES', '1') 
```

```jsx
INSERT INTO system_setting (setting_key, setting_value) 
VALUES ('MIN_USAGE_POINT', '100')
ON DUPLICATE KEY UPDATE setting_value = '100';
```

```jsx
INSERT INTO system_setting (setting_key, setting_value) 
VALUES ('PAYMENT_POINT_EARN_RATE', '5')
ON DUPLICATE KEY UPDATE setting_value = '5';
```

```jsx
INSERT INTO system_setting (setting_key, setting_value)
VALUES ('VEHICLE_APPROVAL_EXPIRE_HOURS', '72');
```

---

-- 23. 신고 (Report)
CREATE TABLE report (
report_id BIGINT AUTO_INCREMENT PRIMARY KEY,
reporter_user_id BIGINT NOT NULL COMMENT '신고를 한 유저 ID',
vehicle_id BIGINT NULL COMMENT '신고 당한 차량 ID (차량 번호로 추후 매칭 가능)',
car_number VARCHAR(25) NOT NULL COMMENT '신고 대상 차량 번호',
report_type ENUM('DOUBLE_PARK', 'BLOCKING', 'NOISE', 'ILLEGAL_PARKING', 'OTHER') NOT NULL COMMENT '신고 유형',
description TEXT COMMENT '상세 신고 내용',
image_url VARCHAR(512) NULL COMMENT '증거 사진 URL',
status ENUM('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED') DEFAULT 'PENDING' NOT NULL COMMENT '신고 처리 상태',
admin_id BIGINT NULL COMMENT '해당 신고를 처리한 관리자 ID',
created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '신고 접수 일시',
resolved_at DATETIME NULL COMMENT '관리자 처리 완료 일시',
CONSTRAINT fk_rep_reporter FOREIGN KEY (reporter_user_id) REFERENCES user(user_id),
CONSTRAINT fk_rep_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicle(vehicle_id),
CONSTRAINT fk_rep_admin FOREIGN KEY (admin_id) REFERENCES admin(admin_id)
);

-- 24. 관리자 감사 로그 (Admin_Action_Log)
CREATE TABLE admin_action_log (
action_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '로그 고유 번호',
admin_id BIGINT NOT NULL COMMENT '행위를 수행한 관리자 ID',
target_type ENUM('USER', 'VEHICLE', 'PAYMENT', 'POLICY', 'RESERVATION','SYSTEM_SETTING','STORE','PARKING_LOG','REPORT','RESERVATION_POLICY') NOT NULL COMMENT '대상 도메인 (유저, 차량, 결제, 정책, 예약, 시스템설정)',
action_type ENUM('CREATE', 'UPDATE', 'DELETE', 'APPROVE', 'REJECT', 'REFUND', 'REPORT', 'BLACKLIST') NOT NULL COMMENT '수행 작업 유형',
target_id BIGINT,
before_data JSON NOT NULL,
after_data JSON NOT NULL,
is_reverted BOOLEAN DEFAULT FALSE NOT NULL COMMENT '되돌림 여부',
created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
reverted_by_admin_id BIGINT NULL COMMENT '원복을 수행한 관리자 ID',
reverted_at DATETIME NULL,
changed_fields JSON NULL COMMENT '실제 변경된 필드 목록 및 값',
CONSTRAINT fk_aal_admin FOREIGN KEY (admin_id) REFERENCES admin(admin_id),
CONSTRAINT fk_aal_revert_admin FOREIGN KEY (reverted_by_admin_id) REFERENCES admin(admin_id)
);

-- 25. 신고 누적 테이블
CREATE TABLE vehicle_report_stat (
car_number VARCHAR(25) PRIMARY KEY,
valid_report_count INT DEFAULT 0, 
total_report_count INT DEFAULT 0,
last_reported_at DATETIME,
updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 26. 차단된 자동차 (Vehicle_Blacklist)
CREATE TABLE vehicle_blacklist (
vehicle_blacklist_id BIGINT AUTO_INCREMENT PRIMARY KEY,
vehicle_id BIGINT NULL COMMENT '등록된 차량 ID (비회원일 경우 NULL)',
car_number VARCHAR(25) NOT NULL UNIQUE COMMENT '차단 대상 차량 번호 (필수)',
reason_type ENUM('REPORT_ACCUMULATION', 'ILLEGAL_VEHICLE', 'USER_BLACKLIST', 'ADMIN_MANUAL', 'SYSTEM_BLOCK') NOT NULL,
reason_detail TEXT,
start_date DATETIME NOT NULL COMMENT '차단 시작 일시',
end_date DATETIME NOT NULL COMMENT '차단 종료 예정 일시 (3000년=무기한)',
status ENUM('ACTIVE', 'RELEASED') DEFAULT 'ACTIVE' NOT NULL,
created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '기록 생성 일시',
released_at DATETIME NULL COMMENT '실제 차단 해제 일시',
CONSTRAINT fk_vbl_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicle(vehicle_id)
);
-- 27. 알림 / 안내 (Notification)
CREATE TABLE notification (
notification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
user_id BIGINT NOT NULL COMMENT '알림을 받는 유저 ID',
type ENUM('PAYMENT', 'RESERVATION', 'EVENT', 'WARNING', 'SYSTEM',’REFUNDED’) NOT NULL COMMENT '알림 유형 (결제, 예약, 이벤트, 경고, 시스템)',
title VARCHAR(255) NOT NULL COMMENT '알림 제목',
content TEXT NOT NULL COMMENT '알림 본문 내용',
read_at DATETIME NULL COMMENT '사용자가 알림을 확인한 시각 (NULL이면 미확인)',
created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '알림 생성일',
status ENUM('ACTIVE', 'DELETED') DEFAULT 'ACTIVE' NOT NULL COMMENT '알림 노출 상태',
deleted_at DATETIME NULL COMMENT '알림 삭제 일시',
CONSTRAINT fk_not_user FOREIGN KEY (user_id) REFERENCES user(user_id)
);
-- 28. 방문예약 이벤트 정책 (Reservation_Event_Policy)
CREATE TABLE reservation_event_policy (
reservation_event_policy_id BIGINT AUTO_INCREMENT PRIMARY KEY,
admin_id BIGINT NOT NULL COMMENT '정책을 설정한 관리자 ID',
event_name VARCHAR(200) NOT NULL,
start_date DATETIME NOT NULL,
end_date DATETIME NULL COMMENT '정책 종료 일시 (NULL이면 무기한 적용)',
daily_limit_per_household INT COMMENT '세대별 일일 예약 가능 횟수 (NULL이면 무제한)',
monthly_limit_per_household INT COMMENT '세대별 월간 총 예약 가능 횟수 (NULL이면 무제한)',
max_active_reservations INT DEFAULT 1 NOT NULL COMMENT '동시에 보유 가능한 활성 예약 수',
permitted_minutes INT DEFAULT 60 NOT NULL COMMENT '방문 예약 시 부여되는 주차 허용 시간(분 단위)',
no_show_penalty_enabled BOOLEAN DEFAULT FALSE NOT NULL COMMENT '노쇼 발생 시 주유량 감점 여부',
CONSTRAINT fk_rep_admin_link FOREIGN KEY (admin_id) REFERENCES admin(admin_id)
);
-- 29. 승인 관리 (Approval)
CREATE TABLE approval (
approval_id BIGINT AUTO_INCREMENT PRIMARY KEY,
approval_type ENUM('RESIDENT', 'VEHICLE', 'RESERVATION', 'REPORT') NOT NULL COMMENT '승인 유형',
target_id BIGINT NOT NULL COMMENT '해당 유형 테이블의 PK',
request_user_id BIGINT COMMENT '요청한 유저 ID',
status ENUM('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED') DEFAULT 'PENDING' COMMENT '현재 처리 상태',
created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '승인 요청 일시',
processed_at DATETIME NULL COMMENT '관리자 처리 완료 일시',
processed_by_admin_id BIGINT COMMENT '처리를 담당한 관리자 ID',
reject_reason TEXT COMMENT '거절 시 사유 (사용자 안내용)',
CONSTRAINT fk_app_user FOREIGN KEY (request_user_id) REFERENCES user(user_id),
CONSTRAINT fk_app_admin FOREIGN KEY (processed_by_admin_id) REFERENCES admin(admin_id)
);
-- 30. 녹화 저장(Camera_Recording)
CREATE TABLE camera_recording(
camera_recording_id BIGINT AUTO_INCREMENT PRIMARY KEY,
camera_id BIGINT NOT NULL,
file_path VARCHAR(255) NOT NULL,
start_time DATETIME NOT NULL,
end_time DATETIME NOT NULL,
file_size BIGINT NOT NULL,
created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
CONSTRAINT fk_camera_recording_camera FOREIGN KEY (camera_id) REFERENCES camera(camera_id)
);
SET FOREIGN_KEY_CHECKS = 1;

— 액션 로그

CREATE TABLE activity_log (
activity_id BIGINT AUTO_INCREMENT PRIMARY KEY,
activity_type ENUM(
'ENTRY',                -- 입차
'EXIT',                 -- 출차

'PAYMENT_PRE',          -- 사전정산
'PAYMENT_EXIT',         -- 출차정산
'PAYMENT_CANCEL',       -- 결제취소

'RESERVATION_CREATED',  -- 방문예약 생성
'RESERVATION_CANCELLED',

'VEHICLE_REGISTERED',   -- 차량 등록
'RESIDENT_REGISTERED’, — 입주민 등록
'PASS_PURCHASED',       -- 정기권 구매

'COUPON_PURCHASED',     -- 상가 할인권 구매
'COUPON_USED',          -- 할인 적용

'ADMIN_FORCE_EXIT'      -- 관리자 강제출차

‘REFUNDED’ —환불
) NOT NULL,
parking_log_id BIGINT NULL,
reservation_id BIGINT NULL,
user_id BIGINT NULL,
payment_id BIGINT NULL,
car_number VARCHAR(25) NULL,
household_id BIGINT NULL,
message VARCHAR(255),
created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
CONSTRAINT fk_activity_parking_log FOREIGN KEY (parking_log_id) REFERENCES parking_log(parking_log_id),
CONSTRAINT fk_activity_reservation FOREIGN KEY (reservation_id) REFERENCES reservation(reservation_id),
CONSTRAINT fk_activity_payment FOREIGN KEY (payment_id) REFERENCES payment(payment_id),
CONSTRAINT fk_activity_household FOREIGN KEY (household_id) REFERENCES, household(household_id) ON DELETE SET NULL,
CONSTRAINT fk_activity_user FOREIGN KEY (user_id) REFERENCES
user(user_id)
INDEX idx_activity_time (created_at DESC),
INDEX idx_activity_household (household_id),
INDEX idx_activity_parking_log (parking_log_id)
);

---
