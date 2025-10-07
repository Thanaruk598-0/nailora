package com.nailora.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "booking_add_on", uniqueConstraints = @UniqueConstraint(name = "uq_booking_addon_pair", columnNames = {
		"booking_id", "add_on_id" }))
public class BookingAddOn {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "booking_id", nullable = false)
	private Booking booking; //FK booking_id

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "add_on_id", nullable = false)
	private AddOn addOn; //FK add_on_id

}
