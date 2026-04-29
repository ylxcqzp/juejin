package com.juejin.social.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 收藏操作DTO
 *
 * @author juejin
 */
@Data
@Schema(description = "收藏操作请求")
public class FavoriteRecordDTO {

    @NotNull(message = "文章ID不能为空")
    @Schema(description = "文章ID", required = true)
    private Long articleId;

    @Schema(description = "收藏夹ID，不传则收藏到默认收藏夹")
    private Long folderId;

}
