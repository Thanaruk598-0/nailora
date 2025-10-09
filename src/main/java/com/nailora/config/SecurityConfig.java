package com.nailora.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				// CSRF: allow POST จาก FE + webhook
				.csrf(csrf -> csrf.ignoringRequestMatchers(new AntPathRequestMatcher("/webhooks/**"),
						new AntPathRequestMatcher("/booking/**", HttpMethod.POST.name()),
						new AntPathRequestMatcher("/payments/**", HttpMethod.POST.name()),
						new AntPathRequestMatcher("/my-bookings/**", HttpMethod.POST.name())))

				// AuthZ
				.authorizeHttpRequests(auth -> auth.requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico")
						.permitAll().requestMatchers("/", "/login").permitAll()
						.requestMatchers("/booking/**", "/payments/**", "/calendar/**", "/my-bookings/**",
								"/webhooks/**")
						.permitAll().requestMatchers("/admin/**").hasRole("ADMIN").anyRequest().permitAll())

				// Form login/logout
				.formLogin(login -> login.loginPage("/login").defaultSuccessUrl("/admin/slots", true) // << เดิมเป็น
																										// /admin/services
						.permitAll())
				.logout(l -> l.logoutUrl("/logout").logoutSuccessUrl("/login?logout").permitAll())

				// Security headers
				.headers(headers -> headers.contentSecurityPolicy(csp -> csp
						.policyDirectives("default-src 'self'; " + "img-src 'self' data: https://*.stripe.com; "
								+ "script-src 'self' 'unsafe-inline' https://js.stripe.com; " + // << เพิ่ม
																								// 'unsafe-inline'
								"style-src 'self' 'unsafe-inline'; "
								+ "frame-src https://js.stripe.com https://hooks.stripe.com; "
								+ "connect-src 'self' https://api.stripe.com https://js.stripe.com; "
								+ "font-src 'self' data:; " + "frame-ancestors 'none'; object-src 'none'"))
						.frameOptions(frame -> frame.deny())
						.httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).preload(true)))

				// Session hardening
				.sessionManagement(sm -> sm.sessionFixation(sf -> sf.migrateSession()));

		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}
}
