package com.nailora.controller.admin;

import com.nailora.entity.TimeSlot;
import com.nailora.repository.ServiceItemRepository;
import com.nailora.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/slots")
public class AdminSlotsController {

	private final ServiceItemRepository serviceRepo;
	private final TimeSlotRepository timeSlotRepo;

	@GetMapping
	public String list(
			@RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate date,
			@RequestParam(required = false) Long serviceId, org.springframework.ui.Model model) {
		var services = serviceRepo.findAll();
		if (date == null || serviceId == null) {
			var today = (date == null ? java.time.LocalDate.now() : date);
			var sid = (serviceId == null && !services.isEmpty() ? services.get(0).getId() : serviceId);
			return "redirect:/admin/slots?date=" + today + "&serviceId=" + sid;
		}

		if (date == null || serviceId == null) {
			var today = (date == null ? java.time.LocalDate.now() : date);
			var sid = (serviceId == null ? services.get(0).getId() : serviceId);
			return "redirect:/admin/slots?date=" + today + "&serviceId=" + sid;
		}

		var svc = serviceRepo.findById(serviceId).orElse(null);
		var start = date.atStartOfDay();
		var end = date.plusDays(1).atStartOfDay();

		var slots = (svc == null) ? java.util.List.<com.nailora.entity.TimeSlot>of()
				: timeSlotRepo.findByServiceIdAndStartAtBetweenOrderByStartAtAsc(serviceId, start, end);

		model.addAttribute("title", "จัดการสลอต (แอดมิน)");
		model.addAttribute("services", services);
		model.addAttribute("serviceId", serviceId);
		model.addAttribute("date", date.toString());
		model.addAttribute("slots", slots);
		return "admin/slots/list";
	}

	@PostMapping("/generate")
	@Transactional
	public String generate(@RequestParam Long serviceId, @RequestParam String date, @RequestParam String fromTime, // "HH:mm"
			@RequestParam String toTime, // "HH:mm"
			@RequestParam Integer durationMin, @RequestParam Integer gapMin, @RequestParam Integer capacity,
			@RequestParam(defaultValue = "true") boolean open, @RequestParam(defaultValue = "Mint") String techName) {

		var svc = serviceRepo.findById(serviceId).orElseThrow();
		LocalDate d = LocalDate.parse(date);
		LocalTime from = LocalTime.parse(fromTime);
		LocalTime to = LocalTime.parse(toTime);

		LocalDateTime cursor = LocalDateTime.of(d, from);
		LocalDateTime end = LocalDateTime.of(d, to);

		while (!cursor.plusMinutes(durationMin).isAfter(end)) {
			TimeSlot t = new TimeSlot();
			t.setService(svc);
			t.setStartAt(cursor);
			t.setEndAt(cursor.plusMinutes(durationMin));
			t.setTechName(techName);
			t.setCapacity(capacity);
			t.setOpen(open);
			timeSlotRepo.save(t);

			cursor = cursor.plusMinutes(durationMin + (gapMin == null ? 0 : gapMin));
		}

		return "redirect:/admin/slots?date=" + date + "&serviceId=" + serviceId;
	}

	@PostMapping("/{id}/delete")
	public String delete(@PathVariable Long id, @RequestParam String date, @RequestParam Long serviceId) {
		timeSlotRepo.deleteById(id);
		return "redirect:/admin/slots?date=" + date + "&serviceId=" + serviceId;
	}
}
