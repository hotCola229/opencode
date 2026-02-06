package com.example.backend.dto;

import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class ProjectUpdateDTO {

    @Size(min = 1, max = 50, message = "项目名称长度必须在1-50之间")
    private String name;

    @Size(max = 50, message = "项目负责人长度不能超过50")
    private String owner;

    private Integer status;
}
