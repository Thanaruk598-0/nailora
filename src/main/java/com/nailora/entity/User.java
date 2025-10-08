package com.nailora.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

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
	private String username;

	@Column(name = "password_hash", nullable = false, length = 120)
	private String passwordHash;

	@Column(nullable = false)
	private Boolean enabled = true;

	// ต้องมี mapping นี้ เพื่อให้ดึง ROLE_XXX ได้
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
	@Builder.Default
	private Set<Role> roles = new HashSet<>();
}
