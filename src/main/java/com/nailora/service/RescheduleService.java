package com.nailora.service;

import com.nailora.dto.RescheduleRequest;

public interface RescheduleService {
	void reschedule(Long bookingId, RescheduleRequest req);
}
