package com.nailora.service.impl;

import com.nailora.entity.Booking;
import com.nailora.repository.BookingRepository;
import com.nailora.service.StripeReceiptService;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StripeReceiptServiceImpl implements StripeReceiptService {

	private final BookingRepository bookingRepo;

	@Override
	public void ensureReceiptUrl(Long bookingId) {
		bookingRepo.findById(bookingId).ifPresent(b -> {
			if (b.getReceiptUrl() != null && !b.getReceiptUrl().isBlank())
				return;
			if (b.getPaymentRef() == null || b.getPaymentRef().isBlank())
				return;
			try {
				PaymentIntent pi = PaymentIntent.retrieve(b.getPaymentRef());
				String lc = pi.getLatestCharge();
				if (lc != null) {
					Charge ch = Charge.retrieve(lc);
					String url = ch.getReceiptUrl();
					if (url != null && !url.isBlank()) {
						b.setReceiptUrl(url);
						bookingRepo.save(b);
					}
				}
			} catch (StripeException e) {
				log.warn("[receipt] fetch fail for booking #{}: {}", bookingId, e.getMessage());
			}
		});
	}
}
