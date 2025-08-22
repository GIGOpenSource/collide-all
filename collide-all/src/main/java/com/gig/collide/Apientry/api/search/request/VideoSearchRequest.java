package com.gig.collide.Apientry.api.search.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 视频混合搜索请求 - 支持多种搜索条件组合
 * 
 * @author GIG Team
 * @version 2.0.0 (视频混合搜索版)
 * @since 2024-01-31
 */
@Data
public class VideoSearchRequest implements Serializable {

    /**
     * 搜索关键词（标题、描述、标签）
     */
    @Size(max = 200, message = "搜索关键词长度不能超过200字符")
    private String keyword;

    /**
     * 内容类型：VIDEO（视频）、NOVEL（小说）、COMIC（漫画）、ARTICLE（文章）、AUDIO（音频）
     * 不传递或传递null表示搜索所有类型
     */
    @Pattern(regexp = "^(VIDEO|NOVEL|COMIC|ARTICLE|AUDIO|ALL)?$", message = "内容类型只能是VIDEO、NOVEL、COMIC、ARTICLE、ALL或为空")
    private String contentType;

    /**
     * 分类ID
     */
    @Min(value = 1, message = "分类ID必须大于0")
    private Long categoryId;

    /**
     * 作者ID
     */
    @Min(value = 1, message = "作者ID必须大于0")
    private Long authorId;

    /**
     * 标签列表（支持多个标签）
     */
    private List<String> tags;

    /**
     * 价格搜索条件
     */
    private PriceSearchCondition priceCondition;

    /**
     * 时间搜索条件
     */
    private TimeSearchCondition timeCondition;

    /**
     * 排序方式
     */
    @Pattern(regexp = "^(hot|favorite|latest|relevance|random)$", message = "排序方式只能是hot、favorite、latest、relevance或random")
    private String sortBy = "hot";

    /**
     * 排序方向
     */
    @Pattern(regexp = "^(ASC|DESC)$", message = "排序方向只能是ASC或DESC")
    private String sortDirection = "DESC";

    /**
     * 分页参数
     */
    @Min(value = 1, message = "页码必须大于0")
    private Integer currentPage = 1;

    @Min(value = 1, message = "页面大小必须大于0")
    private Integer pageSize = 20;

    /**
     * 是否包含下线内容
     */
    private Boolean includeOffline = false;

    /**
     * 价格搜索条件内部类
     */
    @Data
    public static class PriceSearchCondition implements Serializable {
        /**
         * 价格类型：ALL（全类型）、FREE（免费）、VIP（VIP免费）、PAID（付费解锁）
         */
        @Pattern(regexp = "^(ALL|FREE|VIP|PAID)$", message = "价格类型只能是ALL、FREE、VIP或PAID")
        private String priceType = "ALL";

        /**
         * 最小价格
         */
        private BigDecimal minPrice;

        /**
         * 最大价格
         */
        private BigDecimal maxPrice;

        /**
         * 是否只显示VIP专享内容
         */
        private Boolean vipOnly = false;

        /**
         * 是否只显示支持试读的内容
         */
        private Boolean trialEnabled = false;
    }

    /**
     * 时间搜索条件内部类
     */
    @Data
    public static class TimeSearchCondition implements Serializable {
        /**
         * 时间范围：THIS_WEEK（本周上架）、THIS_MONTH（本月上架）、HALF_YEAR（半年上架）、CUSTOM（自定义）
         */
        @Pattern(regexp = "^(THIS_WEEK|THIS_MONTH|HALF_YEAR|CUSTOM)$", message = "时间范围只能是THIS_WEEK、THIS_MONTH、HALF_YEAR或CUSTOM")
        private String timeRange = "THIS_MONTH";

        /**
         * 自定义天数（当timeRange为CUSTOM时使用）
         */
        @Min(value = 1, message = "自定义天数必须大于0")
        private Integer customDays;
    }
}
