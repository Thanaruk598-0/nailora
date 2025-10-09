package com.nailora.util;

public final class Finders {
	private Finders() {
	}

	public static com.nailora.entity.Booking booking(com.nailora.service.BookingService svc, Long id) {
		return svc instanceof com.nailora.service.BookingService ? null : null;
	}
}