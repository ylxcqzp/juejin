package com.juejin.social.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 收藏夹VO
 *
 * @author juejin
 */
@Data
@Schema(description = "收藏夹信息")
public class FavoriteFolderVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "收藏夹ID", example = "1")
    private Long id;

    @Schema(description = "所属用户ID", example = "1")
    private Long userId;

    @Schema(description = "收藏夹名称", example = "前端精选")
    private String name;

    @Schema(description = "收藏夹描述", example = "收藏的前端优质文章")
    private String description;

    @Schema(description = "封面图URL")
    private String coverImage;

    @Schema(description = "收藏文章数", example = "15")
    private Integer articleCount;

    @Schema(description = "是否默认收藏夹：0-否 1-是", example = "0")
    private Integer isDefault;

    @Schema(description = "是否公开：0-私密 1-公开", example = "1")
    private Integer isPublic;

    @Schema(description = "排序值", example = "0")
    private Integer sortOrder;

    @Schema(description = "创建时间", example = "2024-02-10T14:00:00")
    private LocalDateTime createTime;

}
