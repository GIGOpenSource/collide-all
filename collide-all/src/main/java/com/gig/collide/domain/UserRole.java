package com.gig.collide.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 用户角色关联实体类 - 简洁版
 * 对应表：t_user_role
 * 支持用户与角色的多对多关系
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
@TableName("t_user_role")
public class UserRole {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 角色ID
     */
    @TableField("role_id")
    private Integer roleId;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // =================== 扩展字段（用于查询结果，不映射到数据库） ===================

    /**
     * 用户信息（非数据库字段）
     */
    @TableField(exist = false)
    private String username;

    @TableField(exist = false)
    private String nickname;

    @TableField(exist = false)
    private String avatar;

    /**
     * 角色信息（非数据库字段）
     */
    @TableField(exist = false)
    private String roleName;

    @TableField(exist = false)
    private String roleDescription;

    // =================== 业务方法 ===================

    /**
     * 创建用户角色关联
     */
    public static UserRole create(Long userId, Integer roleId) {
        return new UserRole()
            .setUserId(userId)
            .setRoleId(roleId);
    }

    /**
     * 检查是否为有效关联
     */
    public boolean isValid() {
        return this.userId != null && this.userId > 0 
            && this.roleId != null && this.roleId > 0;
    }

    /**
     * 获取关联标识
     */
    public String getAssociationKey() {
        return this.userId + "_" + this.roleId;
    }
}
