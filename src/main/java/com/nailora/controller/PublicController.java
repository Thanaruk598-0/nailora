package com.nailora.controller;

import com.nailora.dto.BookingCreatedResponse;
import com.nailora.dto.BookingRequest;
import com.nailora.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
public class PublicController {

	private final BookingService bookingService;

	@PostMapping("/booking/create")
	@ResponseStatus(HttpStatus.CREATED)
	public BookingCreatedResponse create(@RequestBody BookingRequest req) {
		Long id = bookingService.createBooking(req);
		// คำนวณ due ฝั่ง controller ให้สอดคล้องกับ service (now + 5m)
		// หรือจะอ่านจาก DB ก็ได้ แต่เพื่อความง่าย: FE รู้ว่าให้ retry/จ่ายภายใน 5
		// นาทีจากเวลาตอนนี้
		return new BookingCreatedResponse(id, LocalDateTime.now().plusMinutes(5));
	}

	@GetMapping("/booking/remaining")
	public int remaining(@RequestParam Long slotId) {
		return bookingService.remainingCapacity(slotId, LocalDateTime.now());
	}

}
