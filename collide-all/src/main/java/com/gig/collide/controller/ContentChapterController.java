package com.gig.collide.controller;

import com.gig.collide.Apientry.api.common.response.PageResponse;
import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.Apientry.api.content.ContentChapterFacadeService;
import com.gig.collide.Apientry.api.content.response.ChapterResponse;
import com.gig.collide.service.ContentChapterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 内容章节控制器 - 极简版
 * 基于8个核心方法设计的精简API
 * 
 * @author GIG Team
 * @version 2.0.0 (极简版)
 * @since 2024-01-31
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/content/chapters")
@RequiredArgsConstructor
@Validated
@Tag(name = "内容章节管理", description = "内容章节的查询、统计和管理接口（极简版）")
public class ContentChapterController {

    private final ContentChapterService contentChapterService;

    /**
     * 章节列表查询
     * 支持多种条件查询和分页
     */
    @GetMapping("/list")
    @Operation(summary = "章节列表查询", description = "支持按内容、状态、章节号等条件查询章节列表")
    public Result<PageResponse<ChapterResponse>> listChapters(
            @Parameter(description = "内容ID") @RequestParam(required = false) Long contentId,
            @Parameter(description = "章节状态") @RequestParam(required = false) String status,
            @Parameter(description = "章节类型") @RequestParam(required = false) String chapterType,
            @Parameter(description = "关键词搜索") @RequestParam(required = false) String keyword,
            @Parameter(description = "是否免费") @RequestParam(required = false) Boolean isFree,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "chapterNumber") String orderBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "ASC") String orderDirection,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("REST请求 - 章节列表查询: contentId={}, status={}, type={}, page={}/{}", 
                contentId, status, chapterType, currentPage, pageSize);
        return contentChapterService.listChaptersForController(contentId, status, chapterType, keyword, isFree,
                orderBy, orderDirection, currentPage, pageSize);
    }
}