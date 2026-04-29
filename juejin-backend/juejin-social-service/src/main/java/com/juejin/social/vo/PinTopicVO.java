package com.juejin.social.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 沸点话题VO
 *
 * @author juejin
 */
@Data
@Schema(description = "沸点话题信息")
public class PinTopicVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "话题ID", example = "1")
    private Long id;

    @Schema(description = "话题名称", example = "前端开发")
    private String name;

    @Schema(description = "话题描述", example = "前端技术交流与分享")
    private String description;

    @Schema(description = "封面图URL")
    private String coverImage;

    @Schema(description = "该话题下的沸点数", example = "128")
    private Integer pinCount;

    @Schema(description = "关注该话题的人数", example = "256")
    private Integer followCount;

    @Schema(description = "是否热门：0-否 1-是", example = "1")
    private Integer isHot;

}
