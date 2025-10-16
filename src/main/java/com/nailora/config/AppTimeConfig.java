package com.nailora.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class AppTimeConfig {

	@Bean
	Clock clock(@Value("${app.timezone:Asia/Bangkok}") String tz) {
		return Clock.system(ZoneId.of(tz));
	}
}
