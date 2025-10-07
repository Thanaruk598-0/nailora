package com.nailora.service.impl;

import com.nailora.dto.BookingRequest;
import com.nailora.entity.Booking;
import com.nailora.entity.TimeSlot;
import com.nailora.repository.BookingRepository;
import com.nailora.repository.TimeSlotRepository;
import com.nailora.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

	private final BookingRepository bookingRepo;
	private final TimeSlotRepository timeSlotRepo;

	@Override
	public int remainingCapacity(Long slotId, LocalDateTime now) {
		TimeSlot slot = timeSlotRepo.findById(slotId)
				.orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "timeslot not found"));
		long holding = bookingRepo.countHoldingSeats(slotId, now);
		return Math.max(0, slot.getCapacity() - (int) holding);
	}

	@Override
	@Transactional
	public Long createBooking(BookingRequest req) {
		// 1) โหลด slot
		TimeSlot slot = timeSlotRepo.findById(req.timeSlotId())
				.orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "timeslot not found"));
		if (Boolean.FALSE.equals(slot.getOpen())) {
			throw new ResponseStatusException(CONFLICT, "timeslot closed");
		}

		// 2) กันซ้ำเบอร์เดิมใน slot เดิม
		if (bookingRepo.existsByTimeSlotIdAndPhone(req.timeSlotId(), req.phone())) {
			throw new ResponseStatusException(CONFLICT, "already booked with this phone in this slot");
		}

		// 3) เช็คความจุ (ที่นั่งที่ถูกถือไว้ = paid หรือ unpaid แต่ยังไม่หมดเวลา)
		var now = LocalDateTime.now();
		long holding = bookingRepo.countHoldingSeats(req.timeSlotId(), now);
		if (holding >= slot.getCapacity()) {
			throw new ResponseStatusException(CONFLICT, "slot is full");
		}

		// 4) คำนวณราคา/มัดจำแบบง่าย (Step 2): deposit = depositMin ของ service
		// (Step 4-5 จะต่อยอด logic add-ons / ส่วนลด ฯลฯ)
		BigDecimal servicePrice = slot.getService().getPrice();
		BigDecimal deposit = slot.getService().getDepositMin();

		// 5) บันทึก Booking: ถือไว้ 5 นาที
		LocalDateTime due = now.plusMinutes(5);
		Booking b = Booking.builder().timeSlot(slot).customerName(req.customerName()).phone(req.phone())
				.note(req.note()).servicePrice(servicePrice).addOnPrice(BigDecimal.ZERO) // TODO: คำนวณ add-on ใน Step
																							// ถัดไป
				.depositAmount(deposit).depositStatus(Booking.DepositStatus.UNPAID).depositDueAt(due)
				.status(Booking.Status.BOOKED).gateway(Booking.Gateway.STRIPE).build();

		return bookingRepo.save(b).getId();
	}

}
