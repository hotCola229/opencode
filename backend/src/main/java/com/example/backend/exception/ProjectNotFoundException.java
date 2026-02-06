package com.example.backend.exception;

public class ProjectNotFoundException extends RuntimeException {

    public ProjectNotFoundException() {
        super("项目不存在");
    }

    public ProjectNotFoundException(String message) {
        super(message);
    }
}
