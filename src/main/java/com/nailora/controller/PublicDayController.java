package com.nailora.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/booking")
@RequiredArgsConstructor
public class PublicDayController {

	private final JdbcTemplate jdbc;
	private final java.time.Clock clock;

	// ---------- JSON: ใช้เดิมได้เหมือนก่อน ----------
	@GetMapping("/day.json")
	@ResponseBody
	public Map<String, Object> dayJson(@RequestParam String date, @RequestParam Long serviceId) {
		var range = rangeOf(date);
		List<Map<String, Object>> rows = querySlots(serviceId, range.from(), range.to(), range.now());
		return Map.of("date", date, "serviceId", serviceId, "count", rows.size(), "slots", rows);
	}

	// ---------- VIEW: ตารางเลือกช่วงเวลา ----------
	@GetMapping("/day")
	public String day(@RequestParam String date, @RequestParam Long serviceId, Model model) {
		var range = rangeOf(date);
		List<Map<String, Object>> slots = querySlots(serviceId, range.from(), range.to(), range.now());

		model.addAttribute("title", "เลือกช่วงเวลา");
		model.addAttribute("date", date);
		model.addAttribute("serviceId", serviceId);
		model.addAttribute("slots", slots);
		return "booking/day";
	}

	// ---------- JSON ใหม่: /booking/slots?serviceId=..&date=.. ----------
	// รูปแบบผลลัพธ์เหมือน day.json (อ่านง่าย เรียกใช้จาก FE ได้)
	@GetMapping("/slots")
	@ResponseBody
	public Map<String, Object> slots(@RequestParam Long serviceId, @RequestParam String date) {
		var range = rangeOf(date);
		List<Map<String, Object>> rows = querySlots(serviceId, range.from(), range.to(), range.now());
		return Map.of("serviceId", serviceId, "date", date, "count", rows.size(), "slots", rows);
	}

	// ---------- เมธอดช่วย: ช่วงเวลา/ตอนนี้ ----------
	private record DayRange(LocalDateTime from, LocalDateTime to, LocalDateTime now) {
	}

	private DayRange rangeOf(String date) {
		LocalDate d = LocalDate.parse(date);
		LocalDateTime from = d.atStartOfDay();
		LocalDateTime to = d.plusDays(1).atStartOfDay();
		LocalDateTime now = LocalDateTime.now(clock);
		return new DayRange(from, to, now);
	}

	// ---------- เมธอดช่วย: ดึงสลอตจาก DB ----------
	// NOTE: ให้คอลัมน์ id เป็นชื่อเดียวกับที่หน้า day.html ใช้
	// (id/startAt/endAt/techName/capacity/remaining)
	private List<Map<String, Object>> querySlots(Long serviceId, LocalDateTime from, LocalDateTime to,
			LocalDateTime now) {
		return jdbc.queryForList("""
				SELECT
				  t.id         AS id,
				  t.start_at   AS startAt,
				  t.end_at     AS endAt,
				  t.tech_name  AS techName,
				  t.capacity   AS capacity,
				  (t.capacity -
				     ( SELECT COUNT(b.id)
				       FROM booking b
				       WHERE b.time_slot_id = t.id
				         AND b.status = 'BOOKED'
				         AND (
				           b.deposit_status = 'PAID'
				           OR (b.deposit_status IN ('UNPAID','PROCESSING') AND b.deposit_due_at > ?)
				         )
				     )
				  ) AS remaining
				FROM time_slot t
				WHERE t.service_id = ?
				  AND t.start_at >= ?
				  AND t.start_at <  ?
				  AND t.open = 1
				ORDER BY t.start_at
				""", now, serviceId, from, to);
	}
}
