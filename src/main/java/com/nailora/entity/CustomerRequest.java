package com.nailora.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "customer_request")
public class CustomerRequest {
	public enum Type {
		CANCEL, EMERGENCY
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "booking_id", nullable = false)
	private Booking booking;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Type type; //ประเภทคำขอ

	@Column(length = 300)
	private String reason; //เหตุผล

	private LocalDateTime requestedAt; //เวลายื่นคำขอ
	
}