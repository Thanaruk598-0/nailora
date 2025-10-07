package com.nailora.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "app_user", indexes = @Index(name = "uq_user_username", columnList = "username", unique = true))
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 80, unique = true)
	private String username; //ชื่อผู้ใช้ (ไม่ซ้ำ)

	@Column(nullable = false, length = 120)
	private String passwordHash; //รหัสผ่านเข้ารหัส (BCrypt)

	@Column(nullable = false)
	private Boolean enabled = true; //เปิดใช้งานไหม

}
