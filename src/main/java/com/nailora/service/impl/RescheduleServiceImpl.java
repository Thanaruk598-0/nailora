package com.nailora.service.impl;

import com.nailora.dto.RescheduleRequest;
import com.nailora.entity.Booking;
import com.nailora.repository.BookingRepository;
import com.nailora.repository.TimeSlotRepository;
import com.nailora.service.AvailabilityService;
import com.nailora.service.CustomerAccessService;
import com.nailora.service.RescheduleService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;

@Service
@RequiredArgsConstructor
public class RescheduleServiceImpl implements RescheduleService {
	private final BookingRepository bookingRepo;
	private final TimeSlotRepository slotRepo;
	private final AvailabilityService availabilityService;
	private final CustomerAccessService accessService;
	private final Clock clock;

	@Override
	@Transactional
	public void reschedule(Long bookingId, RescheduleRequest req) {
		var booking = bookingRepo.findById(bookingId).orElseThrow();
		if (!accessService.isOwner(bookingId, req.phone())) {
			throw new IllegalArgumentException("Not owner");
		}
// TODO: policy checks (lead-time, status allowed, etc.)
		int remaining = availabilityService.remaining(req.newTimeSlotId(), java.time.LocalDateTime.now(clock));
		if (remaining <= 0)
			throw new IllegalStateException("Target slot full");
		booking.setTimeSlot(slotRepo.findById(req.newTimeSlotId()).orElseThrow());
// TODO: write BookingHistory entry
		bookingRepo.save(booking);
	}
}
