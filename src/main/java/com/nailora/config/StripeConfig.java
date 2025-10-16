package com.nailora.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
@PropertySource(value = "classpath:application-secret.properties", ignoreResourceNotFound = true)
public class StripeConfig {

  @Value("${stripe.secret.key:}")
  private String stripeSecretKey;

  @PostConstruct
  public void init() {
    if (stripeSecretKey == null || stripeSecretKey.isBlank()) {
      log.error("Stripe secret key is NOT set! Check application.properties");
      return;
    }
    com.stripe.Stripe.apiKey = stripeSecretKey.trim();
    log.info("Stripe API key configured (sk_test...{}).", stripeSecretKey.trim().substring(Math.max(0, stripeSecretKey.length()-6)));
  }
}
