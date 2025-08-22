package com.gig.collide.service;


import com.gig.collide.domain.ContentChapter;

import java.util.List;

/**
 * 内容章节业务服务接口
 * 极简版 - 8个核心方法，使用通用查询
 *
 * @author GIG Team
 * @version 2.0.0 (内容付费版)
 * @since 2024-01-31
 */
public interface ContentChapterService {

    // =================== 核心CRUD功能（4个方法）===================

    /**
     * 创建章节
     */
    ContentChapter createChapter(ContentChapter chapter);

    /**
     * 更新章节
     */
    ContentChapter updateChapter(ContentChapter chapter);

    /**
     * 根据ID获取章节
     */
    ContentChapter getChapterById(Long id);

    /**
     * 软删除章节
     */
    boolean deleteChapter(Long id);

    // =================== 万能查询功能（2个方法）===================

    /**
     * 万能条件查询章节列表 - 替代所有具体查询
     * 可实现：getChaptersByContentId, getPublishedChapters, getChaptersByWordCount等
     */
    List<ContentChapter> getChaptersByConditions(Long contentId, String status,
                                                 Integer chapterNumStart, Integer chapterNumEnd,
                                                 Integer minWordCount, Integer maxWordCount,
                                                 String orderBy, String orderDirection,
                                                 Integer currentPage, Integer pageSize);

    /**
     * 章节导航查询（next、previous、first、last）
     */
    ContentChapter getChapterByNavigation(Long contentId, Integer currentChapterNum, String direction);

    // =================== 批量操作功能（2个方法）===================

    /**
     * 批量更新章节状态
     */
    boolean batchUpdateChapterStatus(List<Long> ids, String status);

    /**
     * 批量软删除章节
     */
    boolean batchDeleteChapters(List<Long> ids);

    // =================== Controller专用方法 ===================

    /**
     * 章节列表查询（Controller专用）
     * 支持多种条件查询和分页，返回Result包装的分页响应
     *
     * @param contentId 内容ID
     * @param status 章节状态
     * @param chapterType 章节类型
     * @param keyword 关键词搜索
     * @param isFree 是否免费
     * @param orderBy 排序字段
     * @param orderDirection 排序方向
     * @param currentPage 当前页码
     * @param pageSize 页面大小
     * @return 分页响应结果
     */
    com.gig.collide.Apientry.api.common.response.Result<com.gig.collide.Apientry.api.common.response.PageResponse<com.gig.collide.Apientry.api.content.response.ChapterResponse>> listChaptersForController(
            Long contentId, String status, String chapterType, String keyword, Boolean isFree,
            String orderBy, String orderDirection, Integer currentPage, Integer pageSize);
}