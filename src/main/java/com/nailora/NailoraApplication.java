package com.nailora;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableScheduling
@EntityScan(basePackages = "com.nailora.entity")
@EnableJpaRepositories(basePackages = "com.nailora.repository")
public class NailoraApplication {

	public static void main(String[] args) {
		SpringApplication.run(NailoraApplication.class, args);
	}

	@Bean
	CommandLineRunner printBcrypt(PasswordEncoder encoder) {
		return args -> {
			System.out.println("BCrypt(admin29041115) = " + encoder.encode("admin29041115"));
		};
	}
}
