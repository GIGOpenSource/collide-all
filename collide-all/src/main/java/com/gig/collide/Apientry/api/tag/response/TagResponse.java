package com.gig.collide.Apientry.api.tag.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签响应
 *
 * @author GIG Team
 * @version 3.0.0
 */
@Data
public class TagResponse {

    /**
     * 标签ID
     */
    private Long id;

    /**
     * 标签名称
     */
    private String name;

    /**
     * 标签描述
     */
    private String description;

    /**
     * 标签类型：content、interest、system
     */
    private String tagType;

    /**
     * 所属分类ID
     */
    private Long categoryId;

    /**
     * 使用次数
     */
    private Long usageCount;

    /**
     * 状态：active、inactive
     */
    private String status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}