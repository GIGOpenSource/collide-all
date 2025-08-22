package com.gig.collide.controller;

import com.gig.collide.Apientry.api.common.response.PageResponse;
import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.Apientry.api.tag.UserInterestTagFacadeService;
import com.gig.collide.Apientry.api.tag.request.UserInterestTagRequest;
import com.gig.collide.Apientry.api.tag.response.UserInterestTagResponse;
import com.gig.collide.service.UserInterestTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 用户兴趣标签管理控制器
 * 负责用户与标签的兴趣关系管理功能
 *
 * @author GIG Team
 * @version 3.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/user-interest-tags")
@RequiredArgsConstructor
@Validated
@Tag(name = "用户兴趣标签管理", description = "用户与标签的兴趣关系管理功能")
public class UserInterestTagController {

    private final UserInterestTagService userInterestTagService;

    /**
     * 用户兴趣标签列表查询
     * 支持多种条件查询和分页
     */
    @GetMapping("/list")
    @Operation(summary = "用户兴趣标签列表查询", description = "支持按用户、标签、兴趣度等条件查询用户兴趣标签列表")
    public Result<PageResponse<UserInterestTagResponse>> listUserInterestTags(
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "标签ID") @RequestParam(required = false) Long tagId,
            @Parameter(description = "兴趣度最小值") @RequestParam(required = false) BigDecimal minInterestLevel,
            @Parameter(description = "兴趣度最大值") @RequestParam(required = false) BigDecimal maxInterestLevel,
            @Parameter(description = "关联状态") @RequestParam(required = false) String status,
            @Parameter(description = "关键词搜索") @RequestParam(required = false) String keyword,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "interestLevel") String orderBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "DESC") String orderDirection,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("REST请求 - 用户兴趣标签列表查询: userId={}, tagId={}, minLevel={}, maxLevel={}, page={}/{}", 
                userId, tagId, minInterestLevel, maxInterestLevel, currentPage, pageSize);
        return userInterestTagService.listUserInterestTagsForController(userId, tagId, minInterestLevel, maxInterestLevel, status, keyword,
                orderBy, orderDirection, currentPage, pageSize);
    }
}