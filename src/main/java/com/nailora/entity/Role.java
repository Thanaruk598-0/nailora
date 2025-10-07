package com.nailora.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "role", indexes = @Index(name = "uq_role_name", columnList = "name", unique = true))
public class Role {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 50, unique = true)
	private String name; //ชื่อบทบาท (เช่น ROLE_ADMIN)
}