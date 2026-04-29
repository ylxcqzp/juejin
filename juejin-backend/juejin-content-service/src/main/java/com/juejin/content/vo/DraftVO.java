package com.juejin.content.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 草稿VO
 *
 * @author juejin
 */
@Data
@Schema(description = "文章草稿信息")
public class DraftVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "草稿ID", example = "10")
    private Long id;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "文章标题", example = "我的草稿文章")
    private String title;

    @Schema(description = "文章内容（Markdown）")
    private String content;

    @Schema(description = "封面图URL")
    private String coverImage;

    @Schema(description = "标签列表（JSON格式）")
    private String tags;

    @Schema(description = "分类ID", example = "2")
    private Long categoryId;

    @Schema(description = "关联文章ID（编辑已有文章时）")
    private Long articleId;

    @Schema(description = "最后自动保存时间", example = "2024-03-19T23:30:00")
    private LocalDateTime autoSaveTime;

    @Schema(description = "创建时间", example = "2024-03-19T22:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2024-03-19T23:30:00")
    private LocalDateTime updateTime;

}
