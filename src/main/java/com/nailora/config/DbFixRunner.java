package com.nailora.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Configuration
public class DbFixRunner {
	private static final Logger log = LoggerFactory.getLogger(DbFixRunner.class);

	// ปีพุทธศักราชที่หลุดเข้ามาจะ > ค.ศ. 2200 แน่ ๆ (2568 BE = 2025 CE)
	private static final LocalDateTime CUT_OFF = LocalDateTime.of(2200, 1, 1, 0, 0);

	@Bean
	CommandLineRunner fixBuddhistYearOnce(JdbcTemplate jdbc) {
		return args -> {
			try {
				Timestamp cutoff = Timestamp.valueOf(CUT_OFF);

				// นับแถวที่ผิด (ใช้เปรียบเทียบ timestamp แทน YEAR(...))
				Integer cnt = jdbc.queryForObject(
						"SELECT COUNT(*) FROM time_slot WHERE start_at >= ? OR end_at >= ?",
						Integer.class, cutoff, cutoff);

				if (cnt == null || cnt == 0) {
					log.info("[TimeSlot Year Fix] No rows to fix. Skip.");
					return;
				}

				log.warn("[TimeSlot Year Fix] Found {} rows with Buddhist year. Fixing...", cnt);

				// 1) ลบ CHECK เดิม (ถ้ามี) – syntax ของ Postgres
				jdbc.execute("ALTER TABLE time_slot DROP CONSTRAINT IF EXISTS ck_timeslot_year");
				log.info("[TimeSlot Year Fix] Dropped ck_timeslot_year (if existed)");

				// 2) ลด 543 ปี เฉพาะแถวที่ >= cutoff (Postgres ใช้ INTERVAL)
				int updated = jdbc.update("""
						UPDATE time_slot
						   SET start_at = CASE WHEN start_at >= ? THEN start_at - INTERVAL '543 years' ELSE start_at END,
						       end_at   = CASE WHEN end_at   >= ? THEN end_at   - INTERVAL '543 years' ELSE end_at   END
						 WHERE start_at >= ? OR end_at >= ?
						""", cutoff, cutoff, cutoff, cutoff);
				log.info("[TimeSlot Year Fix] Updated rows: {}", updated);

				// 3) (ทางเลือก) ใส่ CHECK กลับแบบที่รองรับ Postgres
				//    ใช้ช่วงปีแทน YEAR(...) เพื่อความชัดเจน
				try {
					jdbc.execute("""
							ALTER TABLE time_slot
							  ADD CONSTRAINT ck_timeslot_year
							  CHECK (
							      start_at >= TIMESTAMP '2000-01-01' AND start_at < TIMESTAMP '2101-01-01'
							  AND end_at   >= TIMESTAMP '2000-01-01' AND end_at   < TIMESTAMP '2101-01-01'
							  )
							""");
					log.info("[TimeSlot Year Fix] Re-created ck_timeslot_year");
				} catch (Exception ex) {
					log.warn("[TimeSlot Year Fix] Recreate CHECK skipped: {}", ex.getMessage());
				}
			} catch (Exception e) {
				// กันไม่ให้แอปล้ม ถ้าเกิด error ระหว่าง one-off fix
				log.error("[TimeSlot Year Fix] Failed but app will continue to start", e);
			}
		};
	}
}
