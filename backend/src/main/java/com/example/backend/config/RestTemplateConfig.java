package com.example.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    private final ThirdPartyProperties properties;

    public RestTemplateConfig(ThirdPartyProperties properties) {
        this.properties = properties;
    }

    @Bean
    public RestTemplate restTemplate() {
        org.springframework.http.client.SimpleClientHttpRequestFactory factory =
            new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getHttp().getConnectionTimeout());
        factory.setReadTimeout(properties.getHttp().getReadTimeout());

        RestTemplate restTemplate = new RestTemplate(factory);

        RetryTemplate retryTemplate = new RetryTemplate();

        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(properties.getHttp().getRetry().getInitialDelay());
        backOffPolicy.setMultiplier(properties.getHttp().getRetry().getMultiplier());
        backOffPolicy.setMaxInterval(properties.getHttp().getRetry().getMaxDelay());
        retryTemplate.setBackOffPolicy(backOffPolicy);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(properties.getHttp().getRetry().getMaxAttempts());
        retryTemplate.setRetryPolicy(retryPolicy);

        restTemplate.setRequestFactory(factory);

        return restTemplate;
    }
}
