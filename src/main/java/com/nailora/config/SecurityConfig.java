package com.nailora.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      // ช่วง dev ปิด CSRF ไปก่อน (เดี๋ยวเปิดตอนทำฟอร์มจริง/เว็บฮุค)
      .csrf(csrf -> csrf.disable())

      // เปิดหมดให้เข้าได้ เพื่อไม่ให้เด้ง /login ตอนเริ่มโปรเจกต์
      .authorizeHttpRequests(auth -> auth
        .requestMatchers(
          "/health",
          "/css/**", "/js/**", "/images/**",
          "/webhooks/**"
        ).permitAll()
        .anyRequest().permitAll()
      )

      // ปิดหน้า login/basic ชั่วคราว
      .formLogin(form -> form.disable())
      .httpBasic(basic -> basic.disable());
    
// // เปลี่ยนเฉพาะส่วน authorize + login
//    .authorizeHttpRequests(auth -> auth
//      .requestMatchers("/health", "/css/**", "/js/**", "/images/**", "/webhooks/**").permitAll()
//      .requestMatchers("/admin/**").hasRole("ADMIN")
//      .anyRequest().permitAll()
//    )
//    .formLogin(login -> login
//      .loginPage("/login").permitAll()
//      .defaultSuccessUrl("/admin/services", true)
//    );

    return http.build();
  }
}