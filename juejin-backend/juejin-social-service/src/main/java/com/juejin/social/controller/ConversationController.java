package com.juejin.social.controller;

import com.juejin.common.dto.PageParam;
import com.juejin.common.result.Result;
import com.juejin.common.vo.PageResult;
import com.juejin.social.service.ConversationService;
import com.juejin.social.vo.ConversationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 会话 Controller
 *
 * @author juejin
 */
@Tag(name = "私信会话", description = "会话列表和创建")
@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
@Validated
public class ConversationController {

    private final ConversationService conversationService;

    @Operation(summary = "获取会话列表")
    @GetMapping
    public Result<PageResult<ConversationVO>> list(
            @RequestHeader("X-User-Id") Long userId,
            @Valid PageParam pageParam) {
        PageResult<ConversationVO> result = conversationService.listConversations(userId, pageParam.getPage(), pageParam.getSize());
        return Result.success(result);
    }

    @Operation(summary = "获取或创建会话")
    @PostMapping
    public Result<ConversationVO> getOrCreate(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam Long otherUserId) {
        ConversationVO vo = conversationService.getOrCreateConversation(userId, otherUserId);
        return Result.success(vo);
    }
}
