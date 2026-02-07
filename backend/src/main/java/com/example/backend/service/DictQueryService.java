package com.example.backend.service;

import com.example.backend.dto.DictQueryRequestDTO;

public interface DictQueryService {

    String query(DictQueryRequestDTO request, String traceId);
}
