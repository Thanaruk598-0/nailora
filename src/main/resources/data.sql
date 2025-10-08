-- =========================================================
-- NAILORA data.sql (DEV) - no CREATE INDEX (compat mode)
-- =========================================================

-- ----- DEDUPE -----
DELETE s1 FROM service_item s1
JOIN service_item s2 ON s1.name = s2.name AND s1.id > s2.id;

DELETE a1 FROM add_on a1
JOIN add_on a2 ON a1.name = a2.name AND a1.id > a2.id;

-- (ถ้าลบไม่ได้เพราะติด FK ให้คอมเมนต์บรรทัดนี้)
DELETE t1 FROM time_slot t1
JOIN time_slot t2
  ON t1.service_id = t2.service_id
 AND t1.start_at  = t2.start_at
 AND t1.id > t2.id;

DELETE b1 FROM booking_add_on b1
JOIN booking_add_on b2
  ON b1.booking_id = b2.booking_id
 AND b1.add_on_id  = b2.add_on_id
 AND b1.id > b2.id;

DELETE u1 FROM app_user u1
JOIN app_user u2 ON u1.username = u2.username AND u1.id > u2.id;

DELETE r1 FROM role r1
JOIN role r2 ON r1.name = r2.name AND r1.id > r2.id;

-- ----- CATALOG -----
INSERT INTO service_item(name, duration_min, price, deposit_min, active) VALUES
('ทำเล็บเจล (Basic)', 60, 800.00, 200.00, true),
('ต่อเล็บอะคริลิก',   90, 1500.00, 300.00, true)
ON DUPLICATE KEY UPDATE
  duration_min = VALUES(duration_min),
  price        = VALUES(price),
  deposit_min  = VALUES(deposit_min),
  active       = VALUES(active);

INSERT INTO add_on(name, extra_minutes, extra_price, active) VALUES
('เพ้นต์ลาย',   20, 150.00, true),
('ถอดเล็บเก่า', 15, 100.00, true)
ON DUPLICATE KEY UPDATE
  extra_minutes = VALUES(extra_minutes),
  extra_price   = VALUES(extra_price),
  active        = VALUES(active);

-- ----- TIME SLOTS -----
INSERT INTO time_slot(service_id, start_at, end_at, capacity, open, tech_name)
SELECT s.id,
       DATE_ADD(CURDATE(), INTERVAL 14 HOUR),
       DATE_ADD(CURDATE(), INTERVAL 15 HOUR),
       1, true, 'Mint'
FROM service_item s WHERE s.name='ทำเล็บเจล (Basic)'
ON DUPLICATE KEY UPDATE
  end_at  = VALUES(end_at),
  capacity= VALUES(capacity),
  open    = VALUES(open);

INSERT INTO time_slot(service_id, start_at, end_at, capacity, open, tech_name)
SELECT s.id,
       DATE_ADD(CURDATE(), INTERVAL 15 HOUR),
       DATE_ADD(CURDATE(), INTERVAL 16 HOUR) + INTERVAL 30 MINUTE,
       1, true, 'Praew'
FROM service_item s WHERE s.name='ต่อเล็บอะคริลิก'
ON DUPLICATE KEY UPDATE
  end_at  = VALUES(end_at),
  capacity= VALUES(capacity),
  open    = VALUES(open);

INSERT INTO time_slot(service_id, start_at, end_at, capacity, open, tech_name)
SELECT s.id,
       DATE_ADD(CURDATE(), INTERVAL 1 DAY) + INTERVAL 11 HOUR,
       DATE_ADD(CURDATE(), INTERVAL 1 DAY) + INTERVAL 12 HOUR,
       1, true, 'Mint'
FROM service_item s WHERE s.name='ทำเล็บเจล (Basic)'
ON DUPLICATE KEY UPDATE
  end_at  = VALUES(end_at),
  capacity= VALUES(capacity),
  open    = VALUES(open);

-- ----- DEMO BOOKINGS -----
INSERT INTO booking(
  time_slot_id, customer_name, phone, note,
  status, service_price, add_on_price, deposit_amount,
  deposit_status, deposit_due_at, payment_ref, deposit_paid_at, receipt_url, gateway,
  created_at, canceled_at, cancel_reason
)
SELECT t.id, 'คุณลูกค้าเดโม่', '0890000001', 'ทดสอบ',
       'BOOKED', 800.00, 0.00, 200.00,
       'UNPAID', DATE_ADD(NOW(), INTERVAL 5 MINUTE), NULL, NULL, NULL, 'STRIPE',
       NOW(), NULL, NULL
FROM time_slot t
JOIN service_item s ON s.id=t.service_id
WHERE s.name='ทำเล็บเจล (Basic)'
  AND t.start_at = DATE_ADD(CURDATE(), INTERVAL 14 HOUR)
  AND NOT EXISTS (SELECT 1 FROM booking b WHERE b.time_slot_id=t.id AND b.phone='0890000001')
LIMIT 1;

INSERT INTO booking(
  time_slot_id, customer_name, phone, note,
  status, service_price, add_on_price, deposit_amount,
  deposit_status, deposit_due_at, payment_ref, deposit_paid_at, receipt_url, gateway,
  created_at, canceled_at, cancel_reason
)
SELECT t.id, 'คุณลูกค้าชำระแล้ว', '0890000002', 'ทดสอบชำระแล้ว',
       'BOOKED', 1500.00, 150.00, 300.00,
       'PAID', NULL, 'pi_demo_123456', NOW(), 'https://receipt.stripe.com/demo', 'STRIPE',
       NOW(), NULL, NULL
FROM time_slot t
JOIN service_item s ON s.id=t.service_id
WHERE s.name='ต่อเล็บอะคริลิก'
  AND t.start_at = DATE_ADD(CURDATE(), INTERVAL 15 HOUR)
  AND NOT EXISTS (SELECT 1 FROM booking b WHERE b.time_slot_id=t.id AND b.phone='0890000002')
LIMIT 1;

INSERT INTO booking_add_on(booking_id, add_on_id)
SELECT b.id, a.id
FROM booking b
JOIN add_on a ON a.name='เพ้นต์ลาย'
WHERE b.phone='0890000002'
  AND NOT EXISTS (
    SELECT 1 FROM booking_add_on ba WHERE ba.booking_id=b.id AND ba.add_on_id=a.id
  )
LIMIT 1;
