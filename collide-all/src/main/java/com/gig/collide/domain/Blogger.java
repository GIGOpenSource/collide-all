package com.gig.collide.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 博主实体类 - 简洁版
 * 对应表：t_blo
 * 支持博主信息管理
 * 
 * @author GIG Team
 * @version 2.0.0 (简洁版)
 * @since 2024-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Accessors(chain = true)
@TableName("t_blo")
public class Blogger {

    /**
     * 博主ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 博主UID
     */
    @TableField("blogger_uid")
    private Long bloggerUid;

    /**
     * 主页地址
     */
    @TableField("homepage_url")
    private String homepageUrl;

    /**
     * 博主昵称
     */
    @TableField("blogger_nickname")
    private String bloggerNickname;

    /**
     * 博主签名
     */
    @TableField("blogger_signature")
    private String bloggerSignature;

    /**
     * 头像URL
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 标签（逗号分隔）
     */
    @TableField("tags")
    private String tags;

    /**
     * 粉丝数
     */
    @TableField("follower_count")
    private Long followerCount;

    /**
     * 关注数
     */
    @TableField("following_count")
    private Long followingCount;

    /**
     * 作品数
     */
    @TableField("work_count")
    private Long workCount;

    /**
     * 作品比例（百分比）
     */
    @TableField("work_ratio")
    private BigDecimal workRatio;

    /**
     * 手机号
     */
    @TableField("phone")
    private String phone;

    /**
     * 账户名
     */
    @TableField("account")
    private String account;

    /**
     * 博主类型
     */
    @TableField("type")
    private String type;

    /**
     * 是否在爬取：0-否，1-是
     */
    @TableField("is_python")
    private Integer isPython;

    /**
     * 状态：not_updated、updating、success、failed
     */
    @TableField("status")
    private String status;

    /**
     * 是否删除：N-未删除，Y-已删除
     */
    @TableField("is_delete")
    private String isDelete;

    /**
     * 是否入驻：N-未入驻，Y-已入驻
     */
    @TableField("is_enter")
    private String isEnter;

    /**
     * 爬取类型
     */
    @TableField("pt_type")
    private String ptType;

    /**
     * 扩展字段2
     */
    @TableField("extend_field_2")
    private String extendField2;

    /**
     * 扩展字段3
     */
    @TableField("extend_field_3")
    private String extendField3;

    /**
     * 扩展字段4
     */
    @TableField("extend_field_4")
    private String extendField4;

    /**
     * 扩展字段5
     */
    @TableField("extend_field_5")
    private String extendField5;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    // =================== 业务方法 ===================

    /**
     * 判断是否已删除
     */
    public boolean isDeleted() {
        return "Y".equals(this.isDelete);
    }

    /**
     * 判断是否未删除
     */
    public boolean isNotDeleted() {
        return "N".equals(this.isDelete);
    }

    /**
     * 判断是否已入驻
     */
    public boolean isEntered() {
        return "Y".equals(this.isEnter);
    }

    /**
     * 判断是否未入驻
     */
    public boolean isNotEntered() {
        return "N".equals(this.isEnter);
    }

    /**
     * 判断是否在爬取
     */
    public boolean isPythonCrawling() {
        return this.isPython != null && this.isPython == 1;
    }

    /**
     * 判断状态是否为未更新
     */
    public boolean isNotUpdated() {
        return "not_updated".equals(this.status);
    }

    /**
     * 判断状态是否为更新中
     */
    public boolean isUpdating() {
        return "updating".equals(this.status);
    }

    /**
     * 判断状态是否为更新成功
     */
    public boolean isSuccess() {
        return "success".equals(this.status);
    }

    /**
     * 判断状态是否为更新失败
     */
    public boolean isFailed() {
        return "failed".equals(this.status);
    }

    /**
     * 标记为已删除
     */
    public Blogger markAsDeleted() {
        this.isDelete = "Y";
        return this;
    }

    /**
     * 标记为未删除
     */
    public Blogger markAsNotDeleted() {
        this.isDelete = "N";
        return this;
    }

    /**
     * 标记为已入驻
     */
    public Blogger markAsEntered() {
        this.isEnter = "Y";
        return this;
    }

    /**
     * 标记为未入驻
     */
    public Blogger markAsNotEntered() {
        this.isEnter = "N";
        return this;
    }

    /**
     * 设置爬取状态
     */
    public Blogger setPythonCrawling(boolean isCrawling) {
        this.isPython = isCrawling ? 1 : 0;
        return this;
    }

    /**
     * 更新状态为更新中
     */
    public Blogger setUpdating() {
        this.status = "updating";
        return this;
    }

    /**
     * 更新状态为成功
     */
    public Blogger setSuccess() {
        this.status = "success";
        return this;
    }

    /**
     * 更新状态为失败
     */
    public Blogger setFailed() {
        this.status = "failed";
        return this;
    }

    /**
     * 获取博主显示名称
     */
    public String getDisplayName() {
        return this.bloggerNickname != null && !this.bloggerNickname.trim().isEmpty() 
            ? this.bloggerNickname 
            : "博主";
    }

    /**
     * 获取作品比例百分比
     */
    public BigDecimal getWorkRatioPercentage() {
        return this.workRatio != null ? this.workRatio : BigDecimal.ZERO;
    }

    /**
     * 检查是否有标签
     */
    public boolean hasTags() {
        return this.tags != null && !this.tags.trim().isEmpty();
    }

    /**
     * 创建博主
     */
    public static Blogger create(Long bloggerUid, String bloggerNickname, String homepageUrl, String type) {
        return new Blogger()
            .setBloggerUid(bloggerUid)
            .setBloggerNickname(bloggerNickname)
            .setHomepageUrl(homepageUrl)
            .setType(type)
            .setStatus("not_updated")
            .setIsDelete("N")
            .setIsEnter("N")
            .setIsPython(0)
            .setFollowerCount(0L)
            .setFollowingCount(0L)
            .setWorkCount(0L)
            .setWorkRatio(BigDecimal.ZERO);
    }
}
