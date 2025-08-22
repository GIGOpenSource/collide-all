package com.gig.collide.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 用户拉黑关系实体类 - 简洁版
 * 对应表：t_user_block
 * 支持用户拉黑功能
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
@TableName("t_user_block")
public class UserBlock {

    /**
     * 拉黑记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 拉黑者用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 被拉黑用户ID
     */
    @TableField("blocked_user_id")
    private Long blockedUserId;

    /**
     * 拉黑者用户名
     */
    @TableField("user_username")
    private String userUsername;

    /**
     * 被拉黑用户名
     */
    @TableField("blocked_username")
    private String blockedUsername;

    /**
     * 拉黑状态：active、cancelled
     */
    @TableField("status")
    private String status;

    /**
     * 拉黑原因
     */
    @TableField("reason")
    private String reason;

    /**
     * 拉黑时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // =================== 扩展字段（用于查询结果，不映射到数据库） ===================

    /**
     * 拉黑者昵称（非数据库字段）
     */
    @TableField(exist = false)
    private String userNickname;

    @TableField(exist = false)
    private String userAvatar;

    /**
     * 被拉黑者昵称（非数据库字段）
     */
    @TableField(exist = false)
    private String blockedUserNickname;

    @TableField(exist = false)
    private String blockedUserAvatar;

    // =================== 业务方法 ===================

    /**
     * 判断是否为活跃拉黑状态
     */
    public boolean isActive() {
        return "active".equals(this.status);
    }

    /**
     * 判断是否已取消拉黑
     */
    public boolean isCancelled() {
        return "cancelled".equals(this.status);
    }

    /**
     * 创建拉黑关系
     */
    public static UserBlock create(Long userId, Long blockedUserId, String userUsername, String blockedUsername, String reason) {
        return new UserBlock()
            .setUserId(userId)
            .setBlockedUserId(blockedUserId)
            .setUserUsername(userUsername)
            .setBlockedUsername(blockedUsername)
            .setStatus("active")
            .setReason(reason);
    }

    /**
     * 取消拉黑
     */
    public UserBlock cancel() {
        this.status = "cancelled";
        return this;
    }

    /**
     * 重新拉黑
     */
    public UserBlock reactivate() {
        this.status = "active";
        return this;
    }

    /**
     * 检查是否为有效拉黑关系
     */
    public boolean isValid() {
        return this.userId != null && this.userId > 0 
            && this.blockedUserId != null && this.blockedUserId > 0
            && !this.userId.equals(this.blockedUserId);
    }

    /**
     * 获取拉黑关系标识
     */
    public String getBlockKey() {
        return this.userId + "_" + this.blockedUserId;
    }
}
