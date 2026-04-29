package com.juejin.social.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 收藏夹创建/更新DTO
 *
 * @author juejin
 */
@Data
@Schema(description = "收藏夹创建/更新请求")
public class FavoriteFolderDTO {

    @NotBlank(message = "收藏夹名称不能为空")
    @Size(max = 50, message = "收藏夹名称最多50字")
    @Schema(description = "收藏夹名称", required = true, example = "我的收藏")
    private String name;

    @Size(max = 200, message = "收藏夹描述最多200字")
    @Schema(description = "收藏夹描述")
    private String description;

    @Schema(description = "封面图URL")
    private String coverImage;

    @Schema(description = "是否公开：0-私密 1-公开", example = "1")
    private Integer isPublic;

    @Schema(description = "排序值", example = "0")
    private Integer sortOrder;

}
