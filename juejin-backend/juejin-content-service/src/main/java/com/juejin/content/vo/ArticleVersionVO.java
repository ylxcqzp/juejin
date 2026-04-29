package com.juejin.content.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文章版本VO
 *
 * @author juejin
 */
@Data
@Schema(description = "文章历史版本信息")
public class ArticleVersionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "版本ID", example = "5")
    private Long id;

    @Schema(description = "文章ID", example = "1")
    private Long articleId;

    @Schema(description = "版本号", example = "3")
    private Integer version;

    @Schema(description = "该版本的文章标题", example = "Spring Boot 最佳实践（修订版）")
    private String title;

    @Schema(description = "该版本的文章内容（Markdown）")
    private String content;

    @Schema(description = "变更摘要", example = "修正了第三节的代码示例")
    private String changeSummary;

    @Schema(description = "版本创建时间", example = "2024-03-21T15:00:00")
    private LocalDateTime createTime;

}
