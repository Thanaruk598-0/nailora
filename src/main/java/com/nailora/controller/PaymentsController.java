package com.nailora.controller;

import com.nailora.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentsController {

	private final BookingService bookingService;

	@PostMapping("/intent")
	public ResponseEntity<?> createIntent(@RequestParam Long bookingId) {
		try {
			String clientSecret = bookingService.createStripePaymentIntent(bookingId);
			return ResponseEntity.ok(Map.of("clientSecret", clientSecret));
		} catch (ResponseStatusException ex) {
			// โยนเหตุผลกลับให้ FE เห็น
			String msg = ex.getReason() != null ? ex.getReason() : ex.getMessage();
			return ResponseEntity.status(ex.getStatusCode()).body(Map.of("error", "stripe", "message", msg));
		} catch (Exception ex) {
			return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
					.body(Map.of("error", "unexpected", "message", ex.getMessage()));
		}
	}
}
