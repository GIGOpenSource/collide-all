package com.gig.collide.controller;


import com.gig.collide.Apientry.api.common.response.PageResponse;
import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.Apientry.api.tag.ContentTagFacadeService;
import com.gig.collide.Apientry.api.tag.request.ContentTagRequest;
import com.gig.collide.Apientry.api.tag.response.ContentTagResponse;
import com.gig.collide.service.ContentTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 内容标签管理控制器
 * 负责内容与标签的关联管理功能
 *
 * @author GIG Team
 * @version 3.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/content-tags")
@RequiredArgsConstructor
@Validated
@Tag(name = "内容标签管理", description = "内容与标签的关联管理功能")
public class ContentTagController {

    private final ContentTagService contentTagService;

    /**
     * 内容标签关联列表查询
     * 支持多种条件查询和分页
     */
    @GetMapping("/list")
    @Operation(summary = "内容标签关联列表查询", description = "支持按内容、标签、类型等条件查询关联列表")
    public Result<PageResponse<ContentTagResponse>> listContentTags(
            @Parameter(description = "内容ID") @RequestParam(required = false) Long contentId,
            @Parameter(description = "标签ID") @RequestParam(required = false) Long tagId,
            @Parameter(description = "关联类型") @RequestParam(required = false) String relationType,
            @Parameter(description = "关联状态") @RequestParam(required = false) String status,
            @Parameter(description = "关键词搜索") @RequestParam(required = false) String keyword,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createTime") String orderBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "DESC") String orderDirection,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("REST请求 - 内容标签关联列表查询: contentId={}, tagId={}, type={}, page={}/{}", 
                contentId, tagId, relationType, currentPage, pageSize);
        return contentTagService.listContentTagsForController(contentId, tagId, relationType, status, keyword,
                orderBy, orderDirection, currentPage, pageSize);
    }
}