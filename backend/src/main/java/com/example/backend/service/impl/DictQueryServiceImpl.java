package com.example.backend.service.impl;

import com.example.backend.config.ThirdPartyProperties;
import com.example.backend.dto.DictQueryRequestDTO;
import com.example.backend.entity.ExternalCallLog;
import com.example.backend.exception.RateLimitException;
import com.example.backend.service.DictQueryService;
import com.example.backend.service.ExternalCallLogService;
import com.example.backend.util.ThirdPartySignatureUtil;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class DictQueryServiceImpl implements DictQueryService {

    private static final String SERVICE_NAME = "DICT_QUERY";
    private static final String TARGET_PATH = "/api/v1/dataapi/execute/dict/query";

    private final ThirdPartyProperties properties;
    private final RestTemplate restTemplate;
    private final ThirdPartySignatureUtil signatureUtil;
    private final ExternalCallLogService externalCallLogService;
    private final RateLimiter rateLimitBucket;

    public DictQueryServiceImpl(ThirdPartyProperties properties,
                                RestTemplate restTemplate,
                                ThirdPartySignatureUtil signatureUtil,
                                ExternalCallLogService externalCallLogService,
                                RateLimiter rateLimitBucket) {
        this.properties = properties;
        this.restTemplate = restTemplate;
        this.signatureUtil = signatureUtil;
        this.externalCallLogService = externalCallLogService;
        this.rateLimitBucket = rateLimitBucket;
    }

    @Override
    public String query(DictQueryRequestDTO request, String traceId) {
        if (!rateLimitBucket.tryAcquire()) {
            saveRateLimitLog(traceId);
            throw new RateLimitException("请求频率超过限制，请稍后再试");
        }

        int maxAttempts = properties.getHttp().getRetry().getMaxAttempts();
        long initialDelay = properties.getHttp().getRetry().getInitialDelay();
        double multiplier = properties.getHttp().getRetry().getMultiplier();
        long maxDelay = properties.getHttp().getRetry().getMaxDelay();

        int attempt = 1;
        long delay = initialDelay;

        while (attempt <= maxAttempts) {
            try {
                return doQuery(request, traceId, attempt);
            } catch (HttpStatusCodeException e) {
                log.warn("第三方接口调用失败(attempt {}): status={}", attempt, e.getStatusCode());
                int statusCode = e.getStatusCode().value();
                callLogWithException(traceId, attempt, statusCode, e.getMessage());
                if (attempt >= maxAttempts) {
                    throw e;
                }
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("重试被中断", ie);
                }
                delay = (long) (delay * multiplier);
                delay = Math.min(delay, maxDelay);
                attempt++;
            } catch (RestClientException e) {
                log.warn("第三方接口调用失败(attempt {}): {}", attempt, e.getMessage());
                callLogWithException(traceId, attempt, null, e.getMessage());
                if (attempt >= maxAttempts) {
                    throw e;
                }
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("重试被中断", ie);
                }
                delay = (long) (delay * multiplier);
                delay = Math.min(delay, maxDelay);
                attempt++;
            }
        }
        throw new RuntimeException("第三方接口调用失败，已达到最大重试次数");
    }

    private void callLogWithException(String traceId, int attempt, Integer httpStatus, String message) {
        ExternalCallLog callLog = buildLog(traceId, attempt);
        callLog.setHttpStatus(httpStatus);
        callLog.setSuccess(0);
        callLog.setExceptionType("THIRD_PARTY_ERROR");
        callLog.setExceptionMessage(message);
        callLog.setCreatedAt(LocalDateTime.now());
        externalCallLogService.saveLog(callLog);
    }

    private String doQuery(DictQueryRequestDTO request, String traceId, int attempt) {
        long startTime = System.currentTimeMillis();
        ExternalCallLog callLog = buildLog(traceId, attempt);

        try {
            String url = buildUrl(request);
            Map<String, Object> params = buildParams(request);
            String timestamp = signatureUtil.generateTimestamp();
            String signature = signatureUtil.generateSignature(
                    "GET",
                    TARGET_PATH,
                    params,
                    properties.getAppKey(),
                    properties.getAppSecret(),
                    timestamp
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("AppKey", properties.getAppKey());
            headers.set("Signature", signature);
            headers.set("Timestamp", timestamp);

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(new LinkedMultiValueMap<>(), headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            int statusCode = response.getStatusCode().value();
            log.info("第三方接口响应状态码: {}, attempt: {}", statusCode, attempt);

            callLog.setHttpStatus(statusCode);
            callLog.setSuccess(statusCode >= 200 && statusCode < 300 ? 1 : 0);
            callLog.setDurationMs(System.currentTimeMillis() - startTime);
            callLog.setCreatedAt(LocalDateTime.now());

            String body = response.getBody();
            if (body != null) {
                callLog.setExceptionMessage(body.length() > 500 ? body.substring(0, 500) : body);
            }

            externalCallLogService.saveLog(callLog);

            if (statusCode >= 200 && statusCode < 300) {
                return body;
            }

            throw new RestClientException("第三方接口返回非成功状态: " + statusCode);

        } catch (HttpStatusCodeException e) {
            throw e;
        } catch (RestClientException e) {
            callLog.setSuccess(0);
            callLog.setDurationMs(System.currentTimeMillis() - startTime);
            callLog.setExceptionType("THIRD_PARTY_ERROR");
            callLog.setExceptionMessage(e.getMessage());
            callLog.setCreatedAt(LocalDateTime.now());
            externalCallLogService.saveLog(callLog);
            throw e;
        } catch (Exception e) {
            log.warn("第三方接口调用异常(attempt {}): {}", attempt, e.getMessage());
            callLog.setSuccess(0);
            callLog.setDurationMs(System.currentTimeMillis() - startTime);
            callLog.setExceptionType("THIRD_PARTY_ERROR");
            callLog.setExceptionMessage(e.getMessage());
            callLog.setCreatedAt(LocalDateTime.now());
            externalCallLogService.saveLog(callLog);
            throw new RestClientException("第三方接口调用异常: " + e.getMessage(), e);
        }
    }

    private void saveRateLimitLog(String traceId) {
        ExternalCallLog callLog = new ExternalCallLog();
        callLog.setTraceId(traceId);
        callLog.setService(SERVICE_NAME);
        callLog.setTargetUrl(properties.getBaseUrl() + TARGET_PATH);
        callLog.setHttpMethod("GET");
        callLog.setSuccess(0);
        callLog.setAttempt(1);
        callLog.setExceptionType("RATE_LIMIT");
        callLog.setCreatedAt(LocalDateTime.now());
        externalCallLogService.saveLog(callLog);
    }

    private ExternalCallLog buildLog(String traceId, int attempt) {
        ExternalCallLog callLog = new ExternalCallLog();
        callLog.setTraceId(traceId);
        callLog.setService(SERVICE_NAME);
        callLog.setTargetUrl(properties.getBaseUrl() + TARGET_PATH);
        callLog.setHttpMethod("GET");
        callLog.setQueryString(buildQueryString(buildParams(null)));
        callLog.setAttempt(attempt);
        return callLog;
    }

    private String buildUrl(DictQueryRequestDTO request) {
        return properties.getBaseUrl() + TARGET_PATH + "?" + buildQueryString(buildParams(request));
    }

    private Map<String, Object> buildParams(DictQueryRequestDTO request) {
        Map<String, Object> params = new HashMap<>();
        if (request != null) {
            params.put("pageNum", request.getPageNum());
            params.put("pageSize", request.getPageSize());
            params.put("dictType", request.getDictType());
        }
        return params;
    }

    private String buildQueryString(Map<String, Object> params) {
        StringBuilder sb = new StringBuilder();
        params.forEach((k, v) -> {
            if (sb.length() > 0) sb.append("&");
            sb.append(k).append("=").append(v);
        });
        return sb.toString();
    }
}
