package com.gig.collide.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 管理员实体类 - 简洁版
 * 对应表：t_admin
 * 支持管理员账户管理
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
@TableName("t_admin")
public class Admin {

    /**
     * 管理员ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 昵称
     */
    @TableField("nickname")
    private String nickname;

    /**
     * 邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 手机号
     */
    @TableField("phone")
    private String phone;

    /**
     * 密码哈希
     */
    @TableField("password_hash")
    private String passwordHash;

    /**
     * 密码盐值
     */
    @TableField("salt")
    private String salt;

    /**
     * 角色：admin、super_admin
     */
    @TableField("role")
    private String role;

    /**
     * 状态：active、inactive、locked
     */
    @TableField("status")
    private String status;

    /**
     * 头像URL
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 最后登录时间
     */
    @TableField("last_login_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;

    /**
     * 登录次数
     */
    @TableField("login_count")
    private Long loginCount;

    /**
     * 密码错误次数
     */
    @TableField("password_error_count")
    private Integer passwordErrorCount;

    /**
     * 账号锁定时间
     */
    @TableField("lock_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lockTime;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

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
     * 判断是否为活跃状态
     */
    public boolean isActive() {
        return "active".equals(this.status);
    }

    /**
     * 判断是否为锁定状态
     */
    public boolean isLocked() {
        return "locked".equals(this.status);
    }

    /**
     * 判断是否为超级管理员
     */
    public boolean isSuperAdmin() {
        return "super_admin".equals(this.role);
    }

    /**
     * 判断是否为普通管理员
     */
    public boolean isNormalAdmin() {
        return "admin".equals(this.role);
    }

    /**
     * 获取显示名称
     */
    public String getDisplayName() {
        return this.nickname != null && !this.nickname.trim().isEmpty() 
            ? this.nickname 
            : this.username;
    }

    /**
     * 更新登录信息
     */
    public Admin updateLoginInfo() {
        this.lastLoginTime = LocalDateTime.now();
        this.loginCount = (this.loginCount != null ? this.loginCount : 0L) + 1;
        this.passwordErrorCount = 0; // 登录成功后重置错误次数
        return this;
    }

    /**
     * 增加密码错误次数
     */
    public Admin incrementPasswordErrorCount() {
        this.passwordErrorCount = (this.passwordErrorCount != null ? this.passwordErrorCount : 0) + 1;
        return this;
    }

    /**
     * 锁定账号
     */
    public Admin lockAccount() {
        this.status = "locked";
        this.lockTime = LocalDateTime.now();
        return this;
    }

    /**
     * 解锁账号
     */
    public Admin unlockAccount() {
        this.status = "active";
        this.lockTime = null;
        this.passwordErrorCount = 0;
        return this;
    }

    /**
     * 检查是否需要锁定账号（错误次数过多）
     */
    public boolean shouldLockAccount(int maxErrorCount) {
        return this.passwordErrorCount != null && this.passwordErrorCount >= maxErrorCount;
    }
}
