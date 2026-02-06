package com.example.backend.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class ProjectCreateDTO {

    @NotBlank(message = "项目名称不能为空")
    @Size(min = 1, max = 50, message = "项目名称长度必须在1-50之间")
    private String name;

    @Size(max = 50, message = "项目负责人长度不能超过50")
    private String owner;

    @NotNull(message = "项目状态不能为空")
    @Min(value = 0, message = "状态值必须在0-2之间")
    @Max(value = 2, message = "状态值必须在0-2之间")
    private Integer status;
}
