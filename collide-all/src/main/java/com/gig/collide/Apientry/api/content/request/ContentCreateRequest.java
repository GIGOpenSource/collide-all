package com.gig.collide.Apientry.api.content.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;

/**
 * 内容创建请求 - 简洁版
 * 基于content-simple.sql的无连表设计，包含作者和分类信息冗余
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
public class ContentCreateRequest implements Serializable {

    /**
     * 内容标题
     */
    @NotBlank(message = "内容标题不能为空")
    private String title;

    /**
     * 内容描述
     */
    private String description;

    /**
     * 内容类型：NOVEL、COMIC、VIDEO、ARTICLE、AUDIO
     */
    @NotBlank(message = "内容类型不能为空")
    private String contentType;

    /**
     * 内容数据，JSON格式
     */
    private String contentData;

    /**
     * 封面图片URL
     */
    private String coverUrl;

    /**
     * 标签，JSON数组格式
     */
    private String tags;

    // =================== 作者信息（冗余字段） ===================

    /**
     * 作者用户ID
     */
    @NotNull(message = "作者ID不能为空")
    private Long authorId;

    /**
     * 作者昵称（冗余）
     */
    private String authorNickname;

    /**
     * 作者头像URL（冗余）
     */
    private String authorAvatar;

    // =================== 分类信息（冗余字段） ===================

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 分类名称（冗余）
     */
    private String categoryName;

    /**
     * 状态（默认为DRAFT）
     */
    private String status = "DRAFT";

    /**
     * 审核状态（默认为PENDING）
     */
    private String reviewStatus = "PENDING";

    // =================== 付费配置信息 ===================

    /**
     * 付费类型：FREE（免费）、COIN_PAY（金币付费）、VIP_FREE（VIP免费）、TIME_LIMITED（限时付费）
     */
    private String paymentType = "FREE";

    /**
     * 金币价格（付费内容必填）
     */
    private Long coinPrice = 0L;

    /**
     * 原价（用于折扣显示）
     */
    private Long originalPrice;

    /**
     * 是否VIP免费
     */
    private Boolean vipFree = false;

    /**
     * 是否只有VIP才能购买
     */
    private Boolean vipOnly = false;

    /**
     * 是否支持试读
     */
    private Boolean trialEnabled = false;

    /**
     * 试读字数
     */
    private Integer trialWordCount = 0;

    /**
     * 是否永久有效
     */
    private Boolean isPermanent = true;

    /**
     * 有效天数（非永久时使用）
     */
    private Integer validDays;
} 