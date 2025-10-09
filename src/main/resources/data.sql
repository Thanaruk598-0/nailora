-- ROLE (ใช้ ADMIN ไม่ต้องมีคำว่า ROLE_ นำหน้า)
INSERT INTO role(name) VALUES ('ADMIN')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- USER (เก็บ bcrypt ตรง ๆ ไม่ต้องมี {bcrypt})
INSERT INTO app_user(username, password_hash, enabled)
VALUES ('admin', '$2a$10$xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', true)
ON DUPLICATE KEY UPDATE
    password_hash = VALUES(password_hash),
    enabled = VALUES(enabled);

-- USER_ROLE
INSERT INTO user_role(user_id, role_id)
SELECT u.id, r.id
FROM app_user u JOIN role r ON r.name = 'ADMIN'
WHERE u.username = 'admin'
ON DUPLICATE KEY UPDATE user_id = user_id;

-- SERVICE_ITEM (ของเดิมที่คุณมีอยู่ ใช้ต่อได้เลย)


-- SERVICE_ITEM
INSERT INTO service_item(name, duration_min, price, deposit_min, active)
VALUES ('ทำเล็บเจล (Basic)', 60, 800.00, 200.00, true),
       ('ต่อเล็บอะคริลิก', 90, 1500.00, 300.00, true)
ON DUPLICATE KEY UPDATE duration_min = VALUES(duration_min),
                        price = VALUES(price),
                        deposit_min = VALUES(deposit_min),
                        active = VALUES(active);
