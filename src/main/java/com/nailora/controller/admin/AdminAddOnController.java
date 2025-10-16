package com.nailora.controller.admin;

import com.nailora.dto.AddOnDTO;
import com.nailora.dto.AddOnRequest;
import com.nailora.service.AddOnService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/api/addons")
@RequiredArgsConstructor
public class AdminAddOnController {

	private final AddOnService addOnService;

	@GetMapping
	public List<AddOnDTO> list() {
		return addOnService.findAll();
	}

	@GetMapping("/{id}")
	public AddOnDTO get(@PathVariable Long id) {
		return addOnService.findOne(id);
	}

	@PostMapping
	public AddOnDTO create(@Valid @RequestBody AddOnRequest req) {
		return addOnService.create(req);
	}

	@PutMapping("/{id}")
	public AddOnDTO update(@PathVariable Long id, @Valid @RequestBody AddOnRequest req) {
		return addOnService.update(id, req);
	}

	@DeleteMapping("/{id}")
	public void delete(@PathVariable Long id) {
		addOnService.delete(id);
	}

	// เปิด/ปิดการใช้งาน
	@PatchMapping("/{id}/active")
	public AddOnDTO setActive(@PathVariable Long id, @RequestParam boolean active) {
		return addOnService.setActive(id, active);
	}
}