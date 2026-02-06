package com.example.backend.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    PARAM_VALIDATION_FAIL(40001, "参数校验失败"),
    PARAM_FORMAT_ERROR(40002, "请求参数格式/类型错误"),
    PROJECT_NOT_FOUND(40401, "项目不存在"),
    INTERNAL_ERROR(50000, "服务内部错误");

    private final int code;
    private final String message;
}
