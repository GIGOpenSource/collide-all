package com.gig.collide.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gig.collide.Apientry.api.common.response.PageResponse;
import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.Apientry.api.search.request.SearchHistoryQueryRequest;
import com.gig.collide.Apientry.api.search.request.SearchRequest;
import com.gig.collide.Apientry.api.search.request.VideoSearchRequest;
import com.gig.collide.Apientry.api.search.response.HotSearchResponse;
import com.gig.collide.Apientry.api.search.response.SearchHistoryResponse;
import com.gig.collide.Apientry.api.search.response.SearchResponse;
import com.gig.collide.Apientry.api.search.response.VideoSearchResponse;
import com.gig.collide.domain.HotSearch;
import com.gig.collide.domain.SearchHistory;
import com.gig.collide.mapper.HotSearchMapper;
import com.gig.collide.mapper.SearchHistoryMapper;
import com.gig.collide.mapper.VideoSearchMapper;
import com.gig.collide.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 搜索服务实现类 - 简洁版
 *
 * @author GIG Team
 * @version 2.0.0
 */
@Slf4j
@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private SearchHistoryMapper searchHistoryMapper;

    @Autowired
    private HotSearchMapper hotSearchMapper;

    @Autowired
    private VideoSearchMapper videoSearchMapper;

    @Override
    @Transactional
    public Result<SearchResponse> search(SearchRequest request) {
        // 从SearchRequest中提取参数
        String keyword = request.getKeyword();
        String searchType = request.getSearchType();
        Long userId = request.getUserId();
        Integer pageNum = request.getCurrentPage();
        Integer pageSize = request.getPageSize();
        String sortBy = request.getSortBy();

        // 这里模拟搜索逻辑，实际应该调用不同模块的搜索接口
        List<Object> results = new ArrayList<>();

        try {
            switch (searchType) {
                case "content":
                    // 调用内容模块搜索
                    results = searchContent(keyword, pageNum, pageSize, sortBy);
                    break;
                case "goods":
                    // 调用商品模块搜索
                    results = searchGoods(keyword, pageNum, pageSize, sortBy);
                    break;
                case "user":
                    // 调用用户模块搜索
                    results = searchUsers(keyword, pageNum, pageSize, sortBy);
                    break;
                default:
                    log.warn("未知搜索类型: {}", searchType);
            }

            // 记录搜索历史
            recordSearchHistory(userId, keyword, searchType, results.size());

            // 更新热搜统计
            updateHotSearchStats(keyword);

            // 创建SearchResponse对象
            SearchResponse searchResponse = new SearchResponse();
            searchResponse.setKeyword(keyword);
            searchResponse.setSearchType(searchType);
            searchResponse.setTotalCount(results.size());
            searchResponse.setResults(convertToSearchResultItems(results, searchType));
            searchResponse.setHasMore(results.size() >= (pageSize != null ? pageSize : 20));
            searchResponse.setDuration(0L); // 暂时设置为0，实际应该计算搜索耗时

            // 返回成功结果
            return Result.success(searchResponse);

        } catch (Exception e) {
            log.error("搜索失败", e);
            return Result.error("搜索失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<Void> recordSearchHistory(Long userId, String keyword, String searchType, Integer resultCount) {
        try {
            // 检查是否已存在相同的搜索记录
            int count = searchHistoryMapper.countByUserIdAndKeyword(userId, keyword);
            if (count == 0) {
                SearchHistory history = new SearchHistory();
                history.setUserId(userId);
                history.setKeyword(keyword);
                history.setSearchType(searchType);
                history.setResultCount(resultCount);
                searchHistoryMapper.insert(history);
            }
            return Result.success(null);
        } catch (Exception e) {
            log.error("记录搜索历史失败", e);
            return Result.error("记录搜索历史失败: " + e.getMessage());
        }
    }

    @Override
    public Result<PageResponse<SearchHistoryResponse>> getSearchHistory(SearchHistoryQueryRequest request) {
        try {
            // 从请求中提取参数
            Long userId = request.getUserId();
            String searchType = request.getSearchType();
            String keyword = request.getKeyword();
            Integer currentPage = request.getCurrentPage();
            Integer pageSize = request.getPageSize();
            String sortBy = request.getSortBy();
            String sortDirection = request.getSortDirection();

            Page<SearchHistory> page = new Page<>(currentPage, pageSize);
            LambdaQueryWrapper<SearchHistory> queryWrapper = new LambdaQueryWrapper<>();

            queryWrapper.eq(SearchHistory::getUserId, userId);

            if (StringUtils.hasText(searchType)) {
                queryWrapper.eq(SearchHistory::getSearchType, searchType);
            }

            if (StringUtils.hasText(keyword)) {
                queryWrapper.like(SearchHistory::getKeyword, keyword);
            }

            // 设置排序
            if (StringUtils.hasText(sortBy)) {
                if ("asc".equalsIgnoreCase(sortDirection)) {
                    queryWrapper.orderByAsc(SearchHistory::getCreateTime);
                } else {
                    queryWrapper.orderByDesc(SearchHistory::getCreateTime);
                }
            } else {
                queryWrapper.orderByDesc(SearchHistory::getCreateTime);
            }

            IPage<SearchHistory> result = searchHistoryMapper.selectPage(page, queryWrapper);

            // 转换为PageResponse<SearchHistoryResponse>
            List<SearchHistoryResponse> responses = result.getRecords().stream().map(this::convertToSearchHistoryResponse).collect(Collectors.toList());
            
            // 安全地获取当前页码和页面大小
            int currentPageValue = 1;
            int pageSizeValue = 20;

            if (Objects.nonNull(page.getCurrent())) {
                currentPageValue = (int) page.getCurrent();
            }
            if (Objects.nonNull(page.getSize())) {
                pageSizeValue = (int) page.getSize();
            }
            
            PageResponse<SearchHistoryResponse> response = PageResponse.of(
                    responses, result.getTotal(), currentPageValue, pageSizeValue
            );

            return Result.success(response);
        } catch (Exception e) {
            log.error("获取搜索历史失败", e);
            return Result.error("获取搜索历史失败: " + e.getMessage());
        }
    }

    /**
     * 将SearchHistory转换为SearchHistoryResponse
     */
    private SearchHistoryResponse convertToSearchHistoryResponse(SearchHistory history) {
        SearchHistoryResponse response = new SearchHistoryResponse();
        response.setId(history.getId());
        response.setUserId(history.getUserId());
        response.setKeyword(history.getKeyword());
        response.setSearchType(history.getSearchType());
        response.setResultCount(history.getResultCount());
        response.setCreateTime(history.getCreateTime());
        return response;
    }

    @Override
    @Transactional
    public Result<Void> clearSearchHistory(Long userId) {
        try {
            LambdaQueryWrapper<SearchHistory> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SearchHistory::getUserId, userId);
            searchHistoryMapper.delete(queryWrapper);
            log.info("清空用户搜索历史: userId={}", userId);
            return Result.success(null);
        } catch (Exception e) {
            log.error("清空搜索历史失败", e);
            return Result.error("清空搜索历史失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<Void> deleteSearchHistory(Long historyId) {
        try {
            searchHistoryMapper.deleteById(historyId);
            log.info("删除搜索历史: historyId={}", historyId);
            return Result.success(null);
        } catch (Exception e) {
            log.error("删除搜索历史失败", e);
            return Result.error("删除搜索历史失败: " + e.getMessage());
        }
    }

    @Override
    public Result<List<HotSearchResponse>> getHotSearchKeywords(Integer limit) {
        try {
            LambdaQueryWrapper<HotSearch> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.orderByDesc(HotSearch::getSearchCount);
            queryWrapper.last("LIMIT " + (limit != null ? limit : 10));

            List<HotSearch> hotSearches = hotSearchMapper.selectList(queryWrapper);
            List<HotSearchResponse> responses = hotSearches.stream()
                    .map(this::convertToHotSearchResponse)
                    .collect(Collectors.toList());

            return Result.success(responses);
        } catch (Exception e) {
            log.error("获取热门搜索关键词失败", e);
            return Result.error("获取热门搜索关键词失败: " + e.getMessage());
        }
    }

    @Override
    public Result<List<String>> getSearchSuggestions(String keyword, Integer limit) {
        try {
            // 这里应该实现搜索建议逻辑
            // 可以从搜索历史、热门搜索等数据源获取建议
            List<String> suggestions = new ArrayList<>();

            // 从搜索历史获取建议
            LambdaQueryWrapper<SearchHistory> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.like(SearchHistory::getKeyword, keyword);
            queryWrapper.groupBy(SearchHistory::getKeyword);
            queryWrapper.orderByDesc(SearchHistory::getCreateTime);
            queryWrapper.last("LIMIT " + (limit != null ? limit : 5));

            List<SearchHistory> histories = searchHistoryMapper.selectList(queryWrapper);
            for (SearchHistory history : histories) {
                suggestions.add(history.getKeyword());
            }

            return Result.success(suggestions);
        } catch (Exception e) {
            log.error("获取搜索建议失败", e);
            return Result.error("获取搜索建议失败: " + e.getMessage());
        }
    }

    @Override
    public Result<List<HotSearchResponse>> getHotKeywordsByType(String searchType, Integer limit) {
        try {
            // HotSearch实体没有searchType字段，需要通过关联查询或使用Mapper的专用方法
            // 这里使用Mapper的专用方法
            List<HotSearch> hotSearches = hotSearchMapper.selectHotKeywordsByType(searchType, limit);
            List<HotSearchResponse> responses = hotSearches.stream()
                    .map(this::convertToHotSearchResponse)
                    .collect(Collectors.toList());

            return Result.success(responses);
        } catch (Exception e) {
            log.error("根据类型获取热门关键词失败", e);
            return Result.error("根据类型获取热门关键词失败: " + e.getMessage());
        }
    }

    @Override
    public Result<List<SearchHistoryResponse>> getUserSearchPreferences(Long userId) {
        try {
            LambdaQueryWrapper<SearchHistory> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SearchHistory::getUserId, userId);
            queryWrapper.groupBy(SearchHistory::getSearchType);
            queryWrapper.orderByDesc(SearchHistory::getCreateTime);

            List<SearchHistory> histories = searchHistoryMapper.selectList(queryWrapper);
            List<SearchHistoryResponse> responses = histories.stream()
                    .map(this::convertToSearchHistoryResponse)
                    .collect(Collectors.toList());

            return Result.success(responses);
        } catch (Exception e) {
            log.error("获取用户搜索偏好失败", e);
            return Result.error("获取用户搜索偏好失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<Void> updateHotSearchTrend(String keyword, Double trendScore) {
        try {
            LambdaQueryWrapper<HotSearch> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(HotSearch::getKeyword, keyword);

            HotSearch hotSearch = hotSearchMapper.selectOne(queryWrapper);
            if (hotSearch != null) {
                hotSearch.setTrendScore(BigDecimal.valueOf(trendScore));
                hotSearchMapper.updateById(hotSearch);
            } else {
                // 创建新的热搜记录
                hotSearch = new HotSearch();
                hotSearch.setKeyword(keyword);
                hotSearch.setSearchCount(1L);
                hotSearch.setTrendScore(BigDecimal.valueOf(trendScore));
                hotSearchMapper.insert(hotSearch);
            }
            return Result.success(null);
        } catch (Exception e) {
            log.error("更新热搜趋势失败", e);
            return Result.error("更新热搜趋势失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<Void> updateHotSearchStats(String keyword) {
        try {
            LambdaQueryWrapper<HotSearch> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(HotSearch::getKeyword, keyword);

            HotSearch hotSearch = hotSearchMapper.selectOne(queryWrapper);
            if (hotSearch != null) {
                hotSearch.setSearchCount(hotSearch.getSearchCount() + 1);
                hotSearchMapper.updateById(hotSearch);
            } else {
                // 创建新的热搜记录
                hotSearch = new HotSearch();
                hotSearch.setKeyword(keyword);
                hotSearch.setSearchCount(1L);
                hotSearch.setTrendScore(BigDecimal.valueOf(1.0));
                hotSearchMapper.insert(hotSearch);
            }
            return Result.success(null);
        } catch (Exception e) {
            log.error("更新热搜统计失败", e);
            return Result.error("更新热搜统计失败: " + e.getMessage());
        }
    }

    // =================== 私有方法 ===================

    /**
     * 搜索内容
     */
    private List<Object> searchContent(String keyword, Integer pageNum, Integer pageSize, String sortBy) {
        // 这里应该调用内容模块的搜索接口
        log.debug("搜索内容: keyword={}, page={}, size={}", keyword, pageNum, pageSize);
        return new ArrayList<>();
    }

    /**
     * 搜索商品
     */
    private List<Object> searchGoods(String keyword, Integer pageNum, Integer pageSize, String sortBy) {
        // 这里应该调用商品模块的搜索接口
        log.debug("搜索商品: keyword={}, page={}, size={}", keyword, pageNum, pageSize);
        return new ArrayList<>();
    }

    /**
     * 搜索用户
     */
    private List<Object> searchUsers(String keyword, Integer pageNum, Integer pageSize, String sortBy) {
        // 这里应该调用用户模块的搜索接口
        log.debug("搜索用户: keyword={}, page={}, size={}", keyword, pageNum, pageSize);
        return new ArrayList<>();
    }

    // =================== Tag搜索方法 ===================

    @Override
    public Result<PageResponse<Object>> searchByTagMixed(String tag, Integer currentPage, Integer pageSize) {
        log.debug("Tag混合搜索: tag={}, page={}, size={}", tag, currentPage, pageSize);

        // 这里应该实现Tag混合搜索逻辑
        // 同时搜索用户和内容，返回混合结果
        PageResponse<Object> response = PageResponse.of(
                new ArrayList<>(), 0L, currentPage != null ? currentPage : 1, pageSize != null ? pageSize : 20
        );

        return Result.success(response);
    }

    @Override
    public Result<PageResponse<Object>> searchUsersByTag(String tag, Integer currentPage, Integer pageSize) {
        log.debug("Tag搜索用户: tag={}, page={}, size={}", tag, currentPage, pageSize);

        // 这里应该实现根据Tag搜索用户的逻辑
        PageResponse<Object> response = PageResponse.of(
                new ArrayList<>(), 0L, currentPage != null ? currentPage : 1, pageSize != null ? pageSize : 20
        );

        return Result.success(response);
    }

    @Override
    public Result<PageResponse<Object>> searchContentsByTag(String tag, Integer currentPage, Integer pageSize) {
        log.debug("Tag搜索内容: tag={}, page={}, size={}", tag, currentPage, pageSize);

        // 这里应该实现根据Tag搜索内容的逻辑
        PageResponse<Object> response = PageResponse.of(
                new ArrayList<>(), 0L, currentPage != null ? currentPage : 1, pageSize != null ? pageSize : 20
        );

        return Result.success(response);
    }

    /**
     * 将搜索结果列表转换为SearchResponse中的results字段格式
     */
    private List<SearchResponse.SearchResultItem> convertToSearchResultItems(List<Object> results, String searchType) {
        List<SearchResponse.SearchResultItem> searchResultItems = new ArrayList<>();
        for (Object result : results) {
            SearchResponse.SearchResultItem item = new SearchResponse.SearchResultItem();
            if (result instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) result;
                item.setId((Long) map.get("id"));
                item.setTitle((String) map.get("title"));
                item.setContent((String) map.get("description"));
                item.setType((String) map.get("type")); // 例如 "content", "goods", "user"
                item.setThumbnail((String) map.get("imageUrl"));
                item.setScore(null); // 暂时设置为null，实际应该从搜索结果中获取
                item.setHighlight(null); // 暂时设置为null，实际应该从搜索结果中获取
            } else {
                // 处理非Map类型的结果，例如直接的String或Object
                item.setId(null); // 没有ID
                item.setTitle(result.toString());
                item.setContent(null);
                item.setType(searchType); // 假设所有非Map结果都属于同一类型
                item.setThumbnail(null);
                item.setScore(null);
                item.setHighlight(null);
            }
            searchResultItems.add(item);
        }
        return searchResultItems;
    }

    /**
     * 将HotSearch转换为HotSearchResponse
     */
    private HotSearchResponse convertToHotSearchResponse(HotSearch hotSearch) {
        HotSearchResponse response = new HotSearchResponse();
        response.setId(hotSearch.getId());
        response.setKeyword(hotSearch.getKeyword());
        response.setSearchCount(hotSearch.getSearchCount());
        response.setTrendScore(hotSearch.getTrendScore());
        response.setCreateTime(hotSearch.getCreateTime());
        return response;
    }

    // =================== 视频混合搜索功能 ===================

    @Override
    public Result<PageResponse<VideoSearchResponse>> searchVideos(VideoSearchRequest request) {
        log.info("执行视频混合搜索: {}", request);
        
        try {
            // 构建搜索条件
            VideoSearchCondition condition = buildVideoSearchCondition(request);
            
            // 执行搜索
            List<VideoSearchResponse> results = executeVideoSearch(condition);
            
            // 分页处理
            int total = results.size();
            int startIndex = (request.getCurrentPage() - 1) * request.getPageSize();
            int endIndex = Math.min(startIndex + request.getPageSize(), total);
            
            List<VideoSearchResponse> pagedResults = results.subList(startIndex, endIndex);
            
            // 构建分页响应
            PageResponse<VideoSearchResponse> pageResponse = new PageResponse<>();
            pageResponse.setRecords(pagedResults);
            pageResponse.setTotal((long) total);
            pageResponse.setCurrentPage(request.getCurrentPage());
            pageResponse.setPageSize(request.getPageSize());
            pageResponse.setTotalPages((int) Math.ceil((double) total / request.getPageSize()));
            
            log.info("视频混合搜索完成: 总数={}, 当前页={}, 页面大小={}", total, request.getCurrentPage(), request.getPageSize());
            
            return Result.success(pageResponse);
            
        } catch (Exception e) {
            log.error("视频混合搜索失败", e);
            return Result.error("视频搜索失败: " + e.getMessage());
        }
    }

    @Override
    public Result<List<String>> getVideoSearchSuggestions(String keyword, Integer limit) {
        log.debug("获取视频搜索建议: keyword={}, limit={}", keyword, limit);
        
        try {
            List<String> suggestions = new ArrayList<>();
            
            if (StringUtils.hasText(keyword)) {
                // 从数据库获取搜索建议
                suggestions = videoSearchMapper.getVideoSearchSuggestions(keyword, limit);
            }
            
            // 如果数据库没有返回足够的结果，添加一些默认建议
            if (suggestions.size() < limit) {
                if (StringUtils.hasText(keyword)) {
                    suggestions.add(keyword + " 电影");
                    suggestions.add(keyword + " 电视剧");
                    suggestions.add(keyword + " 纪录片");
                    suggestions.add(keyword + " 综艺");
                }
            }
            
            // 限制返回数量
            if (limit != null && limit > 0) {
                suggestions = suggestions.stream().limit(limit).collect(Collectors.toList());
            }
            
            return Result.success(suggestions);
            
        } catch (Exception e) {
            log.error("获取视频搜索建议失败", e);
            return Result.error("获取搜索建议失败: " + e.getMessage());
        }
    }

    @Override
    public Result<List<String>> getHotVideoTags(Integer limit) {
        log.debug("获取热门视频标签: limit={}", limit);
        
        try {
            List<String> hotTags = new ArrayList<>();
            
            // 从数据库获取热门标签
            hotTags = videoSearchMapper.getHotVideoTags(limit);
            
            // 如果数据库没有返回足够的结果，添加一些默认标签
            if (hotTags.size() < limit) {
                hotTags.add("动作");
                hotTags.add("喜剧");
                hotTags.add("爱情");
                hotTags.add("科幻");
                hotTags.add("恐怖");
                hotTags.add("悬疑");
                hotTags.add("纪录片");
                hotTags.add("综艺");
                hotTags.add("动画");
                hotTags.add("战争");
            }
            
            // 限制返回数量
            if (limit != null && limit > 0) {
                hotTags = hotTags.stream().limit(limit).collect(Collectors.toList());
            }
            
            return Result.success(hotTags);
            
        } catch (Exception e) {
            log.error("获取热门视频标签失败", e);
            return Result.error("获取热门标签失败: " + e.getMessage());
        }
    }

    /**
     * 视频搜索条件构建器
     */
    private static class VideoSearchCondition {
        String keyword;
        String contentType;
        Long categoryId;
        Long authorId;
        List<String> tags;
        String priceType;
        BigDecimal minPrice;
        BigDecimal maxPrice;
        Boolean vipOnly;
        Boolean trialEnabled;
        String timeRange;
        Integer customDays;
        String sortBy;
        String sortDirection;
        Boolean includeOffline;
    }

    /**
     * 构建视频搜索条件
     */
    private VideoSearchCondition buildVideoSearchCondition(VideoSearchRequest request) {
        VideoSearchCondition condition = new VideoSearchCondition();
        
        // 基础搜索条件
        condition.keyword = request.getKeyword();
        condition.contentType = request.getContentType();
        condition.categoryId = request.getCategoryId();
        condition.authorId = request.getAuthorId();
        condition.tags = request.getTags();
        condition.sortBy = request.getSortBy();
        condition.sortDirection = request.getSortDirection();
        condition.includeOffline = request.getIncludeOffline();
        
        // 价格搜索条件
        if (request.getPriceCondition() != null) {
            condition.priceType = request.getPriceCondition().getPriceType();
            condition.minPrice = request.getPriceCondition().getMinPrice();
            condition.maxPrice = request.getPriceCondition().getMaxPrice();
            condition.vipOnly = request.getPriceCondition().getVipOnly();
            condition.trialEnabled = request.getPriceCondition().getTrialEnabled();
        }
        
        // 时间搜索条件
        if (request.getTimeCondition() != null) {
            condition.timeRange = request.getTimeCondition().getTimeRange();
            condition.customDays = request.getTimeCondition().getCustomDays();
        }
        
        return condition;
    }

    /**
     * 执行视频搜索
     */
    private List<VideoSearchResponse> executeVideoSearch(VideoSearchCondition condition) {
        List<VideoSearchResponse> results = new ArrayList<>();
        
        try {
            // 创建分页对象
            Page<Map<String, Object>> page = new Page<>(1, 1000); // 临时使用大页面获取所有结果
            
            // 执行数据库查询
            IPage<Map<String, Object>> searchResults = videoSearchMapper.searchVideosWithConditions(
                    page,
                    condition.keyword,
                    condition.contentType,
                    condition.categoryId,
                    condition.authorId,
                    condition.tags,
                    condition.priceType,
                    condition.minPrice,
                    condition.maxPrice,
                    condition.vipOnly,
                    condition.trialEnabled,
                    condition.timeRange,
                    condition.customDays,
                    condition.sortBy,
                    condition.sortDirection,
                    condition.includeOffline
            );
            
            // 转换查询结果为VideoSearchResponse对象
            for (Map<String, Object> result : searchResults.getRecords()) {
                VideoSearchResponse video = convertMapToVideoSearchResponse(result);
                results.add(video);
            }
            
            log.debug("视频搜索完成，找到 {} 个结果", results.size());
            
        } catch (Exception e) {
            log.error("执行视频搜索失败", e);
            // 如果数据库查询失败，返回空结果而不是抛出异常
        }
        
        return results;
    }

    /**
     * 将Map转换为VideoSearchResponse对象
     */
    private VideoSearchResponse convertMapToVideoSearchResponse(Map<String, Object> result) {
        VideoSearchResponse video = new VideoSearchResponse();
        
        // 基础信息
        video.setContentId(getLongValue(result, "contentId"));
        video.setTitle(getStringValue(result, "title"));
        video.setDescription(getStringValue(result, "description"));
        video.setContentType(getStringValue(result, "contentType"));
        video.setStatus(getStringValue(result, "status"));
        video.setRelevanceScore(getDoubleValue(result, "relevanceScore"));
        
        // 作者信息
        VideoSearchResponse.AuthorInfo author = new VideoSearchResponse.AuthorInfo();
        author.setAuthorId(getLongValue(result, "authorId"));
        author.setAuthorNickname(getStringValue(result, "authorNickname"));
        author.setAuthorAvatar(getStringValue(result, "authorAvatar"));
        video.setAuthor(author);
        
        // 分类信息
        VideoSearchResponse.CategoryInfo category = new VideoSearchResponse.CategoryInfo();
        category.setCategoryId(getLongValue(result, "categoryId"));
        category.setCategoryName(getStringValue(result, "categoryName"));
        video.setCategory(category);
        
        // 标签信息
        String tagsStr = getStringValue(result, "tags");
        if (StringUtils.hasText(tagsStr)) {
            video.setTags(List.of(tagsStr.split(",")));
        }
        
        // 付费信息
        VideoSearchResponse.PaymentInfo payment = new VideoSearchResponse.PaymentInfo();
        payment.setPaymentType(getStringValue(result, "paymentType"));
        payment.setPrice(getBigDecimalValue(result, "price"));
        payment.setOriginalPrice(getBigDecimalValue(result, "originalPrice"));
        payment.setVipFree(getBooleanValue(result, "vipFree"));
        payment.setVipOnly(getBooleanValue(result, "vipOnly"));
        payment.setTrialEnabled(getBooleanValue(result, "trialEnabled"));
        payment.setTrialDuration(getIntegerValue(result, "trialDuration"));
        payment.setValidityDays(getIntegerValue(result, "validityDays"));
        payment.setPermanent(getBooleanValue(result, "permanent"));
        video.setPayment(payment);
        
        // 统计信息
        VideoSearchResponse.StatisticsInfo stats = new VideoSearchResponse.StatisticsInfo();
        stats.setViewCount(getLongValue(result, "viewCount"));
        stats.setLikeCount(getLongValue(result, "likeCount"));
        stats.setFavoriteCount(getLongValue(result, "favoriteCount"));
        stats.setCommentCount(getLongValue(result, "commentCount"));
        stats.setShareCount(getLongValue(result, "shareCount"));
        stats.setScore(getDoubleValue(result, "score"));
        stats.setScoreCount(getLongValue(result, "scoreCount"));
        stats.setSalesCount(getLongValue(result, "salesCount"));
        stats.setRevenue(getBigDecimalValue(result, "revenue"));
        video.setStatistics(stats);
        
        // 时间信息
        VideoSearchResponse.TimeInfo timeInfo = new VideoSearchResponse.TimeInfo();
        timeInfo.setCreateTime(getLocalDateTimeValue(result, "createTime"));
        timeInfo.setPublishTime(getLocalDateTimeValue(result, "publishTime"));
        timeInfo.setUpdateTime(getLocalDateTimeValue(result, "updateTime"));
        video.setTimeInfo(timeInfo);
        
        return video;
    }

    /**
     * 安全的获取Map中的值
     */
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return value != null ? Long.valueOf(value.toString()) : null;
    }

    private Integer getIntegerValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return value != null ? Integer.valueOf(value.toString()) : null;
    }

    private Double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return value != null ? Double.valueOf(value.toString()) : null;
    }

    private Boolean getBooleanValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue() == 1;
        }
        return value != null ? Boolean.valueOf(value.toString()) : null;
    }

    private BigDecimal getBigDecimalValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return new BigDecimal(value.toString());
        }
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    private LocalDateTime getLocalDateTimeValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }
        if (value instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) value).toLocalDateTime();
        }
        return value != null ? LocalDateTime.parse(value.toString()) : null;
    }

    /**
     * 排序视频搜索结果
     */
    private void sortVideoResults(List<VideoSearchResponse> results, String sortBy, String sortDirection) {
        results.sort((a, b) -> {
            int comparison = 0;
            
            switch (sortBy) {
                case "hot":
                    comparison = Long.compare(
                        a.getStatistics() != null ? a.getStatistics().getViewCount() : 0,
                        b.getStatistics() != null ? b.getStatistics().getViewCount() : 0
                    );
                    break;
                case "favorite":
                    comparison = Long.compare(
                        a.getStatistics() != null ? a.getStatistics().getFavoriteCount() : 0,
                        b.getStatistics() != null ? b.getStatistics().getFavoriteCount() : 0
                    );
                    break;
                case "latest":
                    comparison = a.getTimeInfo() != null && b.getTimeInfo() != null ?
                        a.getTimeInfo().getCreateTime().compareTo(b.getTimeInfo().getCreateTime()) : 0;
                    break;
                case "relevance":
                default:
                    comparison = Double.compare(
                        a.getRelevanceScore() != null ? a.getRelevanceScore() : 0.0,
                        b.getRelevanceScore() != null ? b.getRelevanceScore() : 0.0
                    );
                    break;
            }
            
            return "DESC".equals(sortDirection) ? -comparison : comparison;
        });
    }
} 
