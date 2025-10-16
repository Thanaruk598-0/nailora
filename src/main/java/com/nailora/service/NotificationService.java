package com.nailora.service;

import com.nailora.entity.Booking;

public interface NotificationService {
	void notifyBookingCreated(Booking booking);

	void notifyPaymentSuccess(Booking booking);

	void notifyReminder(Booking booking, String whenNote);
}
