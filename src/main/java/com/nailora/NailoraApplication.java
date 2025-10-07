package com.nailora;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NailoraApplication {

	public static void main(String[] args) {
		SpringApplication.run(NailoraApplication.class, args);
	}

}
