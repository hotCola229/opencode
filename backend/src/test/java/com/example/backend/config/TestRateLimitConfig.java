package com.example.backend.config;

import com.google.common.util.concurrent.RateLimiter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestRateLimitConfig {

    @Bean
    @Primary
    public RateLimiter testRateLimitBucket() {
        RateLimiter limiter = RateLimiter.create(10000);
        return limiter;
    }
}
