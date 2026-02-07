package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("external_call_log")
public class ExternalCallLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String traceId;

    private String service;

    private String targetUrl;

    private String httpMethod;

    private String queryString;

    private Integer httpStatus;

    private Integer success;

    private Integer attempt;

    private Long durationMs;

    private String exceptionType;

    private String exceptionMessage;

    private LocalDateTime createdAt;
}
