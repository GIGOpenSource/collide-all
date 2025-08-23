package com.gig.collide.Apientry.api.social;

import com.gig.collide.Apientry.api.common.response.PageResponse;
import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.Apientry.api.social.request.UserInteractionQueryRequest;
import com.gig.collide.Apientry.api.social.response.UserInteractionResponse;

/**
 * 用户互动查询Facade服务接口
 * 整合用户的点赞和评论数据查询功能
 * 
 * @author GIG Team
 * @version 1.0.0
 * @since 2024-01-16
 */
public interface UserInteractionFacadeService {

    /**
     * 查询用户互动数据
     * 整合用户的点赞和评论数据，支持多种过滤条件
     * 
     * @param request 查询请求参数
     * @return 分页的互动数据
     */
    Result<PageResponse<UserInteractionResponse>> getUserInteractions(UserInteractionQueryRequest request);

    /**
     * 查询用户互动数据（简化版）
     * 支持通过URL参数直接查询
     * 
     * @param userId 用户ID
     * @param interactionType 互动类型（ALL/LIKES/COMMENTS）
     * @param likeType 点赞类型（CONTENT/COMMENT）
     * @param commentType 评论类型（CONTENT/DYNAMIC）
     * @param direction 查询方向（ALL/GIVE/RECEIVE）
     * @param orderBy 排序字段
     * @param orderDirection 排序方向
     * @param currentPage 当前页码
     * @param pageSize 页面大小
     * @return 分页的互动数据
     */
    Result<PageResponse<UserInteractionResponse>> getUserInteractions(
            Long userId, String interactionType, String likeType, String commentType,
            String direction, String orderBy, String orderDirection,
            Integer currentPage, Integer pageSize);

    /**
     * 查询所有互动数据
     * 功能为查询所有用户的互动数据（我评论的，我点赞的，评论我的）
     * 
     * @param userId 用户ID
     * @param currentPage 当前页码
     * @param pageSize 页面大小
     * @return 分页的互动数据
     */
    Result<PageResponse<UserInteractionResponse>> getAllInteractions(
            Long userId, Integer currentPage, Integer pageSize);
}
