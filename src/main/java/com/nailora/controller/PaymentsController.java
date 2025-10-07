package com.nailora.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

import com.nailora.service.BookingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentsController {

	private final BookingService bookingService;
	
	public record ClientSecretResponse(String clientSecret) {}

	@PostMapping("/intent")
	@ResponseStatus(HttpStatus.OK)
	public ClientSecretResponse create(@RequestParam Long bookingId) {
		String clientSecret = bookingService.createStripePaymentIntent(bookingId);
		return new ClientSecretResponse(clientSecret);
	}

}
