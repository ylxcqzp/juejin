package com.juejin.content.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 文章更新DTO
 *
 * @author juejin
 */
@Data
public class ArticleUpdateDTO {

    /** 文章ID */
    @NotNull(message = "文章ID不能为空")
    private Long id;

    /** 标题 */
    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题长度不能超过100字符")
    private String title;

    /** 内容 */
    @NotBlank(message = "内容不能为空")
    private String content;

    /** 摘要 */
    private String summary;

    /** 封面图 */
    private String coverImage;

    /** 分类ID */
    @NotNull(message = "分类不能为空")
    private Long categoryId;

    /** 标签ID列表 */
    private List<Long> tagIds;

    /** 是否原创：0-转载 1-原创 */
    private Integer isOriginal;

    /** 原文链接 */
    private String sourceUrl;

}
