package com.nailora.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfig {

	@Value("${stripe.secret.key}")
	String apiKey;

	@PostConstruct
	void init() {
		Stripe.apiKey = apiKey;
	}

}
