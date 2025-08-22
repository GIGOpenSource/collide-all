package com.gig.collide.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户实体类 - 简洁版
 * 对应表：t_user
 * 支持用户注册、登录、信息管理等核心功能
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
@TableName("t_user")
public class User {

    /**
     * 用户ID
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
     * 头像URL
     */
    @TableField("avatar")
    private String avatar;

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
     * 用户角色：user、blogger、admin、vip
     */
    @TableField("role")
    private String role;

    /**
     * 用户状态：active、inactive、suspended、banned
     */
    @TableField("status")
    private String status;

    /**
     * 个人简介
     */
    @TableField("bio")
    private String bio;

    /**
     * 生日
     */
    @TableField("birthday")
    private LocalDate birthday;

    /**
     * 性别：male、female、unknown
     */
    @TableField("gender")
    private String gender;

    /**
     * 所在地
     */
    @TableField("location")
    private String location;

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
     * 内容数
     */
    @TableField("content_count")
    private Long contentCount;

    /**
     * 获得点赞数
     */
    @TableField("like_count")
    private Long likeCount;

    /**
     * VIP过期时间
     */
    @TableField("vip_expire_time")
    private LocalDateTime vipExpireTime;

    /**
     * 是否是VIP用户：Y-是，N-否
     */
    @TableField("is_vip")
    private String isVip;

    /**
     * 最后登录时间
     */
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    /**
     * 登录次数
     */
    @TableField("login_count")
    private Long loginCount;

    /**
     * 邀请码
     */
    @TableField("invite_code")
    private String inviteCode;

    /**
     * 邀请人ID
     */
    @TableField("inviter_id")
    private Long inviterId;

    /**
     * 邀请人数
     */
    @TableField("invited_count")
    private Long invitedCount;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // =================== 业务方法 ===================

    /**
     * 判断是否为活跃状态
     */
    public boolean isActive() {
        return "active".equals(this.status);
    }

    /**
     * 判断是否为VIP用户
     */
    public boolean isVip() {
        return "Y".equals(this.isVip);
    }

    /**
     * 判断是否为管理员
     */
    public boolean isAdmin() {
        return "admin".equals(this.role);
    }

    /**
     * 判断是否为博主
     */
    public boolean isBlogger() {
        return "blogger".equals(this.role);
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
    public User updateLoginInfo() {
        this.lastLoginTime = LocalDateTime.now();
        this.loginCount = (this.loginCount != null ? this.loginCount : 0L) + 1;
        return this;
    }

    /**
     * 增加粉丝数
     */
    public User incrementFollowerCount() {
        this.followerCount = (this.followerCount != null ? this.followerCount : 0L) + 1;
        return this;
    }

    /**
     * 减少粉丝数
     */
    public User decrementFollowerCount() {
        if (this.followerCount != null && this.followerCount > 0) {
            this.followerCount--;
        }
        return this;
    }

    /**
     * 增加关注数
     */
    public User incrementFollowingCount() {
        this.followingCount = (this.followingCount != null ? this.followingCount : 0L) + 1;
        return this;
    }

    /**
     * 减少关注数
     */
    public User decrementFollowingCount() {
        if (this.followingCount != null && this.followingCount > 0) {
            this.followingCount--;
        }
        return this;
    }

    /**
     * 增加内容数
     */
    public User incrementContentCount() {
        this.contentCount = (this.contentCount != null ? this.contentCount : 0L) + 1;
        return this;
    }

    /**
     * 减少内容数
     */
    public User decrementContentCount() {
        if (this.contentCount != null && this.contentCount > 0) {
            this.contentCount--;
        }
        return this;
    }

    /**
     * 增加点赞数
     */
    public User incrementLikeCount() {
        this.likeCount = (this.likeCount != null ? this.likeCount : 0L) + 1;
        return this;
    }

    /**
     * 减少点赞数
     */
    public User decrementLikeCount() {
        if (this.likeCount != null && this.likeCount > 0) {
            this.likeCount--;
        }
        return this;
    }
}
