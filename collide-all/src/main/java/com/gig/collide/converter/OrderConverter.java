package com.gig.collide.converter;

import com.gig.collide.Apientry.api.order.request.OrderCreateRequest;
import com.gig.collide.Apientry.api.order.response.OrderResponse;
import com.gig.collide.domain.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单转换器
 * 用于在请求DTO、实体类和响应DTO之间进行转换
 *
 * @author GIG Team
 * @version 2.0.0
 * @since 2024-01-31
 */
@Component
public class OrderConverter {

    /**
     * 将OrderCreateRequest转换为Order实体
     *
     * @param request 创建请求
     * @return Order实体
     */
    public Order toEntity(OrderCreateRequest request) {
        if (request == null) {
            return null;
        }

        Order order = new Order();

        // 基本信息 - 只映射Order实体中实际存在的字段
        order.setUserId(request.getUserId());
        order.setUserNickname(request.getUserNickname());
        order.setGoodsId(request.getGoodsId());
        order.setQuantity(request.getQuantity());
        order.setDiscountAmount(request.getDiscountAmount());

        // 商品类型和支付模式 - 安全转换
        String goodsType = String.valueOf(request.getGoodsType());
        if (StringUtils.hasText(goodsType)) {
            try {
                order.setGoodsType(Order.GoodsType.valueOf(goodsType.toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("无效的商品类型: " + goodsType);
            }
        }

        String paymentMode = String.valueOf(request.getPaymentMode());
        if (StringUtils.hasText(paymentMode)) {
            try {
                order.setPaymentMode(Order.PaymentMode.valueOf(paymentMode.toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("无效的支付模式: " + paymentMode);
            }
        }

        // 金额相关 - 安全设置
        if (request.getCashAmount() != null) {
            order.setCashAmount(request.getCashAmount());
        }
        if (request.getCoinCost() != null) {
            order.setCoinCost(request.getCoinCost());
        }

        // 内容相关
        if (request.getContentId() != null) {
            order.setContentId(request.getContentId());
        }

        // 设置默认值
        order.setStatus(Order.OrderStatus.PENDING);
        order.setPayStatus(Order.PayStatus.UNPAID);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        return order;
    }

    /**
     * 将Order实体转换为OrderResponse
     *
     * @param entity 实体
     * @return 响应对象
     */
    public OrderResponse toResponse(Order entity) {
        if (entity == null) {
            return null;
        }

        OrderResponse response = new OrderResponse();

        // 基本信息
        response.setId(entity.getId());
        response.setOrderNo(entity.getOrderNo());
        response.setUserId(entity.getUserId());
        response.setUserNickname(entity.getUserNickname());
        response.setGoodsId(entity.getGoodsId());
        response.setGoodsName(entity.getGoodsName());
        response.setGoodsType(entity.getGoodsType() != null ? entity.getGoodsType().name().toLowerCase() : null);
        response.setGoodsCover(entity.getGoodsCover());
        response.setGoodsCategoryName(entity.getGoodsCategoryName());
        response.setQuantity(entity.getQuantity());

        // 金额相关
        response.setTotalAmount(entity.getTotalAmount());
        response.setDiscountAmount(entity.getDiscountAmount());
        response.setFinalAmount(entity.getFinalAmount());
        response.setCashAmount(entity.getCashAmount());
        response.setCoinCost(entity.getCoinCost());
        response.setCoinAmount(entity.getCoinAmount());

        // 状态相关
        response.setStatus(entity.getStatus() != null ? entity.getStatus().name().toLowerCase() : null);
        response.setPayStatus(entity.getPayStatus() != null ? entity.getPayStatus().name().toLowerCase() : null);
        response.setPaymentMode(entity.getPaymentMode() != null ? entity.getPaymentMode().name().toLowerCase() : null);
        response.setPayMethod(entity.getPayMethod());

        // 时间相关
        response.setCreateTime(entity.getCreateTime());
        response.setPayTime(entity.getPayTime());
        response.setUpdateTime(entity.getUpdateTime());

        // 其他信息
        response.setContentId(entity.getContentId());
        response.setContentTitle(entity.getContentTitle());
        response.setSubscriptionDuration(entity.getSubscriptionDuration());
        response.setSubscriptionType(entity.getSubscriptionType());

        // 计算派生字段
        response.setEffectiveAmount(entity.getEffectiveAmount());
        response.setAmountUnit(entity.isCoinPayment() ? "金币" : "元");
        response.setCanPay(entity.canPay());
        response.setCanCancel(entity.canCancel());
        response.setCanRefund(entity.canRefund());
        response.setIsVirtualGoods(entity.isVirtualGoods());
        response.setOrderTypeDesc(entity.getOrderTypeDesc());
        response.setTotalValue(entity.getTotalValue());

        return response;
    }

    /**
     * 批量转换实体列表为响应列表
     *
     * @param entities 实体列表
     * @return 响应列表
     */
    public List<OrderResponse> toResponseList(List<Order> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 创建新的Order实体（工厂方法）
     *
     * @return 新的Order实体
     */
    public Order createNewOrder() {
        Order order = new Order();
        order.setStatus(Order.OrderStatus.PENDING);
        order.setPayStatus(Order.PayStatus.UNPAID);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        return order;
    }

    /**
     * 更新Order实体的基本信息
     *
     * @param order 订单实体
     * @param request 更新请求
     * @return 更新后的订单实体
     */
    public Order updateOrderFromRequest(Order order, OrderCreateRequest request) {
        if (order == null || request == null) {
            return order;
        }

        // 更新基本信息 - 只更新Order实体中实际存在的字段
        if (request.getUserNickname() != null) {
            order.setUserNickname(request.getUserNickname());
        }
        if (request.getQuantity() != null) {
            order.setQuantity(request.getQuantity());
        }
        if (request.getDiscountAmount() != null) {
            order.setDiscountAmount(request.getDiscountAmount());
        }

        // 更新时间
        order.setUpdateTime(LocalDateTime.now());

        return order;
    }
}
