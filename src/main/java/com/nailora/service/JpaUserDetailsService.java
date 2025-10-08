package com.nailora.service;

import java.util.List;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import com.nailora.repository.UserRepository;
import com.nailora.entity.User;

@Service
@RequiredArgsConstructor
public class JpaUserDetailsService implements UserDetailsService {

	private final UserRepository userRepo;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User u = userRepo.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));

		List<SimpleGrantedAuthority> auths = u.getRoles().stream().map(r -> new SimpleGrantedAuthority(r.getName()))
				.toList();

		return org.springframework.security.core.userdetails.User.withUsername(u.getUsername())
				.password(u.getPasswordHash()) // (hash ที่ขึ้นต้น {bcrypt} ตรงกับ data.sql)
				.authorities(auths) // ต้องมี ROLE_ADMIN ถึงจะเข้า /admin/**
				.accountLocked(false).accountExpired(false).credentialsExpired(false)
				.disabled(Boolean.FALSE.equals(u.getEnabled())).build();
	}
}