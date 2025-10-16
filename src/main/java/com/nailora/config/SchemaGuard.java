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
			// 1) ซ่อมข้อมูลที่ปี > 2200 → ลบ 543 ปี (ทำให้เหลือ ค.ศ.)
			int fixed = jdbc.update("""
					UPDATE time_slot
					SET start_at = DATE_SUB(start_at, INTERVAL 543 YEAR),
					    end_at   = DATE_SUB(end_at,   INTERVAL 543 YEAR)
					WHERE (start_at IS NOT NULL AND YEAR(start_at) > 2200)
					   OR (end_at   IS NOT NULL AND YEAR(end_at)   > 2200)
					""");
			if (fixed > 0)
				log.info("[SchemaGuard] normalized Buddhist years in time_slot rows: {}", fixed);

			// 2) UNIQUE (service_id, start_at) — กันสร้างซ้ำ
			// เช็คจาก INFORMATION_SCHEMA.STATISTICS ก่อน
			Integer idxCount = jdbc.queryForObject("""
					SELECT COUNT(*) FROM information_schema.statistics
					WHERE table_schema = DATABASE()
					  AND table_name   = 'time_slot'
					  AND index_name   = 'uq_timeslot_service_start'
					""", Integer.class);
			if (idxCount != null && idxCount == 0) {
				// MySQL 8 มี CREATE INDEX IF NOT EXISTS แล้ว แต่ใช้เช็คเองจะชัวร์กว่า
				jdbc.execute("CREATE UNIQUE INDEX uq_timeslot_service_start ON time_slot(service_id, start_at)");
				log.info("[SchemaGuard] created UNIQUE INDEX uq_timeslot_service_start");
			}

			// 3) CHECK constraint — กันปีหลุดช่วง (ต้อง MySQL 8.0.16+ ถึงจะ enforce)
			Integer ckCount = jdbc.queryForObject("""
					SELECT COUNT(*) FROM information_schema.table_constraints
					WHERE table_schema = DATABASE()
					  AND table_name   = 'time_slot'
					  AND constraint_name = 'ck_timeslot_year'
					  AND constraint_type = 'CHECK'
					""", Integer.class);
			if (ckCount != null && ckCount == 0) {
				try {
					jdbc.execute("""
							ALTER TABLE time_slot
							ADD CONSTRAINT ck_timeslot_year
							CHECK (YEAR(start_at) BETWEEN 2000 AND 2100
							   AND YEAR(end_at)   BETWEEN 2000 AND 2100)
							""");
					log.info("[SchemaGuard] added CHECK constraint ck_timeslot_year");
				} catch (Exception e) {
					// บางระบบสิทธิ์/รุ่น MySQL อาจไม่รองรับ CHECK → ล็อกไว้เฉย ๆ
					log.warn("[SchemaGuard] add CHECK failed (ignored): {}", e.getMessage());
				}
			}
		};
	}
}
