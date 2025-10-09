package com.nailora.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "booking", indexes = { @Index(name = "idx_booking_status", columnList = "status"),
		@Index(name = "idx_booking_deposit_due", columnList = "deposit_due_at") }, uniqueConstraints = {
				@UniqueConstraint(name = "uq_booking_slot_phone", columnNames = { "time_slot_id", "phone" }) })
public class Booking {

	public enum Status {
		BOOKED, CANCELLED
	}

	public enum DepositStatus {
		UNPAID, PROCESSING, PAID, REFUNDED, VOIDED
	}

	public enum CancelReason {
		AUTO_EXPIRED, CUSTOMER, SHOP
	}

	public enum Gateway {
		STRIPE
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "time_slot_id", nullable = false)
	private TimeSlot timeSlot; 

	@Column(nullable = false, length = 120)
	@ToString.Include
	private String customerName;

	@Column(nullable = false, length = 20)
	private String phone;

	@Column(length = 500)
	private String note;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Status status = Status.BOOKED;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal servicePrice; //ราคาบริการตอนจอง (snapshot)

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal addOnPrice; //ราคา add-on รวม (snapshot)

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal depositAmount; //มัดจำที่ต้องจ่าย

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20, name = "deposit_status")
	private DepositStatus depositStatus = DepositStatus.UNPAID;

	@Column(name = "deposit_due_at")
	private LocalDateTime depositDueAt; //กำหนดเวลาชำระมัดจำ

	@Column(length = 120, name = "payment_ref")
	private String paymentRef; //Stripe PaymentIntent id (เช่น pi_xxx)

	private LocalDateTime depositPaidAt; //เวลาที่ชำระสำเร็จ

	@Column(length = 255, name = "receipt_url")
	private String receiptUrl; //ลิงก์ใบเสร็จจาก Stripe

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private Gateway gateway = Gateway.STRIPE; //ช่องทางชำระ (ล็อกไว้ที่ STRIPE)

	private LocalDateTime createdAt;

	private LocalDateTime canceledAt;

	@Enumerated(EnumType.STRING)
	@Column(length = 30)
	private CancelReason cancelReason;

	@PrePersist
	void onCreate() {
		if (createdAt == null)
			createdAt = LocalDateTime.now();
		if (status == null)
			status = Status.BOOKED;
		if (depositStatus == null)
			depositStatus = DepositStatus.UNPAID;
	}
}
