package com.gig.collide.controller;


import com.gig.collide.Apientry.api.common.response.PageResponse;
import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.Apientry.api.like.LikeFacadeService;
import com.gig.collide.Apientry.api.like.request.*;
import com.gig.collide.Apientry.api.like.response.LikeResponse;
import com.gig.collide.domain.Like;
import com.gig.collide.service.InformService;
import com.gig.collide.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * 点赞REST控制器 - MySQL 8.0 优化版
 * 基于新的LikeService接口，提供完整的点赞HTTP API
 * 
 * 接口特性：
 * - 与LikeService接口完全对应
 * - 支持用户、目标对象、作者三个维度的查询
 * - 支持时间范围查询和批量操作
 * - 完整的缓存策略和错误处理
 * - 统一的REST API设计规范
 * 
 * @author GIG Team
 * @version 2.0.0 (MySQL 8.0 优化版)
 * @since 2024-01-16
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/like")
@RequiredArgsConstructor
@Tag(name = "点赞管理", description = "点赞相关的API接口 - MySQL 8.0 优化版")
public class LikeController {

    private final LikeService likeService;
    private final InformService informService;

    /**
     * 点赞列表查询
     * 支持多种条件查询和分页
     */
    @GetMapping("/list")
    @Operation(summary = "点赞列表查询", description = "支持按用户、目标、类型等条件查询点赞列表")
    public Result<PageResponse<LikeResponse>> listLikes(
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "目标ID") @RequestParam(required = false) Long targetId,
            @Parameter(description = "点赞类型") @RequestParam(required = false) String likeType,
            @Parameter(description = "目标类型") @RequestParam(required = false) String targetType,
            @Parameter(description = "点赞状态") @RequestParam(required = false) String status,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createTime") String orderBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "DESC") String orderDirection,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("REST请求 - 点赞列表查询: userId={}, targetId={}, type={}, page={}/{}", 
                userId, targetId, likeType, currentPage, pageSize);
        return likeService.listLikesForController(userId, targetId, likeType, targetType, status,
                orderBy, orderDirection, currentPage, pageSize);
    }

    /**
     * 获取用户获得点赞的数量统计
     * 统计用户作为作者的作品被点赞的总数量
     */
    @GetMapping("/received/count/{userId}")
    @Operation(summary = "获取用户获得点赞数量", description = "统计用户作为作者的作品被点赞的总数量")
    public Result<Long> getUserReceivedLikeCount(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "点赞类型（可选）") @RequestParam(required = false) String likeType) {
        try {
            log.info("REST请求 - 获取用户获得点赞数量: userId={}, likeType={}", userId, likeType);
            
            Long count = likeService.countAuthorLikes(userId, likeType);
            return Result.success(count);
        } catch (Exception e) {
            log.error("获取用户获得点赞数量失败: userId={}", userId, e);
            return Result.error("获取用户获得点赞数量失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户点赞的数量统计
     * 统计用户点赞他人的总数量
     */
    @GetMapping("/given/count/{userId}")
    @Operation(summary = "获取用户点赞数量", description = "统计用户点赞他人的总数量")
    public Result<Long> getUserGivenLikeCount(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "点赞类型（可选）") @RequestParam(required = false) String likeType) {
        try {
            log.info("REST请求 - 获取用户点赞数量: userId={}, likeType={}", userId, likeType);
            
            Long count = likeService.countUserLikes(userId, likeType);
            return Result.success(count);
        } catch (Exception e) {
            log.error("获取用户点赞数量失败: userId={}", userId, e);
            return Result.error("获取用户点赞数量失败: " + e.getMessage());
        }
    }

    /**
     * 获取目标对象被点赞的数量统计
     * 统计特定目标对象被点赞的总数量
     */
    @GetMapping("/target/count/{targetId}")
    @Operation(summary = "获取目标对象被点赞数量", description = "统计特定目标对象被点赞的总数量")
    public Result<Long> getTargetLikeCount(
            @Parameter(description = "目标对象ID") @PathVariable Long targetId,
            @Parameter(description = "点赞类型") @RequestParam String likeType) {
        try {
            log.info("REST请求 - 获取目标对象被点赞数量: targetId={}, likeType={}", targetId, likeType);
            
            Long count = likeService.countTargetLikes(targetId, likeType);
            return Result.success(count);
        } catch (Exception e) {
            log.error("获取目标对象被点赞数量失败: targetId={}", targetId, e);
            return Result.error("获取目标对象被点赞数量失败: " + e.getMessage());
        }
    }

    /**
     * 切换点赞状态
     * 如果已点赞则取消，如果未点赞则添加
     */
    @PostMapping("/toggle")
    @Operation(summary = "切换点赞状态", description = "切换用户对目标对象的点赞状态")
    public Result<LikeResponse> toggleLike(@RequestBody LikeToggleRequest request) {
        try {
            log.info("REST请求 - 切换点赞状态: userId={}, likeType={}, targetId={}", 
                    request.getUserId(), request.getLikeType(), request.getTargetId());
            
            // 转换为Like对象
            Like like = new Like();
            like.setUserId(request.getUserId());
            like.setLikeType(request.getLikeType());
            like.setTargetId(request.getTargetId());
            like.setTargetTitle(request.getTargetTitle());
            like.setTargetAuthorId(request.getTargetAuthorId());
            like.setUserNickname(request.getUserNickname());
            like.setUserAvatar(request.getUserAvatar());
            
            Like result = likeService.toggleLike(like);
            
            if (result != null) {
                // 点赞成功，发送通知
                try {
                    if (request.getTargetAuthorId() != null && !request.getUserId().equals(request.getTargetAuthorId())) {
                        informService.sendLikeNotification(
                            request.getUserId(),
                            request.getUserNickname(),
                            request.getTargetAuthorId(),
                            request.getLikeType(),
                            request.getTargetId(),
                            request.getTargetTitle()
                        );
                        log.info("点赞通知发送成功: likerId={}, targetAuthorId={}", 
                                request.getUserId(), request.getTargetAuthorId());
                    }
                } catch (Exception e) {
                    log.warn("发送点赞通知失败，但不影响点赞操作: {}", e.getMessage());
                }
                
                // 转换为响应对象
                LikeResponse response = new LikeResponse();
                response.setId(result.getId());
                response.setUserId(result.getUserId());
                response.setLikeType(result.getLikeType());
                response.setTargetId(result.getTargetId());
                response.setTargetTitle(result.getTargetTitle());
                response.setTargetAuthorId(result.getTargetAuthorId());
                response.setUserNickname(result.getUserNickname());
                response.setUserAvatar(result.getUserAvatar());
                response.setStatus(result.getStatus());
                response.setCreateTime(result.getCreateTime());
                response.setUpdateTime(result.getUpdateTime());
                return Result.success(response);
            } else {
                // 取消点赞成功
                return Result.success(null);
            }
        } catch (Exception e) {
            log.error("切换点赞状态失败", e);
            return Result.error("切换点赞状态失败: " + e.getMessage());
        }
    }

    /**
     * 检查点赞状态
     * 查询用户是否已对目标对象点赞
     */
    @GetMapping("/check/{userId}/{likeType}/{targetId}")
    @Operation(summary = "检查点赞状态", description = "查询用户是否已对目标对象点赞")
    public Result<Boolean> checkLikeStatus(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "点赞类型") @PathVariable String likeType,
            @Parameter(description = "目标对象ID") @PathVariable Long targetId) {
        try {
            log.info("REST请求 - 检查点赞状态: userId={}, likeType={}, targetId={}", userId, likeType, targetId);
            
            boolean isLiked = likeService.checkLikeStatus(userId, likeType, targetId);
            return Result.success(isLiked);
        } catch (Exception e) {
            log.error("检查点赞状态失败: userId={}, targetId={}", userId, targetId, e);
            return Result.error("检查点赞状态失败: " + e.getMessage());
        }
    }

    /**
     * 批量检查点赞状态
     * 批量查询用户对多个目标对象的点赞状态
     */
    @PostMapping("/batch-check")
    @Operation(summary = "批量检查点赞状态", description = "批量查询用户对多个目标对象的点赞状态")
    public Result<Map<Long, Boolean>> batchCheckLikeStatus(@RequestBody LikeBatchCheckRequest request) {
        try {
            log.info("REST请求 - 批量检查点赞状态: userId={}, likeType={}, targetCount={}", 
                    request.getUserId(), request.getLikeType(), 
                    request.getTargetIds() != null ? request.getTargetIds().size() : 0);
            
            Map<Long, Boolean> statusMap = likeService.batchCheckLikeStatus(
                    request.getUserId(), request.getLikeType(), request.getTargetIds());
            return Result.success(statusMap);
        } catch (Exception e) {
            log.error("批量检查点赞状态失败", e);
            return Result.error("批量检查点赞状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户对指定目标的点赞状态列表
     * 用于前端列表页面的点赞状态回显
     */
    @GetMapping("/user-status/{userId}")
    @Operation(summary = "获取用户点赞状态列表", description = "获取用户对指定类型目标的点赞状态列表")
    public Result<Map<Long, Boolean>> getUserLikeStatus(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "点赞类型") @RequestParam String likeType,
            @Parameter(description = "目标ID列表，逗号分隔") @RequestParam String targetIds) {
        try {
            log.info("REST请求 - 获取用户点赞状态列表: userId={}, likeType={}, targetIds={}", 
                    userId, likeType, targetIds);
            
            if (targetIds == null || targetIds.trim().isEmpty()) {
                return Result.success(new HashMap<>());
            }
            
            // 解析目标ID列表
            List<Long> targetIdList = Arrays.stream(targetIds.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
            
            Map<Long, Boolean> statusMap = likeService.batchCheckLikeStatus(userId, likeType, targetIdList);
            return Result.success(statusMap);
        } catch (Exception e) {
            log.error("获取用户点赞状态列表失败: userId={}", userId, e);
            return Result.error("获取用户点赞状态列表失败: " + e.getMessage());
        }
    }
}