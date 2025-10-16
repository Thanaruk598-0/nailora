package com.nailora.service.impl;

import com.nailora.repository.TimeSlotRepository;
import com.nailora.service.AvailabilityService;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;

@Service
@RequiredArgsConstructor
@Primary
@lombok.extern.slf4j.Slf4j
public class AvailabilityServiceImpl implements AvailabilityService {
	private final TimeSlotRepository timeSlotRepo;
	private final com.nailora.repository.BookingRepository bookingRepo;
	private final Clock clock; // ensure a Clock bean is available (Spring provides systemDefault if you config
								// it)

	@Override
	public int remaining(Long slotId, LocalDateTime now) {
		var slot = timeSlotRepo.findById(slotId).orElseThrow();
		long holding = bookingRepo.countHoldingSeats(slotId, now);
		return Math.max(0, slot.getCapacity() - (int) holding);
	}

	@Override
	public List<SlotDTO> findSlots(Long serviceId, LocalDate date) {
		// TODO Auto-generated method stub
		return null;
	}
}
