package com.nailora.controller.admin;

import com.nailora.dto.BookingRequest_1;
import com.nailora.service.RequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/api/requests")
@RequiredArgsConstructor
public class AdminRequestsController {

	private final RequestService requestService;

	// ✅ GET: ดึงทั้งหมด
	@GetMapping
	public List<BookingRequest_1> listRequests() {
		return requestService.getAllRequests();
	}

	// ✅ POST: สร้างใหม่
	@PostMapping
	public BookingRequest_1 createRequest(@RequestBody BookingRequest_1 request) {
		return requestService.createRequest(request);
	}

	// ✅ PATCH: อัปเดตสถานะ
	@PatchMapping("/{id}/status")
	public BookingRequest_1 updateStatus(@PathVariable Long id, @RequestParam String status) {
		return requestService.updateStatus(id, status);
	}
}