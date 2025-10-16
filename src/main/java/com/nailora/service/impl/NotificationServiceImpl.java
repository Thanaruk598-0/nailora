package com.nailora.service.impl;

import com.nailora.entity.Booking;
import com.nailora.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {
	@Override
	public void notifyBookingCreated(Booking booking) {
		log.info("[Notify] Booking created id={} phone={}", booking.getId(), booking.getPhone());
	}

	@Override
	public void notifyPaymentSuccess(Booking booking) {
		log.info("[Notify] Payment success id={} receipt={}", booking.getId(), booking.getReceiptUrl());
	}

	@Override
	public void notifyReminder(Booking booking, String whenNote) {
		log.info("[Notify] Reminder {} id={} startAt={}", whenNote, booking.getId(),
				booking.getTimeSlot().getStartAt());
	}
}