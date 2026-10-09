package com.bluesky.pos_system.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "stripe")
public class StripeProperties {

    /**
     * Stripe Secret API Key (e.g. sk_test_... or sk_live_...)
     */
    private String apiKey = "";

    /**
     * Stripe Publishable Key for frontend Stripe Elements (e.g. pk_test_... or pk_live_...)
     */
    private String publishableKey = "";

    /**
     * Stripe Webhook Signing Secret (e.g. whsec_...)
     */
    private String webhookSecret = "";

    /**
     * Default currency code (e.g. "vnd", "usd", "eur")
     */
    private String currency = "vnd";

    /**
     * Whether running in sandbox/simulation mode if keys are absent
     */
    public boolean isMockMode() {
        return apiKey == null || apiKey.trim().isEmpty();
    }
}
