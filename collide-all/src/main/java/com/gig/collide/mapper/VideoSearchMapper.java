package com.gig.collide.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gig.collide.domain.Content;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 视频搜索Mapper接口 - 支持复杂的混合搜索查询
 * 
 * @author GIG Team
 * @version 2.0.0 (视频混合搜索版)
 * @since 2024-01-31
 */
public interface VideoSearchMapper extends BaseMapper<Content> {

    /**
     * 视频混合搜索 - 支持多种条件组合查询
     * 
     * @param page 分页参数
     * @param keyword 搜索关键词
     * @param contentType 内容类型
     * @param categoryId 分类ID
     * @param authorId 作者ID
     * @param tags 标签列表
     * @param priceType 价格类型
     * @param minPrice 最小价格
     * @param maxPrice 最大价格
     * @param vipOnly 是否VIP专享
     * @param trialEnabled 是否支持试读
     * @param timeRange 时间范围
     * @param customDays 自定义天数
     * @param sortBy 排序字段
     * @param sortDirection 排序方向
     * @param includeOffline 是否包含下线内容
     * @return 分页搜索结果
     */
    IPage<Map<String, Object>> searchVideosWithConditions(
            IPage<Map<String, Object>> page,
            @Param("keyword") String keyword,
            @Param("contentType") String contentType,
            @Param("categoryId") Long categoryId,
            @Param("authorId") Long authorId,
            @Param("tags") List<String> tags,
            @Param("priceType") String priceType,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("vipOnly") Boolean vipOnly,
            @Param("trialEnabled") Boolean trialEnabled,
            @Param("timeRange") String timeRange,
            @Param("customDays") Integer customDays,
            @Param("sortBy") String sortBy,
            @Param("sortDirection") String sortDirection,
            @Param("includeOffline") Boolean includeOffline
    );

    /**
     * 获取视频搜索建议
     * 
     * @param keyword 关键词前缀
     * @param limit 返回数量限制
     * @return 搜索建议列表
     */
    List<String> getVideoSearchSuggestions(
            @Param("keyword") String keyword,
            @Param("limit") Integer limit
    );

    /**
     * 获取热门视频标签
     * 
     * @param limit 返回数量限制
     * @return 热门标签列表
     */
    List<String> getHotVideoTags(@Param("limit") Integer limit);

    /**
     * 根据标签搜索视频
     * 
     * @param page 分页参数
     * @param tags 标签列表
     * @param contentType 内容类型
     * @param sortBy 排序字段
     * @param sortDirection 排序方向
     * @return 分页搜索结果
     */
    IPage<Map<String, Object>> searchVideosByTags(
            IPage<Map<String, Object>> page,
            @Param("tags") List<String> tags,
            @Param("contentType") String contentType,
            @Param("sortBy") String sortBy,
            @Param("sortDirection") String sortDirection
    );

    /**
     * 根据价格条件搜索视频
     * 
     * @param page 分页参数
     * @param priceType 价格类型
     * @param minPrice 最小价格
     * @param maxPrice 最大价格
     * @param vipOnly 是否VIP专享
     * @param trialEnabled 是否支持试读
     * @param contentType 内容类型
     * @param sortBy 排序字段
     * @param sortDirection 排序方向
     * @return 分页搜索结果
     */
    IPage<Map<String, Object>> searchVideosByPrice(
            IPage<Map<String, Object>> page,
            @Param("priceType") String priceType,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("vipOnly") Boolean vipOnly,
            @Param("trialEnabled") Boolean trialEnabled,
            @Param("contentType") String contentType,
            @Param("sortBy") String sortBy,
            @Param("sortDirection") String sortDirection
    );

    /**
     * 根据时间条件搜索视频
     * 
     * @param page 分页参数
     * @param timeRange 时间范围
     * @param customDays 自定义天数
     * @param contentType 内容类型
     * @param sortBy 排序字段
     * @param sortDirection 排序方向
     * @return 分页搜索结果
     */
    IPage<Map<String, Object>> searchVideosByTime(
            IPage<Map<String, Object>> page,
            @Param("timeRange") String timeRange,
            @Param("customDays") Integer customDays,
            @Param("contentType") String contentType,
            @Param("sortBy") String sortBy,
            @Param("sortDirection") String sortDirection
    );

    /**
     * 获取视频统计信息
     * 
     * @param contentIds 内容ID列表
     * @return 统计信息Map
     */
    List<Map<String, Object>> getVideoStatistics(@Param("contentIds") List<Long> contentIds);

    /**
     * 获取视频付费配置信息
     * 
     * @param contentIds 内容ID列表
     * @return 付费配置信息Map
     */
    List<Map<String, Object>> getVideoPaymentConfigs(@Param("contentIds") List<Long> contentIds);

    /**
     * 获取视频标签信息
     * 
     * @param contentIds 内容ID列表
     * @return 标签信息Map
     */
    List<Map<String, Object>> getVideoTags(@Param("contentIds") List<Long> contentIds);
}
