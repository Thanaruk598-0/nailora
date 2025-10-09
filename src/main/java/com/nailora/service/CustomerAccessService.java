package com.nailora.service;

public interface CustomerAccessService {
	String normalizePhone(String raw);

	boolean isOwner(Long bookingId, String phone);
}