package com.gig.collide.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户奖励记录实体类 - 简洁版
 * 对应表：t_user_reward_record
 * 支持用户任务奖励记录
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
@TableName("t_user_reward_record")
public class UserRewardRecord {

    /**
     * 奖励记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 任务模板ID
     */
    @TableField("task_template_id")
    private Long taskTemplateId;

    /**
     * 任务记录ID
     */
    @TableField("user_task_record_id")
    private Long userTaskRecordId;

    /**
     * 奖励类型：coin、cash、experience、item
     */
    @TableField("reward_type")
    private String rewardType;

    /**
     * 奖励数量
     */
    @TableField("reward_amount")
    private BigDecimal rewardAmount;

    /**
     * 奖励描述
     */
    @TableField("reward_desc")
    private String rewardDesc;

    /**
     * 奖励状态：pending、claimed、expired
     */
    @TableField("status")
    private String status;

    /**
     * 领取时间
     */
    @TableField("claimed_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime claimedTime;

    /**
     * 过期时间
     */
    @TableField("expire_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

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

    // =================== 扩展字段（用于查询结果，不映射到数据库） ===================

    /**
     * 任务模板信息（非数据库字段）
     */
    @TableField(exist = false)
    private String taskName;

    @TableField(exist = false)
    private String taskDesc;

    /**
     * 用户信息（非数据库字段）
     */
    @TableField(exist = false)
    private String username;

    @TableField(exist = false)
    private String nickname;

    // =================== 业务方法 ===================

    /**
     * 判断奖励是否待领取
     */
    public boolean isPending() {
        return "pending".equals(this.status);
    }

    /**
     * 判断奖励是否已领取
     */
    public boolean isClaimed() {
        return "claimed".equals(this.status);
    }

    /**
     * 判断奖励是否已过期
     */
    public boolean isExpired() {
        return "expired".equals(this.status);
    }

    /**
     * 判断奖励是否可以领取
     */
    public boolean canClaim() {
        return isPending() && (this.expireTime == null || this.expireTime.isAfter(LocalDateTime.now()));
    }

    /**
     * 判断奖励是否已过期
     */
    public boolean isExpiredByTime() {
        return this.expireTime != null && this.expireTime.isBefore(LocalDateTime.now());
    }

    /**
     * 领取奖励
     */
    public UserRewardRecord claim() {
        if (canClaim()) {
            this.status = "claimed";
            this.claimedTime = LocalDateTime.now();
        }
        return this;
    }

    /**
     * 标记奖励过期
     */
    public UserRewardRecord markAsExpired() {
        this.status = "expired";
        return this;
    }

    /**
     * 检查并更新过期状态
     */
    public UserRewardRecord checkAndUpdateExpiredStatus() {
        if (isPending() && isExpiredByTime()) {
            this.status = "expired";
        }
        return this;
    }

    /**
     * 获取奖励显示名称
     */
    public String getDisplayName() {
        if (this.rewardDesc != null && !this.rewardDesc.trim().isEmpty()) {
            return this.rewardDesc;
        }
        
        if (this.rewardType != null) {
            switch (this.rewardType.toLowerCase()) {
                case "coin":
                    return "金币奖励";
                case "cash":
                    return "现金奖励";
                case "experience":
                    return "经验奖励";
                case "item":
                    return "道具奖励";
                default:
                    return "任务奖励";
            }
        }
        
        return "任务奖励";
    }

    /**
     * 获取奖励状态描述
     */
    public String getStatusDescription() {
        if (isExpired()) {
            return "已过期";
        }
        if (isClaimed()) {
            return "已领取";
        }
        if (isExpiredByTime()) {
            return "已过期";
        }
        return "待领取";
    }

    /**
     * 创建用户奖励记录
     */
    public static UserRewardRecord create(Long userId, Long taskTemplateId, Long userTaskRecordId, 
                                        String rewardType, BigDecimal rewardAmount, String rewardDesc) {
        return new UserRewardRecord()
            .setUserId(userId)
            .setTaskTemplateId(taskTemplateId)
            .setUserTaskRecordId(userTaskRecordId)
            .setRewardType(rewardType)
            .setRewardAmount(rewardAmount)
            .setRewardDesc(rewardDesc)
            .setStatus("pending");
    }
}
