package com.nailora.service;

public interface StripeReceiptService {
	void ensureReceiptUrl(Long bookingId);
}
