package com.nailora.controller.admin;

import com.nailora.entity.TimeSlot;
import com.nailora.repository.ServiceItemRepository;
import com.nailora.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/slots")
public class AdminSlotsController {

	private static final Logger log = LoggerFactory.getLogger(AdminSlotsController.class);
	private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_DATE;

	private final ServiceItemRepository serviceRepo;
	private final TimeSlotRepository timeSlotRepo;
	private final TransactionTemplate tx; // ใช้ทำทรานแซกชันย่อยต่อสลอต

	@PersistenceContext
	private EntityManager em;

	/** แปลง LocalDate พ.ศ. → ค.ศ. */
	private LocalDate normalize(LocalDate d) {
		if (d != null && d.getYear() > 2200)
			return d.minusYears(543);
		return d;
	}

	/** แปลงสตริง yyyy-MM-dd (ถ้าเป็น พ.ศ. จะ -543) */
	private String normalizeDateString(String s) {
		LocalDate d = LocalDate.parse(s, ISO_DATE);
		if (d.getYear() > 2200)
			d = d.minusYears(543);
		return d.format(ISO_DATE);
	}

	@GetMapping
	public String list(
			@RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate date,
			@RequestParam(required = false) Long serviceId, Model model) {

		var services = serviceRepo.findAll();

		// redirect ใส่ default
		if (date == null || serviceId == null) {
			var today = (date == null ? LocalDate.now() : date);
			today = normalize(today);
			var sid = (serviceId == null && !services.isEmpty() ? services.get(0).getId() : serviceId);
			return "redirect:/admin/slots?date=" + today + "&serviceId=" + sid;
		}

		date = normalize(date);

		var svc = serviceRepo.findById(serviceId).orElse(null);
		var start = date.atStartOfDay();
		var end = date.plusDays(1).atStartOfDay();

		var slots = (svc == null) ? List.<TimeSlot>of()
				: timeSlotRepo.findByServiceItem_IdAndStartAtBetweenOrderByStartAtAsc(serviceId, start, end);

		model.addAttribute("title", "จัดการสลอต (แอดมิน)");
		model.addAttribute("services", services);
		model.addAttribute("serviceId", serviceId);
		model.addAttribute("date", date.toString()); // ค.ศ. เสมอ
		model.addAttribute("slots", slots);
		return "admin/slots/list";
	}

	// ⛔️ ไม่ใส่ @Transactional ที่เมธอดนี้ — จะใช้ tx ต่อสลอตแทน
	@PostMapping("/generate")
	public String generate(@RequestParam Long serviceId, @RequestParam String date, // อาจมาเป็น 2568-10-10
			@RequestParam String fromTime, // HH:mm
			@RequestParam String toTime, // HH:mm
			@RequestParam Integer durationMin, @RequestParam(required = false) Integer gapMin,
			@RequestParam Integer capacity, @RequestParam(defaultValue = "true") boolean open,
			@RequestParam(defaultValue = "Mint") String techName) {

		var svcOpt = serviceRepo.findById(serviceId);
		if (svcOpt.isEmpty()) {
			return "redirect:/admin/slots?date=" + date + "&serviceId=" + serviceId + "&error=serviceNotFound";
		}
		var svc = svcOpt.get();

		// 1) normalize วันที่ (กัน พ.ศ.)
		LocalDate d = toCE(LocalDate.parse(date, ISO_DATE));
		LocalTime from = LocalTime.parse(fromTime);
		LocalTime to = LocalTime.parse(toTime);

		LocalDateTime cursor = LocalDateTime.of(d, from);
		LocalDateTime end = LocalDateTime.of(d, to);
		int gap = (gapMin == null ? 0 : gapMin);

		while (!cursor.plusMinutes(durationMin).isAfter(end)) {
			// 2) normalize LDT + guard ช่วงปี
			LocalDateTime startAt = toCE(cursor);
			LocalDateTime endAt = toCE(cursor.plusMinutes(durationMin));

			if (!isYearInRange(startAt) || !isYearInRange(endAt)) {
				log.warn("Skip slot {} – {} (year out of range)", startAt, endAt);
				cursor = cursor.plusMinutes(durationMin + gap);
				continue;
			}

			// 3) ทรานแซกชันย่อยต่อ 1 ช่อง
			LocalDateTime s = startAt, e = endAt;
			tx.executeWithoutResult(status -> {
				try {
					if (!timeSlotRepo.existsByServiceItem_IdAndStartAt(serviceId, s)) {
						TimeSlot t = new TimeSlot();
						t.setServiceItem(svc);
						t.setStartAt(s);
						t.setEndAt(e);
						t.setTechName(techName);
						t.setCapacity(capacity);
						t.setOpen(open);
						t.setActive(Boolean.TRUE);

						timeSlotRepo.saveAndFlush(t); // ให้ fail ตรงนี้ทันทีถ้าชน CHECK/UNIQUE
						em.detach(t); // กัน auto-flush กระทบ slot ถัดไป
					}
				} catch (Exception ex) {
					// rollback เฉพาะช่องนี้ แล้วล้าง PC กัน auto-flush
					status.setRollbackOnly();
					log.warn("Skip slot {} – {} due to DB constraint: {}", s, e, ex.getMessage());
					em.clear();
				}
			});

			cursor = cursor.plusMinutes(durationMin + gap);
		}

		return "redirect:/admin/slots?date=" + d + "&serviceId=" + serviceId;
	}

	@PostMapping("/{id}/delete")
	public String delete(@PathVariable Long id, @RequestParam String date, @RequestParam Long serviceId) {
		timeSlotRepo.deleteById(id);
		String normalizedDate = normalizeDateString(date);
		return "redirect:/admin/slots?date=" + normalizedDate + "&serviceId=" + serviceId;
	}

	// ---------- helpers ----------
	private static LocalDate toCE(LocalDate d) {
		return (d != null && d.getYear() > 2200) ? d.minusYears(543) : d;
	}

	private static LocalDateTime toCE(LocalDateTime dt) {
		return (dt != null && dt.getYear() > 2200) ? dt.minusYears(543) : dt;
	}

	private static boolean isYearInRange(LocalDateTime dt) {
		int y = dt.getYear();
		return y >= 2000 && y <= 2100; // ให้ตรงกับ CHECK ฝั่ง DB
	}
}
