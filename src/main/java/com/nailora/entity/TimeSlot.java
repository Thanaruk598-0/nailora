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
@Table(name = "time_slot")
public class TimeSlot {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "service_id", nullable = false)
	private ServiceItem service; //อ้างบริการ

	@Column(nullable = false)
	private LocalDateTime startAt; //เวลาเริ่มคิว

	@Column(nullable = false)
	private LocalDateTime endAt; //เวลาจบคิว

	@Column(nullable = false)
	private Integer capacity = 1; //จำนวนที่นั่ง/ที่รับได้ในช่วงนั้น

	@Column(nullable = false)
	private Boolean open = true; //เปิดให้จองสาธารณะไหม

	@Column(length = 80)
	private String techName; //ชื่อช่าง/ผู้ทำบริการ

}
