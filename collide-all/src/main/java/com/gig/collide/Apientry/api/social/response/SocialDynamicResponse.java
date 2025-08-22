package com.gig.collide.Apientry.api.social.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 社交动态响应 - 简洁版
 * 基于t_social_dynamic表结构
 * 
 * @author GIG Team
 * @version 2.0.0
 */
@Data
public class SocialDynamicResponse implements Serializable {

    private Long id;

    private String content;

    private String title;

    private String dynamicType;

    private List<String> images;

    private String videoUrl;

    /**
     * 付费相关字段
     */
    private Boolean isFree;
    private java.math.BigDecimal price;

    /**
     * 用户信息（冗余字段）
     */
    private Long userId;
    private String userNickname;
    private String userAvatar;

    /**
     * 分享相关字段
     */
    private String shareTargetType;
    private Long shareTargetId;
    private String shareTargetTitle;

    /**
     * 统计字段（冗余存储）
     */
    private Long likeCount;
    private Long commentCount;
    private Long shareCount;

    private String status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 当前用户互动状态（新增字段）
     */
    private Boolean isLiked;        // 当前用户是否点赞过
    private Boolean isFollowed;     // 当前用户是否关注过动态作者
} 