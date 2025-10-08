package com.nailora.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable()) // กัน CSRF block webhook
				.authorizeHttpRequests(auth -> auth.requestMatchers("/css/**", "/js/**", "/login").permitAll()
						.requestMatchers("/webhooks/**").permitAll() // สำคัญมาก!!
						.requestMatchers("/admin/**").hasRole("ADMIN").anyRequest().permitAll())
				.formLogin(login -> login.loginPage("/login").defaultSuccessUrl("/admin/services", true));
		return http.build();
	}
}