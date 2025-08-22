package com.gig.collide.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 内容通知实体类 - 简洁版
 * 对应表：t_inform
 * 支持系统通知管理
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
@TableName("t_inform")
public class Inform {

    /**
     * 通知ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 所属APP名称
     */
    @TableField("app_name")
    private String appName;

    /**
     * 类型关系
     */
    @TableField("type_relation")
    private String typeRelation;

    /**
     * 用户类型
     */
    @TableField("user_type")
    private String userType;

    /**
     * 通知内容
     */
    @TableField("notification_content")
    private String notificationContent;

    /**
     * 发送时间
     */
    @TableField("send_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sendTime;

    /**
     * 是否删除：N-未删除，Y-已删除
     */
    @TableField("is_deleted")
    private String isDeleted;

    /**
     * 是否已发送：N-未发送，Y-已发送
     */
    @TableField("is_sent")
    private String isSent;

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
     * 判断是否已删除
     */
    public boolean isDeleted() {
        return "Y".equals(this.isDeleted);
    }

    /**
     * 判断是否未删除
     */
    public boolean isNotDeleted() {
        return "N".equals(this.isDeleted);
    }

    /**
     * 判断是否已发送
     */
    public boolean isSent() {
        return "Y".equals(this.isSent);
    }

    /**
     * 判断是否未发送
     */
    public boolean isNotSent() {
        return "N".equals(this.isSent);
    }

    /**
     * 标记为已发送
     */
    public Inform markAsSent() {
        this.isSent = "Y";
        this.sendTime = LocalDateTime.now();
        return this;
    }

    /**
     * 标记为未发送
     */
    public Inform markAsNotSent() {
        this.isSent = "N";
        this.sendTime = null;
        return this;
    }

    /**
     * 标记为已删除
     */
    public Inform markAsDeleted() {
        this.isDeleted = "Y";
        return this;
    }

    /**
     * 标记为未删除
     */
    public Inform markAsNotDeleted() {
        this.isDeleted = "N";
        return this;
    }

    /**
     * 检查是否可以发送
     */
    public boolean canSend() {
        return isNotDeleted() && isNotSent();
    }

    /**
     * 获取通知状态描述
     */
    public String getStatusDescription() {
        if (isDeleted()) {
            return "已删除";
        }
        if (isSent()) {
            return "已发送";
        }
        return "未发送";
    }

    /**
     * 创建通知
     */
    public static Inform create(String appName, String typeRelation, String userType, String notificationContent) {
        return new Inform()
            .setAppName(appName)
            .setTypeRelation(typeRelation)
            .setUserType(userType)
            .setNotificationContent(notificationContent)
            .setIsDeleted("N")
            .setIsSent("N");
    }
}
