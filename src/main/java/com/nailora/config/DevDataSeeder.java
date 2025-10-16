package com.nailora.config;

import com.nailora.entity.AddOn;
import com.nailora.entity.ServiceItem;
import com.nailora.entity.TimeSlot;
import com.nailora.repository.AddOnRepository;
import com.nailora.repository.ServiceItemRepository;
import com.nailora.repository.TimeSlotRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Configuration
@Profile("dev")
public class DevDataSeeder {

	@Bean
	ApplicationRunner seedDevData(ServiceItemRepository serviceRepo, AddOnRepository addOnRepo,
			TimeSlotRepository slotRepo) {
		return args -> {
			// ----- Services (idempotent ด้วยการหา top-asc) -----
			ServiceItem gel = serviceRepo.findTopByNameIgnoreCaseOrderByIdAsc("ทำเล็บเจล (Basic)").orElseGet(() -> {
				ServiceItem s = new ServiceItem();
				s.setName("ทำเล็บเจล (Basic)");
				s.setDurationMin(60);
				s.setPrice(new BigDecimal("800.00"));
				s.setDepositMin(new BigDecimal("200.00"));
				s.setActive(true);
				return serviceRepo.save(s);
			});

			ServiceItem acrylic = serviceRepo.findTopByNameIgnoreCaseOrderByIdAsc("ต่อเล็บอะคริลิก").orElseGet(() -> {
				ServiceItem s = new ServiceItem();
				s.setName("ต่อเล็บอะคริลิก");
				s.setDurationMin(90);
				s.setPrice(new BigDecimal("1500.00"));
				s.setDepositMin(new BigDecimal("300.00"));
				s.setActive(true);
				return serviceRepo.save(s);
			});

			// ----- Add-ons -----
			addOnRepo.findTopByNameIgnoreCaseOrderByIdAsc("เพ้นต์ลาย").orElseGet(() -> {
				AddOn a = new AddOn();
				a.setName("เพ้นต์ลาย");
				a.setExtraMinutes(20);
				a.setExtraPrice(new BigDecimal("150.00"));
				a.setActive(true);
				return addOnRepo.save(a);
			});

			addOnRepo.findTopByNameIgnoreCaseOrderByIdAsc("ถอดเล็บเก่า").orElseGet(() -> {
				AddOn a = new AddOn();
				a.setName("ถอดเล็บเก่า");
				a.setExtraMinutes(15);
				a.setExtraPrice(new BigDecimal("100.00"));
				a.setActive(true);
				return addOnRepo.save(a);
			});

			// ----- Time slots (idempotent) -----
			LocalDate today = LocalDate.now();

			// วันนี้ 14:00–15:00 → ทำเล็บเจล
			createSlotIfAbsent(slotRepo, gel, LocalDateTime.of(today, LocalTime.of(14, 0)),
					LocalDateTime.of(today, LocalTime.of(15, 0)), 1, "Mint");

			// วันนี้ 15:00–16:30 → ต่อเล็บอะคริลิก
			createSlotIfAbsent(slotRepo, acrylic, LocalDateTime.of(today, LocalTime.of(15, 0)),
					LocalDateTime.of(today, LocalTime.of(16, 30)), 1, "Praew");

			// พรุ่งนี้ 11:00–12:00 → ทำเล็บเจล
			createSlotIfAbsent(slotRepo, gel, LocalDateTime.of(today.plusDays(1), LocalTime.of(11, 0)),
					LocalDateTime.of(today.plusDays(1), LocalTime.of(12, 0)), 1, "Mint");
		};
	}

	/**
	 * helper แบบไม่สร้างซ้ำ: - ใช้ existsByServiceItem_IdAndStartAt(...) เช็คก่อน -
	 * ค่า open/active = true เสมอสำหรับ seed
	 */
	private void createSlotIfAbsent(TimeSlotRepository slotRepo, ServiceItem service, LocalDateTime start,
			LocalDateTime end, int capacity, String techName) {
		if (slotRepo.existsByServiceItem_IdAndStartAt(service.getId(), start)) {
			return; // มีแล้ว ข้าม
		}
		TimeSlot t = new TimeSlot();
		t.setServiceItem(service);
		t.setStartAt(start);
		t.setEndAt(end);
		t.setTechName(techName);
		t.setCapacity(capacity);
		t.setOpen(true);
		t.setActive(true);
		try {
			slotRepo.save(t);
		} catch (org.springframework.dao.DataIntegrityViolationException ignore) {
			// กันกรณี race/unique ล้ม — ข้ามได้
		}
	}
}
