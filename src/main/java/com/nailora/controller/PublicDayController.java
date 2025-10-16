package com.nailora.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Clock;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/booking")
@RequiredArgsConstructor
public class PublicDayController {

	private static final Logger log = LoggerFactory.getLogger(PublicDayController.class);

	private final JdbcTemplate jdbc;
	private final Clock clock;

	// ---------- JSON (debug fields included) ----------
	@GetMapping("/day.json")
	@ResponseBody
	public Map<String, Object> dayJson(@RequestParam String date, @RequestParam Long serviceId) {
		LocalDate d = normalizeDate(date);
		LocalDateTime now = LocalDateTime.now(clock);
		List<Map<String, Object>> rows = querySlotsByDate(serviceId, d, now);

		log.info("[day.json] serviceId={}, date={}, now={}, rows={}", serviceId, d, now, rows.size());

		return Map.of("date", date, "serviceId", serviceId, "count", rows.size(), "slots", rows,
				// debug fields
				"_debug_date", d.toString(), "_debug_now", now.toString());
	}

	// ---------- VIEW ----------
	@GetMapping("/day")
	public String day(@RequestParam String date, @RequestParam Long serviceId, Model model) {
		LocalDate d = normalizeDate(date);
		LocalDateTime now = LocalDateTime.now(clock);
		List<Map<String, Object>> slots = querySlotsByDate(serviceId, d, now);

		log.info("[day] serviceId={}, date={}, now={}, rows={}", serviceId, d, now, slots.size());

		model.addAttribute("title", "เลือกช่วงเวลา");
		model.addAttribute("date", date); // แสดงตามที่ผู้ใช้ส่งมา
		model.addAttribute("serviceId", serviceId);
		model.addAttribute("slots", slots);
		return "booking/day";
	}

	// ---------- JSON ใหม่ ----------
	@GetMapping("/slots")
	@ResponseBody
	public Map<String, Object> slots(@RequestParam Long serviceId, @RequestParam String date) {
		LocalDate d = normalizeDate(date);
		LocalDateTime now = LocalDateTime.now(clock);
		List<Map<String, Object>> rows = querySlotsByDate(serviceId, d, now);

		log.info("[slots] serviceId={}, date={}, now={}, rows={}", serviceId, d, now, rows.size());

		return Map.of("serviceId", serviceId, "date", date, "count", rows.size(), "slots", rows);
	}

	// ---------- helper: ปรับ พ.ศ. → ค.ศ. ----------
	private LocalDate normalizeDate(String yyyyMmDd) {
		LocalDate d = LocalDate.parse(yyyyMmDd);
		if (d.getYear() > 2200)
			d = d.minusYears(543);
		return d;
	}

	// ---------- QUERY (Postgres-safe): [from, to) + boolean ตรง ๆ ----------
	private List<Map<String, Object>> querySlotsByDate(Long serviceId, LocalDate date, LocalDateTime now) {
		LocalDateTime from = date.atStartOfDay();
		LocalDateTime to = date.plusDays(1).atStartOfDay();

		return jdbc.queryForList("""
				SELECT
				  t.id                               AS id,
				  t.start_at                         AS "startAt",
				  t.end_at                           AS "endAt",
				  t.tech_name                        AS "techName",
				  t.capacity                         AS "capacity",
				  (t.capacity - (
				     SELECT COUNT(1)
				     FROM booking b
				     WHERE b.time_slot_id = t.id
				       AND b.status = 'BOOKED'
				       AND (
				         b.deposit_status = 'PAID'
				         OR (b.deposit_status IN ('UNPAID','PROCESSING') AND b.deposit_due_at > ?)
				       )
				  ))                                  AS "remaining"
				FROM time_slot t
				WHERE t.service_id = ?
				  AND t.start_at >= ?
				  AND t.start_at <  ?
				  AND t.open   = TRUE
				  AND t.active = TRUE
				ORDER BY t.start_at
				""", now, serviceId, from, to);
	}
}
