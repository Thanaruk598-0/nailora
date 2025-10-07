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
@Entity
@Table(name = "deposit_txn")
public class DepositTxn {
	public enum Type {
		VERIFY, VOID, REFUND, CREDIT
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "booking_id", nullable = false)
	private Booking booking; //FK booking_id

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Type type; //ประเภทธุรกรรม

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal amount; //จำนวนเงิน

	@Column(length = 120)
	private String reference; //อ้างอิง (เช่น เลขสลิป/คืนเงิน)

	private LocalDateTime actedAt; //เวลาเกิดธุรกรรม

	@Column(length = 80)
	private String actor; //ผู้ดำเนินการ (username)
}
