package com.gig.collide.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户任务记录实体类 - 简洁版
 * 对应表：t_user_task_record
 * 支持用户任务完成记录
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
@TableName("t_user_task_record")
public class UserTaskRecord {

    /**
     * 记录ID
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
    @TableField("task_id")
    private Long taskId;

    /**
     * 任务类型：daily、weekly、achievement
     */
    @TableField("task_type")
    private String taskType;

    /**
     * 任务分类：login、content、social、consume
     */
    @TableField("task_category")
    private String taskCategory;

    /**
     * 任务动作：login、publish_content、like、comment、share、purchase
     */
    @TableField("task_action")
    private String taskAction;

    /**
     * 目标完成次数
     */
    @TableField("target_count")
    private Integer targetCount;

    /**
     * 当前完成次数
     */
    @TableField("current_count")
    private Integer currentCount;

    /**
     * 是否已完成
     */
    @TableField("is_completed")
    private Boolean isCompleted;

    /**
     * 是否已领取奖励
     */
    @TableField("is_rewarded")
    private Boolean isRewarded;

    /**
     * 任务日期（用于每日/每周任务）
     */
    @TableField("task_date")
    private LocalDate taskDate;

    /**
     * 完成时间
     */
    @TableField("complete_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completeTime;

    /**
     * 奖励领取时间
     */
    @TableField("reward_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rewardTime;

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
     * 判断任务是否进行中
     */
    public boolean isInProgress() {
        return !Boolean.TRUE.equals(this.isCompleted);
    }

    /**
     * 判断任务是否已完成
     */
    public boolean isCompleted() {
        return Boolean.TRUE.equals(this.isCompleted);
    }

    /**
     * 判断奖励是否已领取
     */
    public boolean isRewarded() {
        return Boolean.TRUE.equals(this.isRewarded);
    }

    /**
     * 判断任务是否可完成
     */
    public boolean canComplete() {
        return isInProgress() && this.currentCount != null && this.targetCount != null 
            && this.currentCount >= this.targetCount;
    }

    /**
     * 获取完成进度百分比
     */
    public int getProgressPercentage() {
        if (this.targetCount == null || this.targetCount <= 0) {
            return 0;
        }
        if (this.currentCount == null) {
            return 0;
        }
        return Math.min(100, (this.currentCount * 100) / this.targetCount);
    }

    /**
     * 增加完成次数
     */
    public UserTaskRecord incrementCount() {
        this.currentCount = (this.currentCount != null ? this.currentCount : 0) + 1;
        return this;
    }

    /**
     * 设置完成次数
     */
    public UserTaskRecord setCurrentCount(Integer count) {
        this.currentCount = count != null ? count : 0;
        return this;
    }

    /**
     * 完成任务
     */
    public UserTaskRecord complete() {
        if (canComplete()) {
            this.isCompleted = true;
            this.completeTime = LocalDateTime.now();
        }
        return this;
    }

    /**
     * 重置任务进度
     */
    public UserTaskRecord reset() {
        this.currentCount = 0;
        this.isCompleted = false;
        this.isRewarded = false;
        this.completeTime = null;
        this.rewardTime = null;
        return this;
    }

    /**
     * 获取任务显示名称
     */
    public String getDisplayName() {
        return this.taskName != null && !this.taskName.trim().isEmpty() 
            ? this.taskName 
            : "任务";
    }

    /**
     * 创建用户任务记录
     */
    public static UserTaskRecord create(Long userId, Long taskId, String taskType, 
                                      String taskCategory, String taskAction, Integer targetCount, LocalDate taskDate) {
        return new UserTaskRecord()
            .setUserId(userId)
            .setTaskId(taskId)
            .setTaskType(taskType)
            .setTaskCategory(taskCategory)
            .setTaskAction(taskAction)
            .setTargetCount(targetCount)
            .setCurrentCount(0)
            .setIsCompleted(false)
            .setIsRewarded(false)
            .setTaskDate(taskDate);
    }
}
