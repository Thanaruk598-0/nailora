-- ===== 1) Roles & Admin User =====
INSERT INTO role(name) VALUES ('ROLE_ADMIN')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- ใช้ bcrypt จริงของรหัสผ่านที่ตั้งเอง แล้วแทน {bcrypt}… ด้านล่าง
-- ตัวอย่าง (รหัส: admin123) — แนะนำให้เปลี่ยนก่อนส่งงานจริง
INSERT INTO app_user(username, password_hash, enabled)
VALUES ('admin', '{bcrypt}$2a$10$J8j3NfM9xgZ9q3X6r2wqzOM1wC3wq8C0f3Qq6l0y7q3rV2o4WqIh2', true)
ON DUPLICATE KEY UPDATE enabled = VALUES(enabled);

-- map admin → ROLE_ADMIN
INSERT INTO user_roles(user_id, role_id)
SELECT u.id, r.id
FROM app_user u, role r
WHERE u.username='admin' AND r.name='ROLE_ADMIN'
ON DUPLICATE KEY UPDATE user_id = user_id; -- no-op แต่ป้องกัน insert ซ้ำ

-- ===== 2) Catalog: Service Items =====
INSERT INTO service_item(name, duration_min, price, deposit_min, active) VALUES
('ทำเล็บเจล (Basic)', 60, 800.00, 200.00, true),
('ต่อเล็บอะคริลิก',   90, 1500.00, 300.00, true)
ON DUPLICATE KEY UPDATE
  duration_min = VALUES(duration_min),
  price        = VALUES(price),
  deposit_min  = VALUES(deposit_min),
  active       = VALUES(active);

-- ===== 3) Add-ons =====
INSERT INTO add_on(name, extra_minutes, extra_price, active) VALUES
('เพ้นต์ลาย',   20, 150.00, true),
('ถอดเล็บเก่า', 15, 100.00, true)
ON DUPLICATE KEY UPDATE
  extra_minutes = VALUES(extra_minutes),
  extra_price   = VALUES(extra_price),
  active        = VALUES(active);

-- ===== 4) Time Slots (วันนี้ & พรุ่งนี้) =====
-- วันนี้ 14:00–15:00 (ทำเล็บเจล)
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

-- วันนี้ 15:00–16:30 (ต่อเล็บอะคริลิก)
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

-- พรุ่งนี้ 11:00–12:00 (ทำเล็บเจล)
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

-- ===== 5) (ออปชัน) ตัวอย่าง Booking =====
-- UNPAID (เดโม่ขั้นตอนก่อนชำระ)
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

-- PAID (เดโม่จ่ายเสร็จพร้อมใบเสร็จ)
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

-- ผูก Add-on ให้กับใบจองที่ 2 (ถ้ามี)
INSERT INTO booking_add_on(booking_id, add_on_id)
SELECT b.id, a.id
FROM booking b
JOIN add_on a ON a.name='เพ้นต์ลาย'
WHERE b.phone='0890000002'
  AND NOT EXISTS (
    SELECT 1 FROM booking_add_on ba WHERE ba.booking_id=b.id AND ba.add_on_id=a.id
  )
LIMIT 1;
