package com.gig.collide.Apientry.api.follow.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;

/**
 * 取消关注请求 - 简洁版
 * 
 * @author Collide
 * @version 2.0.0 (简洁版)
 * @since 2024-01-01
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class FollowUnfollowRequest implements Serializable {

    /**
     * 关注者用户ID
     */
    @NotNull(message = "关注者ID不能为空")
    private Long followerId;

    /**
     * 被关注者用户ID
     */
    @NotNull(message = "被关注者ID不能为空")
    private Long followeeId;

    /**
     * 被关注者用户ID（兼容字段）
     * 支持 followedId 格式，用于向后兼容
     */
    private Long followedId;
}
