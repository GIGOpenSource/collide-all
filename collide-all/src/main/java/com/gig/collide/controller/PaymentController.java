package com.gig.collide.controller;


import com.gig.collide.Apientry.api.common.response.PageResponse;
import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.Apientry.api.payment.PaymentFacadeService;
import com.gig.collide.Apientry.api.payment.request.PaymentCallbackRequest;
import com.gig.collide.Apientry.api.payment.request.PaymentCreateRequest;
import com.gig.collide.Apientry.api.payment.request.PaymentQueryRequest;
import com.gig.collide.Apientry.api.payment.response.PaymentResponse;
import com.gig.collide.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 支付控制器 - 缓存增强版
 * 对齐search模块设计风格，集成缓存功能和测试接口
 *
 * @author GIG Team
 * @version 2.0.0 (缓存增强版)
 * @since 2024-01-16
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "支付管理", description = "支付相关的API接口")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 支付记录列表查询
     * 支持多种条件查询和分页
     */
    @GetMapping("/list")
    @Operation(summary = "支付记录列表查询", description = "支持按用户、订单、状态等条件查询支付记录列表")
    public Result<PageResponse<PaymentResponse>> listPayments(
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "订单ID") @RequestParam(required = false) Long orderId,
            @Parameter(description = "支付状态") @RequestParam(required = false) String status,
            @Parameter(description = "支付方式") @RequestParam(required = false) String paymentMethod,
            @Parameter(description = "支付类型") @RequestParam(required = false) String paymentType,
            @Parameter(description = "关键词搜索") @RequestParam(required = false) String keyword,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createTime") String orderBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "DESC") String orderDirection,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("REST请求 - 支付记录列表查询: userId={}, orderId={}, status={}, page={}/{}", 
                userId, orderId, status, currentPage, pageSize);
        return paymentService.listPaymentsForController(userId, orderId, status, paymentMethod, paymentType, keyword,
                orderBy, orderDirection, currentPage, pageSize);
    }
} 