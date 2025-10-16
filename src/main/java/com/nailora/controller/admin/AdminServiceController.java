package com.nailora.controller.admin;

import com.nailora.entity.ServiceItem;
import com.nailora.repository.ServiceItemRepository;
import com.nailora.repository.TimeSlotRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin/services")
@RequiredArgsConstructor
public class AdminServiceController {

	private final ServiceItemRepository serviceRepo;

	private final TimeSlotRepository timeSlotRepo;

	/** รายการบริการ (หน้า templates/admin/services.html ของคุณ) */
	@GetMapping
	public String list(Model model) {
		model.addAttribute("services", serviceRepo.findAll(Sort.by(Sort.Direction.ASC, "id")));
		return "admin/services";
	}

	/** ฟอร์มสร้างใหม่ (หน้า service-form.html) */
	@GetMapping("/new")
	public String newForm(Model model) {
		if (!model.containsAttribute("service")) {
			model.addAttribute("service", ServiceItem.builder().name("").durationMin(60).price(new BigDecimal("0.00"))
					.depositMin(new BigDecimal("0.00")).active(true).build());
		}
		model.addAttribute("mode", "create");
		return "admin/service-form";
	}

	/** ฟอร์มแก้ไข (หน้า service-form.html) */
	@GetMapping("/{id}/edit")
	public String editForm(@PathVariable Long id, Model model) {
		ServiceItem svc = serviceRepo.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Service not found: " + id));
		model.addAttribute("service", svc);
		model.addAttribute("mode", "edit");
		return "admin/service-form";
	}

	@PostMapping
	public String create(@ModelAttribute("service") @Valid ServiceItem form, BindingResult binding,
			RedirectAttributes ra) {
		if (binding.hasErrors()) {
			ra.addFlashAttribute("org.springframework.validation.BindingResult.service", binding);
			ra.addFlashAttribute("service", form);
			return "redirect:/admin/services/new";
		}
		serviceRepo.findTopByNameIgnoreCaseOrderByIdAsc(form.getName()).ifPresent(existing -> {
			throw new IllegalArgumentException("มีบริการชื่อนี้อยู่แล้ว");
		});

		serviceRepo.save(form);
		ra.addFlashAttribute("message", "เพิ่มบริการเรียบร้อย");
		return "redirect:/admin/services";
	}

	@PostMapping("/{id}")
	public String update(@PathVariable Long id, @ModelAttribute("service") @Valid ServiceItem form,
			BindingResult binding, RedirectAttributes ra) {
		if (binding.hasErrors()) {
			ra.addFlashAttribute("org.springframework.validation.BindingResult.service", binding);
			ra.addFlashAttribute("service", form);
			return "redirect:/admin/services/" + id + "/edit";
		}
		var svc = serviceRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Service not found: " + id));

		// ไม่ให้เปลี่ยนชื่อไปชนตัวอื่น (ยกเว้นเป็นตัวเดิม)
		serviceRepo.findTopByNameIgnoreCaseOrderByIdAsc(form.getName()).ifPresent(dup -> {
			if (!dup.getId().equals(id)) {
				throw new IllegalArgumentException("มีบริการชื่อนี้อยู่แล้ว");
			}
		});

		svc.setName(form.getName());
		svc.setDurationMin(form.getDurationMin());
		svc.setPrice(form.getPrice());
		svc.setDepositMin(form.getDepositMin());
		svc.setActive(form.getActive() != null ? form.getActive() : Boolean.TRUE);

		serviceRepo.save(svc);
		ra.addFlashAttribute("message", "บันทึกการแก้ไขเรียบร้อย");
		return "redirect:/admin/services";
	}

	/** ลบ (มี try/catch กัน FK) */
	@GetMapping("/{id}/delete")
	public String delete(@PathVariable Long id, RedirectAttributes ra) {
		var svc = serviceRepo.findById(id).orElse(null);
		if (svc == null) {
			ra.addFlashAttribute("error", "ไม่พบรายการ");
			return "redirect:/admin/services";
		}

		long slotRefs = timeSlotRepo.countByServiceItem_Id(id);
		if (slotRefs > 0) {
			// ปิดการใช้งานแทน
			svc.setActive(false);
			serviceRepo.save(svc);
			ra.addFlashAttribute("error",
					"ลบไม่ได้เพราะมีการอ้างอิง (" + slotRefs + " time slots) -> ปิดการใช้งานให้แล้ว");
			return "redirect:/admin/services";
		}

		try {
			serviceRepo.deleteById(id);
			ra.addFlashAttribute("message", "ลบรายการแล้ว");
		} catch (DataIntegrityViolationException ex) {
			// กันกรณีมี booking อ้างผ่าน slot/constraint อื่น ๆ
			svc.setActive(false);
			serviceRepo.save(svc);
			ra.addFlashAttribute("error", "ลบไม่ได้: มีข้อมูลอ้างอิง → ปิดการใช้งานให้แล้ว");
		}
		return "redirect:/admin/services";
	}

	@ExceptionHandler({ IllegalArgumentException.class })
	public String handleIllegalArg(IllegalArgumentException ex, RedirectAttributes ra) {
		ra.addFlashAttribute("error", ex.getMessage());
		return "redirect:/admin/services";
	}
}
