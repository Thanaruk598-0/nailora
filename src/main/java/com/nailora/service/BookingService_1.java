package com.nailora.service;

import com.nailora.entity.Booking;
import com.nailora.entity.TimeSlot;
import com.nailora.exception.BadRequestException;
import com.nailora.exception.NotFoundException;
import com.nailora.repository.BookingRepository_1;
import com.nailora.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService_1 {

	private final BookingRepository_1 bookingRepository;
	private final TimeSlotRepository timeSlotRepository;

	// ---------- READ ----------
	@Transactional(readOnly = true)
	public List<Booking> getAllBookings() {
		// ใช้ fetch join กัน LazyInitializationException
		return bookingRepository.findAllWithSlot(null);
	}

	@Transactional(readOnly = true)
	public Booking getBooking(Long id) {
		return bookingRepository.findByIdWithSlot(id)
				.orElseThrow(() -> new NotFoundException("Booking not found with id " + id));
	}

	// ---------- CREATE ----------
	@Transactional
	public Booking createBooking(Long timeSlotId, String customerName, String phone, String note,
			BigDecimal servicePrice, BigDecimal addOnPrice, BigDecimal depositAmount) {

		if (timeSlotId == null) {
			throw new BadRequestException("timeSlotId is required");
		}

		TimeSlot ts = timeSlotRepository.findById(timeSlotId)
				.orElseThrow(() -> new NotFoundException("TimeSlot not found with id " + timeSlotId));

		long current = bookingRepository.countByTimeSlotIdAndStatus(ts.getId(), Booking.Status.BOOKED);
		if (current >= ts.getCapacity()) {
			throw new BadRequestException("This time slot is full (capacity reached)");
		}

		if (bookingRepository.existsByTimeSlotIdAndPhone(ts.getId(), phone)) {
			throw new BadRequestException("This phone already booked this time slot");
		}

		// กัน null
		if (servicePrice == null)
			servicePrice = BigDecimal.ZERO;
		if (addOnPrice == null)
			addOnPrice = BigDecimal.ZERO;
		if (depositAmount == null)
			depositAmount = BigDecimal.ZERO;

		Booking b = Booking.builder().timeSlot(ts).customerName(customerName).phone(phone).note(note)
				.status(Booking.Status.BOOKED).servicePrice(servicePrice).addOnPrice(addOnPrice)
				.depositAmount(depositAmount).depositStatus(Booking.DepositStatus.UNPAID)
				.depositDueAt(LocalDateTime.now().plusHours(2)).createdAt(LocalDateTime.now())
				.gateway(Booking.Gateway.STRIPE).build();

		b = bookingRepository.save(b);
		// reload พร้อม fetch join
		return bookingRepository.findByIdWithSlot(b.getId())
				.orElseThrow(() -> new NotFoundException("Created booking not found"));
	}

	// ---------- UPDATE ----------
	@Transactional
	public Booking updateBooking(Long id, String customerName, String phone, String note) {
		Booking b = bookingRepository.findByIdWithSlot(id)
				.orElseThrow(() -> new NotFoundException("Booking not found with id " + id));

		if (customerName != null)
			b.setCustomerName(customerName);
		if (phone != null)
			b.setPhone(phone);
		if (note != null)
			b.setNote(note);

		bookingRepository.save(b);
		return bookingRepository.findByIdWithSlot(b.getId())
				.orElseThrow(() -> new NotFoundException("Updated booking not found"));
	}

	@Transactional
	public Booking updateStatus(Long id, Booking.Status status) {
		Booking b = bookingRepository.findByIdWithSlot(id)
				.orElseThrow(() -> new NotFoundException("Booking not found with id " + id));

		b.setStatus(status);
		if (status == Booking.Status.CANCELLED && b.getCanceledAt() == null) {
			b.setCanceledAt(LocalDateTime.now());
		}

		bookingRepository.save(b);
		return bookingRepository.findByIdWithSlot(b.getId())
				.orElseThrow(() -> new NotFoundException("Updated booking not found"));
	}

	// ---------- PAYMENT ----------
	@Transactional
	public Booking verifyDeposit(Long id, String paymentRef) {
		Booking b = bookingRepository.findByIdWithSlot(id)
				.orElseThrow(() -> new NotFoundException("Booking not found with id " + id));

		b.setDepositStatus(Booking.DepositStatus.PAID);
		b.setPaymentRef(paymentRef);
		b.setDepositPaidAt(LocalDateTime.now());

		bookingRepository.save(b);
		return bookingRepository.findByIdWithSlot(b.getId())
				.orElseThrow(() -> new NotFoundException("Updated booking not found"));
	}

	@Transactional
	public Booking refundDeposit(Long id, String paymentRef) {
		Booking b = bookingRepository.findByIdWithSlot(id)
				.orElseThrow(() -> new NotFoundException("Booking not found with id " + id));

		b.setDepositStatus(Booking.DepositStatus.REFUNDED);
		if (paymentRef != null)
			b.setPaymentRef(paymentRef);

		bookingRepository.save(b);
		return bookingRepository.findByIdWithSlot(b.getId())
				.orElseThrow(() -> new NotFoundException("Updated booking not found"));
	}

	@Transactional
	public Booking voidDeposit(Long id, String paymentRef) {
		Booking b = bookingRepository.findByIdWithSlot(id)
				.orElseThrow(() -> new NotFoundException("Booking not found with id " + id));

		b.setDepositStatus(Booking.DepositStatus.VOIDED);
		if (paymentRef != null)
			b.setPaymentRef(paymentRef);

		bookingRepository.save(b);
		return bookingRepository.findByIdWithSlot(b.getId())
				.orElseThrow(() -> new NotFoundException("Updated booking not found"));
	}

	// ---------- DELETE ----------
	@Transactional
	public void deleteBooking(Long id) {
		bookingRepository.deleteById(id);
	}
}
