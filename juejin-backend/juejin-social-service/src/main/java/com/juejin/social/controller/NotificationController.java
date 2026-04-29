package com.juejin.social.controller;

import com.juejin.common.result.Result;
import com.juejin.common.vo.PageResult;
import com.juejin.social.service.NotificationService;
import com.juejin.social.vo.NotificationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 通知控制器
 *
 * @author juejin
 */
@Tag(name = "消息通知", description = "通知列表、未读数、标记已读相关接口")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "通知列表", description = "分页查询当前用户的通知列表")
    @GetMapping
    public Result<PageResult<NotificationVO>> getNotificationList(
            @Parameter(description = "用户ID", hidden = true) @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String type) {
        return Result.success(notificationService.getNotificationList(userId, page, size, type));
    }

    @Operation(summary = "未读通知数", description = "获取当前用户的未读通知数量")
    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount(
            @Parameter(description = "用户ID", hidden = true) @RequestHeader("X-User-Id") Long userId) {
        return Result.success(notificationService.getUnreadCount(userId));
    }

    @Operation(summary = "全部已读", description = "将所有未读通知标记为已读")
    @PutMapping("/read-all")
    public Result<Boolean> markAllAsRead(
            @Parameter(description = "用户ID", hidden = true) @RequestHeader("X-User-Id") Long userId) {
        return Result.success(notificationService.markAllAsRead(userId));
    }

}
