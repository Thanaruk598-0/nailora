package com.nailora.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "service_add_on", uniqueConstraints = @UniqueConstraint(name = "uq_service_addon_pair", columnNames = {
		"service_id", "add_on_id" }))
public class ServiceAddOn {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "service_id", nullable = false)
	private ServiceItem service;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "add_on_id", nullable = false)
	private AddOn addOn;

	@Column(nullable = false)
	private Boolean active = true;
}
