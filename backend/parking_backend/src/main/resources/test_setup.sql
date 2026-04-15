-- 1. 가장 하위 데이터들 (참조하는 놈들) 먼저 삭제
DELETE FROM point_log WHERE user_id = 73;

-- 2. 그다음 parking_ticket 삭제 (아까 에러 났던 부분)
DELETE FROM parking_ticket
WHERE parking_log_id IN (SELECT parking_log_id FROM parking_log WHERE car_number_snapshot = '65라4088');

-- 3. 그다음 activity_log 삭제
DELETE FROM activity_log
WHERE parking_log_id IN (SELECT parking_log_id FROM parking_log WHERE car_number_snapshot = '65라4088');

-- 4. 그다음 payment 삭제 (이제 point_log가 없어서 지워질 겁니다!)
DELETE FROM payment
WHERE parking_log_id IN (SELECT parking_log_id FROM parking_log WHERE car_number_snapshot = '65라4088');

-- 5. 이제 드디어 부모인 parking_log 삭제
DELETE FROM parking_log WHERE car_number_snapshot = '65라4088';

-- 6. 포인트 초기화 및 데이터 재생성 (parking_status는 'IN_USE'나 'PARKED' 확인!)
INSERT INTO user_point (user_id, current_point)
VALUES (73, 5000)
ON DUPLICATE KEY UPDATE current_point = 5000;

INSERT INTO parking_log (
    vehicle_id, car_number_snapshot, entry_time, entered_at,
    entry_plate_image, parking_type_snapshot, payment_status,
    parking_fee_policy_id, is_blacklist, parking_status
) VALUES (
             8, '65라4088', DATE_SUB(NOW(), INTERVAL 5 HOUR), DATE_SUB(NOW(), INTERVAL 5 HOUR),
             'https://parking-storage.com/entry/2026/04/10/car_65la4088_entry.jpg',
             'USER', 'UNPAID', 1, 0, 'ENTERED'
         );