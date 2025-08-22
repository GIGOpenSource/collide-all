package com.gig.collide.controller;

import com.gig.collide.Apientry.api.common.response.PageResponse;
import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.domain.Inform;
import com.gig.collide.service.InformService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 消息通知管理REST控制器
 * 提供系统通知的查询、管理、统计等功能
 * 与模块接口文档完全一致
 *
 * @author GIG Team
 * @version 1.0.0
 * @since 2024-01-31
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/inform")
@RequiredArgsConstructor
@Validated
@Tag(name = "消息通知管理", description = "系统通知的查询、管理、统计等功能 - 我的模块")
public class InformController {

    private final InformService informService;

    /**
     * 获取我的通知列表
     * 获取用户收到的系统通知列表
     */
    @GetMapping("/my/{userId}")
    @Operation(summary = "获取我的通知列表", description = "获取用户收到的系统通知列表")
    public Result<PageResponse<Inform>> getMyNotifications(
            @Parameter(description = "用户ID") @PathVariable @NotNull @Min(1) Long userId,
            @Parameter(description = "通知类型") @RequestParam(required = false) String typeRelation,
            @Parameter(description = "是否已发送") @RequestParam(required = false) String isSent,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") @Min(1) Integer currentPage,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "20") @Min(1) Integer pageSize) {
        log.info("REST请求 - 获取我的通知列表: userId={}, typeRelation={}, isSent={}, page={}/{}", 
                userId, typeRelation, isSent, currentPage, pageSize);
        
        try {
            // 调用服务查询通知
            var result = informService.queryInforms(
                "collide-app", // 固定APP名称
                typeRelation,
                "user", // 固定用户类型
                "N", // 未删除
                isSent,
                null, // 开始时间
                null, // 结束时间
                currentPage,
                pageSize
            );
            
            // 转换为PageResponse格式
            PageResponse<Inform> pageResponse = new PageResponse<>();
            pageResponse.setRecords(result.getRecords());
            pageResponse.setTotal(result.getTotal());
            pageResponse.setCurrentPage((int) result.getCurrent());
            pageResponse.setPageSize((int) result.getSize());
            pageResponse.setTotalPages((int) Math.ceil((double) result.getTotal() / result.getSize()));
            
            return Result.success(pageResponse);
        } catch (Exception e) {
            log.error("获取我的通知列表失败: userId={}", userId, e);
            return Result.error("获取通知列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取未读通知数量
     * 获取用户未读通知数量
     */
    @GetMapping("/unread/count/{userId}")
    @Operation(summary = "获取未读通知数量", description = "获取用户未读通知数量")
    public Result<Long> getUnreadNotificationCount(
            @Parameter(description = "用户ID") @PathVariable @NotNull @Min(1) Long userId) {
        log.info("REST请求 - 获取未读通知数量: userId={}", userId);
        
        try {
            // 查询未发送的通知数量（未读）
            var result = informService.queryInforms(
                "collide-app",
                null,
                "user",
                "N",
                "N", // 未发送表示未读
                null,
                null,
                1,
                1
            );
            
            return Result.success(result.getTotal());
        } catch (Exception e) {
            log.error("获取未读通知数量失败: userId={}", userId, e);
            return Result.error("获取未读通知数量失败: " + e.getMessage());
        }
    }

    /**
     * 标记通知为已读
     * 标记指定通知为已读状态
     */
    @PutMapping("/read/{informId}")
    @Operation(summary = "标记通知为已读", description = "标记指定通知为已读状态")
    public Result<Boolean> markNotificationAsRead(
            @Parameter(description = "通知ID") @PathVariable @NotNull @Min(1) Long informId) {
        log.info("REST请求 - 标记通知为已读: informId={}", informId);
        
        try {
            // 获取通知
            Inform inform = informService.getInformById(informId);
            if (inform == null) {
                return Result.error("通知不存在");
            }
            
            // 标记为已发送（已读）
            inform.markAsSent();
            boolean success = informService.updateInform(inform);
            
            if (success) {
                log.info("通知标记为已读成功: informId={}", informId);
                return Result.success(true);
            } else {
                log.error("通知标记为已读失败: informId={}", informId);
                return Result.error("标记失败");
            }
        } catch (Exception e) {
            log.error("标记通知为已读失败: informId={}", informId, e);
            return Result.error("标记通知为已读失败: " + e.getMessage());
        }
    }

    /**
     * 删除通知
     * 删除指定通知（逻辑删除）
     */
    @DeleteMapping("/{informId}")
    @Operation(summary = "删除通知", description = "删除指定通知（逻辑删除）")
    public Result<Boolean> deleteNotification(
            @Parameter(description = "通知ID") @PathVariable @NotNull @Min(1) Long informId) {
        log.info("REST请求 - 删除通知: informId={}", informId);
        
        try {
            boolean success = informService.deleteInform(informId);
            
            if (success) {
                log.info("通知删除成功: informId={}", informId);
                return Result.success(true);
            } else {
                log.error("通知删除失败: informId={}", informId);
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除通知失败: informId={}", informId, e);
            return Result.error("删除通知失败: " + e.getMessage());
        }
    }

    /**
     * 获取通知统计信息
     * 获取用户通知统计信息
     */
    @GetMapping("/statistics/{userId}")
    @Operation(summary = "获取通知统计信息", description = "获取用户通知统计信息")
    public Result<Map<String, Object>> getNotificationStatistics(
            @Parameter(description = "用户ID") @PathVariable @NotNull @Min(1) Long userId) {
        log.info("REST请求 - 获取通知统计信息: userId={}", userId);
        
        try {
            // 获取总体统计
            var totalResult = informService.queryInforms(
                "collide-app", null, "user", "N", null, null, null, 1, 1
            );
            
            // 获取已读统计
            var readResult = informService.queryInforms(
                "collide-app", null, "user", "N", "Y", null, null, 1, 1
            );
            
            // 获取未读统计
            var unreadResult = informService.queryInforms(
                "collide-app", null, "user", "N", "N", null, null, 1, 1
            );
            
            // 获取点赞通知统计
            var likeResult = informService.queryInforms(
                "collide-app", "LIKE", "user", "N", null, null, null, 1, 1
            );
            
            // 获取评论通知统计
            var commentResult = informService.queryInforms(
                "collide-app", "COMMENT", "user", "N", null, null, null, 1, 1
            );
            
            // 获取回复通知统计
            var replyResult = informService.queryInforms(
                "collide-app", "REPLY", "user", "N", null, null, null, 1, 1
            );
            
            // 构建统计信息
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalCount", totalResult.getTotal());
            statistics.put("readCount", readResult.getTotal());
            statistics.put("unreadCount", unreadResult.getTotal());
            statistics.put("likeCount", likeResult.getTotal());
            statistics.put("commentCount", commentResult.getTotal());
            statistics.put("replyCount", replyResult.getTotal());
            
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取通知统计信息失败: userId={}", userId, e);
            return Result.error("获取通知统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 批量标记通知为已读
     * 批量标记用户的通知为已读状态
     */
    @PutMapping("/batch-read/{userId}")
    @Operation(summary = "批量标记通知为已读", description = "批量标记用户的通知为已读状态")
    public Result<Boolean> batchMarkNotificationsAsRead(
            @Parameter(description = "用户ID") @PathVariable @NotNull @Min(1) Long userId) {
        log.info("REST请求 - 批量标记通知为已读: userId={}", userId);
        
        try {
            // 获取用户所有未读通知
            var unreadResult = informService.queryInforms(
                "collide-app", null, "user", "N", "N", null, null, 1, 1000
            );
            
            if (unreadResult.getTotal() == 0) {
                return Result.success(true);
            }
            
            // 提取通知ID列表
            var informIds = unreadResult.getRecords().stream()
                .map(Inform::getId)
                .toList();
            
            // 批量标记为已读
            int successCount = informService.batchSendInforms(informIds);
            
            if (successCount > 0) {
                log.info("批量标记通知为已读成功: userId={}, count={}", userId, successCount);
                return Result.success(true);
            } else {
                log.error("批量标记通知为已读失败: userId={}", userId);
                return Result.error("批量标记失败");
            }
        } catch (Exception e) {
            log.error("批量标记通知为已读失败: userId={}", userId, e);
            return Result.error("批量标记通知为已读失败: " + e.getMessage());
        }
    }

    /**
     * 清空用户通知
     * 清空用户的所有通知（逻辑删除）
     */
    @DeleteMapping("/clear/{userId}")
    @Operation(summary = "清空用户通知", description = "清空用户的所有通知（逻辑删除）")
    public Result<Boolean> clearUserNotifications(
            @Parameter(description = "用户ID") @PathVariable @NotNull @Min(1) Long userId) {
        log.info("REST请求 - 清空用户通知: userId={}", userId);
        
        try {
            // 获取用户所有通知
            var allResult = informService.queryInforms(
                "collide-app", null, "user", "N", null, null, null, 1, 1000
            );
            
            if (allResult.getTotal() == 0) {
                return Result.success(true);
            }
            
            // 逐个删除通知
            int successCount = 0;
            for (Inform inform : allResult.getRecords()) {
                if (informService.deleteInform(inform.getId())) {
                    successCount++;
                }
            }
            
            if (successCount > 0) {
                log.info("清空用户通知成功: userId={}, count={}", userId, successCount);
                return Result.success(true);
            } else {
                log.error("清空用户通知失败: userId={}", userId);
                return Result.error("清空失败");
            }
        } catch (Exception e) {
            log.error("清空用户通知失败: userId={}", userId, e);
            return Result.error("清空用户通知失败: " + e.getMessage());
        }
    }
}
