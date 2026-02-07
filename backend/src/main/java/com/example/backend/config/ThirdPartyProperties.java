package com.example.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "third-party")
public class ThirdPartyProperties {

    private String baseUrl;
    private String appKey;
    private String appSecret;
    private Http http = new Http();
    private RateLimit rateLimit = new RateLimit();

    @Data
    public static class Http {
        private int connectionTimeout = 5000;
        private int readTimeout = 10000;
        private Retry retry = new Retry();

        @Data
        public static class Retry {
            private int maxAttempts = 3;
            private long initialDelay = 1000;
            private double multiplier = 2.0;
            private long maxDelay = 10000;
        }
    }

    @Data
    public static class RateLimit {
        private int capacity = 10;
        private int refillTokens = 10;
        private int refillDurationSeconds = 1;
    }
}
