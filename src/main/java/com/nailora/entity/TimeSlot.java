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

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "service_id")
	private ServiceItem service; // อ้างบริการ

	@Column(name = "start_at")   
	private LocalDateTime startAt; // เวลาเริ่มคิว

	@Column(name = "end_at")
	private LocalDateTime endAt; // เวลาจบคิว

	@Column(name = "capacity")
	private Integer capacity; // จำนวนที่นั่ง/ที่รับได้ในช่วงนั้น

	@Column(name = "open")
	private Boolean open; // เปิดให้จองสาธารณะไหม

	@Column(name = "tech_name")
	private String techName; // ชื่อช่าง/ผู้ทำบริการ

}
