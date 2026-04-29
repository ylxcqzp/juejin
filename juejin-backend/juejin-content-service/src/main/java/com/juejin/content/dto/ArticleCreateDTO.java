package com.juejin.content.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 文章创建DTO
 *
 * @author juejin
 */
@Data
@Schema(description = "创建文章请求")
public class ArticleCreateDTO {

    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题长度不能超过100字符")
    @Schema(description = "文章标题（最多100字）", required = true, example = "Spring Boot 微服务最佳实践")
    private String title;

    @NotBlank(message = "内容不能为空")
    @Schema(description = "文章内容（Markdown格式）", required = true)
    private String content;

    @Schema(description = "文章摘要（不传则自动从内容提取前200字）", example = "本文介绍了Spring Boot微服务架构的核心实践...")
    private String summary;

    @Schema(description = "封面图URL", example = "https://cdn.example.com/article/cover1.png")
    private String coverImage;

    @NotNull(message = "分类不能为空")
    @Schema(description = "分类ID", required = true, example = "2")
    private Long categoryId;

    @Schema(description = "标签ID列表（最多5个）", example = "[1, 3, 5]")
    private List<Long> tagIds;

    @Schema(description = "是否原创：0-转载 1-原创（默认1）", example = "1")
    private Integer isOriginal;

    @Schema(description = "原文链接（转载时必填）")
    private String sourceUrl;

}
