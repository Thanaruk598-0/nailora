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
@Table(name = "time_slot", uniqueConstraints = {
		@UniqueConstraint(name = "uq_timeslot_service_start", columnNames = { "service_id", "start_at" }) })
public class TimeSlot {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "service_id", nullable = false)
	private ServiceItem serviceItem;

	@Column(name = "start_at", nullable = false)
	private LocalDateTime startAt;

	@Column(name = "end_at", nullable = false)
	private LocalDateTime endAt;

	// กันปี พ.ศ. ให้กลายเป็น ค.ศ. ก่อน persist/update เสมอ
	@PrePersist
	@PreUpdate
	void fixThaiYear() {
		if (startAt != null && startAt.getYear() > 2200)
			startAt = startAt.minusYears(543);
		if (endAt != null && endAt.getYear() > 2200)
			endAt = endAt.minusYears(543);
	}

	@Column(name = "capacity")
	private Integer capacity;

	@Column(name = "open")
	private Boolean open;

	@Column(name = "tech_name")
	private String techName;

	@Column(nullable = false)
	private Boolean active = true;
}
