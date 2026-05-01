SET FOREIGN_KEY_CHECKS = 0;

-- 1. 세대 정보 (Household)
CREATE TABLE household (
    household_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    unit_no INT NOT NULL UNIQUE,
    is_active ENUM('ACTIVE', 'INACTIVE') DEFAULT 'INACTIVE' NOT NULL,
    total_visit_count INT DEFAULT 0 NOT NULL,
    today_visit_count INT DEFAULT 0 NOT NULL,
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
    phone VARCHAR(30) NOT NULL UNIQUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    status ENUM('ACTIVE', 'DELETED') DEFAULT 'ACTIVE' NOT NULL,
    deleted_at DATETIME NULL,
    CONSTRAINT fk_user_household FOREIGN KEY (household_id) REFERENCES household(household_id)
);

-- 3. 로그인 수단 (SocialAccount)
CREATE TABLE social_account (
    social_account_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    provider ENUM('LOCAL', 'KAKAO', 'NAVER') NOT NULL,
    provider_id VARCHAR(150) NOT NULL,
    connected_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UNIQUE KEY uq_provider (provider, provider_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
);

-- 4. 차량 (Vehicle)
-- [수정] status ENUM: PENDING 추가
CREATE TABLE vehicle (
    vehicle_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,
    vehicle_name VARCHAR(100),
    car_number VARCHAR(25) NOT NULL UNIQUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted_at DATETIME NULL,
    status ENUM('ACTIVE', 'DELETED', 'PENDING') DEFAULT 'ACTIVE' NOT NULL,
    CONSTRAINT fk_vehicle_user FOREIGN KEY (user_id) REFERENCES user(user_id)
);

-- 5. 관리자 계정 (Admin)
-- 다른 테이블에서 FK 참조하므로 앞쪽에 생성
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

-- 6. 주차 요금 정책 (Parking_Fee_Policy)
CREATE TABLE parking_fee_policy (
    parking_fee_policy_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_id BIGINT NOT NULL COMMENT '등록한 관리자 ID',
    parking_type ENUM('VISIT', 'RESERVATION') NOT NULL COMMENT '정책 구분',
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

-- 7. 카메라 (Camera)
CREATE TABLE camera (
    camera_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    camera_code VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    location VARCHAR(200) NOT NULL,
    floor ENUM('B1', 'B2') NOT NULL,
    camera_type ENUM('ENTRY', 'EXIT', 'AREA') NOT NULL,
    rtsp_url VARCHAR(500)
);

-- 8. 주차 공간 (Parking_Space)
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

-- 9. 주차 로그 (Parking_Log)
-- [수정] parking_status ENUM: BLACKLIST_REJECTED 추가
-- [수정] row_fee → raw_fee (오타 수정)
-- [제거] 중복 ALTER TABLE 구문 (해당 컬럼이 이미 CREATE TABLE에 포함됨)
CREATE TABLE parking_log (
    parking_log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id BIGINT NULL,
    parking_space_id BIGINT NULL,
    car_number_snapshot VARCHAR(25) NOT NULL COMMENT '인식된 번호판 번호',
    entry_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '입차 감지 시점',
    exit_time DATETIME NULL COMMENT '출차 감지 시점',
    entry_camera_id BIGINT NULL COMMENT '입차 인식 카메라',
    exit_camera_id BIGINT NULL COMMENT '출차 인식 카메라',
    parking_type_snapshot ENUM('RESIDENT', 'VISIT', 'USER', 'RESERVATION', 'SUBSCRIPTION') NOT NULL COMMENT '입차 시점 권한',
    is_blacklist BOOLEAN DEFAULT FALSE NOT NULL,
    fee INT DEFAULT 0 NOT NULL COMMENT '최종 결제 금액',
    latest_order_id VARCHAR(512) NULL,
    payment_status ENUM('NONE', 'UNPAID', 'PAID', 'REFUNDED') DEFAULT 'NONE' NOT NULL,
    parking_status ENUM('DETECTED', 'ENTRY_CANCELLED', 'ENTERED', 'EXIT_REQUESTED', 'EXITED', 'FORCE_EXITED', 'BLACKLIST_REJECTED') DEFAULT 'DETECTED',
    parking_fee_policy_id BIGINT NOT NULL COMMENT '적용된 요금 정책 ID',
    calculated_fee BIGINT DEFAULT 0 NOT NULL COMMENT '계산된 발생 요금',
    entered_at DATETIME NULL COMMENT '실제 입차 완료(게이트 통과)',
    exited_at DATETIME NULL COMMENT '실제 출차 완료(세션 종료)',
    paid_at DATETIME NULL COMMENT '결제 완료 시점',
    free_exit_until DATETIME NULL COMMENT '무료 출차 가능 데드라인',
    grace_minutes_snapshot BIGINT NOT NULL COMMENT '입차 시점 회차 시간(분) 실제 계산되는 값',
    entry_plate_image VARCHAR(512) NOT NULL,
    exit_plate_image VARCHAR(512) NULL,
    raw_fee INT DEFAULT 0 NOT NULL COMMENT '할인 받기 전 순수요금',
    total_discount_minutes INT DEFAULT 0 NOT NULL,
    total_discount_amount INT DEFAULT 0 NOT NULL,
    payment_requested_at DATETIME NULL COMMENT '요금 조회 및 결제 요청 시점 검증',
    CONSTRAINT fk_log_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicle(vehicle_id),
    CONSTRAINT fk_log_space FOREIGN KEY (parking_space_id) REFERENCES parking_space(parking_space_id),
    CONSTRAINT fk_log_policy FOREIGN KEY (parking_fee_policy_id) REFERENCES parking_fee_policy(parking_fee_policy_id),
    CONSTRAINT fk_log_entry_camera FOREIGN KEY (entry_camera_id) REFERENCES camera(camera_id),
    CONSTRAINT fk_log_exit_camera FOREIGN KEY (exit_camera_id) REFERENCES camera(camera_id)
);

-- 10. 방문 예약 (Reservation)
CREATE TABLE reservation (
    reservation_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id BIGINT NULL COMMENT '방문 차량 ID (등록된 차량일 경우)',
    user_id BIGINT NOT NULL COMMENT '신청한 입주민 ID',
    car_number VARCHAR(25) NOT NULL COMMENT '방문 차량 번호 (필수)',
    visit_start_at DATETIME NOT NULL COMMENT '입차 허용 시작 시간',
    visit_end_at DATETIME NOT NULL COMMENT '예약 종료(출차 권장) 시간',
    status ENUM('PENDING', 'REJECTED', 'RESERVED', 'ENTERED', 'NO_SHOW', 'CANCELLED', 'COMPLETED') DEFAULT 'PENDING' NOT NULL,
    purpose ENUM('FAMILY', 'FRIEND', 'BUSINESS', 'DELIVERY', 'OTHER') NOT NULL,
    actual_entry_at DATETIME NULL,
    is_free BOOLEAN DEFAULT TRUE NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    cancelled_at DATETIME NULL COMMENT '취소 버튼을 누른 시각',
    CONSTRAINT fk_res_host FOREIGN KEY (user_id) REFERENCES user(user_id),
    CONSTRAINT fk_res_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicle(vehicle_id)
);

-- 11. 상가 (Store)
-- [수정] created_by, updated_by FK 제약조건 추가
CREATE TABLE store (
    store_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    location VARCHAR(255) NULL,
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE' NOT NULL,
    terminal_password VARCHAR(255) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    CONSTRAINT fk_store_created_by FOREIGN KEY (created_by) REFERENCES admin(admin_id),
    CONSTRAINT fk_store_updated_by FOREIGN KEY (updated_by) REFERENCES admin(admin_id)
);

-- 12. 할인권 정책 (Ticket_Policy)
-- [수정] 마크다운 ** 기호 제거, use_type 컬럼 정상화
-- [제거] 중복 ALTER TABLE 구문
CREATE TABLE ticket_policy (
    ticket_policy_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price INT DEFAULT 0 NOT NULL,
    discount_type ENUM('TIME', 'AMOUNT', 'FREE', 'RATE') NOT NULL,
    discount_value INT NOT NULL,
    use_type ENUM('STORE', 'ADMIN') DEFAULT 'STORE' NOT NULL COMMENT '정책 사용 주체 (상가용 또는 관리자 직접 할인용)',
    max_discount_amount INT,
    valid_minutes INT,
    valid_days INT,
    stackable BOOLEAN DEFAULT TRUE NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE', 'DELETED') DEFAULT 'ACTIVE' NOT NULL,
    is_free_ticket BOOLEAN DEFAULT FALSE NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 13. 상가 할인권 설정 (Store_Ticket_Config)
CREATE TABLE store_ticket_config (
    store_ticket_config_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    store_id BIGINT NOT NULL,
    ticket_policy_id BIGINT NOT NULL,
    monthly_quota INT DEFAULT 0 NOT NULL,
    last_issued_at DATETIME NULL,
    CONSTRAINT fk_stc_store FOREIGN KEY (store_id) REFERENCES store(store_id),
    CONSTRAINT fk_stc_ticket_policy FOREIGN KEY (ticket_policy_id) REFERENCES ticket_policy(ticket_policy_id),
    UNIQUE KEY uq_store_policy (store_id, ticket_policy_id)
);

-- 14. 할인권 지갑 (Store_Ticket_Wallet)
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

-- 15. 할인권 수량 변동 로그 (Store_Ticket_Transaction)
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

-- 16. 주차 할인권 매핑 (Parking_Ticket)
-- [수정] status ENUM 뒤 콤마 문법 오류 수정
CREATE TABLE parking_ticket (
    parking_ticket_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    store_id BIGINT NOT NULL,
    parking_log_id BIGINT NOT NULL,
    ticket_policy_id BIGINT NOT NULL,
    status ENUM('ADMIN', 'STORE') DEFAULT 'STORE' NOT NULL,
    applied_amount INT NOT NULL,
    CONSTRAINT fk_pt_store FOREIGN KEY (store_id) REFERENCES store(store_id),
    CONSTRAINT fk_pt_log FOREIGN KEY (parking_log_id) REFERENCES parking_log(parking_log_id),
    CONSTRAINT fk_pt_policy FOREIGN KEY (ticket_policy_id) REFERENCES ticket_policy(ticket_policy_id)
);

-- 17. 결제 (Payment)
-- [수정] 마크다운 ** 기호 제거, payment_status ENUM 정상화 (REFUNDED 포함)
CREATE TABLE payment (
    payment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parking_log_id BIGINT NULL COMMENT '출차 결제 시 참조',
    store_id BIGINT NULL COMMENT '할인권 구매 시 참조',
    vehicle_id BIGINT NULL COMMENT '정기권 결제 시 참조',
    amount BIGINT NOT NULL DEFAULT 0 COMMENT '실제로 사용자가 지불(승인)한 금액',
    price_snapshot BIGINT NOT NULL COMMENT '할인이 적용된 후 사용자가 최종적으로 내야 할 청구 금액',
    payment_method ENUM('PAY', 'POINT', 'FREE_POLICY') NOT NULL,
    payment_status ENUM('READY', 'SUCCESS', 'FAILED', 'CANCELLED', 'REFUNDED') DEFAULT 'READY' NOT NULL,
    payment_type ENUM('PARKING', 'SUBSCRIPTION', 'TICKET') NOT NULL,
    ticket_quantity INT NULL COMMENT '할인권 구매 시 수량',
    external_payment_id VARCHAR(255) NULL COMMENT '결제 후 받는 고유 id',
    paid_at DATETIME NULL COMMENT '실제 결제가 성공한 시각',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    refunded_amount INT NOT NULL DEFAULT 0 COMMENT '환불 처리된 누적 금액',
    CONSTRAINT fk_pay_log FOREIGN KEY (parking_log_id) REFERENCES parking_log(parking_log_id),
    CONSTRAINT fk_pay_store FOREIGN KEY (store_id) REFERENCES store(store_id),
    CONSTRAINT fk_pay_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicle(vehicle_id)
);

-- 18. 정기권 (Subscription)
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

-- 19. 유저 포인트 (User_Point)
CREATE TABLE user_point (
    user_id BIGINT PRIMARY KEY,
    current_point INT DEFAULT 0 NOT NULL,
    CONSTRAINT fk_up_user FOREIGN KEY (user_id) REFERENCES user(user_id)
);

-- 20. 포인트 로그 (Point_Log)
-- [수정] description 컬럼 추가 (엔티티에 존재하나 스키마에 누락됨)
CREATE TABLE point_log (
    point_log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_id BIGINT NULL,
    user_id BIGINT NOT NULL,
    change_amount INT NOT NULL,
    before_point INT NOT NULL,
    after_point INT NOT NULL,
    reason ENUM('PAYMENT_EARN', 'PAYMENT_USE', 'REFUND', 'ADMIN_GRANT', 'ADMIN_REVOKE') NOT NULL,
    description VARCHAR(255) NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_pl_user FOREIGN KEY (user_id) REFERENCES user(user_id),
    CONSTRAINT fk_pl_payment FOREIGN KEY (payment_id) REFERENCES payment(payment_id)
);

-- 21. 시스템 설정 (System_Setting)
CREATE TABLE system_setting (
    setting_key VARCHAR(150) PRIMARY KEY NOT NULL,
    setting_value TEXT NOT NULL,
    description VARCHAR(255) NULL,
    is_editable BOOLEAN NOT NULL DEFAULT TRUE
);

-- [수정] 마크다운 코드블록 → 실행 가능한 INSERT 문으로 변환, setting_key 오타 수정
INSERT INTO system_setting (setting_key, setting_value, description, is_editable) VALUES
('POST_PAYMENT_GRACE_MINUTES', '5',   '정산 후 무료 출차 허용 시간(분)',         TRUE),
('PAYMENT_VALID_MINUTES',      '5',   '결제 유효 시간(분)',                        TRUE),
('ENTRY_LOCK',                 'mutex','입차 트랜잭션 LOCK 키',                   FALSE),
('DETECTED_CANCEL_MINUTES',    '1',   '미입차 자동 취소 대기 시간(분)',           TRUE),
('MIN_USAGE_POINT',            '100', '포인트 최소 사용 단위',                    TRUE),
('PAYMENT_POINT_EARN_RATE',    '5',   '결제 금액 대비 포인트 적립율(%)',          TRUE),
('VEHICLE_APPROVAL_EXPIRE_HOURS', '72','차량 등록 승인 만료 시간(시간)',          TRUE);

-- 22. 신고 (Report)
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

-- 23. 관리자 감사 로그 (Admin_Action_Log)
-- [수정] action_type ENUM: ACTIVE, INACTIVE 추가
-- [수정] target_type ENUM: PARKING_SPACE 추가
CREATE TABLE admin_action_log (
    action_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '로그 고유 번호',
    admin_id BIGINT NOT NULL COMMENT '행위를 수행한 관리자 ID',
    target_type ENUM('USER', 'VEHICLE', 'PAYMENT', 'POLICY', 'RESERVATION', 'SYSTEM_SETTING', 'STORE', 'PARKING_LOG', 'PARKING_SPACE', 'REPORT', 'RESERVATION_POLICY') NOT NULL COMMENT '대상 도메인',
    action_type ENUM('CREATE', 'UPDATE', 'DELETE', 'APPROVE', 'REJECT', 'REFUND', 'REPORT', 'BLACKLIST', 'ACTIVE', 'INACTIVE') NOT NULL COMMENT '수행 작업 유형',
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

-- 24. 신고 누적 테이블 (Vehicle_Report_Stat)
CREATE TABLE vehicle_report_stat (
    car_number VARCHAR(25) PRIMARY KEY,
    valid_report_count INT DEFAULT 0,
    total_report_count INT DEFAULT 0,
    last_reported_at DATETIME,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 25. 차단된 자동차 (Vehicle_Blacklist)
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

-- 26. 알림 / 안내 (Notification)
CREATE TABLE notification (
    notification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '알림을 받는 유저 ID',
    type ENUM('PAYMENT', 'RESERVATION', 'EVENT', 'WARNING', 'SYSTEM', 'REFUNDED') NOT NULL COMMENT '알림 유형',
    title VARCHAR(255) NOT NULL COMMENT '알림 제목',
    content TEXT NOT NULL COMMENT '알림 본문 내용',
    read_at DATETIME NULL COMMENT '사용자가 알림을 확인한 시각 (NULL이면 미확인)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '알림 생성일',
    status ENUM('ACTIVE', 'DELETED') DEFAULT 'ACTIVE' NOT NULL COMMENT '알림 노출 상태',
    deleted_at DATETIME NULL COMMENT '알림 삭제 일시',
    CONSTRAINT fk_not_user FOREIGN KEY (user_id) REFERENCES user(user_id)
);

-- 27. 방문예약 이벤트 정책 (Reservation_Event_Policy)
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
    no_show_penalty_enabled BOOLEAN DEFAULT FALSE NOT NULL COMMENT '노쇼 발생 시 페널티 여부',
    CONSTRAINT fk_rep_admin_link FOREIGN KEY (admin_id) REFERENCES admin(admin_id)
);

-- 28. 승인 관리 (Approval)
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

-- 29. 녹화 저장 (Camera_Recording)
CREATE TABLE camera_recording (
    camera_recording_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    camera_id BIGINT NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    file_size BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_camera_recording_camera FOREIGN KEY (camera_id) REFERENCES camera(camera_id)
);

-- 30. 활동 로그 (Activity_Log)
-- [수정] ENUM: 'ADMIN_FORCE_EXIT' 뒤 콤마 누락 수정, 'REFUNDED' 정상 추가
-- [수정] REFERENCES 뒤 콤마 오타 수정, REFERENCES 개행 문제 수정
-- [수정] INDEX 위치를 CONSTRAINT 뒤로 이동하여 문법 오류 수정
CREATE TABLE activity_log (
    activity_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    activity_type ENUM(
        'ENTRY',
        'EXIT',
        'PAYMENT_PRE',
        'PAYMENT_EXIT',
        'PAYMENT_CANCEL',
        'RESERVATION_CREATED',
        'RESERVATION_CANCELLED',
        'VEHICLE_REGISTERED',
        'RESIDENT_REGISTERED',
        'PASS_PURCHASED',
        'COUPON_PURCHASED',
        'COUPON_USED',
        'ADMIN_FORCE_EXIT',
        'REFUNDED'
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
    CONSTRAINT fk_activity_household FOREIGN KEY (household_id) REFERENCES household(household_id) ON DELETE SET NULL,
    CONSTRAINT fk_activity_user FOREIGN KEY (user_id) REFERENCES user(user_id),
    INDEX idx_activity_time (created_at),
    INDEX idx_activity_household (household_id),
    INDEX idx_activity_parking_log (parking_log_id)
);

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================================
-- 인덱스 (Indexes)
-- MySQL InnoDB FK 컬럼은 자동 단일 인덱스 생성됨
-- → 단일 FK 컬럼 인덱스는 생략, 복합·비FK 컬럼 위주로 생성
-- =============================================================================

-- parking_log: 전체에서 가장 빈번하게 조회되는 테이블
-- car_number_snapshot + parking_status: 입출차 조회의 핵심 조합
CREATE INDEX idx_pl_car_status      ON parking_log (car_number_snapshot, parking_status);
-- parking_status 단독: 강제출차, 상태별 필터링
CREATE INDEX idx_pl_parking_status  ON parking_log (parking_status);
-- payment_status 단독: 미결제 조회
CREATE INDEX idx_pl_payment_status  ON parking_log (payment_status);
-- entry_time: 날짜 범위 통계 조회
CREATE INDEX idx_pl_entry_time      ON parking_log (entry_time);
-- exited_at: 출차 완료 여부 + 날짜 범위 필터링
CREATE INDEX idx_pl_exited_at       ON parking_log (exited_at);
-- vehicle_id + parking_status: 특정 차량의 현재 주차 상태 조회 (FK 복합)
CREATE INDEX idx_pl_vehicle_status  ON parking_log (vehicle_id, parking_status);

-- reservation: 예약 조회 패턴이 다양하고 빈도 높음
-- car_number + status: 입차 가능 예약 확인 핵심 조합
CREATE INDEX idx_res_car_status     ON reservation (car_number, status);
-- user_id + status: 사용자별 활성 예약 목록 (FK 복합)
CREATE INDEX idx_res_user_status    ON reservation (user_id, status);
-- status + visit_end_at: 노쇼 스케줄러가 만료 예약 일괄 처리
CREATE INDEX idx_res_status_end     ON reservation (status, visit_end_at);
-- created_at + status: 기간별 예약 통계
CREATE INDEX idx_res_created_status ON reservation (created_at, status);

-- subscription: 정기권 유효성 검사가 입출차마다 발생
-- vehicle_id + status + end_date: 차량별 유효한 정기권 조회 핵심 (FK 복합)
CREATE INDEX idx_sub_vehicle_status_end ON subscription (vehicle_id, status, end_date);
-- user_id + status: 사용자별 정기권 목록 (FK 복합)
CREATE INDEX idx_sub_user_status        ON subscription (user_id, status);
-- status + end_date: 만료 예정 정기권 스케줄러
CREATE INDEX idx_sub_status_end         ON subscription (status, end_date);

-- notification: 알림 조회는 로그인 후 매번 발생
-- user_id + status: 사용자 알림 목록 (FK 복합)
CREATE INDEX idx_noti_user_status       ON notification (user_id, status);
-- user_id + read_at + status: 미읽음 개수 조회
CREATE INDEX idx_noti_user_read_status  ON notification (user_id, read_at, status);

-- approval: 승인 목록 필터링 + 중복 신청 방지 exists 쿼리
-- status 단독: 대기중 개수 카운트
CREATE INDEX idx_app_status             ON approval (status);
-- request_user_id + approval_type + status: 중복 신청 방지 exists (FK 복합)
CREATE INDEX idx_app_user_type_status   ON approval (request_user_id, approval_type, status);
-- target_id + approval_type + status: 특정 대상의 승인 상태 확인 (비FK 복합)
CREATE INDEX idx_app_target_type_status ON approval (target_id, approval_type, status);

-- vehicle
-- user_id + status: 사용자 등록 차량 목록 (FK 복합)
CREATE INDEX idx_veh_user_status        ON vehicle (user_id, status);
-- status 단독: 전체 차량 수 카운트
CREATE INDEX idx_veh_status             ON vehicle (status);

-- payment: 결제 상태 확인과 통계 쿼리 빈번
-- parking_log_id + payment_status: 특정 주차건의 결제 상태 확인 (FK 복합)
CREATE INDEX idx_pay_log_status         ON payment (parking_log_id, payment_status);
-- external_payment_id: Toss 결제 콜백 시 단건 조회
CREATE INDEX idx_pay_external_id        ON payment (external_payment_id);
-- payment_status + paid_at: 기간별 매출 통계
CREATE INDEX idx_pay_status_paid        ON payment (payment_status, paid_at);
-- payment_type + payment_status: 유형별 통계
CREATE INDEX idx_pay_type_status        ON payment (payment_type, payment_status);

-- admin_action_log: 관리자 활동 필터링
-- target_type + action_type: 대상/행동 유형 복합 필터
CREATE INDEX idx_aal_target_action      ON admin_action_log (target_type, action_type);
-- created_at: 날짜 정렬 및 범위 조회
CREATE INDEX idx_aal_created_at         ON admin_action_log (created_at);

-- report: 신고 목록 조회
-- reporter_user_id + status: 내가 신고한 목록 (FK 복합)
CREATE INDEX idx_rep_reporter_status    ON report (reporter_user_id, status);
-- status 단독: 처리 대기중 개수 카운트
CREATE INDEX idx_rep_status             ON report (status);
-- car_number: 차량번호로 신고 이력 조회
CREATE INDEX idx_rep_car_number         ON report (car_number);

-- activity_log: created_at, household_id, parking_log_id는 CREATE TABLE에서 이미 생성
-- user_id 단독: 사용자별 활동 목록 (FK이지만 자주 단독 조회)
CREATE INDEX idx_act_user_id            ON activity_log (user_id);
-- car_number + activity_type + created_at: 관리자 활동내역 복합 필터
CREATE INDEX idx_act_car_type_time      ON activity_log (car_number, activity_type, created_at);

-- store_ticket_transaction: 상가 할인권 구매·사용 통계
-- transaction_type + created_at: 유형별 기간 통계
CREATE INDEX idx_stt_type_created       ON store_ticket_transaction (transaction_type, created_at);

-- vehicle_blacklist: 입차 때마다 실시간 조회
-- car_number는 UNIQUE(자동 인덱스), + status 복합으로 유효한 차단만 필터
CREATE INDEX idx_vbl_car_status         ON vehicle_blacklist (car_number, status);

-- parking_fee_policy: 입차 시 적용 정책 조회
-- parking_type + is_active + effective_from: 현재 활성 정책 조회 핵심 조합
CREATE INDEX idx_pfp_type_active_from   ON parking_fee_policy (parking_type, is_active, effective_from);

-- parking_space: 층별·상태별 공간 조회
CREATE INDEX idx_ps_floor_status        ON parking_space (floor, status);
CREATE INDEX idx_ps_status              ON parking_space (status);

-- ticket_policy: 할인권 정책 조회
CREATE INDEX idx_tp_use_status          ON ticket_policy (use_type, status);

-- household: 활성화 여부 필터링
CREATE INDEX idx_hh_is_active           ON household (is_active);

-- store: 상가 목록 검색
CREATE INDEX idx_store_status_name      ON store (status, name);

-- camera: 타입별 카메라 조회
CREATE INDEX idx_camera_type            ON camera (camera_type);

-- store_ticket_wallet: 상가+정책 조합 조회 (FK 복합)
CREATE INDEX idx_stw_store_policy       ON store_ticket_wallet (store_id, ticket_policy_id);

-- store_ticket_config: 상가별 설정 조회 (FK이지만 자주 단독 조회)
CREATE INDEX idx_stc_store_id           ON store_ticket_config (store_id);

-- =============================================================================
-- 초기 데이터 (Seed Data)
-- =============================================================================

-- Camera: 입구 게이트 A/B, 출구 게이트 A/B, 내부 구역 카메라 4개
INSERT INTO camera (camera_code, description, location, floor, camera_type) VALUES
('ENTRY-A',   '입구 게이트 A 카메라', '지하주차장 입구 게이트 A', 'B1', 'ENTRY'),
('ENTRY-B',   '입구 게이트 B 카메라', '지하주차장 입구 게이트 B', 'B1', 'ENTRY'),
('EXIT-A',    '출구 게이트 A 카메라', '지하주차장 출구 게이트 A', 'B1', 'EXIT'),
('EXIT-B',    '출구 게이트 B 카메라', '지하주차장 출구 게이트 B', 'B1', 'EXIT'),
('AREA-B1-1', 'B1층 내부 카메라 1',   'B1층 구역 1 (A열)',        'B1', 'AREA'),
('AREA-B1-2', 'B1층 내부 카메라 2',   'B1층 구역 2 (B열)',        'B1', 'AREA'),
('AREA-B2-1', 'B2층 내부 카메라 1',   'B2층 구역 1 (A열)',        'B2', 'AREA'),
('AREA-B2-2', 'B2층 내부 카메라 2',   'B2층 구역 2 (B열)',        'B2', 'AREA');

-- Parking Space: B1층 30개 (B1-001 ~ B1-030), B2층 30개 (B2-001 ~ B2-030), 전부 AVAILABLE
INSERT INTO parking_space (space_code, floor, is_reservation, status, is_disabled, is_ev_charge)
SELECT CONCAT('B1-', LPAD(n, 3, '0')), 'B1', FALSE, 'AVAILABLE', FALSE, FALSE
FROM (
    SELECT a.n + b.n * 10 + 1 AS n
    FROM (SELECT 0 n UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
          UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) a
    CROSS JOIN (SELECT 0 n UNION SELECT 1 UNION SELECT 2) b
    HAVING n BETWEEN 1 AND 30
    ORDER BY n
) seq;

INSERT INTO parking_space (space_code, floor, is_reservation, status, is_disabled, is_ev_charge)
SELECT CONCAT('B2-', LPAD(n, 3, '0')), 'B2', FALSE, 'AVAILABLE', FALSE, FALSE
FROM (
    SELECT a.n + b.n * 10 + 1 AS n
    FROM (SELECT 0 n UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
          UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) a
    CROSS JOIN (SELECT 0 n UNION SELECT 1 UNION SELECT 2) b
    HAVING n BETWEEN 1 AND 30
    ORDER BY n
) seq;

-- Household: 101호 ~ 120호, 전부 INACTIVE
INSERT INTO household (unit_no, is_active, total_visit_count, today_visit_count, monthly_visit_count, active_reservation_count) VALUES
(101, 'INACTIVE', 0, 0, 0, 0),
(102, 'INACTIVE', 0, 0, 0, 0),
(103, 'INACTIVE', 0, 0, 0, 0),
(104, 'INACTIVE', 0, 0, 0, 0),
(105, 'INACTIVE', 0, 0, 0, 0),
(106, 'INACTIVE', 0, 0, 0, 0),
(107, 'INACTIVE', 0, 0, 0, 0),
(108, 'INACTIVE', 0, 0, 0, 0),
(109, 'INACTIVE', 0, 0, 0, 0),
(110, 'INACTIVE', 0, 0, 0, 0),
(111, 'INACTIVE', 0, 0, 0, 0),
(112, 'INACTIVE', 0, 0, 0, 0),
(113, 'INACTIVE', 0, 0, 0, 0),
(114, 'INACTIVE', 0, 0, 0, 0),
(115, 'INACTIVE', 0, 0, 0, 0),
(116, 'INACTIVE', 0, 0, 0, 0),
(117, 'INACTIVE', 0, 0, 0, 0),
(118, 'INACTIVE', 0, 0, 0, 0),
(119, 'INACTIVE', 0, 0, 0, 0),
(120, 'INACTIVE', 0, 0, 0, 0);

-- Admin: admin01 ~ admin05, 비밀번호 '1234' BCrypt 해시 (strength=10)
-- $2b$ 접두사는 Spring Security BCryptPasswordEncoder와 완전 호환
INSERT INTO admin (login_id, password, name, status) VALUES
('admin01', '$2b$10$wMi1vLBRCPuyqqiNleEHru9jliBKBxTZATHC7c62xBH2RbB9u/p0i', '윤상호', 'ACTIVE'),
('admin02', '$2b$10$LqvaM0IwX5zBT7hqDim5t.LYky4bJcr7aorkNVrXo08..ZqqyZV1e', '김보경', 'ACTIVE'),
('admin03', '$2b$10$L1UDD5O2.7oqjhLu7yP63elnvEp.bNBJbLKQDXwIECfjI9wqPanzS', '최주연', 'ACTIVE'),
('admin04', '$2b$10$MDwqScOtuR13VnMQYrnlDeFRi6eMq3FjHEtLFzbKTIC7WxkJYzgT2', '유승원', 'ACTIVE'),
('admin05', '$2b$10$pkkbYoPy/x4FDFRdfkK2mOli7SyYKaJ/kr6V.wSOmk/OaWLKVGhS2', '이윤진',  'ACTIVE');

-- Store: 10개, 전부 INACTIVE
-- terminal_password는 평문 '1234' (실제 운영 시 BCrypt 해시값으로 교체 필요)
INSERT INTO store (name, location, status, terminal_password) VALUES
('1층 카페',    '1층 101호', 'INACTIVE', '1234'),
('1층 편의점',  '1층 102호', 'INACTIVE', '1234'),
('1층 약국',    '1층 103호', 'INACTIVE', '1234'),
('2층 식당',    '2층 201호', 'INACTIVE', '1234'),
('2층 미용실',  '2층 202호', 'INACTIVE', '1234'),
('2층 세탁소',  '2층 203호', 'INACTIVE', '1234'),
('3층 학원',    '3층 301호', 'INACTIVE', '1234'),
('3층 치과',    '3층 302호', 'INACTIVE', '1234'),
('3층 부동산',  '3층 303호', 'INACTIVE', '1234'),
('4층 피부과',  '4층 401호', 'INACTIVE', '1234');
