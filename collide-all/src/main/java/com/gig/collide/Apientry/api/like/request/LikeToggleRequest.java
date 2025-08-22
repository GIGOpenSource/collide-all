package com.gig.collide.Apientry.api.like.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;

/**
 * 切换点赞状态请求 - 简洁版
 * 如果已点赞则取消，如果未点赞则添加
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
public class LikeToggleRequest implements Serializable {

    /**
     * 点赞类型：CONTENT、COMMENT、DYNAMIC
     */
    @NotBlank(message = "点赞类型不能为空")
    private String likeType;

    /**
     * 目标对象ID
     */
    @NotNull(message = "目标对象ID不能为空")
    private Long targetId;

    /**
     * 点赞用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    // =================== 目标对象信息（冗余字段） ===================
    
    /**
     * 目标对象标题（冗余）
     */
    private String targetTitle;

    /**
     * 目标对象作者ID（冗余）
     */
    private Long targetAuthorId;

    // =================== 用户信息（冗余字段） ===================
    
    /**
     * 用户昵称（冗余）
     */
    private String userNickname;

    /**
     * 用户头像（冗余）
     */
    private String userAvatar;
} 