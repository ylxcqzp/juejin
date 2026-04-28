package com.juejin.common.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
public class PageParam {

    @Min(value = 1, message = "Page number must be at least 1")
    private Integer page = 1;

    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size must not exceed 100")
    private Integer size = 20;

    private String sort;

    private Long cursor;

    public Integer getOffset() {
        return (page - 1) * size;
    }

}
