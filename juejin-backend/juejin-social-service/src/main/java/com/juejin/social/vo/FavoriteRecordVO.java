package com.juejin.social.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 收藏记录VO
 *
 * @author juejin
 */
@Data
@Schema(description = "收藏记录")
public class FavoriteRecordVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "收藏记录ID", example = "50")
    private Long id;

    @Schema(description = "收藏用户ID", example = "1")
    private Long userId;

    @Schema(description = "被收藏的文章ID", example = "10")
    private Long articleId;

    @Schema(description = "收藏夹ID", example = "1")
    private Long folderId;

    @Schema(description = "收藏夹名称", example = "默认收藏夹")
    private String folderName;

    @Schema(description = "收藏时间", example = "2024-03-20T11:00:00")
    private LocalDateTime createTime;

}
