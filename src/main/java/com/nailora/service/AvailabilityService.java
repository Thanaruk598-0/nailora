package com.nailora.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface AvailabilityService {
	record SlotDTO(Long slotId, LocalDateTime startAt, LocalDateTime endAt, String techName, int capacity,
			int remaining) {
	}

	List<SlotDTO> findSlots(Long serviceId, LocalDate date);

	int remaining(Long slotId, LocalDateTime now);
}
