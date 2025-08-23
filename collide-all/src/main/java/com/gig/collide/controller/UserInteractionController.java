package com.gig.collide.controller;

import com.gig.collide.Apientry.api.common.response.PageResponse;
import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.Apientry.api.social.UserInteractionFacadeService;
import com.gig.collide.Apientry.api.social.request.UserInteractionQueryRequest;
import com.gig.collide.Apientry.api.social.response.UserInteractionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户互动查询REST控制器
 * 整合用户的点赞和评论数据查询功能
 * 
 * @author GIG Team
 * @version 1.0.0
 * @since 2024-01-16
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/user-interactions")
@RequiredArgsConstructor
@Tag(name = "用户互动查询", description = "整合用户点赞和评论数据的查询接口")
public class UserInteractionController {

    private final UserInteractionFacadeService userInteractionFacadeService;

    /**
     * 查询用户互动数据（POST方式）
     * 支持复杂的查询条件
     */
    @PostMapping("/list")
    @Operation(summary = "查询用户互动数据", description = "整合查询用户的点赞和评论数据，支持多种过滤条件")
    public Result<PageResponse<UserInteractionResponse>> getUserInteractions(@RequestBody UserInteractionQueryRequest request) {
        log.info("REST请求 - 查询用户互动数据: {}", request);
        return userInteractionFacadeService.getUserInteractions(request);
    }

    /**
     * 查询用户互动数据（GET方式）
     * 支持通过URL参数查询，兼容原有API风格
     */
    @GetMapping("/list")
    @Operation(summary = "查询用户互动数据（GET方式）", description = "通过URL参数查询用户互动数据，兼容原有API风格")
    public Result<PageResponse<UserInteractionResponse>> getUserInteractions(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "互动类型：ALL/LIKES/COMMENTS") @RequestParam(required = false) String interactionType,
            @Parameter(description = "点赞类型：CONTENT/COMMENT") @RequestParam(required = false) String likeType,
            @Parameter(description = "评论类型：CONTENT/DYNAMIC") @RequestParam(required = false) String commentType,
            @Parameter(description = "查询方向：ALL/GIVE/RECEIVE") @RequestParam(required = false) String direction,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createTime") String orderBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "DESC") String orderDirection,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "20") Integer pageSize) {
        
        log.info("REST请求 - 查询用户互动数据: userId={}, interactionType={}, likeType={}, commentType={}, direction={}, page={}/{}", 
                userId, interactionType, likeType, commentType, direction, currentPage, pageSize);
        
        return userInteractionFacadeService.getUserInteractions(
                userId, interactionType, likeType, commentType, direction, orderBy, orderDirection, currentPage, pageSize);
    }

    /**
     * 查询我点赞的内容
     * 对应原有API: GET /api/v1/like/list?userId=123&likeType=CONTENT
     */
    @GetMapping("/my-liked-contents")
    @Operation(summary = "查询我点赞的内容", description = "查询用户点赞的内容列表")
    public Result<PageResponse<UserInteractionResponse>> getMyLikedContents(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "20") Integer pageSize) {
        
        log.info("REST请求 - 查询我点赞的内容: userId={}, page={}/{}", userId, currentPage, pageSize);
        
        return userInteractionFacadeService.getUserInteractions(
                userId, "LIKES", "CONTENT", null, "GIVE", "createTime", "DESC", currentPage, pageSize);
    }

    /**
     * 查询我点赞的评论
     * 对应原有API: GET /api/v1/like/list?userId=123&likeType=COMMENT
     */
    @GetMapping("/my-liked-comments")
    @Operation(summary = "查询我点赞的评论", description = "查询用户点赞的评论列表")
    public Result<PageResponse<UserInteractionResponse>> getMyLikedComments(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "20") Integer pageSize) {
        
        log.info("REST请求 - 查询我点赞的评论: userId={}, page={}/{}", userId, currentPage, pageSize);
        
        return userInteractionFacadeService.getUserInteractions(
                userId, "LIKES", "COMMENT", null, "GIVE", "createTime", "DESC", currentPage, pageSize);
    }

    /**
     * 查询我发出的评论
     * 对应原有API: GET /api/v1/comments/list?userId=123
     */
    @GetMapping("/my-comments")
    @Operation(summary = "查询我发出的评论", description = "查询用户发出的评论列表")
    public Result<PageResponse<UserInteractionResponse>> getMyComments(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "评论类型：CONTENT/DYNAMIC") @RequestParam(required = false) String commentType,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "20") Integer pageSize) {
        
        log.info("REST请求 - 查询我发出的评论: userId={}, commentType={}, page={}/{}", userId, commentType, currentPage, pageSize);
        
        return userInteractionFacadeService.getUserInteractions(
                userId, "COMMENTS", null, commentType, "GIVE", "createTime", "DESC", currentPage, pageSize);
    }

    /**
     * 查询我收到的评论
     * 对应原有API: GET /api/v1/comments/list?targetAuthorId=123
     */
    @GetMapping("/comments-to-me")
    @Operation(summary = "查询我收到的评论", description = "查询用户收到的评论列表（回复我的评论）")
    public Result<PageResponse<UserInteractionResponse>> getCommentsToMe(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "20") Integer pageSize) {
        
        log.info("REST请求 - 查询我收到的评论: userId={}, page={}/{}", userId, currentPage, pageSize);
        
        return userInteractionFacadeService.getUserInteractions(
                userId, "COMMENTS", null, null, "RECEIVE", "createTime", "DESC", currentPage, pageSize);
    }

    /**
     * 查询我收到的点赞
     * 查询我的评论被点赞的记录
     */
    @GetMapping("/likes-to-my-comments")
    @Operation(summary = "查询我收到的点赞", description = "查询用户评论被点赞的记录")
    public Result<PageResponse<UserInteractionResponse>> getLikesToMyComments(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "20") Integer pageSize) {
        
        log.info("REST请求 - 查询我收到的点赞: userId={}, page={}/{}", userId, currentPage, pageSize);
        
        return userInteractionFacadeService.getUserInteractions(
                userId, "LIKES", "COMMENT", null, "RECEIVE", "createTime", "DESC", currentPage, pageSize);
    }

    /**
     * 查询所有互动数据
     * 功能为查询所有用户的互动数据（我评论的，我点赞的，评论我的）
     */
    @GetMapping("/all")
    @Operation(summary = "查询所有互动数据", description = "整合查询用户的所有点赞和评论数据")
    public Result<PageResponse<UserInteractionResponse>> getAllInteractions(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "20") Integer pageSize) {

        log.info("REST请求 - 查询所有互动数据: userId={}, page={}/{}", userId, currentPage, pageSize);
        
        // 调用Facade服务查询所有互动数据
        return userInteractionFacadeService.getAllInteractions(userId, currentPage, pageSize);
    }

}
