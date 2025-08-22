package com.gig.collide.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * VIP特权文案实体类 - 简洁版
 * 对应表：t_vip_power
 * 支持VIP特权管理
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
@TableName("t_vip_power")
public class VipPower {

    /**
     * 策略ID（序号）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 特权文案名称
     */
    @TableField("power_name")
    private String powerName;

    /**
     * 附件（字符串格式存储）
     */
    @TableField("attachment")
    private String attachment;

    /**
     * 所属VIP名称
     */
    @TableField("vip_name")
    private String vipName;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 优先级（数值越大优先级越高）
     */
    @TableField("priority")
    private Integer priority;

    /**
     * 状态：active-启用、inactive-禁用
     */
    @TableField("status")
    private String status;

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
     * 判断是否为启用状态
     */
    public boolean isActive() {
        return "active".equals(this.status);
    }

    /**
     * 判断是否为禁用状态
     */
    public boolean isInactive() {
        return "inactive".equals(this.status);
    }

    /**
     * 启用特权
     */
    public VipPower enable() {
        this.status = "active";
        return this;
    }

    /**
     * 禁用特权
     */
    public VipPower disable() {
        this.status = "inactive";
        return this;
    }

    /**
     * 设置优先级
     */
    public VipPower setPriority(Integer priority) {
        this.priority = priority != null ? priority : 0;
        return this;
    }

    /**
     * 获取优先级（默认0）
     */
    public Integer getPriority() {
        return this.priority != null ? this.priority : 0;
    }

    /**
     * 检查是否有附件
     */
    public boolean hasAttachment() {
        return this.attachment != null && !this.attachment.trim().isEmpty();
    }

    /**
     * 获取特权显示名称
     */
    public String getDisplayName() {
        return this.powerName != null && !this.powerName.trim().isEmpty() 
            ? this.powerName 
            : "VIP特权";
    }

    /**
     * 创建VIP特权
     */
    public static VipPower create(String powerName, String vipName, String remark, Integer priority) {
        return new VipPower()
            .setPowerName(powerName)
            .setVipName(vipName)
            .setRemark(remark)
            .setPriority(priority)
            .setStatus("active");
    }
}
