package com.gig.collide.Apientry.api.social.request;

import lombok.*;

import java.io.Serializable;

/**
 * 用户互动查询请求对象
 * 用于整合查询用户的点赞和评论数据
 * 
 * @author GIG Team
 * @version 1.0.0
 * @since 2024-01-16
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserInteractionQueryRequest implements Serializable {

    /**
     * 用户ID（必填）
     */
    private Long userId;

    /**
     * 互动类型过滤
     * ALL: 所有互动
     * LIKES: 只查询点赞
     * COMMENTS: 只查询评论
     */
    private String interactionType = "ALL";

    /**
     * 点赞类型过滤（当interactionType为LIKES或ALL时生效）
     * CONTENT: 内容点赞
     * COMMENT: 评论点赞
     */
    private String likeType;

    /**
     * 评论类型过滤（当interactionType为COMMENTS或ALL时生效）
     * CONTENT: 内容评论
     * DYNAMIC: 动态评论
     */
    private String commentType;

    /**
     * 查询方向
     * GIVE: 我发出的（我点赞的内容/我发出的评论）
     * RECEIVE: 我收到的（我点赞的评论/我收到的评论）
     * ALL: 所有
     */
    private String direction = "ALL";

    /**
     * 排序字段
     * createTime: 按创建时间排序
     * updateTime: 按更新时间排序
     */
    private String orderBy = "createTime";

    /**
     * 排序方向
     * ASC: 升序
     * DESC: 降序
     */
    private String orderDirection = "DESC";

    /**
     * 当前页码
     */
    private Integer currentPage = 1;

    /**
     * 页面大小
     */
    private Integer pageSize = 20;
}
