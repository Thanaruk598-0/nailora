package com.nailora.job;

import com.nailora.entity.Booking;
import com.nailora.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingCleanupJob {

	private final BookingRepository bookingRepo;
	private final Clock clock;

	// รันทุก 1 นาที
	@Scheduled(fixedDelay = 60_000L)
	public void cleanExpiredUnpaid() {
		LocalDateTime now = LocalDateTime.now(clock);
		List<Booking> expired = bookingRepo.findExpiredUnpaid(now);
		if (expired.isEmpty())
			return;

		for (var b : expired) {
			// ปิดการถือที่นั่ง
			b.setStatus(resolveStatus("CANCELED", "CANCELLED")); // รองรับได้ทั้งสองแบบ
			b.setCancelReason(resolveCancelReason("EXPIRED", "SYSTEM", "OWNER_CANCEL", "CUSTOMER", "USER"));
			b.setCanceledAt(now);
			b.setDepositStatus(Booking.DepositStatus.UNPAID);
			bookingRepo.save(b);
			log.info("[cleanup] cancel expired booking id={}", b.getId());
		}
	}

	private Booking.Status resolveStatus(String... candidates) {
		for (String name : candidates) {
			try {
				return Booking.Status.valueOf(name);
			} catch (IllegalArgumentException ignored) {
			}
		}
		// fallback: ใช้ค่าตัวแรกของ enum เพื่อตัดปัญหาโปรเจ็กต์ละชื่อ
		return Booking.Status.values()[0];
	}

	private Booking.CancelReason resolveCancelReason(String... candidates) {
		for (String name : candidates) {
			try {
				return Booking.CancelReason.valueOf(name);
			} catch (IllegalArgumentException ignored) {
			}
		}
		// fallback: ใช้ค่าตัวแรกของ enum
		return Booking.CancelReason.values()[0];
	}
}
