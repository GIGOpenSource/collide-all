package com.gig.collide.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 任务奖励配置实体类 - 简洁版
 * 对应表：t_task_reward
 * 支持任务奖励管理
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
@TableName("t_task_reward")
public class TaskReward {

    /**
     * 奖励配置ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 任务模板ID
     */
    @TableField("task_template_id")
    private Long taskTemplateId;

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
     * 是否启用
     */
    @TableField("is_active")
    private Boolean isActive;

    /**
     * 排序值
     */
    @TableField("sort_order")
    private Integer sortOrder;

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

    // =================== 扩展字段（用于查询结果，不映射到数据库） ===================

    /**
     * 任务模板信息（非数据库字段）
     */
    @TableField(exist = false)
    private String taskName;

    @TableField(exist = false)
    private String taskDesc;

    @TableField(exist = false)
    private String taskType;

    // =================== 业务方法 ===================

    /**
     * 判断是否为启用状态
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(this.isActive);
    }

    /**
     * 判断是否为禁用状态
     */
    public boolean isInactive() {
        return Boolean.FALSE.equals(this.isActive);
    }

    /**
     * 判断是否为金币奖励
     */
    public boolean isCoinReward() {
        return "coin".equals(this.rewardType);
    }

    /**
     * 判断是否为现金奖励
     */
    public boolean isCashReward() {
        return "cash".equals(this.rewardType);
    }

    /**
     * 判断是否为经验奖励
     */
    public boolean isExperienceReward() {
        return "experience".equals(this.rewardType);
    }

    /**
     * 判断是否为道具奖励
     */
    public boolean isItemReward() {
        return "item".equals(this.rewardType);
    }

    /**
     * 启用奖励
     */
    public TaskReward enable() {
        this.isActive = true;
        return this;
    }

    /**
     * 禁用奖励
     */
    public TaskReward disable() {
        this.isActive = false;
        return this;
    }

    /**
     * 设置排序值
     */
    public TaskReward setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder != null ? sortOrder : 0;
        return this;
    }

    /**
     * 获取排序值（默认0）
     */
    public Integer getSortOrder() {
        return this.sortOrder != null ? this.sortOrder : 0;
    }

    /**
     * 获取奖励数量（默认0）
     */
    public BigDecimal getRewardAmount() {
        return this.rewardAmount != null ? this.rewardAmount : BigDecimal.ZERO;
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
                    return "金币";
                case "cash":
                    return "现金";
                case "experience":
                    return "经验";
                case "item":
                    return "道具";
                default:
                    return "奖励";
            }
        }
        
        return "奖励";
    }

    /**
     * 创建任务奖励
     */
    public static TaskReward create(Long taskTemplateId, String rewardType, 
                                  BigDecimal rewardAmount, String rewardDesc) {
        return new TaskReward()
            .setTaskTemplateId(taskTemplateId)
            .setRewardType(rewardType)
            .setRewardAmount(rewardAmount)
            .setRewardDesc(rewardDesc)
            .setSortOrder(0)
            .setIsActive(true);
    }
}
