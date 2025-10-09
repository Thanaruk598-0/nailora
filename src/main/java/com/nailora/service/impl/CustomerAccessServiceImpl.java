package com.nailora.service.impl;

import com.nailora.repository.BookingRepository;
import com.nailora.service.CustomerAccessService;
import com.nailora.util.PhoneNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerAccessServiceImpl implements CustomerAccessService {
	private final BookingRepository bookingRepo;

	@Override
	public String normalizePhone(String raw) {
		return PhoneNormalizer.normalize(raw);
	}

	@Override
	public boolean isOwner(Long bookingId, String phone) {
		var booking = bookingRepo.findById(bookingId).orElse(null);
		if (booking == null)
			return false;
		return normalizePhone(booking.getPhone()).equals(normalizePhone(phone));
	}
}
