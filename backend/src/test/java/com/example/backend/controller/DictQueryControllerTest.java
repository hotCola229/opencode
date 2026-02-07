package com.example.backend.controller;

import com.example.backend.config.TestRateLimitConfig;
import com.example.backend.entity.ExternalCallLog;
import com.example.backend.mapper.ExternalCallLogMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.google.common.util.concurrent.RateLimiter;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "third-party.rate-limit.capacity=1000",
    "third-party.rate-limit.refill-tokens=1000",
    "third-party.http.retry.initial-delay=50",
    "third-party.http.retry.max-delay=100"
})
@Import(TestRateLimitConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class DictQueryControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ExternalCallLogMapper externalCallLogMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired(required = false)
    private RateLimiter testRateLimiter;

    private WireMockServer wireMockServer;

    @BeforeEach
    public void setUp() {
        wireMockServer = new WireMockServer(options().port(8089));
        wireMockServer.start();
        WireMock.configureFor(wireMockServer.port());

        externalCallLogMapper.delete(null);
    }

    @AfterEach
    public void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Test
    @DisplayName("成功场景：第三方返回200，日志表插入1条")
    public void testSuccessScenario() {
        String expectedResponse = "{\"total\":5,\"data\":[{\"code\":\"4\",\"value\":\"jar\"}]}";

        stubFor(get(urlPathEqualTo("/api/v1/dataapi/execute/dict/query"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(expectedResponse)));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/dict/query?pageNum=1&pageSize=10&dictType=job_type",
                HttpMethod.GET,
                entity,
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        List<ExternalCallLog> logs = externalCallLogMapper.selectList(null);
        assertEquals(1, logs.size());

        ExternalCallLog log = logs.get(0);
        assertEquals(1, log.getAttempt());
        assertEquals(1, log.getSuccess());
        assertEquals(200, log.getHttpStatus());
        assertEquals("DICT_QUERY", log.getService());
    }

    @Test
    @DisplayName("失败场景：第三方返回500，触发重试2次，共3条日志")
    public void testRetryScenario() {
        stubFor(get(urlPathEqualTo("/api/v1/dataapi/execute/dict/query"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"success\":false,\"errCode\":\"500\",\"errMessage\":\"Internal Server Error\"}")));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/dict/query?pageNum=1&pageSize=10&dictType=job_type",
                HttpMethod.GET,
                entity,
                String.class
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        List<ExternalCallLog> logs = externalCallLogMapper.selectList(null);
        assertEquals(3, logs.size(), "期望3条日志，实际" + logs.size() + "条");

        for (int i = 0; i < 3; i++) {
            ExternalCallLog log = logs.get(i);
            assertEquals(i + 1, log.getAttempt(), "第" + (i+1) + "次尝试的attempt值不正确");
            assertEquals(0, log.getSuccess(), "第" + (i+1) + "次尝试的success值不正确");
            assertEquals(500, log.getHttpStatus(), "第" + (i+1) + "次尝试的httpStatus值不正确");
            assertEquals("DICT_QUERY", log.getService(), "第" + (i+1) + "次尝试的service值不正确");
        }
    }
}
