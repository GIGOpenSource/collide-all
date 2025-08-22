package com.gig.collide.controller;


import com.gig.collide.Apientry.api.common.response.PageResponse;
import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.Apientry.api.content.ContentPaymentFacadeService;
import com.gig.collide.Apientry.api.content.response.ContentPaymentConfigResponse;
import com.gig.collide.service.ContentPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 内容付费配置控制器 - 极简版
 * 基于12个核心方法设计的精简API
 * 
 * @author GIG Team
 * @version 2.0.0 (极简版)
 * @since 2024-01-31
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/content/payment")
@RequiredArgsConstructor
@Validated
@Tag(name = "内容付费管理", description = "内容付费配置的管理、查询和统计接口（极简版）")
public class ContentPaymentController {

    private final ContentPaymentService contentPaymentService;

    /**
     * 付费配置列表查询
     * 支持多种条件查询和分页
     */
    @GetMapping("/list")
    @Operation(summary = "付费配置列表查询", description = "支持按内容、类型、状态等条件查询付费配置列表")
    public Result<PageResponse<ContentPaymentConfigResponse>> listPaymentConfigs(
            @Parameter(description = "内容ID") @RequestParam(required = false) Long contentId,
            @Parameter(description = "配置类型") @RequestParam(required = false) String configType,
            @Parameter(description = "配置状态") @RequestParam(required = false) String status,
            @Parameter(description = "是否启用") @RequestParam(required = false) Boolean isEnabled,
            @Parameter(description = "关键词搜索") @RequestParam(required = false) String keyword,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createTime") String orderBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "DESC") String orderDirection,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("REST请求 - 付费配置列表查询: contentId={}, type={}, status={}, page={}/{}", 
                contentId, configType, status, currentPage, pageSize);
        return contentPaymentService.listPaymentConfigsForController(contentId, configType, status, isEnabled, keyword,
                orderBy, orderDirection, currentPage, pageSize);
    }
}