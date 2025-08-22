package com.gig.collide.Apientry.api.search.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 视频混合搜索响应 - 包含视频内容和付费信息的聚合结果
 * 
 * @author GIG Team
 * @version 2.0.0 (视频混合搜索版)
 * @since 2024-01-31
 */
@Data
public class VideoSearchResponse implements Serializable {

    /**
     * 内容ID
     */
    private Long contentId;

    /**
     * 内容标题
     */
    private String title;

    /**
     * 内容描述
     */
    private String description;

    /**
     * 内容类型
     */
    private String contentType;

    /**
     * 内容状态
     */
    private String status;

    /**
     * 作者信息
     */
    private AuthorInfo author;

    /**
     * 分类信息
     */
    private CategoryInfo category;

    /**
     * 标签列表
     */
    private List<String> tags;

    /**
     * 付费信息
     */
    private PaymentInfo payment;

    /**
     * 统计信息
     */
    private StatisticsInfo statistics;

    /**
     * 时间信息
     */
    private TimeInfo timeInfo;

    /**
     * 搜索相关度分数
     */
    private Double relevanceScore;

    /**
     * 作者信息内部类
     */
    @Data
    public static class AuthorInfo implements Serializable {
        private Long authorId;
        private String authorNickname;
        private String authorAvatar;
        private String authorDescription;
    }

    /**
     * 分类信息内部类
     */
    @Data
    public static class CategoryInfo implements Serializable {
        private Long categoryId;
        private String categoryName;
        private String categoryDescription;
    }

    /**
     * 付费信息内部类
     */
    @Data
    public static class PaymentInfo implements Serializable {
        /**
         * 付费类型：FREE、COIN_PAY、VIP_FREE、TIME_LIMITED
         */
        private String paymentType;

        /**
         * 价格（金币）
         */
        private BigDecimal price;

        /**
         * 原价（金币）
         */
        private BigDecimal originalPrice;

        /**
         * 是否VIP免费
         */
        private Boolean vipFree;

        /**
         * 是否VIP专享
         */
        private Boolean vipOnly;

        /**
         * 是否支持试读
         */
        private Boolean trialEnabled;

        /**
         * 试读时长（秒）
         */
        private Integer trialDuration;

        /**
         * 有效期（天）
         */
        private Integer validityDays;

        /**
         * 是否永久有效
         */
        private Boolean permanent;

        /**
         * 折扣信息
         */
        private DiscountInfo discount;
    }

    /**
     * 折扣信息内部类
     */
    @Data
    public static class DiscountInfo implements Serializable {
        /**
         * 折扣率（0-1之间）
         */
        private BigDecimal discountRate;

        /**
         * 折扣开始时间
         */
        private LocalDateTime discountStartTime;

        /**
         * 折扣结束时间
         */
        private LocalDateTime discountEndTime;

        /**
         * 折扣描述
         */
        private String discountDescription;
    }

    /**
     * 统计信息内部类
     */
    @Data
    public static class StatisticsInfo implements Serializable {
        /**
         * 浏览量
         */
        private Long viewCount;

        /**
         * 点赞数
         */
        private Long likeCount;

        /**
         * 收藏数
         */
        private Long favoriteCount;

        /**
         * 评论数
         */
        private Long commentCount;

        /**
         * 分享数
         */
        private Long shareCount;

        /**
         * 评分
         */
        private Double score;

        /**
         * 评分人数
         */
        private Long scoreCount;

        /**
         * 销量
         */
        private Long salesCount;

        /**
         * 收入
         */
        private BigDecimal revenue;
    }

    /**
     * 时间信息内部类
     */
    @Data
    public static class TimeInfo implements Serializable {
        /**
         * 创建时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;

        /**
         * 发布时间
         */
        private LocalDateTime publishTime;

        /**
         * 更新时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updateTime;

        /**
         * 上架时间
         */
        private LocalDateTime onlineTime;
    }
}
