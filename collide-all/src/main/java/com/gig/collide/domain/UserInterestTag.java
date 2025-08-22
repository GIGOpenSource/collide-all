package com.gig.collide.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户兴趣标签实体 - 简洁版
 * 对应t_user_interest_tag表
 *
 * @author GIG Team
 * @version 2.0.0
 */
@Data
@TableName("t_user_interest_tag")
public class UserInterestTag {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 标签ID
     */
    private Long tagId;

    /**
     * 兴趣分数（0-100）
     */
    private BigDecimal interestScore;

    /**
     * 状态：active、inactive
     */
    private String status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // =================== 扩展字段（用于查询结果） ===================

    /**
     * 标签名称（非数据库字段）
     */
    @TableField(exist = false)
    private String tagName;

    /**
     * 标签描述（非数据库字段）
     */
    @TableField(exist = false)
    private String tagDescription;
} 