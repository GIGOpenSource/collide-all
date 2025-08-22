package com.gig.collide.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * Python爬虫博主实体类 - 简洁版
 * 对应表：t_blo_python
 * 支持Python爬虫博主管理
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
@TableName("t_blo_python")
public class BloggerPython {

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
     * 博主昵称
     */
    @TableField("blogger_nickname")
    private String bloggerNickname;

    /**
     * 主页地址
     */
    @TableField("homepage_url")
    private String homepageUrl;

    /**
     * 状态：updated、not_updated
     */
    @TableField("status")
    private String status;

    /**
     * 是否删除：N-未删除，Y-已删除
     */
    @TableField("is_delete")
    private String isDelete;

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
     * 判断状态是否为已更新
     */
    public boolean isUpdated() {
        return "updated".equals(this.status);
    }

    /**
     * 判断状态是否为未更新
     */
    public boolean isNotUpdated() {
        return "not_updated".equals(this.status);
    }

    /**
     * 标记为已删除
     */
    public BloggerPython markAsDeleted() {
        this.isDelete = "Y";
        return this;
    }

    /**
     * 标记为未删除
     */
    public BloggerPython markAsNotDeleted() {
        this.isDelete = "N";
        return this;
    }

    /**
     * 更新状态为已更新
     */
    public BloggerPython setUpdated() {
        this.status = "updated";
        return this;
    }

    /**
     * 更新状态为未更新
     */
    public BloggerPython setNotUpdated() {
        this.status = "not_updated";
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
     * 检查是否有主页地址
     */
    public boolean hasHomepageUrl() {
        return this.homepageUrl != null && !this.homepageUrl.trim().isEmpty();
    }

    /**
     * 创建Python爬虫博主
     */
    public static BloggerPython create(Long bloggerUid, String bloggerNickname, String homepageUrl) {
        return new BloggerPython()
            .setBloggerUid(bloggerUid)
            .setBloggerNickname(bloggerNickname)
            .setHomepageUrl(homepageUrl)
            .setStatus("not_updated")
            .setIsDelete("N");
    }
}
