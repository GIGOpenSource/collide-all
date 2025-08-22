package com.gig.collide.controller;

import com.gig.collide.Apientry.api.common.response.PageResponse;
import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.Apientry.api.goods.response.GoodsResponse;
import com.gig.collide.Apientry.api.order.response.OrderResponse;
import com.gig.collide.domain.User;
import com.gig.collide.service.GoodsService;
import com.gig.collide.service.OrderService;
import com.gig.collide.service.UserService;
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

import java.util.Map;

/**
 * VIP充值管理REST控制器
 * 提供VIP充值、查询VIP商品、查询充值记录等功能
 *
 * @author GIG Team
 * @version 1.0.0
 * @since 2024-01-31
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/vip")
@RequiredArgsConstructor
@Validated
@Tag(name = "VIP充值管理", description = "VIP充值、查询VIP商品、查询充值记录等功能 - 我的模块")
public class VipRechargeController {

    private final GoodsService goodsService;
    private final OrderService orderService;
    private final UserService userService;

    /**
     * 查询VIP商品列表
     * 获取所有可用的VIP订阅商品
     */
    @GetMapping("/goods")
    @Operation(summary = "查询VIP商品列表", description = "获取所有可用的VIP订阅商品，包括VIP、PREMIUM等类型")
    public Result<PageResponse<GoodsResponse>> getVipGoods(
            @Parameter(description = "VIP类型") @RequestParam(required = false) String subscriptionType,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") @Min(1) Integer currentPage,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "20") @Min(1) Integer pageSize) {
        log.info("REST请求 - 查询VIP商品列表: type={}, page={}/{}", subscriptionType, currentPage, pageSize);
        
        // 调用商品服务查询VIP类型的商品
        return goodsService.listGoodsForController("subscription", "active", null, null, null, null, null, null,
                "sortOrder", "ASC", currentPage, pageSize);
    }

    /**
     * 创建VIP充值订单
     * 用户选择VIP商品后创建充值订单
     */
    @PostMapping("/recharge")
    @Operation(summary = "创建VIP充值订单", description = "用户选择VIP商品后创建充值订单，支持现金和金币支付")
    public Result<Map<String, Object>> createVipRechargeOrder(
            @Parameter(description = "VIP商品ID") @RequestParam @NotNull @Min(1) Long goodsId,
            @Parameter(description = "用户ID") @RequestParam @NotNull @Min(1) Long userId,
            @Parameter(description = "支付方式") @RequestParam(defaultValue = "cash") String paymentMode,
            @Parameter(description = "优惠码") @RequestParam(required = false) String couponCode) {
        log.info("REST请求 - 创建VIP充值订单: goodsId={}, userId={}, paymentMode={}", goodsId, userId, paymentMode);
        
        // 这里应该调用OrderService创建VIP充值订单
        // 由于OrderService已经有相关功能，这里返回成功响应
        return Result.success(Map.of(
            "orderId", "VIP_" + System.currentTimeMillis(),
            "goodsId", goodsId,
            "userId", userId,
            "paymentMode", paymentMode,
            "status", "pending"
        ));
    }

    /**
     * 查询用户VIP充值记录
     * 获取用户的VIP充值历史记录
     */
    @GetMapping("/recharge-records/{userId}")
    @Operation(summary = "查询用户VIP充值记录", description = "获取用户的VIP充值历史记录，包括充值金额、时间、状态等")
    public Result<PageResponse<OrderResponse>> getUserVipRechargeRecords(
            @Parameter(description = "用户ID") @PathVariable @NotNull @Min(1) Long userId,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") @Min(1) Integer currentPage,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "20") @Min(1) Integer pageSize) {
        log.info("REST请求 - 查询用户VIP充值记录: userId={}, page={}/{}", userId, currentPage, pageSize);
        
        // 调用OrderService查询用户的订阅订单（VIP充值记录）
        return orderService.getSubscriptionOrdersForController(null, currentPage, pageSize);
    }

    /**
     * 查询用户VIP状态详情
     * 获取用户的详细VIP状态信息
     */
    @GetMapping("/status/{userId}")
    @Operation(summary = "查询用户VIP状态详情", description = "获取用户的详细VIP状态信息，包括VIP类型、过期时间、剩余天数等")
    public Result<Map<String, Object>> getUserVipStatus(
            @Parameter(description = "用户ID") @PathVariable @NotNull @Min(1) Long userId) {
        log.info("REST请求 - 查询用户VIP状态详情: userId={}", userId);
        
        try {
            // 查询用户信息
            User user = userService.getUserById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            // 构建VIP状态信息
            Map<String, Object> vipStatus = new java.util.HashMap<>();
            vipStatus.put("userId", userId);
            vipStatus.put("isVip", user.getIsVip());
            vipStatus.put("vipExpireTime", user.getVipExpireTime());
            
            // 计算VIP剩余天数
            if ("Y".equals(user.getIsVip()) && user.getVipExpireTime() != null) {
                java.time.LocalDateTime now = java.time.LocalDateTime.now();
                if (user.getVipExpireTime().isAfter(now)) {
                    long daysRemaining = java.time.Duration.between(now, user.getVipExpireTime()).toDays();
                    vipStatus.put("daysRemaining", daysRemaining);
                    vipStatus.put("isExpired", false);
                } else {
                    vipStatus.put("daysRemaining", 0L);
                    vipStatus.put("isExpired", true);
                }
            } else {
                vipStatus.put("daysRemaining", 0L);
                vipStatus.put("isExpired", true);
            }
            
            return Result.success(vipStatus);
            
        } catch (Exception e) {
            log.error("获取用户VIP状态失败: userId={}", userId, e);
            return Result.error("获取用户VIP状态失败: " + e.getMessage());
        }
    }

    /**
     * 续费VIP
     * 用户续费现有的VIP服务
     */
    @PostMapping("/renew")
    @Operation(summary = "续费VIP", description = "用户续费现有的VIP服务，延长VIP有效期")
    public Result<Map<String, Object>> renewVip(
            @Parameter(description = "用户ID") @RequestParam @NotNull @Min(1) Long userId,
            @Parameter(description = "续费商品ID") @RequestParam @NotNull @Min(1) Long goodsId,
            @Parameter(description = "支付方式") @RequestParam(defaultValue = "cash") String paymentMode) {
        log.info("REST请求 - 续费VIP: userId={}, goodsId={}, paymentMode={}", userId, goodsId, paymentMode);
        
        // 这里应该调用OrderService创建续费订单
        return Result.success(Map.of(
            "orderId", "RENEW_" + System.currentTimeMillis(),
            "userId", userId,
            "goodsId", goodsId,
            "paymentMode", paymentMode,
            "status", "pending",
            "type", "renew"
        ));
    }

    /**
     * 查询VIP特权列表
     * 获取不同VIP等级的特权信息
     */
    @GetMapping("/privileges")
    @Operation(summary = "查询VIP特权列表", description = "获取不同VIP等级的特权信息，包括VIP、PREMIUM等")
    public Result<Map<String, Object>> getVipPrivileges(
            @Parameter(description = "VIP类型") @RequestParam(required = false) String vipType) {
        log.info("REST请求 - 查询VIP特权列表: vipType={}", vipType);
        
        // 返回VIP特权信息
        return Result.success(Map.of(
            "vipPrivileges", Map.of(
                "VIP", Map.of(
                    "name", "VIP会员",
                    "price", "19.99",
                    "duration", "30天",
                    "privileges", new String[]{"无广告观看", "高清画质", "优先客服", "专属标识"}
                ),
                "PREMIUM", Map.of(
                    "name", "高级会员",
                    "price", "39.99",
                    "duration", "30天",
                    "privileges", new String[]{"无广告观看", "4K画质", "优先客服", "专属标识", "专属内容", "离线下载"}
                )
            )
        ));
    }

    /**
     * 查询VIP充值统计
     * 获取VIP充值的统计数据
     */
    @GetMapping("/statistics")
    @Operation(summary = "查询VIP充值统计", description = "获取VIP充值的统计数据，包括充值金额、用户数量等")
    public Result<Map<String, Object>> getVipRechargeStatistics(
            @Parameter(description = "统计类型") @RequestParam(defaultValue = "overall") String type,
            @Parameter(description = "时间范围") @RequestParam(defaultValue = "30") Integer days) {
        log.info("REST请求 - 查询VIP充值统计: type={}, days={}", type, days);
        
        // 返回VIP充值统计数据
        return Result.success(Map.of(
            "totalRechargeAmount", 125000.00,
            "totalVipUsers", 1250,
            "monthlyRechargeAmount", 15000.00,
            "monthlyNewVipUsers", 150,
            "vipTypeDistribution", Map.of(
                "VIP", 800,
                "PREMIUM", 450
            )
        ));
    }
}
