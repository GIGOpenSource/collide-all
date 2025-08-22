package com.gig.collide.Apientry.api.user.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户响应对象
 * 
 * <p>用于返回用户信息的响应对象，包含用户的完整信息。</p>
 * <p>包含以下信息：</p>
 * <ul>
 *   <li>基本信息：ID、用户名、昵称、头像、邮箱、手机号</li>
 *   <li>状态信息：用户状态、VIP状态</li>
 *   <li>统计数据：粉丝数、关注数、内容数、点赞数</li>
 *   <li>时间信息：创建时间、更新时间</li>
 * </ul>
 * 
 * @author GIG Team
 * @version 2.0.0 (支持Dubbo序列化)
 */
@Data
@Schema(description = "用户响应对象")
public class UserResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID", example = "123")
    private Long id;

    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    @Schema(description = "昵称", example = "张三")
    private String nickname;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "邮箱地址", example = "zhangsan@example.com")
    private String email;

    @Schema(description = "手机号码", example = "13800138000")
    private String phone;

    @Schema(description = "用户角色列表")
    private List<String> roles;

    @Schema(description = "用户状态", example = "active")
    private String status;

    @Schema(description = "个人简介")
    private String bio;

    @Schema(description = "生日")
    private LocalDate birthday;

    @Schema(description = "性别", example = "male")
    private String gender;

    @Schema(description = "所在地", example = "北京")
    private String location;

    /**
     * 统计数据
     */
    @Schema(description = "粉丝数量", example = "100")
    private Long followerCount;

    @Schema(description = "关注数量", example = "50")
    private Long followingCount;

    @Schema(description = "内容数量", example = "25")
    private Long contentCount;

    @Schema(description = "获赞数量", example = "1000")
    private Long likeCount;

    /**
     * VIP信息
     */
    @Schema(description = "VIP过期时间")
    private LocalDateTime vipExpireTime;

    /**
     * 是否是VIP用户：Y-是，N-否
     */
    @Schema(description = "VIP状态：Y-是VIP，N-非VIP", example = "N")
    private String isVip;

    /**
     * 登录信息
     */
    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;

    @Schema(description = "登录次数", example = "10")
    private Long loginCount;

    /**
     * 邀请信息
     */
    @Schema(description = "邀请码", example = "ABC123")
    private String inviteCode;

    @Schema(description = "邀请人ID", example = "456")
    private Long inviterId;

    @Schema(description = "已邀请人数", example = "5")
    private Long invitedCount;

    /**
     * 钱包信息
     */
    @Schema(description = "钱包余额", example = "100.50")
    private BigDecimal walletBalance;

    @Schema(description = "冻结金额", example = "10.00")
    private BigDecimal walletFrozen;

    @Schema(description = "钱包状态", example = "active")
    private String walletStatus;

    /**
     * 时间信息
     */
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