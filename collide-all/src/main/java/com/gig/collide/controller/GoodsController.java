package com.gig.collide.controller;


import com.gig.collide.Apientry.api.common.response.PageResponse;
import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.Apientry.api.goods.GoodsFacadeService;
import com.gig.collide.Apientry.api.goods.request.GoodsCreateRequest;
import com.gig.collide.Apientry.api.goods.request.GoodsQueryRequest;
import com.gig.collide.Apientry.api.goods.response.GoodsResponse;
import com.gig.collide.service.GoodsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 商品管理REST控制器 - 缓存增强版
 * 提供商品的完整REST API接口
 *
 * @author GIG Team
 * @version 2.0.0 (缓存增强版)
 * @since 2024-01-31
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/goods")
@RequiredArgsConstructor
@Validated
@Tag(name = "商品管理", description = "商品的增删改查、库存管理、统计分析等功能")
public class GoodsController {

    private final GoodsService goodsService;

    /**
     * 商品列表查询
     * 支持多种条件查询和分页
     */
    @GetMapping("/list")
    @Operation(summary = "商品列表查询", description = "支持按类型、状态、价格等条件查询商品列表")
    public Result<PageResponse<GoodsResponse>> listGoods(
            @Parameter(description = "商品类型") @RequestParam(required = false) String goodsType,
            @Parameter(description = "商品状态") @RequestParam(required = false) String status,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "卖家ID") @RequestParam(required = false) Long sellerId,
            @Parameter(description = "关键词搜索") @RequestParam(required = false) String keyword,
            @Parameter(description = "最小价格") @RequestParam(required = false) Long minPrice,
            @Parameter(description = "最大价格") @RequestParam(required = false) Long maxPrice,
            @Parameter(description = "是否有库存") @RequestParam(required = false) Boolean hasStock,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createTime") String orderBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "DESC") String orderDirection,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("REST请求 - 商品列表查询: type={}, status={}, categoryId={}, page={}/{}", 
                goodsType, status, categoryId, currentPage, pageSize);
        return goodsService.listGoodsForController(goodsType, status, categoryId, sellerId, keyword, minPrice, maxPrice, hasStock,
                orderBy, orderDirection, currentPage, pageSize);
    }
}