package com.juejin.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * 分页请求参数
 *
 * @author juejin
 */
@Data
@Schema(description = "分页请求参数")
public class PageParam {

    @Min(value = 1, message = "Page number must be at least 1")
    @Schema(description = "页码（从1开始）", example = "1")
    private Integer page = 1;

    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size must not exceed 100")
    @Schema(description = "每页数量（1-100）", example = "20")
    private Integer size = 20;

    @Schema(description = "排序字段", example = "create_time")
    private String sort;

    @Schema(description = "游标（用于游标分页）")
    private Long cursor;

    public Integer getOffset() {
        return (page - 1) * size;
    }

}
