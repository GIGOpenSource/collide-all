package com.gig.collide.Apientry.api.search.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 热门搜索响应 - 简洁版
 * 基于t_hot_search表结构
 * 
 * @author GIG Team
 * @version 2.0.0
 */
@Data
public class HotSearchResponse implements Serializable {

    private Long id;

    private String keyword;

    private Long searchCount;

    private BigDecimal trendScore;

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

    /**
     * 排名位置
     */
    private Integer rank;
} 