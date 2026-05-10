package com.juejin.social.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 发送消息DTO
 *
 * @author juejin
 */
@Data
@Schema(description = "发送消息请求")
public class SendMessageDTO {

    @NotNull(message = "接收者ID不能为空")
    @Schema(description = "接收者用户ID", example = "1002")
    private Long receiverId;

    @NotBlank(message = "消息内容不能为空")
    @Size(max = 1000, message = "消息内容最长1000字")
    @Schema(description = "消息内容", example = "你好，想请教一个问题")
    private String content;

    @Min(1)
    @Max(2)
    @Schema(description = "内容类型：1-文本 2-图片", example = "1")
    private Integer contentType = 1;
}
