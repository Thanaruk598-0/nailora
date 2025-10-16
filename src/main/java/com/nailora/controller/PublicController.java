package com.nailora.controller;

import com.nailora.dto.BookingRequest;
import com.nailora.dto.BookingSummary;
import com.nailora.repository.BookingRepository;
import com.nailora.repository.ServiceItemRepository;
import com.nailora.repository.TimeSlotRepository;
import com.nailora.service.BookingService;
import com.nailora.service.PriceEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/booking")
public class PublicController {

	private final PriceEngine priceEngine;
	private final BookingService bookingService;
	private final Clock clock;
	private final ServiceItemRepository serviceItemRepository;
	private final TimeSlotRepository timeSlotRepository;
	private final BookingRepository bookingRepository;
	private final JdbcTemplate jdbc;

	@PostMapping("/create")
	@ResponseBody
	public ResponseEntity<?> create(@RequestBody BookingRequest req) {
		try {
			Long id = bookingService.createBooking(req);

			var b = bookingRepository.findById(id).orElseThrow();

			var body = Map.of("bookingId", id, "depositAmount", b.getDepositAmount(), "servicePrice",
					b.getServicePrice(), "addOnPrice", b.getAddOnPrice(), "depositDueAt", b.getDepositDueAt(),
					"serverNow", LocalDateTime.now(clock));
			return ResponseEntity.ok(body);

		} catch (org.springframework.web.server.ResponseStatusException rse) {
			// ให้ GlobalErrorHandler แปลงสถานะตามจริง (409/404 ฯลฯ)
			throw rse;
		} catch (Exception ex) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("error", ex.getClass().getSimpleName(), "message", String.valueOf(ex.getMessage())));
		}
	}

	@GetMapping("/price-quote")
	@ResponseBody
	public ResponseEntity<?> quote(@RequestParam Long serviceId, @RequestParam(required = false) String addOnIds,
			@RequestParam(required = false) String coupon) {
		List<Long> list = (addOnIds == null || addOnIds.isBlank()) ? List.of()
				: Arrays.stream(addOnIds.split(",")).filter(s -> !s.isBlank()).map(Long::valueOf).toList();

		return ResponseEntity.ok(priceEngine.quote(serviceId, list, coupon));
	}

	@GetMapping("/services")
	public String services(Model model) {
		model.addAttribute("title", "บริการ");
		model.addAttribute("services", serviceItemRepository.findAll());
		
		
		return "booking/catalog";
	}

	@GetMapping("/confirm")
	public String confirm(@RequestParam Long slotId, @RequestParam Long serviceId, Model model) {
		var slot = timeSlotRepository.findByIdWithService(slotId).orElseThrow();
		var svc = slot.getServiceItem();

		// ป้องกันกรณีแก้ serviceId ใน URL ไม่ตรงกับ slot: บังคับใช้ตาม slot
		Long effectiveServiceId = svc.getId();

		// --- ส่ง add-on ทั้งหมดที่ active (เลือกได้ทุก service) ---
		var addOns = jdbc.queryForList("""
				    select id, name, extra_minutes as extraMinutes, extra_price as extraPrice
				    from add_on
				    where active = 1
				    order by name
				""");

		// ถ้าอยาก “จำกัดตาม service” ให้ใช้ SQL ด้านล่างแทน (ต้องมีตาราง
		// service_add_on)
		// var addOns = jdbc.queryForList("""
		// select a.id, a.name, a.extra_minutes as extraMinutes, a.extra_price as
		// extraPrice
		// from service_add_on sao
		// join add_on a on a.id = sao.add_on_id
		// where sao.service_id = ? and sao.active = 1 and a.active = 1
		// order by a.name
		// """, effectiveServiceId);

		model.addAttribute("title", "ยืนยันการจอง");
		model.addAttribute("slotId", slotId);
		model.addAttribute("serviceId", effectiveServiceId);
		model.addAttribute("addOns", addOns);

		// สรุปเบื้องต้น (ยังไม่รวม add-on)
		model.addAttribute("bookingSummary", new BookingSummary(0L, svc.getName(), slot.getTechName(),
				slot.getStartAt(), "BOOKED", "UNPAID", svc.getDepositMin(), String.valueOf(slot.getEndAt()) // ฟิลด์ท้ายของ
																											// BookingSummary
																											// เป็นข้อความเวลาจบ
		));
		

		return "booking/confirm";
	}
}
