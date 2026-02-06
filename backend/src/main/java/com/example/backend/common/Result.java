package com.example.backend.common;

import lombok.Data;

@Data
public class Result<T> {

    private int code;

    private String message;

    private T data;

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setMessage("ok");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> fail(int code, String message) {
        return fail(code, message, null);
    }

    public static <T> Result<T> fail(int code, String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setData(data);
        return result;
    }
}
