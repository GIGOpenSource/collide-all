package com.gig.collide.service.Impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gig.collide.Apientry.api.common.response.PageResponse;
import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.Apientry.api.content.response.ChapterResponse;
import com.gig.collide.domain.ContentChapter;
import com.gig.collide.mapper.ContentChapterMapper;
import com.gig.collide.service.ContentChapterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 内容章节业务服务实现
 * 极简�?- 8个核心方法，使用通用查询
 *
 * @author GIG Team
 * @version 2.0.0 (内容付费�?
 * @since 2024-01-31
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class ContentChapterServiceImpl implements ContentChapterService {

    private final ContentChapterMapper contentChapterMapper;

    // =================== 核心CRUD功能�?个方法）===================

    @Override
    public ContentChapter createChapter(ContentChapter chapter) {
        log.info("创建章节: contentId={}, title={}", chapter.getContentId(), chapter.getTitle());

        // 基础验证
        if (chapter.getContentId() == null || !StringUtils.hasText(chapter.getTitle())) {
            throw new IllegalArgumentException("内容ID和章节标题不能为�?");
        }

        // 设置默认�?
        if (chapter.getCreateTime() == null) {
            chapter.setCreateTime(LocalDateTime.now());
        }
        if (chapter.getUpdateTime() == null) {
            chapter.setUpdateTime(LocalDateTime.now());
        }
        if (!StringUtils.hasText(chapter.getStatus())) {
            chapter.setStatus("DRAFT");
        }

        contentChapterMapper.insert(chapter);
        log.info("章节创建成功: id={}", chapter.getId());
        return chapter;
    }

    @Override
    public ContentChapter updateChapter(ContentChapter chapter) {
        log.info("更新章节: id={}", chapter.getId());

        if (chapter.getId() == null) {
            throw new IllegalArgumentException("章节ID不能为空");
        }

        chapter.setUpdateTime(LocalDateTime.now());
        contentChapterMapper.updateById(chapter);

        log.info("章节更新成功: id={}", chapter.getId());
        return chapter;
    }

    @Override
    public ContentChapter getChapterById(Long id) {
        log.debug("获取章节详情: id={}", id);

        if (id == null) {
            throw new IllegalArgumentException("章节ID不能为空");
        }

        return contentChapterMapper.selectById(id);
    }

    @Override
    public boolean deleteChapter(Long id) {
        log.info("软删除章�? id={}", id);

        if (id == null) {
            throw new IllegalArgumentException("章节ID不能为空");
        }

        try {
            int result = contentChapterMapper.softDeleteChapter(id);
            boolean success = result > 0;
            if (success) {
                log.info("章节软删除成�? id={}", id);
            }
            return success;
        } catch (Exception e) {
            log.error("章节软删除失�? id={}", id, e);
            return false;
        }
    }

    // =================== 万能查询功能�?个方法）===================

    @Override
    public List<ContentChapter> getChaptersByConditions(Long contentId, String status,
                                                        Integer chapterNumStart, Integer chapterNumEnd,
                                                        Integer minWordCount, Integer maxWordCount,
                                                        String orderBy, String orderDirection,
                                                        Integer currentPage, Integer pageSize) {
        log.debug("万能条件查询章节: contentId={}, status={}", contentId, status);

        return contentChapterMapper.selectChaptersByConditions(
                contentId, status, chapterNumStart, chapterNumEnd,
                minWordCount, maxWordCount, orderBy, orderDirection,
                currentPage, pageSize
        );
    }

    @Override
    public ContentChapter getChapterByNavigation(Long contentId, Integer currentChapterNum, String direction) {
        log.debug("章节导航查询: contentId={}, currentChapterNum={}, direction={}",
                contentId, currentChapterNum, direction);

        if (contentId == null) {
            throw new IllegalArgumentException("内容ID不能为空");
        }

        return contentChapterMapper.selectChapterByNavigation(contentId, currentChapterNum, direction);
    }

    // =================== 批量操作功能�?个方法）===================

    @Override
    public boolean batchUpdateChapterStatus(List<Long> ids, String status) {
        log.info("批量更新章节状�? ids.size={}, status={}",
                ids != null ? ids.size() : 0, status);

        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("章节ID列表不能为空");
        }
        if (!StringUtils.hasText(status)) {
            throw new IllegalArgumentException("状态不能为�?");
        }

        try {
            int result = contentChapterMapper.batchUpdateChapterStatus(ids, status);
            boolean success = result > 0;
            if (success) {
                log.info("批量更新章节状态成�? 影响行数={}", result);
            }
            return success;
        } catch (Exception e) {
            log.error("批量更新章节状态失�?", e);
            return false;
        }
    }

    @Override
    public boolean batchDeleteChapters(List<Long> ids) {
        log.info("批量软删除章�? ids.size={}", ids != null ? ids.size() : 0);

        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("章节ID列表不能为空");
        }

        try {
            int result = contentChapterMapper.batchSoftDeleteChapters(ids);
            boolean success = result > 0;
            if (success) {
                log.info("批量软删除章节成�? 影响行数={}", result);
            }
            return success;
        } catch (Exception e) {
            log.error("批量软删除章节失�?, e");
            return false;
        }
    }

    // =================== Controller专用方法 ===================

    @Override
    public Result<PageResponse<ChapterResponse>> listChaptersForController(
            Long contentId, String status, String chapterType, String keyword, Boolean isFree,
            String orderBy, String orderDirection, Integer currentPage, Integer pageSize) {
        try {
            log.info("Controller层 - 章节列表查询: contentId={}, status={}, chapterType={}, page={}/{}", 
                    contentId, status, chapterType, currentPage, pageSize);

            // 参数验证和默认值设置
            if (currentPage == null || currentPage < 1) {
                currentPage = 1;
            }
            if (pageSize == null || pageSize < 1 || pageSize > 100) {
                pageSize = 20;
            }
            if (orderBy == null || orderBy.trim().isEmpty()) {
                orderBy = "chapterNumber";
            }
            if (orderDirection == null || orderDirection.trim().isEmpty()) {
                orderDirection = "ASC";
            }

            // 构建查询条件
            LambdaQueryWrapper<ContentChapter> queryWrapper = new LambdaQueryWrapper<>();

            // 动态查询条件
            if (contentId != null) {
                queryWrapper.eq(ContentChapter::getContentId, contentId);
            }
            if (StringUtils.hasText(status)) {
                queryWrapper.eq(ContentChapter::getStatus, status);
            }
            // chapterType 字段在当前 ContentChapter 实体中不存在，暂时注释掉
            // if (StringUtils.hasText(chapterType)) {
            //     queryWrapper.eq(ContentChapter::getChapterType, chapterType);
            // }
            if (StringUtils.hasText(keyword)) {
                queryWrapper.like(ContentChapter::getTitle, keyword);
            }
            // isFree 字段在当前 ContentChapter 实体中不存在，暂时注释掉
            // if (isFree != null) {
            //     queryWrapper.eq(ContentChapter::getIsFree, isFree);
            // }

            // 排序
            if ("title".equals(orderBy)) {
                if ("ASC".equals(orderDirection)) {
                    queryWrapper.orderByAsc(ContentChapter::getTitle);
                } else {
                    queryWrapper.orderByDesc(ContentChapter::getTitle);
                }
            } else if ("createTime".equals(orderBy)) {
                if ("ASC".equals(orderDirection)) {
                    queryWrapper.orderByAsc(ContentChapter::getCreateTime);
                } else {
                    queryWrapper.orderByDesc(ContentChapter::getCreateTime);
                }
            } else if ("updateTime".equals(orderBy)) {
                if ("ASC".equals(orderDirection)) {
                    queryWrapper.orderByAsc(ContentChapter::getUpdateTime);
                } else {
                    queryWrapper.orderByDesc(ContentChapter::getUpdateTime);
                }
            } else {
                // 默认按章节号排序
                if ("ASC".equals(orderDirection)) {
                    queryWrapper.orderByAsc(ContentChapter::getChapterNum);
                } else {
                    queryWrapper.orderByDesc(ContentChapter::getChapterNum);
                }
            }

            // 使用MyBatis-Plus分页
            Page<ContentChapter> page = new Page<>(currentPage, pageSize);
            IPage<ContentChapter> result = contentChapterMapper.selectPage(page, queryWrapper);

            // 转换为Response对象
            List<ChapterResponse> responses = result.getRecords().stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            // 构建分页响应
            PageResponse<ChapterResponse> pageResponse = new PageResponse<>();
            pageResponse.setRecords(responses);
            pageResponse.setTotal(result.getTotal());
            pageResponse.setCurrentPage(currentPage);
            pageResponse.setPageSize(pageSize);
            pageResponse.setTotalPages((int) Math.ceil((double) result.getTotal() / pageSize));

            log.info("章节列表查询成功: 总数={}, 当前页={}, 页面大小={}", result.getTotal(), currentPage, pageSize);
            return Result.success(pageResponse);

        } catch (Exception e) {
            log.error("Controller层 - 章节列表查询失败", e);
            return Result.error("章节列表查询失败: " + e.getMessage());
        }
    }

    /**
     * 将ContentChapter实体转换为ChapterResponse
     */
    private ChapterResponse convertToResponse(ContentChapter chapter) {
        if (chapter == null) {
            return null;
        }

        ChapterResponse response = new ChapterResponse();
        response.setId(chapter.getId());
        response.setContentId(chapter.getContentId());
        response.setTitle(chapter.getTitle());
        response.setChapterNum(chapter.getChapterNum());

        // chapterType 和 isFree 字段在当前 ChapterResponse 中不存在，暂时注释掉
        // response.setChapterType(null);
        response.setContent(chapter.getContent());
        response.setWordCount(chapter.getWordCount());
        // response.setIsFree(null);
        response.setStatus(chapter.getStatus());
        response.setCreateTime(chapter.getCreateTime());
        response.setUpdateTime(chapter.getUpdateTime());

        return response;
    }
}
