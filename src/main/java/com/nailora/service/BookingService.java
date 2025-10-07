package com.nailora.service;

import java.time.LocalDateTime;

import com.nailora.dto.BookingRequest;

public interface BookingService {

	Long createBooking(BookingRequest req);

	int remainingCapacity(Long slotId, LocalDateTime now);

	String createStripePaymentIntent(Long bookingId);
}
