package com.gig.collide.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 任务模板实体类 - 简洁版
 * 对应表：t_task_template
 * 支持任务系统管理
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
@TableName("t_task_template")
public class TaskTemplate {

    /**
     * 任务模板ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 任务名称
     */
    @TableField("task_name")
    private String taskName;

    /**
     * 任务描述
     */
    @TableField("task_desc")
    private String taskDesc;

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
     * 排序值
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 是否启用
     */
    @TableField("is_active")
    private Boolean isActive;

    /**
     * 任务开始日期
     */
    @TableField("start_date")
    private LocalDate startDate;

    /**
     * 任务结束日期
     */
    @TableField("end_date")
    private LocalDate endDate;

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
     * 判断是否为每日任务
     */
    public boolean isDailyTask() {
        return "daily".equals(this.taskType);
    }

    /**
     * 判断是否为每周任务
     */
    public boolean isWeeklyTask() {
        return "weekly".equals(this.taskType);
    }

    /**
     * 判断是否为成就任务
     */
    public boolean isAchievementTask() {
        return "achievement".equals(this.taskType);
    }

    /**
     * 判断任务是否在有效期内
     */
    public boolean isInValidPeriod() {
        LocalDate now = LocalDate.now();
        
        if (this.startDate != null && now.isBefore(this.startDate)) {
            return false;
        }
        
        if (this.endDate != null && now.isAfter(this.endDate)) {
            return false;
        }
        
        return true;
    }

    /**
     * 启用任务
     */
    public TaskTemplate enable() {
        this.isActive = true;
        return this;
    }

    /**
     * 禁用任务
     */
    public TaskTemplate disable() {
        this.isActive = false;
        return this;
    }

    /**
     * 设置排序值
     */
    public TaskTemplate setSortOrder(Integer sortOrder) {
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
     * 获取目标完成次数（默认1）
     */
    public Integer getTargetCount() {
        return this.targetCount != null ? this.targetCount : 1;
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
     * 创建任务模板
     */
    public static TaskTemplate create(String taskName, String taskDesc, String taskType, 
                                    String taskCategory, String taskAction, Integer targetCount) {
        return new TaskTemplate()
            .setTaskName(taskName)
            .setTaskDesc(taskDesc)
            .setTaskType(taskType)
            .setTaskCategory(taskCategory)
            .setTaskAction(taskAction)
            .setTargetCount(targetCount)
            .setSortOrder(0)
            .setIsActive(true);
    }
}
