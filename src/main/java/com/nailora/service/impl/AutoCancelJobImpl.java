package com.nailora.service.impl;

import com.nailora.entity.Booking;
import com.nailora.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AutoCancelJobImpl {

	private final BookingRepository bookingRepo;

	// รันทุก 60 วิ
	@Scheduled(fixedRate = 60_000)
	@Transactional
	public void run() {
		LocalDateTime now = LocalDateTime.now();
		List<Booking> expired = bookingRepo.findExpiredUnpaid(now);
		if (expired.isEmpty()) {
			log.debug("[job] auto-cancel: no expired bookings at {}", now);
			return;
		}
		log.info("[job] auto-cancel: {} booking(s) expired", expired.size());

		for (Booking b : expired) {
			b.setStatus(Booking.Status.CANCELLED);
			b.setCancelReason(Booking.CancelReason.AUTO_EXPIRED);
			b.setCanceledAt(now);
			// ถ้าถือที่นั่งไว้แล้วไม่จ่าย ให้ void สถานะมัดจำไปเลย
			if (b.getDepositStatus() != Booking.DepositStatus.PAID) {
				b.setDepositStatus(Booking.DepositStatus.VOIDED);
			}
			bookingRepo.save(b);
			log.info("[job] booking #{} -> CANCELLED (auto expired)", b.getId());
		}
	}

}
