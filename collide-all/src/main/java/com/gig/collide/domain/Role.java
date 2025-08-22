package com.gig.collide.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 角色实体类 - 简洁版
 * 对应表：t_role
 * 支持用户角色管理
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
@TableName("t_role")
public class Role {

    /**
     * 角色ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 角色名称 (user, blogger, admin, vip)
     */
    @TableField("name")
    private String name;

    /**
     * 角色描述
     */
    @TableField("description")
    private String description;

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
     * 判断是否为普通用户角色
     */
    public boolean isUserRole() {
        return "user".equals(this.name);
    }

    /**
     * 判断是否为博主角色
     */
    public boolean isBloggerRole() {
        return "blogger".equals(this.name);
    }

    /**
     * 判断是否为管理员角色
     */
    public boolean isAdminRole() {
        return "admin".equals(this.name);
    }

    /**
     * 判断是否为VIP角色
     */
    public boolean isVipRole() {
        return "vip".equals(this.name);
    }

    /**
     * 获取角色显示名称
     */
    public String getDisplayName() {
        if (this.description != null && !this.description.trim().isEmpty()) {
            return this.description;
        }
        return this.name != null ? this.name.toUpperCase() : "UNKNOWN";
    }

    /**
     * 获取角色权限级别
     */
    public int getPermissionLevel() {
        if (this.name == null) {
            return 0;
        }
        switch (this.name.toLowerCase()) {
            case "user":
                return 1;
            case "vip":
                return 2;
            case "blogger":
                return 3;
            case "admin":
                return 4;
            default:
                return 0;
        }
    }

    /**
     * 检查是否有权限执行操作
     */
    public boolean hasPermission(Role otherRole) {
        if (otherRole == null) {
            return false;
        }
        return this.getPermissionLevel() >= otherRole.getPermissionLevel();
    }
}
