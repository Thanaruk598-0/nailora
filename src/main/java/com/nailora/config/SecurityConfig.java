package com.nailora.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.ignoringRequestMatchers("/webhooks/**", "/booking/**", "/payments/**")) // เว้นเฉพาะ webhook
				.authorizeHttpRequests(auth -> auth.requestMatchers("/css/**", "/js/**", "/images/**", "/login")
						.permitAll().requestMatchers("/webhooks/**").permitAll().requestMatchers("/admin/**")
						.hasRole("ADMIN").anyRequest().permitAll())
				.formLogin(login -> login.loginPage("/login").defaultSuccessUrl("/admin/services", true).permitAll())
				.logout(l -> l.logoutUrl("/logout").logoutSuccessUrl("/login?logout").permitAll());
		return http.build();
	}

	@Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}