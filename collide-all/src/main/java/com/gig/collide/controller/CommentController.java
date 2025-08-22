package com.gig.collide.controller;


import com.gig.collide.Apientry.api.comment.CommentFacadeService;
import com.gig.collide.Apientry.api.comment.request.CommentCreateRequest;
import com.gig.collide.Apientry.api.comment.request.CommentUpdateRequest;
import com.gig.collide.Apientry.api.comment.response.CommentResponse;
import com.gig.collide.Apientry.api.common.response.PageResponse;
import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.domain.Comment;
import com.gig.collide.service.Impl.CommentServiceImpl;
import com.gig.collide.service.InformService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 评论REST控制器 - 规范版
 * 提供完整的评论功能HTTP接口：基础操作、高级查询、统计分析、管理功能
 * 支持评论类型：CONTENT、DYNAMIC
 * 
 * @author Collide
 * @version 5.0.0 (与Content模块一致版)
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
@Tag(name = "评论管理", description = "评论相关的API接口")
public class CommentController {

    private final CommentServiceImpl commentService;
    private final InformService informService;

    /**
     * 评论列表查询
     * 支持多种条件查询和分页
     */
    @GetMapping("/list")
    @Operation(summary = "评论列表查询", description = "支持按类型、目标、用户等条件查询评论列表")
    public Result<PageResponse<CommentResponse>> listComments(
            @Parameter(description = "评论类型") @RequestParam(required = false) String commentType,
            @Parameter(description = "目标ID") @RequestParam(required = false) Long targetId,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "父评论ID") @RequestParam(required = false) Long parentId,
            @Parameter(description = "评论状态") @RequestParam(required = false) String status,
            @Parameter(description = "关键词搜索") @RequestParam(required = false) String keyword,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createTime") String orderBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "DESC") String orderDirection,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("REST请求 - 评论列表查询: type={}, targetId={}, userId={}, page={}/{}", 
                commentType, targetId, userId, currentPage, pageSize);
        return commentService.listCommentsForController(commentType, targetId, userId, parentId, status, keyword,
                orderBy, orderDirection, currentPage, pageSize);
    }

    /**
     * 创建评论
     * 支持根评论和回复评论，包含消息通知功能
     */
    @PostMapping("/create")
    @Operation(summary = "创建评论", description = "创建评论，支持根评论和回复评论")
    public Result<CommentResponse> createComment(@Valid @RequestBody CommentCreateRequest request) {
        try {
            log.info("REST请求 - 创建评论: userId={}, commentType={}, targetId={}, parentId={}", 
                    request.getUserId(), request.getCommentType(), request.getTargetId(), request.getParentCommentId());
            
            // 将CommentCreateRequest转换为Comment对象
            Comment comment = new Comment();
            comment.setCommentType(request.getCommentType());
            comment.setTargetId(request.getTargetId());
            comment.setParentCommentId(request.getParentCommentId());
            comment.setContent(request.getContent());
            comment.setUserId(request.getUserId());
            comment.setUserNickname(request.getUserNickname());
            comment.setUserAvatar(request.getUserAvatar());
            comment.setReplyToUserId(request.getReplyToUserId());
            comment.setReplyToUserNickname(request.getReplyToUserNickname());
            comment.setReplyToUserAvatar(request.getReplyToUserAvatar());
            comment.setStatus(request.getStatus());
            
            // 调用服务创建评论
            Comment createdComment = commentService.createComment(comment);
            
            if (createdComment != null) {
                // 评论创建成功，发送通知
                try {
                    // 这里需要从其他地方获取targetAuthorId和targetTitle
                    // 暂时使用默认值，实际项目中应该从内容服务获取
                    Long targetAuthorId = getTargetAuthorId(request.getTargetId(), request.getCommentType());
                    String targetTitle = getTargetTitle(request.getTargetId(), request.getCommentType());
                    
                    if (request.getParentCommentId() != null && request.getParentCommentId() > 0) {
                        // 回复评论通知
                        if (targetAuthorId != null && !request.getUserId().equals(targetAuthorId)) {
                            informService.sendReplyNotification(
                                request.getUserId(),
                                request.getUserNickname(),
                                targetAuthorId,
                                request.getCommentType(),
                                request.getTargetId(),
                                targetTitle,
                                request.getContent()
                            );
                            log.info("回复评论通知发送成功: replierId={}, targetAuthorId={}", 
                                    request.getUserId(), targetAuthorId);
                        }
                    } else {
                        // 根评论通知
                        if (targetAuthorId != null && !request.getUserId().equals(targetAuthorId)) {
                            informService.sendCommentNotification(
                                request.getUserId(),
                                request.getUserNickname(),
                                targetAuthorId,
                                request.getCommentType(),
                                request.getTargetId(),
                                targetTitle,
                                request.getContent()
                            );
                            log.info("评论通知发送成功: commenterId={}, targetAuthorId={}", 
                                    request.getUserId(), targetAuthorId);
                        }
                    }
                } catch (Exception e) {
                    log.warn("发送评论通知失败，但不影响评论创建: {}", e.getMessage());
                }
                
                // 转换为响应对象
                CommentResponse response = convertToCommentResponse(createdComment);
                return Result.success(response);
            } else {
                return Result.error("创建评论失败");
            }
        } catch (Exception e) {
            log.error("创建评论失败", e);
            return Result.error("创建评论失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取目标作者ID
     * 这里需要根据实际业务逻辑实现
     */
    private Long getTargetAuthorId(Long targetId, String commentType) {
        // TODO: 根据targetId和commentType从相应的服务获取作者ID
        // 这里暂时返回null，实际项目中应该调用ContentService或DynamicService
        return null;
    }
    
    /**
     * 获取目标标题
     * 这里需要根据实际业务逻辑实现
     */
    private String getTargetTitle(Long targetId, String commentType) {
        // TODO: 根据targetId和commentType从相应的服务获取标题
        // 这里暂时返回null，实际项目中应该调用ContentService或DynamicService
        return null;
    }
    
    /**
     * 将Comment对象转换为CommentResponse
     */
    private CommentResponse convertToCommentResponse(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setCommentType(comment.getCommentType());
        response.setTargetId(comment.getTargetId());
        response.setParentCommentId(comment.getParentCommentId());
        response.setContent(comment.getContent());
        response.setUserId(comment.getUserId());
        response.setUserNickname(comment.getUserNickname());
        response.setUserAvatar(comment.getUserAvatar());
        response.setReplyToUserId(comment.getReplyToUserId());
        response.setReplyToUserNickname(comment.getReplyToUserNickname());
        response.setReplyToUserAvatar(comment.getReplyToUserAvatar());
        response.setLikeCount(comment.getLikeCount());
        response.setReplyCount(comment.getReplyCount());
        response.setStatus(comment.getStatus());
        response.setCreateTime(comment.getCreateTime());
        response.setUpdateTime(comment.getUpdateTime());
        return response;
    }
}