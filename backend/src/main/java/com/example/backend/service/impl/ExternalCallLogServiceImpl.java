package com.example.backend.service.impl;

import com.example.backend.entity.ExternalCallLog;
import com.example.backend.mapper.ExternalCallLogMapper;
import com.example.backend.service.ExternalCallLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ExternalCallLogServiceImpl implements ExternalCallLogService {

    private final ExternalCallLogMapper externalCallLogMapper;

    public ExternalCallLogServiceImpl(ExternalCallLogMapper externalCallLogMapper) {
        this.externalCallLogMapper = externalCallLogMapper;
    }

    @Override
    public void saveLog(ExternalCallLog callLog) {
        try {
            externalCallLogMapper.insert(callLog);
        } catch (Exception e) {
            log.error("保存外部调用日志失败: {}", e.getMessage(), e);
        }
    }
}
