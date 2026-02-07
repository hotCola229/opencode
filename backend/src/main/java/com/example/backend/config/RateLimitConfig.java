package com.example.backend.config;

import com.google.common.util.concurrent.RateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimitConfig {

    private final ThirdPartyProperties properties;

    public RateLimitConfig(ThirdPartyProperties properties) {
        this.properties = properties;
    }

    @Bean
    public RateLimiter rateLimitBucket() {
        return RateLimiter.create(properties.getRateLimit().getRefillTokens());
    }
}
