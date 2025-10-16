package com.nailora.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
  name = "service_item",
  uniqueConstraints = {
    @UniqueConstraint(name = "uq_service_item_name_ci", columnNames = "name")
  }
)
public class ServiceItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; //ไอดีบริการ

	@Column(nullable = false, length = 120)
	private String name; //ชื่อบริการ

	@Column(nullable = false)
	private Integer durationMin; //ระยะเวลาต่อคิว (นาที)

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal price; //ราคาบริการ

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal depositMin; //มัดจำขั้นต่ำ

	@Column(nullable = false)
	private Boolean active = true; //เปิดใช้งานไหม

}
