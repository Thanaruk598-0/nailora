package com.nailora.config;

import com.nailora.entity.TimeSlot;
import com.nailora.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class StartupTimeSlotFixer  {

	private final TimeSlotRepository timeSlotRepo;
	
	private static final LocalDateTime CUTOFF = LocalDateTime.of(2200, 1, 1, 0, 0);

	@Bean
	ApplicationRunner fixBuddhistYearsOnStartup() {
		return args -> normalizeBuddhistYearRecords();
	}

	@Transactional
    void normalizeBuddhistYearRecords() {
        List<TimeSlot> bad = timeSlotRepo.findWithBuddhistYear(CUTOFF);
        if (bad.isEmpty()) {
            log.info("[TimeSlotFix] no buddhist-year records. ✅");
            return;
        }
        bad.forEach(t -> {
            boolean mutated = false;
            if (t.getStartAt() != null && t.getStartAt().isAfter(CUTOFF.minusNanos(1))) {
                t.setStartAt(t.getStartAt().minusYears(543));
                mutated = true;
            }
            if (t.getEndAt() != null && t.getEndAt().isAfter(CUTOFF.minusNanos(1))) {
                t.setEndAt(t.getEndAt().minusYears(543));
                mutated = true;
            }
            if (mutated) {
                log.warn("[TimeSlotFix] normalized id={} to CE start={} end={}",
                        t.getId(), t.getStartAt(), t.getEndAt());
            }
        });
        timeSlotRepo.saveAll(bad);
        log.info("[TimeSlotFix] fixed {} record(s). ✅", bad.size());
    }
}
