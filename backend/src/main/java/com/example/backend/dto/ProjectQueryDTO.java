package com.example.backend.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Data
public class ProjectQueryDTO {

    @Range(min = 1, max = 100, message = "每页条数必须在1-100之间")
    private Integer size = 10;

    @Range(min = 1, message = "页码必须大于0")
    private Integer page = 1;

    private String keyword;
}
