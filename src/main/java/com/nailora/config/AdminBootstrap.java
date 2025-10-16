package com.nailora.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.nailora.repository.UserRepository;
import com.nailora.repository.RoleRepository;
import com.nailora.entity.User;
import com.nailora.entity.Role;

import java.util.HashSet;

@Configuration
@RequiredArgsConstructor
public class AdminBootstrap {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;

    @Bean
    @Transactional
    CommandLineRunner ensureAdmin(PasswordEncoder encoder) {
        return args -> {
            final String USERNAME = "NailoraAdmin";
            final String RAW_PASSWORD = "admin29041115";

            // --- ensure ROLE_ADMIN exists ---
            Role adminRole = roleRepo.findAll().stream()
                    .filter(r -> "ROLE_ADMIN".equals(r.getName()))
                    .findFirst()
                    .orElseGet(() -> roleRepo.save(Role.builder().name("ROLE_ADMIN").build()));

            // --- create/update admin user ---
            User u = userRepo.findByUsername(USERNAME).orElse(null);
            if (u == null) {
                u = User.builder()
                        .username(USERNAME)
                        .passwordHash(encoder.encode(RAW_PASSWORD))
                        .enabled(true)
                        .build();
            } else {
                // ถ้าไม่อยากทับรหัสทุกครั้ง ให้คอมเมนต์ 2 บรรทัดด้านล่างได้
                u.setPasswordHash(encoder.encode(RAW_PASSWORD));
                u.setEnabled(true);
            }

            // --- attach ROLE_ADMIN (requires roles mapping on User) ---
            try {
                if (u.getRoles() == null) {
                    u.setRoles(new HashSet<>());
                }
                if (!u.getRoles().contains(adminRole)) {
                    u.getRoles().add(adminRole);
                }
            } catch (NoSuchMethodError | NullPointerException e) {
                // ถ้า entity User ยังไม่มี field roles ให้ดูหมายเหตุด้านล่าง
            }

            userRepo.save(u);
        };
    }
}