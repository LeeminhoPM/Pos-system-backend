package com.bluesky.pos_system.configuration;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class StripeConfig {

    private final StripeProperties stripeProperties;

    @PostConstruct
    public void initStripe() {
        if (!stripeProperties.isMockMode()) {
            Stripe.apiKey = stripeProperties.getApiKey().trim();
            log.info("Stripe Java SDK initialized successfully with provided API key");
        } else {
            log.warn("Stripe API key is not configured. Running in Sandbox Simulation Mode.");
        }
    }
}
