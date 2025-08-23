package com.gig.collide.Apientry.api.user.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户内容响应类
 * 用于统一返回用户发布的所有内容（包括t_content表和t_social_dynamic表的数据）
 * 
 * @author GIG Team
 * @version 1.0.0
 */
@Data
public class UserContentResponse implements Serializable {

    /**
     * 内容ID
     */
    private Long id;

    /**
     * 内容标题
     */
    private String title;

    /**
     * 内容描述/动态内容
     */
    private String content;

    /**
     * 内容类型：NOVEL、COMIC、VIDEO、ARTICLE、AUDIO、SHOTVIDEO、LONGVIDEO、SOCIAL_DYNAMIC
     */
    private String contentType;

    /**
     * 封面图片URL
     */
    private String coverUrl;

    /**
     * 图片列表（动态专用）
     */
    private String images;

    /**
     * 视频URL
     */
    private String videoUrl;

    /**
     * 内容数据，JSON格式
     */
    private String contentData;

    /**
     * 标签
     */
    private String tags;

    /**
     * 状态：DRAFT、PUBLISHED、OFFLINE、normal、hidden、deleted
     */
    private String status;

    /**
     * 审核状态：PENDING、APPROVED、REJECTED（仅content表）
     */
    private String reviewStatus;

    /**
     * 是否免费
     */
    private Boolean isFree;

    /**
     * 价格
     */
    private java.math.BigDecimal price;

    /**
     * 查看数
     */
    private Long viewCount;

    /**
     * 点赞数
     */
    private Long likeCount;

    /**
     * 评论数
     */
    private Long commentCount;

    /**
     * 收藏数
     */
    private Long favoriteCount;

    /**
     * 分享数
     */
    private Long shareCount;

    /**
     * 分享目标类型（动态专用）
     */
    private String shareTargetType;

    /**
     * 分享目标ID（动态专用）
     */
    private Long shareTargetId;

    /**
     * 分享目标标题（动态专用）
     */
    private String shareTargetTitle;

    /**
     * 数据来源：content 或 social_dynamic
     */
    private String dataSource;

    /**
     * 发布时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;

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
}
