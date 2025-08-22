package com.gig.collide.controller;


import com.gig.collide.Apientry.api.common.response.PageResponse;
import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.Apientry.api.tag.TagFacadeService;
import com.gig.collide.Apientry.api.tag.request.TagCreateRequest;
import com.gig.collide.Apientry.api.tag.request.TagQueryRequest;
import com.gig.collide.Apientry.api.tag.response.TagResponse;
import com.gig.collide.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.gig.collide.Apientry.api.tag.request.TagUpdateRequest;

import java.util.List;
import java.util.Map;

/**
 * 标签管理控制器
 * 负责基础标签的增删改查和管理功能
 *
 * @author GIG Team
 * @version 3.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
@Validated
@Tag(name = "标签管理", description = "基础标签的增删改查和管理功能")
public class TagController {

    private final TagService tagService;

    /**
     * 标签列表查询
     * 支持多种条件查询和分页
     */
    @GetMapping("/list")
    @Operation(summary = "标签列表查询", description = "支持按类型、状态、使用频率等条件查询标签列表")
    public Result<PageResponse<TagResponse>> listTags(
            @Parameter(description = "标签类型") @RequestParam(required = false) String tagType,
            @Parameter(description = "标签状态") @RequestParam(required = false) String status,
            @Parameter(description = "关键词搜索") @RequestParam(required = false) String keyword,
            @Parameter(description = "是否热门") @RequestParam(required = false) Boolean isHot,
            @Parameter(description = "最小使用次数") @RequestParam(required = false) Long minUsageCount,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "usageCount") String orderBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "DESC") String orderDirection,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("REST请求 - 标签列表查询: type={}, status={}, keyword={}, page={}/{}", 
                tagType, status, keyword, currentPage, pageSize);
        return tagService.listTagsForController(tagType, status, keyword, isHot, minUsageCount,
                orderBy, orderDirection, currentPage, pageSize);
    }
}