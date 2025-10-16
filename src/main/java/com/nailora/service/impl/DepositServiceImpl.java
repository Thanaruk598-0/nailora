package com.nailora.service.impl;

import org.springframework.stereotype.Service;

import com.nailora.repository.BookingRepository;
import com.nailora.service.DepositService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepositServiceImpl implements DepositService {
	private final BookingRepository bookingRepo;
	// inject Stripe client/secret ผ่าน AppProps หรือ Environment ก็ได้

	@Override
	@Transactional
	public void voidDeposit(Long bookingId, String reason) {
		var b = bookingRepo.findById(bookingId).orElseThrow();
		if (b.getPaymentRef() == null || b.getPaymentRef().isBlank())
			return;
		log.info("Void PI {} for booking {}", b.getPaymentRef(), bookingId);
		// Stripe: PaymentIntent.cancel(...)
		// บันทึก DepositTxn(type=VOID, reference=PI, actor=SYSTEM/USER, reason)
	}

	@Override
	@Transactional
	public void refundFull(Long bookingId, String refundRef, String reason) {
		var b = bookingRepo.findById(bookingId).orElseThrow();
		if (b.getPaymentRef() == null || b.getPaymentRef().isBlank())
			return;
		log.info("Refund full for booking {} (PI={})", bookingId, b.getPaymentRef());
		// Stripe: Refund.create(...) จาก charge ของ PI
		// บันทึก DepositTxn(type=REFUND, reference=refundId, actor=SYSTEM/ADMIN,
		// reason)
	}
}
