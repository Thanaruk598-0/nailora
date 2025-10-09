package com.nailora.service.impl;

import com.nailora.dto.BookingRequest;
import com.nailora.entity.Booking;
import com.nailora.entity.TimeSlot;
import com.nailora.repository.BookingRepository;
import com.nailora.repository.TimeSlotRepository;
import com.nailora.service.BookingService;
import com.nailora.service.CustomerAccessService;
import com.nailora.service.DepositService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCreateParams;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

	private final BookingRepository bookingRepo;
	private final TimeSlotRepository timeSlotRepo;
	private final CustomerAccessService accessService;
	private final DepositService depositService;
	private final java.time.Clock clock;
	private final JdbcTemplate jdbc;

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
		var now = java.time.LocalDateTime.now(); // หรือ LocalDateTime.now(clock)
		long holding = bookingRepo.countHoldingSeats(req.timeSlotId(), now);
		if (holding >= slot.getCapacity()) {
			throw new ResponseStatusException(CONFLICT, "slot is full");
		}

		// 4) คำนวณราคา/มัดจำแบบง่าย (Step 2): deposit = depositMin ของ service
		// (Step 4-5 จะต่อยอด logic add-ons / ส่วนลด ฯลฯ)
		BigDecimal servicePrice = slot.getService().getPrice();
		BigDecimal deposit = slot.getService().getDepositMin();

		List<Long> addOnIds = req.addOnIds() == null ? java.util.List.of() : req.addOnIds();
		BigDecimal addOnPrice = java.math.BigDecimal.ZERO;
		if (!addOnIds.isEmpty()) {
			String inSql = addOnIds.stream().map(id -> "?").reduce((a, b) -> a + "," + b).orElse("?");
			BigDecimal sum = jdbc.queryForObject(
					"SELECT COALESCE(SUM(extra_price),0) FROM add_on WHERE id IN (" + inSql + ")", BigDecimal.class,
					addOnIds.toArray());
			addOnPrice = (sum == null ? BigDecimal.ZERO : sum);
		}

		// 5) บันทึก Booking: ถือไว้ 5 นาที
		// บันทึก Booking
		var due = now.plusMinutes(5);
		Booking b = Booking.builder().timeSlot(slot).customerName(req.customerName()).phone(req.phone())
				.note(req.note()).servicePrice(servicePrice).addOnPrice(addOnPrice).depositAmount(deposit)
				.depositStatus(Booking.DepositStatus.UNPAID).depositDueAt(due).status(Booking.Status.BOOKED)
				.gateway(Booking.Gateway.STRIPE).createdAt(now).build();

		Long bookingId = bookingRepo.save(b).getId();
		
		if (!addOnIds.isEmpty()) {
            addOnIds.forEach(aid -> jdbc.update(
                "INSERT INTO booking_add_on(booking_id, add_on_id) VALUES (?, ?)",
                bookingId, aid
            ));
        }
		
		return bookingId;
	}

	@Override
	public String createStripePaymentIntent(Long bookingId) {
		var b = bookingRepo.findById(bookingId)
				.orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "booking not found"));

		// guard พื้นฐาน
		if (b.getStatus() != Booking.Status.BOOKED)
			throw new ResponseStatusException(CONFLICT, "booking not active");
		if (b.getDepositDueAt() != null && b.getDepositDueAt().isBefore(LocalDateTime.now()))
			throw new ResponseStatusException(CONFLICT, "deposit window expired");
		if (b.getDepositStatus() == Booking.DepositStatus.PAID)
			throw new ResponseStatusException(CONFLICT, "already paid");

		try {
			// ถ้ามี paymentRef แล้ว เช็คสถานะก่อน
			if (b.getPaymentRef() != null && !b.getPaymentRef().isBlank()) {
				PaymentIntent existing = PaymentIntent.retrieve(b.getPaymentRef());
				String st = existing.getStatus(); // requires_payment_method / requires_confirmation / processing /
													// succeeded / canceled / ...

				switch (st) {
				case "requires_payment_method":
				case "requires_confirmation":
					// ใช้ client_secret เดิมต่อได้
					return existing.getClientSecret();

				case "processing":
					// อย่าคอนเฟิร์มซ้ำ
					throw new ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT,
							"payment is processing, try again in a moment");

				case "succeeded":
					throw new ResponseStatusException(CONFLICT, "already paid");

				case "canceled":
				default:
					// ใช้ต่อไม่ได้ -> สร้างใหม่ด้านล่าง
					break;
				}
			}

			// --- สร้าง PaymentIntent ใหม่ ---
			long amount = b.getDepositAmount().multiply(new java.math.BigDecimal("100")).longValueExact();

			PaymentIntentCreateParams params = PaymentIntentCreateParams.builder().setAmount(amount).setCurrency("thb")
					.setDescription("NAILORA Deposit for Booking #" + bookingId)
					.putMetadata("bookingId", String.valueOf(bookingId)) // ให้ webhook ใช้แมป booking
					.setAutomaticPaymentMethods(
							PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build())
					.build();

			// ใช้ idempotency key ใหม่ทุกครั้งที่ “สร้างใหม่”
			String idemKey = "PAYINT:" + bookingId + ":" + System.currentTimeMillis();

			RequestOptions opts = RequestOptions.builder().setIdempotencyKey(idemKey).build();

			PaymentIntent pi = PaymentIntent.create(params, opts);
			b.setPaymentRef(pi.getId());
			b.setDepositStatus(Booking.DepositStatus.PROCESSING); // optional
			bookingRepo.save(b);

			return pi.getClientSecret();

		} catch (com.stripe.exception.StripeException e) {
			var err = e.getStripeError();
			String requestId = e.getRequestId(); // from StripeException
			Integer statusCode = e.getStatusCode(); // <-- ใช้จาก StripeException
			String code = (err != null ? err.getCode() : null);
			String declineCode = (err != null ? err.getDeclineCode() : null);
			String userMsg = (err != null && err.getMessage() != null) ? err.getMessage() : e.getMessage();

			org.slf4j.LoggerFactory.getLogger(getClass()).error(
					"Stripe error creating/retrieving PI: requestId={}, status={}, code={}, declineCode={}, message={}",
					requestId, statusCode, code, declineCode, userMsg, e);

			throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_GATEWAY,
					"stripe error: " + userMsg);
		}
	}

	@Override
	@Transactional
	public void cancelByOwner(Long bookingId, String phone, String reason) {
		var b = bookingRepo.findById(bookingId)
				.orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "booking not found"));

		// เช็คเจ้าของ
		if (!accessService.normalizePhone(b.getPhone()).equals(accessService.normalizePhone(phone))) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not owner");
		}

		var now = LocalDateTime.now(clock);

		// ห้ามยกเลิกหลังเริ่ม
		if (now.isAfter(b.getTimeSlot().getStartAt())) {
			throw new ResponseStatusException(CONFLICT, "service already started");
		}

		switch (b.getDepositStatus()) {
		case UNPAID -> {
			// depositService.voidDeposit(...)
			// if (b.getPaymentRef() != null && !b.getPaymentRef().isBlank()) {
			// depositService.voidDeposit(bookingId, "Owner-cancel: " + reason);
			// }
			b.setStatus(resolveStatus("CANCELED", "CANCELLED"));
			b.setCancelReason(resolveCancelReason("OWNER_CANCEL", "CUSTOMER", "USER"));
			b.setCanceledAt(now);
			bookingRepo.save(b);
		}
		case PAID -> {
			// นโยบายตัวอย่าง: ต้อง ≥ 120 นาทีล่วงหน้า
			long minutesBefore = java.time.Duration.between(now, b.getTimeSlot().getStartAt()).toMinutes();
			if (minutesBefore >= 120) {
				// "auto-refund", "Owner-cancel: " + reason);
				b.setStatus(resolveStatus("CANCELED", "CANCELLED"));
				b.setCancelReason(resolveCancelReason("OWNER_CANCEL", "CUSTOMER", "USER"));
				b.setCanceledAt(now);
				bookingRepo.save(b);
			} else {
				throw new ResponseStatusException(CONFLICT, "too late to cancel; contact admin");
			}
		}
		default -> throw new ResponseStatusException(CONFLICT, "unsupported deposit status: " + b.getDepositStatus());
		}
	}

	private Booking.Status resolveStatus(String... candidates) {
		for (String name : candidates) {
			try {
				return Booking.Status.valueOf(name);
			} catch (IllegalArgumentException ignored) {
			}
		}
		throw new IllegalStateException("No matching Booking.Status for " + java.util.Arrays.toString(candidates));
	}

	private Booking.CancelReason resolveCancelReason(String... candidates) {
		for (String name : candidates) {
			try {
				return Booking.CancelReason.valueOf(name);
			} catch (IllegalArgumentException ignored) {
			}
		}
		// fallback: ถ้าชื่อไม่ตรงจริง ๆ ลองใช้ค่าตัวแรกของ enum เพื่อไม่ให้คอมไพล์ล้ม
		return Booking.CancelReason.values()[0];
	}

}
