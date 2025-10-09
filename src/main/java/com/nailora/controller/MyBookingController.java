package com.nailora.controller;

import com.nailora.repository.BookingRepository;
import com.nailora.service.BookingService;
import com.nailora.service.StripeReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequestMapping("/my-bookings")
@RequiredArgsConstructor
public class MyBookingController {

	private final BookingRepository bookingRepo;
	private final BookingService bookingService;
	private final StripeReceiptService receiptService;
	private final JdbcTemplate jdbc;

	// NEW: ถ้าไม่ส่ง phone ให้เด้งไปหน้า “กรอกเบอร์”
	@GetMapping
	public String findPage(@RequestParam(required = false) String phone, Model model) {
		if (phone == null || phone.isBlank()) {
			model.addAttribute("title", "เช็ค/ยกเลิกคิว");
			return "booking/find";
		}
		// มีเบอร์แล้ว → โชว์รายการ
		return listByPhone(phone, model);
	}

	// แยกเมธอดจริงที่เรนเดอร์ตาราง (ถูกเรียกจาก findPage เมื่อมี phone)
	private String listByPhone(String phone, Model model) {
		var list = bookingRepo.findByPhoneWithSlotAndService(phone);

		// เติม receiptUrl ถ้าจ่ายแล้วแต่ยังไม่มี
		list.stream().filter(b -> b.getDepositStatus() == com.nailora.entity.Booking.DepositStatus.PAID)
				.filter(b -> b.getReceiptUrl() == null || b.getReceiptUrl().isBlank())
				.forEach(b -> receiptService.ensureReceiptUrl(b.getId()));

		// โหลดใหม่เผื่ออัปเดต
		list = bookingRepo.findByPhoneWithSlotAndService(phone);

		list.sort(java.util.Comparator.comparing((com.nailora.entity.Booking b) -> b.getTimeSlot().getStartAt())
				.reversed());

		DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM HH:mm");
		List<Map<String, Object>> rows = new ArrayList<>();

		// กำหนด “เฉพาะรายการล่าสุดที่ยังไม่ถึงเวลาเริ่ม” ให้กดยกเลิกได้
		LocalDateTime now = LocalDateTime.now();
		boolean cancelGranted = false; // อนุญาตแค่ตัวแรกที่ยังไม่เริ่ม

		for (var b : list) {
			var m = new LinkedHashMap<String, Object>();
			m.put("id", b.getId());
			m.put("serviceName", b.getTimeSlot().getService().getName());
			m.put("techName", b.getTimeSlot().getTechName());
			m.put("startAt", b.getTimeSlot().getStartAt());
			m.put("startAtText", b.getTimeSlot().getStartAt().format(fmt));
			m.put("status", b.getStatus().name());
			m.put("depositStatus", b.getDepositStatus().name());
			m.put("depositAmount", b.getDepositAmount());
			m.put("receiptUrl", b.getReceiptUrl());

			// ราคา
			m.put("servicePrice", b.getServicePrice());
			m.put("addOnPrice", b.getAddOnPrice());
			m.put("totalPrice", b.getServicePrice().add(b.getAddOnPrice()));

			// ชื่อ add-on จริง
			List<String> addOnNames = jdbc.queryForList("""
					SELECT a.name
					FROM booking_add_on ba
					JOIN add_on a ON a.id = ba.add_on_id
					WHERE ba.booking_id = ?
					ORDER BY a.name
					""", String.class, b.getId());
			m.put("addOnNames", addOnNames);

			// กดยกเลิกได้เฉพาะ “รายการล่าสุดที่ยังไม่เริ่ม”
			boolean isFuture = b.getTimeSlot().getStartAt().isAfter(now);
			boolean canCancel = false;
			String cannotReason = null;

			if (!isFuture) {
				canCancel = false;
				cannotReason = "เลยเวลาแล้ว (ประวัติการจอง)";
			} else if (!cancelGranted) {
				canCancel = true; // อนุญาตเฉพาะตัวแรก
				cancelGranted = true; // ล็อกไว้ไม่ให้ตัวถัดไปได้สิทธิ์
			} else {
				canCancel = false;
				cannotReason = "ยกเลิกได้เฉพาะรายการล่าสุด";
			}

			m.put("canCancel", canCancel);
			m.put("cannotReason", cannotReason);

			rows.add(m);
		}

		model.addAttribute("phone", phone);
		model.addAttribute("bookings", rows);
		model.addAttribute("title", "รายการจองของคุณ");
		return "booking/my-bookings";
	}

	@PostMapping("/{id}/cancel")
	@ResponseBody
	public Object cancel(@PathVariable Long id, @RequestBody Map<String, String> body) {
		String phone = body.getOrDefault("phone", "");
		String reason = body.getOrDefault("reason", "cancel-via-ui");
		bookingService.cancelByOwner(id, phone, reason);
		return Map.of("ok", true);
	}
}
