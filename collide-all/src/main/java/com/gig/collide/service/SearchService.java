package com.gig.collide.service;



import com.gig.collide.Apientry.api.common.response.PageResponse;
import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.Apientry.api.search.request.SearchHistoryQueryRequest;
import com.gig.collide.Apientry.api.search.request.SearchRequest;
import com.gig.collide.Apientry.api.search.request.VideoSearchRequest;
import com.gig.collide.Apientry.api.search.response.HotSearchResponse;
import com.gig.collide.Apientry.api.search.response.SearchHistoryResponse;
import com.gig.collide.Apientry.api.search.response.SearchResponse;
import com.gig.collide.Apientry.api.search.response.VideoSearchResponse;

import java.util.List;

/**
 * 搜索服务接口 - 简洁版
 *
 * @author GIG Team
 * @version 2.0.0
 */
public interface SearchService {

    /**
     * 执行搜索并记录历史
     */
    Result<SearchResponse> search(SearchRequest request);

    /**
     * 记录搜索历史
     */
    Result<Void> recordSearchHistory(Long userId, String keyword, String searchType, Integer resultCount);

    /**
     * 获取用户搜索历史 - 支持SearchHistoryQueryRequest
     */
    Result<PageResponse<SearchHistoryResponse>> getSearchHistory(SearchHistoryQueryRequest request);

    /**
     * 清空用户搜索历史
     */
    Result<Void> clearSearchHistory(Long userId);

    /**
     * 删除指定搜索历史
     */
    Result<Void> deleteSearchHistory(Long historyId);

    /**
     * 获取热门搜索关键词
     */
    Result<List<HotSearchResponse>> getHotSearchKeywords(Integer limit);

    /**
     * 获取搜索建议
     */
    Result<List<String>> getSearchSuggestions(String keyword, Integer limit);

    /**
     * 根据搜索类型获取热门关键词
     */
    Result<List<HotSearchResponse>> getHotKeywordsByType(String searchType, Integer limit);

    /**
     * 获取用户搜索偏好
     */
    Result<List<SearchHistoryResponse>> getUserSearchPreferences(Long userId);

    /**
     * 更新热搜趋势分数
     */
    Result<Void> updateHotSearchTrend(String keyword, Double trendScore);

    /**
     * 更新热搜统计
     */
    Result<Void> updateHotSearchStats(String keyword);

    /**
     * Tag混合搜索 - 同时搜索用户和内容
     */
    Result<PageResponse<Object>> searchByTagMixed(String tag, Integer currentPage, Integer pageSize);

    /**
     * 根据Tag搜索用户
     */
    Result<PageResponse<Object>> searchUsersByTag(String tag, Integer currentPage, Integer pageSize);

    /**
     * 根据Tag搜索内容
     */
    Result<PageResponse<Object>> searchContentsByTag(String tag, Integer currentPage, Integer pageSize);

    // =================== 视频混合搜索功能 ===================

    /**
     * 视频混合搜索 - 支持多种搜索条件组合
     * 包括：类型搜索、排序搜索、标签搜索、价格搜索、时间搜索
     */
    Result<PageResponse<VideoSearchResponse>> searchVideos(VideoSearchRequest request);

    /**
     * 获取视频搜索建议
     */
    Result<List<String>> getVideoSearchSuggestions(String keyword, Integer limit);

    /**
     * 获取热门视频标签
     */
    Result<List<String>> getHotVideoTags(Integer limit);
} 