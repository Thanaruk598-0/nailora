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
		http.csrf(csrf -> csrf.ignoringRequestMatchers(new AntPathRequestMatcher("/webhooks/**"),
				new AntPathRequestMatcher("/booking/**", HttpMethod.POST.name()),
				new AntPathRequestMatcher("/payments/**", HttpMethod.POST.name()),
				new AntPathRequestMatcher("/my-bookings/**", HttpMethod.POST.name())))
				.authorizeHttpRequests(auth -> auth.requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico")
						.permitAll().requestMatchers("/", "/login").permitAll()
						.requestMatchers("/booking/**", "/payments/**", "/calendar/**", "/my-bookings/**",
								"/webhooks/**")
						.permitAll().requestMatchers("/admin/**").hasRole("ADMIN").anyRequest().permitAll())
				.formLogin(login -> login.loginPage("/login").defaultSuccessUrl("/admin/services", true).permitAll())
				.logout(l -> l.logoutUrl("/logout").logoutSuccessUrl("/login?logout").permitAll())
				.headers(headers -> headers.contentSecurityPolicy(csp -> csp.policyDirectives(String.join(" ",
						// ❗ รวมเป็นบรรทัดเดียว ไม่มี connect-src ซ้ำ
						"default-src 'self';", "img-src 'self' data: https://*.stripe.com;",
						"script-src 'self' 'unsafe-inline' https://js.stripe.com https://cdn.jsdelivr.net;",
						"style-src  'self' 'unsafe-inline' https://cdn.jsdelivr.net;",
						"font-src 'self' data: https://cdn.jsdelivr.net;",
						"frame-src https://js.stripe.com https://hooks.stripe.com;",
						"connect-src 'self' https://api.stripe.com https://js.stripe.com https://cdn.jsdelivr.net;",
						"object-src 'none';", "frame-ancestors 'none'"))).frameOptions(frame -> frame.deny())
						.httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).preload(true)))
				.sessionManagement(sm -> sm.sessionFixation(sf -> sf.migrateSession()));

		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}
}
