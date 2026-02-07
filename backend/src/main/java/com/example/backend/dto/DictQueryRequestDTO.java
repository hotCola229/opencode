package com.example.backend.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class DictQueryRequestDTO {

    @Min(value = 1, message = "pageNum必须大于等于1")
    private Integer pageNum;

    @Min(value = 1, message = "pageSize必须大于等于1")
    @Max(value = 100, message = "pageSize不能超过100")
    private Integer pageSize;

    @NotBlank(message = "dictType不能为空")
    @Size(max = 50, message = "dictType长度不能超过50")
    private String dictType;
}
