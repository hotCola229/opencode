package com.example.backend.controller;

import com.example.backend.common.Result;
import com.example.backend.dto.DictQueryRequestDTO;
import com.example.backend.service.DictQueryService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/dict")
public class DictQueryController {

    private final DictQueryService dictQueryService;

    public DictQueryController(DictQueryService dictQueryService) {
        this.dictQueryService = dictQueryService;
    }

    @GetMapping("/query")
    public Result<String> query(@Valid DictQueryRequestDTO request,
                                  @RequestHeader(value = "X-Trace-Id", required = false) String traceId) {
        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }

        MDC.put("traceId", traceId);
        try {
            String response = dictQueryService.query(request, traceId);
            return Result.success(response);
        } finally {
            MDC.remove("traceId");
        }
    }
}
