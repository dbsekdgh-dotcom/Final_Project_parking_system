-- [1] 기존 데이터 삭제 (성능과 안전을 위해 별칭 없이 명확하게 작성)
DELETE FROM point_log WHERE user_id = 73;

DELETE FROM parking_ticket
WHERE parking_log_id IN (SELECT pl.parking_log_id FROM (SELECT parking_log_id FROM parking_log WHERE car_number_snapshot = '65라4088') AS pl);

DELETE FROM activity_log
WHERE parking_log_id IN (SELECT pl.parking_log_id FROM (SELECT parking_log_id FROM parking_log WHERE car_number_snapshot = '65라4088') AS pl);

DELETE FROM payment
WHERE parking_log_id IN (SELECT pl.parking_log_id FROM (SELECT parking_log_id FROM parking_log WHERE car_number_snapshot = '65라4088') AS pl);

DELETE FROM parking_log WHERE car_number_snapshot = '65라4088';

-- [2] 포인트 초기화
INSERT INTO user_point (user_id, current_point)
VALUES (73, 5000)
ON DUPLICATE KEY UPDATE current_point = 5000;


-- [3] 신규 주차 기록 생성 (5시간 전 입차)
INSERT INTO parking_log (
    vehicle_id, car_number_snapshot, entry_time, entered_at,
    entry_plate_image, parking_type_snapshot, payment_status,
    parking_fee_policy_id, is_blacklist, parking_status
) VALUES (
             8, '65라4088', DATE_SUB(NOW(), INTERVAL 5 HOUR), DATE_SUB(NOW(), INTERVAL 5 HOUR),
             'https://parking-storage.com/entry/2026/04/10/car_65la4088_entry.jpg',
             'USER', 'UNPAID', 1, 0, 'ENTERED'
         );

-- 방금 생성된 ID를 변수에 저장
SET @new_log_id = LAST_INSERT_ID();


-- [4] 중복 티켓 데이터 생성 (3,000원 할인권 2장 적용)
INSERT INTO parking_ticket (
    store_id,
    parking_log_id,
    ticket_policy_id,
    status,
    applied_amount
) VALUES
      (1, @new_log_id, 1, 'STORE', 0), -- 첫 번째 관리자 특별 할인 (3000원)
      (1, @new_log_id, 1, 'STORE', 0); -- 두 번째 관리자 특별 할인 (3000원)


-- [5] 결과 확인
SELECT 'SUCCESS' as result, @new_log_id as generated_log_id;
SELECT * FROM parking_ticket WHERE parking_log_id = @new_log_id;