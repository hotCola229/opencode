package com.example.backend.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProjectVO {

    private Long id;

    private String name;

    private String owner;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
