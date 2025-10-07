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
@Table(name = "add_on")
public class AddOn {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 120)
	private String name; //ชื่อ Add-on

	@Column(nullable = false)
	private Integer extraMinutes = 0; //เวลาที่เพิ่มจากบริการหลัก

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal extraPrice; //ราคาเพิ่ม

	@Column(nullable = false)
	private Boolean active = true; //เปิดใช้งานไหม

}
