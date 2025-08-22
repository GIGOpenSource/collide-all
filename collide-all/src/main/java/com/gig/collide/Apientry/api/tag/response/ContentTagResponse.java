package com.gig.collide.Apientry.api.tag.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 内容标签响应
 *
 * @author GIG Team
 * @version 3.0.0
 */
@Data
public class ContentTagResponse {

    /**
     * 记录ID
     */
    private Long id;

    /**
     * 内容ID
     */
    private Long contentId;

    /**
     * 标签ID
     */
    private Long tagId;

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 标签类型
     */
    private String tagType;

    /**
     * 标签使用次数
     */
    private Long tagUsageCount;

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