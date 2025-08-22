package com.gig.collide.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 博主申请实体类 - 简洁版
 * 对应表：t_blogger_application
 * 支持博主申请管理
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
@TableName("t_blogger_application")
public class BloggerApplication {

    /**
     * 申请ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 申请用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 申请状态：PENDING、APPROVED、REJECTED
     */
    @TableField("status")
    private String status;

    /**
     * 申请时间
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

    // =================== 扩展字段（用于查询结果，不映射到数据库） ===================

    /**
     * 用户信息（非数据库字段）
     */
    @TableField(exist = false)
    private String username;

    @TableField(exist = false)
    private String nickname;

    @TableField(exist = false)
    private String avatar;

    @TableField(exist = false)
    private String email;

    @TableField(exist = false)
    private String phone;

    // =================== 业务方法 ===================

    /**
     * 判断申请状态是否为待审核
     */
    public boolean isPending() {
        return "PENDING".equals(this.status);
    }

    /**
     * 判断申请状态是否为已通过
     */
    public boolean isApproved() {
        return "APPROVED".equals(this.status);
    }

    /**
     * 判断申请状态是否为已拒绝
     */
    public boolean isRejected() {
        return "REJECTED".equals(this.status);
    }

    /**
     * 判断申请是否可以审核
     */
    public boolean canReview() {
        return isPending();
    }

    /**
     * 通过申请
     */
    public BloggerApplication approve() {
        if (canReview()) {
            this.status = "APPROVED";
        }
        return this;
    }

    /**
     * 拒绝申请
     */
    public BloggerApplication reject() {
        if (canReview()) {
            this.status = "REJECTED";
        }
        return this;
    }

    /**
     * 重置申请状态为待审核
     */
    public BloggerApplication resetToPending() {
        this.status = "PENDING";
        return this;
    }

    /**
     * 获取申请状态描述
     */
    public String getStatusDescription() {
        if (isApproved()) {
            return "已通过";
        }
        if (isRejected()) {
            return "已拒绝";
        }
        return "待审核";
    }

    /**
     * 检查申请是否已完成（已通过或已拒绝）
     */
    public boolean isCompleted() {
        return isApproved() || isRejected();
    }

    /**
     * 创建博主申请
     */
    public static BloggerApplication create(Long userId) {
        return new BloggerApplication()
            .setUserId(userId)
            .setStatus("PENDING");
    }
}
