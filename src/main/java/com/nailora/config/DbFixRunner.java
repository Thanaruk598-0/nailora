package com.nailora.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DbFixRunner {
	private static final Logger log = LoggerFactory.getLogger(DbFixRunner.class);

	@Bean
	CommandLineRunner fixBuddhistYearOnce(JdbcTemplate jdbc) {
		return args -> {
			// มีงานให้แก้ไหม?
			Integer cnt = jdbc.queryForObject(
					"SELECT COUNT(*) FROM time_slot WHERE YEAR(start_at) > 2200 OR YEAR(end_at) > 2200", Integer.class);
			if (cnt == null || cnt == 0) {
				log.info("[TimeSlot Year Fix] No rows to fix. Skip.");
				return;
			}

			log.warn("[TimeSlot Year Fix] Found {} rows with Buddhist year. Fixing...", cnt);

			// 1) ลบ CHECK ถ้ามี (กัน UPDATE ไม่ผ่าน)
			try {
				jdbc.execute("ALTER TABLE time_slot DROP CHECK ck_timeslot_year");
				log.info("[TimeSlot Year Fix] Dropped ck_timeslot_year");
			} catch (Exception ex) {
				log.info("[TimeSlot Year Fix] DROP CHECK skipped: {}", ex.getMessage());
			}

			// 2) ลด 543 ปี (JDBC ไม่มี safe-update mode แบบ Workbench จึงอัปเดตได้)
			int updated = jdbc.update("""
					    UPDATE time_slot
					       SET start_at = DATE_SUB(start_at, INTERVAL 543 YEAR),
					           end_at   = DATE_SUB(end_at,   INTERVAL 543 YEAR)
					     WHERE YEAR(start_at) > 2200 OR YEAR(end_at) > 2200
					""");
			log.info("[TimeSlot Year Fix] Updated rows: {}", updated);

			// 3) (ทางเลือก) ใส่ CHECK กลับ ถ้าต้องการ
			try {
				jdbc.execute("""
						    ALTER TABLE time_slot
						      ADD CONSTRAINT ck_timeslot_year
						      CHECK (YEAR(start_at) BETWEEN 2000 AND 2100
						         AND YEAR(end_at)   BETWEEN 2000 AND 2100)
						""");
				log.info("[TimeSlot Year Fix] Re-created ck_timeslot_year");
			} catch (Exception ex) {
				log.warn("[TimeSlot Year Fix] Recreate CHECK skipped: {}", ex.getMessage());
			}
		};
	}
}
