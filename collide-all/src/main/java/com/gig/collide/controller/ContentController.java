package com.gig.collide.controller;


import com.gig.collide.Apientry.api.common.response.PageResponse;
import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.Apientry.api.content.ContentFacadeService;
import com.gig.collide.Apientry.api.content.request.ContentCreateRequest;
import com.gig.collide.Apientry.api.content.request.ContentUpdateRequest;
import com.gig.collide.Apientry.api.content.response.ContentResponse;
import com.gig.collide.domain.Content;
import com.gig.collide.service.ContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 内容管理控制器 - 极简版
 * 基于12个核心方法设计的精简API
 * 
 * @author GIG Team
 * @version 2.0.0 (极简版)
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/content/core")
@RequiredArgsConstructor
@Validated
@Tag(name = "内容管理", description = "内容的创建、更新、查询、发布等管理接口（极简版）")
public class ContentController {

    private final ContentService contentService;

    /**
     * 创建内容（支持付费配置）
     */
    @PostMapping("/create")
    @Operation(summary = "创建内容", description = "创建内容并同步创建付费配置")
    public Result<Void> createContent(@Valid @RequestBody ContentCreateRequest request) {
        log.info("REST请求 - 创建内容: title={}, contentType={}, paymentType={}", 
                request.getTitle(), request.getContentType(), request.getPaymentType());

        try {
            // 构建Content对象
            Content content = new Content();
            content.setTitle(request.getTitle());
            content.setDescription(request.getDescription());
            content.setContentType(request.getContentType());
            content.setContentData(request.getContentData());
            content.setCoverUrl(request.getCoverUrl());
            content.setTags(request.getTags());
            content.setAuthorId(request.getAuthorId());
            content.setAuthorNickname(request.getAuthorNickname());
            content.setAuthorAvatar(request.getAuthorAvatar());
            content.setCategoryId(request.getCategoryId());
            content.setCategoryName(request.getCategoryName());
            content.setStatus(request.getStatus());
            content.setReviewStatus(request.getReviewStatus());

            // 创建内容并同步创建付费配置
            contentService.createContentWithPayment(
                content,
                request.getPaymentType(),
                request.getCoinPrice(),
                request.getOriginalPrice(),
                request.getVipFree(),
                request.getVipOnly(),
                request.getTrialEnabled(),
                request.getTrialWordCount(),
                request.getIsPermanent(),
                request.getValidDays()
            );

            return Result.success();
        } catch (Exception e) {
            log.error("创建内容失败", e);
            return Result.error("创建内容失败: " + e.getMessage());
        }
    }

    /**
     * 内容列表查询
     * 支持多种条件查询和分页
     */
    @GetMapping("/list")
    @Operation(summary = "内容列表查询", description = "支持按类型、状态、作者等条件查询内容列表")
    public Result<PageResponse<ContentResponse>> listContents(
            @Parameter(description = "内容类型") @RequestParam(required = false) String contentType,
            @Parameter(description = "内容状态") @RequestParam(required = false) String status,
            @Parameter(description = "作者ID") @RequestParam(required = false) Long authorId,
            @Parameter(description = "多个作者ID，逗号分隔") @RequestParam(required = false) String authorIds,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "关键词搜索") @RequestParam(required = false) String keyword,
            @Parameter(description = "是否发布") @RequestParam(required = false) Boolean isPublished,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createTime") String orderBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "DESC") String orderDirection,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "当前用户ID") @RequestParam(required = false) Long userId) {
        log.info("REST请求 - 内容列表查询: type={}, status={}, authorId={}, authorIds={}, page={}/{}", 
                contentType, status, authorId, authorIds, currentPage, pageSize);
        return contentService.listContentsForController(contentType, status, authorId, authorIds, categoryId, keyword, isPublished,
                orderBy, orderDirection, currentPage, pageSize, userId);
    }

    @GetMapping("/listForFX")
    @Operation(summary = "内容列表查询", description = "支持按类型、状态、作者等条件查询内容列表，支持selectType字段进行特殊排序")
    public Result<PageResponse<ContentResponse>> listForFXContents(
            @Parameter(description = "内容类型") @RequestParam(required = false) String contentType,
            @Parameter(description = "内容状态") @RequestParam(required = false) String status,
            @Parameter(description = "作者ID") @RequestParam(required = false) Long authorId,
            @Parameter(description = "多个作者ID，逗号分隔") @RequestParam(required = false) String authorIds,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "关键词搜索") @RequestParam(required = false) String keyword,
            @Parameter(description = "是否发布") @RequestParam(required = false) Boolean isPublished,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createTime") String orderBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "DESC") String orderDirection,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "当前用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "选择类型：F-按时间排序，T-按点赞数排序") @RequestParam(required = false) String selectType) {
        log.info("REST请求 - 内容列表查询: type={}, status={}, authorId={}, authorIds={}, selectType={}, page={}/{}",
                contentType, status, authorId, authorIds, selectType, currentPage, pageSize);
        
        // 根据selectType调整排序参数
        String finalOrderBy = orderBy;
        String finalOrderDirection = orderDirection;
        
        if ("F".equals(selectType)) {
            // F: 按时间排序，最新发布的排在前面
            finalOrderBy = "createTime";
            finalOrderDirection = "DESC";
        } else if ("T".equals(selectType)) {
            // T: 按点赞数量排序
            finalOrderBy = "likeCount";
            finalOrderDirection = "DESC";
        }
        
        return contentService.listContentsForController(contentType, status, authorId, authorIds, categoryId, keyword, isPublished,
                finalOrderBy, finalOrderDirection, currentPage, pageSize, userId);
    }

    /**
     * 根据内容ID查询内容详情
     */
    @GetMapping("/{contentId}")
    @Operation(summary = "查询内容详情", description = "根据内容ID查询单个内容的详细信息")
    public Result<ContentResponse> getContentById(
            @Parameter(description = "内容ID", required = true) @PathVariable Long contentId,
            @Parameter(description = "用户ID（用于互动状态查询）") @RequestParam(required = false) Long userId,
            @Parameter(description = "是否包含下线内容") @RequestParam(defaultValue = "false") Boolean includeOffline) {
        try {
            log.info("REST请求 - 查询内容详情: contentId={}, userId={}, includeOffline={}", contentId, userId, includeOffline);
            
            // 获取内容基本信息
            Content content = contentService.getContentById(contentId, includeOffline);
            if (content == null) {
                log.warn("内容不存在: contentId={}", contentId);
                return Result.error("内容不存在");
            }
            
            // 转换为响应对象
            ContentResponse response = convertToResponse(content);
            
            // 如果提供了userId，查询互动状态
            if (userId != null) {
                // 这里可以添加查询点赞、收藏、关注状态的逻辑
                // 为了简化，先设置为false，实际项目中应该调用相应的service方法
                response.setIsLiked(false);
                response.setIsFavorited(false);
                response.setIsFollowed(false);
                log.debug("已设置用户互动状态: userId={}, contentId={}", userId, contentId);
            }
            
            log.info("查询内容详情成功: contentId={}, title={}", contentId, content.getTitle());
            return Result.success(response);
            
        } catch (Exception e) {
            log.error("查询内容详情失败: contentId={}", contentId, e);
            return Result.error("查询内容详情失败: " + e.getMessage());
        }
    }

    /**
     * 随机获取5个内容详情
     */
    @GetMapping("/random")
    @Operation(summary = "随机内容详情", description = "随机获取5个已发布内容的详细信息，可以乱序")
    public Result<List<ContentResponse>> getRandomContents(
            @Parameter(description = "用户ID（用于互动状态查询）") @RequestParam(required = false) Long userId) {
        try {
            log.info("REST请求 - 随机获取内容详情: userId={}", userId);
            
            // 获取随机5个内容
            List<Content> contentList = contentService.getRandomContents(5);
            if (contentList == null || contentList.isEmpty()) {
                log.warn("没有找到可用的内容");
                return Result.success(new ArrayList<>());
            }
            
            // 转换为响应对象列表
            List<ContentResponse> responseList = contentList.stream()
                .map(content -> {
                    ContentResponse response = convertToResponse(content);
                    
                    // 如果提供了userId，查询互动状态
                    if (userId != null) {
                        // 这里可以添加查询点赞、收藏、关注状态的逻辑
                        // 为了简化，先设置为false，实际项目中应该调用相应的service方法
                        response.setIsLiked(false);
                        response.setIsFavorited(false);
                        response.setIsFollowed(false);
                    }
                    
                    return response;
                })
                .collect(Collectors.toList());
            
            log.info("随机获取内容详情成功: count={}", responseList.size());
            return Result.success(responseList);
            
        } catch (Exception e) {
            log.error("随机获取内容详情失败", e);
            return Result.error("随机获取内容详情失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户关注作者的内容
     */
    @GetMapping("/following/{userId}")
    @Operation(summary = "获取关注内容", description = "获取用户关注作者发布的内容")
    public Result<PageResponse<ContentResponse>> getFollowingContents(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "内容类型") @RequestParam(required = false) String contentType,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createTime") String orderBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "DESC") String orderDirection,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("REST请求 - 获取关注内容: userId={}, contentType={}, page={}/{}", 
                userId, contentType, currentPage, pageSize);
        return contentService.getFollowingContentsForController(userId, contentType, orderBy, orderDirection, currentPage, pageSize);
    }

    /**
     * 删除内容
     * 只能删除自己的内容，使用现有登录权限控制
     * 
     * @param contentId 内容ID
     * @param userId 操作用户ID（用于验证权限）
     * @return 删除结果
     */
    @DeleteMapping("/{contentId}")
    @Operation(summary = "删除内容", description = "删除指定内容，只能删除自己的内容")
    public Result<Boolean> deleteContent(
            @Parameter(description = "内容ID", required = true) @PathVariable Long contentId,
            @Parameter(description = "操作用户ID", required = true) @RequestParam Long userId) {
        try {
            log.info("REST请求 - 删除内容: contentId={}, userId={}", contentId, userId);
            
                           // 验证内容是否存在
               Content content = contentService.getContentById(contentId, false);
               if (content == null) {
                   log.warn("删除内容失败：内容不存在，contentId={}", contentId);
                   return Result.error("内容不存在");
               }
            
            // 权限验证：只能删除自己的内容
            if (!content.getAuthorId().equals(userId)) {
                log.warn("删除内容失败：权限不足，contentId={}, contentAuthorId={}, operatorId={}", 
                        contentId, content.getAuthorId(), userId);
                return Result.error("权限不足，只能删除自己的内容");
            }
            
            // 执行删除操作
            boolean success = contentService.deleteContent(contentId, userId);
            if (success) {
                log.info("内容删除成功: contentId={}, userId={}", contentId, userId);
                return Result.success(true);
            } else {
                log.error("内容删除失败: contentId={}, userId={}", contentId, userId);
                return Result.error("删除失败");
            }
            
        } catch (Exception e) {
            log.error("删除内容异常: contentId={}, userId={}", contentId, userId, e);
            return Result.error("删除内容失败: " + e.getMessage());
        }
    }
    
    /**
     * 将Content实体转换为ContentResponse
     * 私有辅助方法，用于数据转换
     */
    private ContentResponse convertToResponse(Content content) {
        if (content == null) {
            return null;
        }

        ContentResponse response = new ContentResponse();
        
        // 基本信息
        response.setId(content.getId());
        response.setTitle(content.getTitle());
        response.setDescription(content.getDescription());
        response.setContentData(content.getContentData());
        response.setContentType(content.getContentType());
        response.setCoverUrl(content.getCoverUrl());
        response.setTags(content.getTags());
        
        // 作者信息
        response.setAuthorId(content.getAuthorId());
        response.setAuthorNickname(content.getAuthorNickname());
        response.setAuthorAvatar(content.getAuthorAvatar());
        
        // 分类信息
        response.setCategoryId(content.getCategoryId());
        response.setCategoryName(content.getCategoryName());
        
        // 状态信息
        response.setStatus(content.getStatus());
        response.setReviewStatus(content.getReviewStatus());
        
        // 统计信息
        response.setViewCount(content.getViewCount());
        response.setLikeCount(content.getLikeCount());
        response.setCommentCount(content.getCommentCount());
        response.setFavoriteCount(content.getFavoriteCount());
        response.setShareCount(content.getShareCount());
        response.setScoreCount(content.getScoreCount());
        response.setScoreTotal(content.getScoreTotal());
        
        // 时间信息
        response.setPublishTime(content.getPublishTime());
        response.setCreateTime(content.getCreateTime());
        response.setUpdateTime(content.getUpdateTime());

        return response;
    }
}