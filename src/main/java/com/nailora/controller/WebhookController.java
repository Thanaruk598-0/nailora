package com.nailora.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nailora.entity.Booking;
import com.nailora.repository.BookingRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/webhooks")
public class WebhookController {
	private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

	private final BookingRepository bookingRepo;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Value("${stripe.webhook.secret}")
	private String webhookSecret;

	@PostMapping(value = "/stripe", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	@Transactional
	public void handle(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
		// 1) verify signature ด้วย Stripe SDK
		final Event event;
		try {
			event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
		} catch (SignatureVerificationException e) {
			log.warn("[stripe] invalid signature: {}", e.getMessage());
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid signature");
		}

		// 2) log type
		log.info("[stripe] event: {} id={}", event.getType(), event.getId());

		// 3) แตก payload ด้วย Jackson (ไม่ใช้ EventDataObjectDeserializer)
		try {
			JsonNode root = objectMapper.readTree(payload);
			JsonNode obj = root.path("data").path("object"); // payment_intent object
			String type = root.path("type").asText(null);

			switch (type) {
			case "payment_intent.succeeded" -> onSucceeded(obj);
			case "payment_intent.payment_failed" -> onFailed(obj);
			case "charge.refunded" -> onRefunded(root.path("data").path("object")); // object เป็น charge
			default -> log.info("[stripe] ignore event: {}", type);
			}
		} catch (Exception ex) {
			log.warn("[stripe] parse payload error: {}", ex.getMessage());
			// 200 OK เพื่อไม่ให้ Stripe รีทริกเกอร์ตามดีไซน์, แต่ log ไว้พอ
		}
	}

	private void onSucceeded(JsonNode pi) {
		String piId = textOrNull(pi, "id");
		String bookingIdStr = pi.path("metadata").path("bookingId").asText(null);
		String latestChargeId = textOrNull(pi, "latest_charge");
		Long bookingId = parseLong(bookingIdStr);

		log.info("[stripe] succeeded for pi={}, bookingId={}", piId, bookingId);

		if (bookingId == null)
			return;

		Optional<Booking> opt = bookingRepo.findById(bookingId);
		if (opt.isEmpty())
			return;
		Booking b = opt.get();

		// idempotent
		if (b.getDepositStatus() == Booking.DepositStatus.PAID) {
			log.info("[stripe] booking #{} already PAID, skip", bookingId);
			return;
		}

		String receiptUrl = null;
		if (latestChargeId != null) {
			try {
				Charge ch = Charge.retrieve(latestChargeId);
				receiptUrl = ch.getReceiptUrl();
			} catch (StripeException e) {
				log.warn("[stripe] get receipt fail for {}: {}", latestChargeId, e.getMessage());
			}
		}

		b.setDepositStatus(Booking.DepositStatus.PAID);
		b.setDepositPaidAt(LocalDateTime.now());
		b.setReceiptUrl(receiptUrl);
		b.setPaymentRef(piId);
		bookingRepo.save(b);
		log.info("[stripe] booking #{} -> PAID", bookingId);
	}

	private void onFailed(JsonNode pi) {
		String piId = textOrNull(pi, "id");
		String bookingIdStr = pi.path("metadata").path("bookingId").asText(null);
		Long bookingId = parseLong(bookingIdStr);
		log.info("[stripe] failed for pi={}, bookingId={}", piId, bookingId);
		if (bookingId == null)
			return;

		bookingRepo.findById(bookingId).ifPresent(b -> {
			if (b.getDepositStatus() != Booking.DepositStatus.PAID) {
				b.setDepositStatus(Booking.DepositStatus.UNPAID);
				bookingRepo.save(b);
				log.info("[stripe] booking #{} -> UNPAID", bookingId);
			}
		});
	}

	private void onRefunded(JsonNode charge) {
		String piId = textOrNull(charge, "payment_intent");
		if (piId == null)
			return;

		bookingRepo.findAll().stream().filter(b -> piId.equals(b.getPaymentRef())).findFirst().ifPresent(b -> {
			b.setDepositStatus(Booking.DepositStatus.REFUNDED);
			bookingRepo.save(b);
			log.info("[stripe] booking #{} -> REFUNDED", b.getId());
		});
	}

	private Long parseLong(String s) {
		try {
			return s == null ? null : Long.valueOf(s);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private String textOrNull(JsonNode node, String field) {
		JsonNode v = node.path(field);
		return v.isMissingNode() || v.isNull() ? null : v.asText();
	}
}
