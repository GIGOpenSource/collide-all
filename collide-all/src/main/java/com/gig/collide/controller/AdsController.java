package com.gig.collide.controller;


import com.gig.collide.Apientry.api.ads.response.AdResponse;
import com.gig.collide.Apientry.api.common.response.PageResponse;
import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.service.AdService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 广告管理控制器 - 极简版
 *
 * @author GIG Team
 * @version 3.0.0 (极简版)
 * @since 2024-01-16
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/ads")
@RequiredArgsConstructor
@Validated
@Tag(name = "广告管理", description = "广告管理相关接口")
public class AdsController {

    private final AdService adService;

    /**
     * 广告列表查询
     * 支持多种条件查询和分页
     */
    @GetMapping("/list")
    @Operation(summary = "广告列表查询", description = "支持按ID、类型、关键词等条件查询广告列表")
    public Result<PageResponse<AdResponse>> listAds(
            @Parameter(description = "广告ID") @RequestParam(required = false) Long id,
            @Parameter(description = "广告类型") @RequestParam(required = false) String adType,
            @Parameter(description = "关键词搜索") @RequestParam(required = false) String keyword,
            @Parameter(description = "是否有效") @RequestParam(required = false) Integer isValid,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createTime") String orderBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "DESC") String orderDirection,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "20") Integer pageSize) {
        try {
            log.info("REST请求 - 广告列表查询: id={}, type={}, keyword={}, page={}/{}",
                    id, adType, keyword, currentPage, pageSize);
            return adService.listAdsForController(id, adType, null, null, keyword, isValid,
                    orderBy, orderDirection, currentPage, pageSize);
        } catch (Exception e) {
            log.error("广告列表查询失败: type={}, keyword={}, page={}/{}",
                    adType, keyword, currentPage, pageSize, e);
            return Result.error("广告列表查询失败: " + e.getMessage());
        }
    }
}
