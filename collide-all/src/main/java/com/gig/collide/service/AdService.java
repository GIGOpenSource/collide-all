package com.gig.collide.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gig.collide.domain.Ad;
import com.gig.collide.Apientry.api.common.response.PageResponse;
import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.Apientry.api.ads.response.AdResponse;

import java.util.List;

/**
 * 广告业务服务接口 - 极简版
 * 只包含核心的广告管理功能
 *
 * @author GIG Team
 * @version 3.0.0 (极简版)
 * @since 2024-01-16
 */
public interface AdService {

    // =================== 基础 CRUD 操作 ===================

    /**
     * 创建广告
     */
    Ad createAd(Ad ad);

    /**
     * 根据ID获取广告
     */
    Ad getAdById(Long id);

    /**
     * 更新广告
     */
    boolean updateAd(Ad ad);

    /**
     * 删除广告
     */
    boolean deleteAd(Long id);

    /**
     * 分页查询广告
     */
    Page<Ad> queryAds(String adName, String adType, Integer isActive, Integer currentPage, Integer pageSize);

    // =================== 核心查询功能 ===================

    /**
     * 根据广告类型查询启用的广告列表（按权重排序）
     */
    List<Ad> getAdsByType(String adType);

    /**
     * 随机获取指定类型的单个广告
     */
    Ad getRandomAdByType(String adType);

    /**
     * 随机获取指定类型的多个广告
     */
    List<Ad> getRandomAdsByType(String adType, Integer limit);

    /**
     * 获取所有启用的广告类型
     */
    List<String> getAllAdTypes();

    /**
     * 按权重获取推荐广告
     */
    List<Ad> getTopRecommendedAds(Integer limit);

    /**
     * 获取所有启用的广告
     */
    List<Ad> getAllEnabledAds();

    // =================== 状态管理 ===================

    /**
     * 启用广告
     */
    boolean enableAd(Long id);

    /**
     * 禁用广告
     */
    boolean disableAd(Long id);

    /**
     * 批量更新广告状态
     */
    boolean batchUpdateAdStatus(List<Long> ids, Integer status);

    // =================== 权重管理 ===================

    /**
     * 更新广告权重
     */
    boolean updateAdSortOrder(Long id, Integer sortOrder);

    /**
     * 批量更新广告权重
     */
    boolean batchUpdateAdSortOrder(List<Long> ids, List<Integer> sortOrders);

    // =================== Controller专用方法 ===================

    /**
     * 广告列表查询（Controller专用）
     * 支持多种条件查询和分页，返回Result包装的分页响应
     *
     * @param id             广告ID
     * @param adType         广告类型
     * @param status         广告状态
     * @param position       广告位置
     * @param keyword        关键词搜索
     * @param isValid        是否有效
     * @param orderBy        排序字段
     * @param orderDirection 排序方向
     * @param currentPage    当前页码
     * @param pageSize       页面大小
     * @return 分页响应结果
     */
    Result<PageResponse<AdResponse>> listAdsForController(
            Long id, String adType, String status, String position, String keyword, Integer isValid,
            String orderBy, String orderDirection, Integer currentPage, Integer pageSize);
}
