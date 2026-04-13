-- 1. 65라4088과 관련된 모든 '흔적' 지우기 (자식부터 부모 순으로)
DELETE FROM activity_log WHERE parking_log_id IN (SELECT parking_log_id FROM parking_log WHERE car_number_snapshot = '65라4088');
DELETE FROM point_log WHERE user_id = 73;
DELETE FROM payment WHERE parking_log_id IN (SELECT parking_log_id FROM parking_log WHERE car_number_snapshot = '65라4088');
DELETE FROM parking_log WHERE car_number_snapshot = '65라4088';

-- 2. 깨끗한 상태에서 포인트 다시 충전
INSERT INTO user_point (user_id, current_point)
VALUES (73, 5000)
ON DUPLICATE KEY UPDATE current_point = 5000;

-- 3. 포인트 로그 생성 (히스토리용)
INSERT INTO point_log (user_id, change_amount, before_point, after_point, reason, created_at, description)
VALUES (73, 5000, 0, 5000, 'ADMIN_GRANT', NOW(), '초기화 후 재충전');

-- 4. 65라4088 차량 '입차' 상태로 새로 만들기
INSERT INTO parking_log (
    vehicle_id, car_number_snapshot, entry_time, entered_at,
    entry_plate_image, parking_type_snapshot, payment_status,
    parking_fee_policy_id, is_blacklist, parking_status
) VALUES (
             8, '65라4088', DATE_SUB(NOW(), INTERVAL 5 HOUR), DATE_SUB(NOW(), INTERVAL 5 HOUR),
             'https://parking-storage.com/entry/2026/04/10/car_65la4088_entry.jpg',
             'USER', 'UNPAID', 1, 0, 'ENTERED'
         );

-- 5. 결과 확인 (제대로 들어갔는지 한눈에 보기)
SELECT * FROM parking_log WHERE car_number_snapshot = '65라4088';
SELECT * FROM user_point WHERE user_id = 73;