package com.gig.collide.controller;


import com.gig.collide.Apientry.api.common.response.PageResponse;
import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.Apientry.api.search.request.SearchHistoryQueryRequest;
import com.gig.collide.Apientry.api.search.request.SearchRequest;
import com.gig.collide.Apientry.api.search.request.VideoSearchRequest;
import com.gig.collide.Apientry.api.search.response.HotSearchResponse;
import com.gig.collide.Apientry.api.search.response.SearchHistoryResponse;
import com.gig.collide.Apientry.api.search.response.SearchResponse;
import com.gig.collide.Apientry.api.search.response.VideoSearchResponse;
import com.gig.collide.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 搜索控制器 - 缓存增强版
 * 对齐social模块设计风格，提供完整的搜索服务
 * 支持智能搜索、搜索历史、热门搜索、混合搜索等功能
 *
 * @author GIG Team
 * @version 2.0.0 (缓存增强版)
 * @since 2024-01-16
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Tag(name = "搜索服务", description = "提供智能搜索、搜索历史、热门搜索、Tag混合搜索等功能")
public class SearchController {

    private final SearchService searchService;

    // =================== 搜索核心功能 ===================

    /**
     * 执行搜索
     */
    @PostMapping
    @Operation(summary = "执行搜索", description = "根据关键词和搜索类型执行搜索，支持分页和排序")
    public Result<SearchResponse> search(@Valid @RequestBody SearchRequest request) {
        log.info("REST - 执行搜索：关键词={}, 类型={}", request.getKeyword(), request.getSearchType());
        return searchService.search(request);
    }

    // =================== 搜索历史管理 ===================

    /**
     * 获取搜索历史
     */
    @GetMapping("/history")
    @Operation(summary = "获取搜索历史", description = "分页获取用户的搜索历史记录")
    public Result<PageResponse<SearchHistoryResponse>> getSearchHistory(@Valid SearchHistoryQueryRequest request) {
        log.debug("REST - 获取搜索历史：用户={}, 页码={}", request.getUserId(), request.getCurrentPage());
        return searchService.getSearchHistory(request);
    }

    /**
     * 清空搜索历史
     */
    @DeleteMapping("/history/user/{userId}")
    @Operation(summary = "清空搜索历史", description = "清空指定用户的所有搜索历史记录")
    public Result<Void> clearSearchHistory(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        log.info("REST - 清空搜索历史：用户={}", userId);
        return searchService.clearSearchHistory(userId);
    }

    /**
     * 删除指定搜索历史
     */
    @DeleteMapping("/history/{historyId}")
    @Operation(summary = "删除搜索历史", description = "删除指定的搜索历史记录")
    public Result<Void> deleteSearchHistory(
            @Parameter(description = "搜索历史ID") @PathVariable Long historyId) {
        log.info("REST - 删除搜索历史：ID={}", historyId);
        return searchService.deleteSearchHistory(historyId);
    }

    // =================== 热门搜索功能 ===================

    /**
     * 获取热门搜索
     */
    @GetMapping("/hot")
    @Operation(summary = "获取热门搜索", description = "获取当前热门搜索关键词列表")
    public Result<List<HotSearchResponse>> getHotSearchKeywords(
            @Parameter(description = "返回数量限制") @RequestParam(defaultValue = "10") Integer limit) {
        log.debug("REST - 获取热门搜索：限制={}", limit);
        return searchService.getHotSearchKeywords(limit);
    }

    /**
     * 根据类型获取热搜
     */
    @GetMapping("/hot/type/{searchType}")
    @Operation(summary = "按类型获取热搜", description = "根据搜索类型获取热门关键词")
    public Result<List<HotSearchResponse>> getHotKeywordsByType(
            @Parameter(description = "搜索类型") @PathVariable String searchType,
            @Parameter(description = "返回数量限制") @RequestParam(defaultValue = "10") Integer limit) {
        log.debug("REST - 按类型获取热搜：类型={}, 限制={}", searchType, limit);
        return searchService.getHotKeywordsByType(searchType, limit);
    }

    // =================== 搜索建议功能 ===================

    /**
     * 获取搜索建议
     */
    @GetMapping("/suggestions")
    @Operation(summary = "获取搜索建议", description = "根据输入关键词获取搜索建议和自动补全")
    public Result<List<String>> getSearchSuggestions(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "返回数量限制") @RequestParam(defaultValue = "10") Integer limit) {
        log.debug("REST - 获取搜索建议：关键词={}, 限制={}", keyword, limit);
        return searchService.getSearchSuggestions(keyword, limit);
    }

    // =================== 用户偏好分析 ===================

    /**
     * 获取用户搜索偏好
     */
    @GetMapping("/preferences/{userId}")
    @Operation(summary = "获取用户搜索偏好", description = "分析并获取用户的搜索偏好和习惯")
    public Result<List<SearchHistoryResponse>> getUserSearchPreferences(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        log.debug("REST - 获取用户搜索偏好：用户={}", userId);
        return searchService.getUserSearchPreferences(userId);
    }

    // =================== 管理功能 ===================

    /**
     * 更新热搜趋势分数
     */
    @PutMapping("/hot/trend")
    @Operation(summary = "更新热搜趋势", description = "更新热搜关键词的趋势分数（管理员功能）")
    public Result<Void> updateHotSearchTrend(
            @Parameter(description = "关键词") @RequestParam String keyword,
            @Parameter(description = "趋势分数") @RequestParam Double trendScore) {
        log.info("REST - 更新热搜趋势：关键词={}, 分数={}", keyword, trendScore);
        return searchService.updateHotSearchTrend(keyword, trendScore);
    }

    // =================== 混合搜索功能 ===================

    /**
     * Tag混合搜索
     */
    @GetMapping("/tag/{tag}/mixed")
    @Operation(summary = "Tag混合搜索", description = "根据标签同时搜索用户和内容，返回聚合结果")
    public Result<PageResponse<Object>> searchByTagMixed(
            @Parameter(description = "标签名称") @PathVariable String tag,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("REST - Tag混合搜索：标签={}, 页码={}, 大小={}", tag, currentPage, pageSize);
        return searchService.searchByTagMixed(tag, currentPage, pageSize);
    }

    /**
     * 根据Tag搜索用户
     */
    @GetMapping("/tag/{tag}/users")
    @Operation(summary = "Tag搜索用户", description = "根据标签搜索相关用户")
    public Result<PageResponse<Object>> searchUsersByTag(
            @Parameter(description = "标签名称") @PathVariable String tag,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("REST - Tag搜索用户：标签={}, 页码={}, 大小={}", tag, currentPage, pageSize);
        return searchService.searchUsersByTag(tag, currentPage, pageSize);
    }

    /**
     * 根据Tag搜索内容
     */
    @GetMapping("/tag/{tag}/contents")
    @Operation(summary = "Tag搜索内容", description = "根据标签搜索相关内容")
    public Result<PageResponse<Object>> searchContentsByTag(
            @Parameter(description = "标签名称") @PathVariable String tag,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("REST - Tag搜索内容：标签={}, 页码={}, 大小={}", tag, currentPage, pageSize);
        return searchService.searchContentsByTag(tag, currentPage, pageSize);
    }

    // =================== 视频混合搜索功能 ===================

    /**
     * 视频混合搜索
     */
    @PostMapping("/videos")
    @Operation(summary = "视频混合搜索", description = "支持多种搜索条件组合：类型搜索、排序搜索、标签搜索、价格搜索、时间搜索")
    public Result<PageResponse<VideoSearchResponse>> searchVideos(@Valid @RequestBody VideoSearchRequest request) {
        log.info("REST - 视频混合搜索：关键词={}, 类型={}, 排序={}, 标签={}, 价格类型={}, 时间范围={}", 
                request.getKeyword(), request.getContentType(), request.getSortBy(), 
                request.getTags(), request.getPriceCondition() != null ? request.getPriceCondition().getPriceType() : "ALL",
                request.getTimeCondition() != null ? request.getTimeCondition().getTimeRange() : "THIS_MONTH");
        return searchService.searchVideos(request);
    }

    /**
     * 获取视频搜索建议
     */
    @GetMapping("/videos/suggestions")
    @Operation(summary = "获取视频搜索建议", description = "根据输入关键词获取视频搜索建议和自动补全")
    public Result<List<String>> getVideoSearchSuggestions(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "返回数量限制") @RequestParam(defaultValue = "10") Integer limit) {
        log.debug("REST - 获取视频搜索建议：关键词={}, 限制={}", keyword, limit);
        return searchService.getVideoSearchSuggestions(keyword, limit);
    }

    /**
     * 获取热门视频标签
     */
    @GetMapping("/videos/hot-tags")
    @Operation(summary = "获取热门视频标签", description = "获取当前热门视频标签列表")
    public Result<List<String>> getHotVideoTags(
            @Parameter(description = "返回数量限制") @RequestParam(defaultValue = "20") Integer limit) {
        log.debug("REST - 获取热门视频标签：限制={}", limit);
        return searchService.getHotVideoTags(limit);
    }
} 