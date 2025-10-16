package com.nailora.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Slf4j
@Configuration
public class SchemaGuard {

    @Bean
    ApplicationRunner schemaGuardRunner(JdbcTemplate jdbc) {
        return args -> {

            // 1) ซ่อมข้อมูลปีพุทธ (>= ค.ศ. 2200) → ลบ 543 ปี ด้วย interval ของ Postgres
            try {
                int fixed = jdbc.update("""
                        UPDATE time_slot
                           SET start_at = CASE
                                             WHEN start_at IS NOT NULL AND start_at >= TIMESTAMP '2200-01-01 00:00:00'
                                               THEN start_at - INTERVAL '543 years'
                                             ELSE start_at
                                           END,
                               end_at   = CASE
                                             WHEN end_at   IS NOT NULL AND end_at   >= TIMESTAMP '2200-01-01 00:00:00'
                                               THEN end_at   - INTERVAL '543 years'
                                             ELSE end_at
                                           END
                         WHERE (start_at IS NOT NULL AND start_at >= TIMESTAMP '2200-01-01 00:00:00')
                            OR (end_at   IS NOT NULL AND end_at   >= TIMESTAMP '2200-01-01 00:00:00')
                        """);
                if (fixed > 0) {
                    log.warn("[SchemaGuard] normalized Buddhist years in time_slot rows: {}", fixed);
                } else {
                    log.info("[SchemaGuard] no time_slot rows need Buddhist-year fix");
                }
            } catch (Exception e) {
                log.error("[SchemaGuard] step#1 (fix BE years) failed, continuing", e);
            }

            // 2) สร้าง UNIQUE INDEX (service_id, start_at) กันซ้ำ — ใช้ IF NOT EXISTS ของ Postgres
            try {
                jdbc.execute("""
                        CREATE UNIQUE INDEX IF NOT EXISTS uq_timeslot_service_start
                          ON time_slot(service_id, start_at)
                        """);
                log.info("[SchemaGuard] ensured UNIQUE INDEX uq_timeslot_service_start");
            } catch (Exception e) {
                log.error("[SchemaGuard] step#2 (unique index) failed, continuing", e);
            }

            // 3) ใส่ CHECK constraint แบบ Postgres (ไม่ใช้ YEAR(...))
            try {
                Integer ckCount = jdbc.queryForObject("""
                        SELECT COUNT(*) 
                          FROM pg_constraint c
                          JOIN pg_class t ON t.oid = c.conrelid
                          JOIN pg_namespace n ON n.oid = t.relnamespace
                         WHERE c.conname = 'ck_timeslot_year'
                           AND t.relname = 'time_slot'
                           AND n.nspname = current_schema()
                        """, Integer.class);

                if (ckCount != null && ckCount == 0) {
                    jdbc.execute("""
                            ALTER TABLE time_slot
                              ADD CONSTRAINT ck_timeslot_year
                              CHECK (
                                (start_at IS NULL OR (start_at >= TIMESTAMP '2000-01-01 00:00:00' AND start_at < TIMESTAMP '2101-01-01 00:00:00'))
                                AND
                                (end_at   IS NULL OR (end_at   >= TIMESTAMP '2000-01-01 00:00:00' AND end_at   < TIMESTAMP '2101-01-01 00:00:00'))
                              )
                            """);
                    log.info("[SchemaGuard] added CHECK constraint ck_timeslot_year");
                } else {
                    log.info("[SchemaGuard] CHECK constraint ck_timeslot_year already exists");
                }
            } catch (Exception e) {
                // ถ้าใส่ CHECK ไม่ได้ (เช่น สิทธิ์ไม่พอ) ก็ปล่อยผ่าน แค่ล็อกไว้
                log.warn("[SchemaGuard] step#3 (check constraint) failed (ignored): {}", e.getMessage());
            }
        };
    }
}
