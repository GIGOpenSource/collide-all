package com.gig.collide.Apientry.api.social.request;

import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 社交动态查询请求 - 简洁版
 *
 * @author GIG Team
 * @version 2.0.0
 */
@Data
public class SocialDynamicQueryRequest implements Serializable {

    /**
     * 当前页码
     */
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNumber = 1;

    /**
     * 页面大小
     */
    @Min(value = 1, message = "页面大小不能小于1")
    private Integer pageSize = 20;

    /**
     * 用户ID（查询指定用户的动态）
     */
    private Long userId;

    /**
     * 当前登录用户ID（用于获取互动状态）
     */
    private Long currentUserId;

    /**
     * 动态类型：text、image、video、share
     */
    private String dynamicType;

    /**
     * 分享目标类型：content、goods
     */
    private String shareTargetType;

    /**
     * 状态：normal、hidden、deleted
     */
    private String status;

    /**
     * 搜索关键词
     */
    private String keyword;

    /**
     * 最小点赞数
     */
    private Long minLikeCount;

    /**
     * 排序字段
     */
    private String sortBy;

    /**
     * 排序方向：asc、desc
     */
    private String sortDirection;
}
