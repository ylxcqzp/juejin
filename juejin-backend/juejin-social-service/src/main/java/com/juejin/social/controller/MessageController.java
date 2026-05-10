package com.juejin.social.controller;

import com.juejin.common.dto.PageParam;
import com.juejin.common.result.Result;
import com.juejin.common.vo.PageResult;
import com.juejin.social.dto.SendMessageDTO;
import com.juejin.social.service.MessageService;
import com.juejin.social.vo.MessageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 消息 Controller
 *
 * @author juejin
 */
@Tag(name = "私信消息", description = "消息发送、查询、已读、撤回")
@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
@Validated
public class MessageController {

    private final MessageService messageService;

    @Operation(summary = "获取会话消息列表")
    @GetMapping("/{conversationId}/messages")
    public Result<PageResult<MessageVO>> getMessages(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long conversationId,
            @Valid PageParam pageParam) {
        PageResult<MessageVO> result = messageService.getMessages(userId, conversationId, pageParam.getPage(), pageParam.getSize());
        return Result.success(result);
    }

    @Operation(summary = "发送消息")
    @PostMapping("/{conversationId}/messages")
    public Result<MessageVO> send(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long conversationId,
            @RequestBody @Valid SendMessageDTO dto) {
        MessageVO vo = messageService.sendMessage(userId, conversationId, dto);
        return Result.success(vo);
    }

    @Operation(summary = "标记会话已读")
    @PutMapping("/{conversationId}/read")
    public Result<Void> markAsRead(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long conversationId) {
        messageService.markAsRead(userId, conversationId);
        return Result.success();
    }

    @Operation(summary = "撤回消息")
    @PutMapping("/{conversationId}/messages/{messageId}/recall")
    public Result<Void> recall(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long conversationId,
            @PathVariable Long messageId) {
        messageService.recallMessage(userId, messageId);
        return Result.success();
    }
}
