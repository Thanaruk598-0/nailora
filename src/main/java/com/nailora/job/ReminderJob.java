package com.nailora.job;

import com.nailora.repository.BookingRepository;
import com.nailora.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReminderJob {
	private final BookingRepository bookingRepo;
	private final NotificationService notify;

	@Scheduled(fixedRate = 300_000) // 5 นาที
	public void run() {
		var now = LocalDateTime.now();
		var t24 = now.plusHours(24);
		var t2 = now.plusHours(2);
// TODO: query bookings at ~T-24h/T-2h windows and call notify
		log.debug("ReminderJob tick at {}", now);
	}
}
