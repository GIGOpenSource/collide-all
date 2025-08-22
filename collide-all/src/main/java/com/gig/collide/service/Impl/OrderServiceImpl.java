package com.gig.collide.service.Impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gig.collide.Apientry.api.common.response.PageResponse;
import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.Apientry.api.order.request.OrderCreateRequest;
import com.gig.collide.Apientry.api.order.request.OrderQueryRequest;
import com.gig.collide.Apientry.api.order.response.OrderResponse;
import com.gig.collide.converter.OrderConverter;
import com.gig.collide.domain.Order;
import com.gig.collide.mapper.OrderMapper;
import com.gig.collide.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单业务服务实现类 - 缓存增强版
 * 基于JetCache的高性能订单服务
 *
 * @author GIG Team
 * @version 2.0.0 (缓存增强版)
 * @since 2024-01-31
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderConverter orderConverter;

    // =================== 订单创建和管理 ===================

    @Override
    @Transactional(rollbackFor = Exception.class)
//    @CacheInvalidate(name = OrderCacheConstant.ORDER_LIST_CACHE)
//    @CacheInvalidate(name = OrderCacheConstant.USER_ORDER_CACHE)
//    @CacheInvalidate(name = OrderCacheConstant.ORDER_STATISTICS_CACHE)
    public Long createOrder(Order order) {
        log.info("创建订单: orderNo={}, userId={}, goodsId={}, goodsType={}",
                order.getOrderNo(), order.getUserId(), order.getGoodsId(), order.getGoodsType());

        // 设置默认值
        setDefaultValues((OrderCreateRequest) order);

        // 保存订单
        int result = orderMapper.insert(order);
        if (result > 0) {
            log.info("订单创建成功: id={}, orderNo={}", order.getId(), order.getOrderNo());
            return order.getId();
        } else {
            throw new RuntimeException("订单创建失败");
        }
    }

    @Override
//    @Cached(name = OrderCacheConstant.ORDER_DETAIL_CACHE,
//            key = OrderCacheConstant.ORDER_DETAIL_KEY + "#id",
//            expire = OrderCacheConstant.DETAIL_EXPIRE,
//            timeUnit = TimeUnit.MINUTES)
    public Order getOrderById(Long id) {
        log.debug("查询订单详情: id={}", id);

        if (id == null || id <= 0) {
            return null;
        }

        Order order = orderMapper.selectById(id);
        if (order != null) {
            log.debug("订单查询成功: id={}, orderNo={}, status={}",
                    order.getId(), order.getOrderNo(), order.getStatus());
        }

        return order;
    }

    @Override
//    @Cached(name = OrderCacheConstant.ORDER_DETAIL_CACHE,
//            key = OrderCacheConstant.ORDER_NO_KEY + "#orderNo",
//            expire = OrderCacheConstant.DETAIL_EXPIRE,
//            timeUnit = TimeUnit.MINUTES)
    public Order getOrderByOrderNo(String orderNo) {
        log.debug("根据订单号查询订单: orderNo={}", orderNo);

        if (!StringUtils.hasText(orderNo)) {
            return null;
        }

        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order != null) {
            log.debug("订单查询成功: orderNo={}, id={}, status={}",
                    orderNo, order.getId(), order.getStatus());
        }

        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
//    @CacheUpdate(name = OrderCacheConstant.ORDER_DETAIL_CACHE,
//            key = OrderCacheConstant.ORDER_DETAIL_KEY + "#order.id",
//            value = "#order")
//    @CacheInvalidate(name = OrderCacheConstant.ORDER_LIST_CACHE)
//    @CacheInvalidate(name = OrderCacheConstant.USER_ORDER_CACHE)
    public boolean updateOrder(Order order) {
        log.info("更新订单: id={}, orderNo={}", order.getId(), order.getOrderNo());

        if (order.getId() == null || order.getId() <= 0) {
            throw new IllegalArgumentException("订单ID不能为空");
        }

        // 设置更新时间
        order.setUpdateTime(LocalDateTime.now());

        int result = orderMapper.updateById(order);
        if (result > 0) {
            log.info("订单更新成功: id={}", order.getId());
            return true;
        } else {
            log.warn("订单更新失败: id={}", order.getId());
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
//    @CacheInvalidate(name = OrderCacheConstant.ORDER_DETAIL_CACHE,
//            key = OrderCacheConstant.ORDER_DETAIL_KEY + "#orderId")
//    @CacheInvalidate(name = OrderCacheConstant.ORDER_LIST_CACHE)
//    @CacheInvalidate(name = OrderCacheConstant.USER_ORDER_CACHE)
    public boolean cancelOrder(Long orderId, String reason) {
        log.info("取消订单: orderId={}, reason={}", orderId, reason);

        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("订单ID不能为空");
        }

        // 验证是否可以取消
        Map<String, Object> validation = validateCancel(orderId);
        if (!(Boolean) validation.get("valid")) {
            throw new IllegalStateException((String) validation.get("message"));
        }

        // 更新订单状态
        LambdaUpdateWrapper<Order> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Order::getId, orderId)
                .set(Order::getStatus, Order.OrderStatus.CANCELLED)
                .set(Order::getUpdateTime, LocalDateTime.now());

        int result = orderMapper.update(null, updateWrapper);
        if (result > 0) {
            log.info("订单取消成功: orderId={}", orderId);
            return true;
        } else {
            log.warn("订单取消失败: orderId={}", orderId);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
//    @CacheInvalidate(name = OrderCacheConstant.ORDER_LIST_CACHE)
//    @CacheInvalidate(name = OrderCacheConstant.USER_ORDER_CACHE)
    public boolean batchCancelOrders(List<Long> orderIds, String reason) {
        log.info("批量取消订单: count={}, reason={}", orderIds.size(), reason);

        if (CollectionUtils.isEmpty(orderIds)) {
            return true;
        }

        int result = orderMapper.batchUpdateStatus(orderIds, Order.OrderStatus.CANCELLED.getCode());
        log.info("批量取消订单完成: 目标={}, 实际={}", orderIds.size(), result);
        return result > 0;
    }

    // =================== 订单查询 ===================

    @Override
//    @Cached(name = OrderCacheConstant.USER_ORDER_CACHE,
//            key = "T(com.gig.collide.order.infrastructure.cache.OrderCacheConstant).buildUserOrderKey(#userId, #status, #page.current, #page.size)",
//            expire = OrderCacheConstant.USER_ORDER_EXPIRE,
//            timeUnit = TimeUnit.MINUTES)
    public IPage<Order> getOrdersByUserId(Page<Order> page, Long userId, String status) {
        log.debug("根据用户查询订单: userId={}, status={}, page={}, size={}",
                userId, status, page.getCurrent(), page.getSize());

        return orderMapper.selectByUserId(page, userId, status);
    }

    @Override
//    @Cached(name = OrderCacheConstant.ORDER_LIST_CACHE,
//            expire = OrderCacheConstant.LIST_EXPIRE,
//            timeUnit = TimeUnit.MINUTES)
    public IPage<Order> getOrdersByGoodsType(Page<Order> page, String goodsType, String status) {
        log.debug("根据商品类型查询订单: goodsType={}, status={}, page={}, size={}",
                goodsType, status, page.getCurrent(), page.getSize());

        return orderMapper.selectByGoodsType(page, goodsType, status);
    }

    @Override
    public PageResponse<OrderResponse> getOrdersByGoodsType(String goodsType, String status, Integer currentPage, Integer pageSize) {
        try {
            log.info("根据商品类型查询订单（简化版）: goodsType={}, status={}, page={}, size={}",
                    goodsType, status, currentPage, pageSize);

            // 参数验证和默认值设置
            if (currentPage == null || currentPage <= 0) {
                currentPage = 1;
            }
            if (pageSize == null || pageSize <= 0) {
                pageSize = 10;
            }
            if (pageSize > 100) {
                pageSize = 100;
            }

            // 构建查询条件
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            if (StringUtils.hasText(goodsType)) {
                wrapper.eq(Order::getGoodsType, Order.GoodsType.valueOf(goodsType.toUpperCase()));
            }
            if (StringUtils.hasText(status)) {
                wrapper.eq(Order::getStatus, status);
            }

            // 按创建时间倒序排列
            wrapper.orderByDesc(Order::getCreateTime);

            // 分页查询
            Page<Order> page = new Page<>(currentPage, pageSize);
            IPage<Order> orderPage = orderMapper.selectPage(page, wrapper);

            // 转换为响应
            List<OrderResponse> responses = orderConverter.toResponseList(orderPage.getRecords());

            PageResponse<OrderResponse> pageResponse = new PageResponse<>();
            pageResponse.setRecords(responses);
            pageResponse.setTotal(orderPage.getTotal());
            pageResponse.setCurrentPage(currentPage);
            pageResponse.setPageSize(pageSize);

            log.debug("商品类型订单查询成功: goodsType={}, total={}, page={}, size={}",
                    goodsType, orderPage.getTotal(), currentPage, pageSize);

            return pageResponse;

        } catch (Exception e) {
            log.error("根据商品类型查询订单失败: goodsType={}, status={}", goodsType, status, e);
            throw new RuntimeException("根据商品类型查询订单失败: " + e.getMessage());
        }
    }

    @Override
//    @Cached(name = OrderCacheConstant.ORDER_LIST_CACHE,
//            expire = OrderCacheConstant.LIST_EXPIRE,
//            timeUnit = TimeUnit.MINUTES)
    public IPage<Order> getOrdersByPaymentMode(Page<Order> page, String paymentMode, String payStatus) {
        log.debug("根据支付模式查询订单: paymentMode={}, payStatus={}, page={}, size={}",
                paymentMode, payStatus, page.getCurrent(), page.getSize());

        return orderMapper.selectByPaymentMode(page, paymentMode, payStatus);
    }

    @Override
//    @Cached(name = OrderCacheConstant.ORDER_LIST_CACHE,
//            key = "T(com.gig.collide.order.infrastructure.cache.OrderCacheConstant).buildSellerOrderKey(#sellerId, #page.current, #page.size)",
//            expire = OrderCacheConstant.LIST_EXPIRE,
//            timeUnit = TimeUnit.MINUTES)
    public IPage<Order> getOrdersBySellerId(Page<Order> page, Long sellerId, String status) {
        log.debug("根据商家查询订单: sellerId={}, status={}, page={}, size={}",
                sellerId, status, page.getCurrent(), page.getSize());

        return orderMapper.selectBySellerId(page, sellerId, status);
    }

    @Override
    public IPage<Order> getOrdersByTimeRange(Page<Order> page, LocalDateTime startTime, LocalDateTime endTime, String status) {
        log.debug("根据时间范围查询订单: start={}, end={}, status={}, page={}, size={}",
                startTime, endTime, status, page.getCurrent(), page.getSize());

        return orderMapper.selectByTimeRange(page, startTime, endTime, status);
    }

    @Override
    public PageResponse<OrderResponse> getOrdersByTimeRange(LocalDateTime startTime, LocalDateTime endTime, String status, Integer currentPage, Integer pageSize) {
        try {
            log.info("根据时间范围查询订单（简化版）: start={}, end={}, status={}, page={}, size={}",
                    startTime, endTime, status, currentPage, pageSize);

            // 参数验证和默认值设置
            if (currentPage == null || currentPage <= 0) {
                currentPage = 1;
            }
            if (pageSize == null || pageSize <= 0) {
                pageSize = 10;
            }
            if (pageSize > 100) {
                pageSize = 100;
            }

            // 构建查询条件
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            if (startTime != null && endTime != null) {
                wrapper.between(Order::getCreateTime, startTime, endTime);
            } else if (startTime != null) {
                wrapper.ge(Order::getCreateTime, startTime);
            } else if (endTime != null) {
                wrapper.le(Order::getCreateTime, endTime);
            }

            if (StringUtils.hasText(status)) {
                wrapper.eq(Order::getStatus, status);
            }

            // 按创建时间倒序排列
            wrapper.orderByDesc(Order::getCreateTime);

            // 分页查询
            Page<Order> page = new Page<>(currentPage, pageSize);
            IPage<Order> orderPage = orderMapper.selectPage(page, wrapper);

            // 转换为响应
            List<OrderResponse> responses = orderConverter.toResponseList(orderPage.getRecords());

            PageResponse<OrderResponse> pageResponse = new PageResponse<>();
            pageResponse.setRecords(responses);
            pageResponse.setTotal(orderPage.getTotal());
            pageResponse.setCurrentPage(currentPage);
            pageResponse.setPageSize(pageSize);

            log.debug("时间范围订单查询成功: start={}, end={}, total={}, page={}, size={}",
                    startTime, endTime, orderPage.getTotal(), currentPage, pageSize);

            return pageResponse;

        } catch (Exception e) {
            log.error("根据时间范围查询订单失败: start={}, end={}, status={}", startTime, endTime, status, e);
            throw new RuntimeException("根据时间范围查询订单失败: " + e.getMessage());
        }
    }

    @Override
    public IPage<Order> searchOrders(Page<Order> page, String keyword) {
        log.debug("搜索订单: keyword={}, page={}, size={}",
                keyword, page.getCurrent(), page.getSize());

        if (!StringUtils.hasText(keyword)) {
            return new Page<>(page.getCurrent(), page.getSize());
        }

        return orderMapper.searchOrders(page, keyword.trim());
    }

    @Override
    public PageResponse<OrderResponse> searchOrders(String keyword, Integer currentPage, Integer pageSize) {
        try {
            log.info("搜索订单（简化版）: keyword={}, page={}, size={}", keyword, currentPage, pageSize);

            // 参数验证和默认值设置
            if (currentPage == null || currentPage <= 0) {
                currentPage = 1;
            }
            if (pageSize == null || pageSize <= 0) {
                pageSize = 10;
            }
            if (pageSize > 100) {
                pageSize = 100;
            }

            if (!StringUtils.hasText(keyword)) {
                // 如果关键词为空，返回空结果
                PageResponse<OrderResponse> pageResponse = new PageResponse<>();
                pageResponse.setRecords(List.of());
                pageResponse.setTotal(0L);
                pageResponse.setCurrentPage(currentPage);
                pageResponse.setPageSize(pageSize);
                return pageResponse;
            }

            // 构建查询条件
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.and(w -> w.like(Order::getOrderNo, keyword.trim())
                    .or().like(Order::getGoodsName, keyword.trim())
                    .or().like(Order::getUserNickname, keyword.trim()));

            // 按创建时间倒序排列
            wrapper.orderByDesc(Order::getCreateTime);

            // 分页查询
            Page<Order> page = new Page<>(currentPage, pageSize);
            IPage<Order> orderPage = orderMapper.selectPage(page, wrapper);

            // 转换为响应
            List<OrderResponse> responses = orderConverter.toResponseList(orderPage.getRecords());

            PageResponse<OrderResponse> pageResponse = new PageResponse<>();
            pageResponse.setRecords(responses);
            pageResponse.setTotal(orderPage.getTotal());
            pageResponse.setCurrentPage(currentPage);
            pageResponse.setPageSize(pageSize);

            log.debug("订单搜索成功: keyword={}, total={}, page={}, size={}",
                    keyword, orderPage.getTotal(), currentPage, pageSize);

            return pageResponse;

        } catch (Exception e) {
            log.error("搜索订单失败: keyword={}", keyword, e);
            throw new RuntimeException("搜索订单失败: " + e.getMessage());
        }
    }

    // =================== 支付相关 ===================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> processPayment(Long orderId, String payMethod) {
        log.info("处理订单支付: orderId={}, payMethod={}", orderId, payMethod);

        Map<String, Object> result = new HashMap<>();

        try {
            // 验证订单是否可以支付
            Map<String, Object> validation = validatePayment(orderId);
            if (!(Boolean) validation.get("valid")) {
                result.put("success", false);
                result.put("message", validation.get("message"));
                return result;
            }

            Order order = (Order) validation.get("order");

            // 根据支付模式处理
            if (order.getPaymentMode() == Order.PaymentMode.COIN) {
                // 金币支付逻辑
                result = processCoinPayment(order, payMethod);
            } else {
                // 现金支付逻辑
                result = processCashPayment(order, payMethod);
            }

            return result;

        } catch (Exception e) {
            log.error("处理订单支付失败: orderId={}", orderId, e);
            result.put("success", false);
            result.put("message", "支付处理失败: " + e.getMessage());
            return result;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> processPayment(Long orderId, String payMethod, Long userId) {
        log.info("处理订单支付（带用户验证）: orderId={}, payMethod={}, userId={}", orderId, payMethod, userId);

        try {
            // 验证订单是否存在
            Order order = getOrderById(orderId);
            if (order == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "订单不存在");
                return result;
            }

            // 验证用户权限
            if (!order.getUserId().equals(userId)) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "无权限操作此订单");
                return result;
            }

            // 调用原有的支付处理方法
            return processPayment(orderId, payMethod);

        } catch (Exception e) {
            log.error("处理订单支付失败（带用户验证）: orderId={}, userId={}", orderId, userId, e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "支付处理失败: " + e.getMessage());
            return result;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
//    @CacheInvalidate(name = OrderCacheConstant.ORDER_DETAIL_CACHE,
//            key = OrderCacheConstant.ORDER_DETAIL_KEY + "#orderId")
//    @CacheInvalidate(name = OrderCacheConstant.USER_ORDER_CACHE)
    public boolean confirmPayment(Long orderId, String payMethod) {
        log.info("确认支付成功: orderId={}, payMethod={}", orderId, payMethod);

        // 更新支付信息
        int result = orderMapper.updatePaymentInfo(orderId,
                Order.PayStatus.PAID.getCode(), payMethod, LocalDateTime.now());

        if (result > 0) {
            // 更新订单状态
            Order order = getOrderById(orderId);
            if (order != null) {
                String newStatus = order.isVirtualGoods() ?
                        Order.OrderStatus.COMPLETED.getCode() : Order.OrderStatus.PAID.getCode();
                updateOrderStatus(orderId, newStatus);

                // 处理后续业务逻辑
                handlePaymentSuccess(order);
            }

            log.info("支付确认成功: orderId={}", orderId);
            return true;
        } else {
            log.warn("支付确认失败: orderId={}", orderId);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
//    @CacheInvalidate(name = OrderCacheConstant.ORDER_DETAIL_CACHE,
//            key = OrderCacheConstant.ORDER_DETAIL_KEY + "#orderId")
//    @CacheInvalidate(name = OrderCacheConstant.USER_ORDER_CACHE)
    public boolean confirmPayment(Long orderId, String payMethod, Long userId) {
        log.info("确认支付成功（带用户验证）: orderId={}, payMethod={}, userId={}", orderId, payMethod, userId);

        try {
            // 验证订单是否存在
            Order order = getOrderById(orderId);
            if (order == null) {
                log.warn("订单不存在: orderId={}", orderId);
                return false;
            }

            // 验证用户权限
            if (!order.getUserId().equals(userId)) {
                log.warn("无权限操作此订单: orderId={}, userId={}, orderUserId={}", orderId, userId, order.getUserId());
                return false;
            }

            // 调用原有的确认支付方法
            return confirmPayment(orderId, payMethod);

        } catch (Exception e) {
            log.error("确认支付失败（带用户验证）: orderId={}, userId={}", orderId, userId, e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
//    @CacheInvalidate(name = OrderCacheConstant.ORDER_DETAIL_CACHE,
//            key = OrderCacheConstant.ORDER_DETAIL_KEY + "#orderId")
//    @CacheInvalidate(name = OrderCacheConstant.USER_ORDER_CACHE)
    public boolean updatePaymentStatus(Long orderId, String payStatus, String payMethod) {
        log.info("更新订单支付状态: orderId={}, payStatus={}, payMethod={}", orderId, payStatus, payMethod);

        try {
            // 更新支付状态和支付方式
            LocalDateTime payTime = "paid".equals(payStatus) ? LocalDateTime.now() : null;
            int result = orderMapper.updatePaymentInfo(orderId, payStatus, payMethod, payTime);

            if (result > 0) {
                // 如果支付成功，更新订单状态
                if ("paid".equals(payStatus)) {
                    Order order = getOrderById(orderId);
                    if (order != null) {
                        String newStatus = order.isVirtualGoods() ?
                                Order.OrderStatus.COMPLETED.getCode() : Order.OrderStatus.PAID.getCode();
                        updateOrderStatus(orderId, newStatus);

                        // 处理支付成功后的业务逻辑
                        handlePaymentSuccess(order);
                    }
                }

                log.info("订单支付状态更新成功: orderId={}, payStatus={}", orderId, payStatus);
                return true;
            } else {
                log.warn("订单支付状态更新失败: orderId={}, payStatus={}", orderId, payStatus);
                return false;
            }
        } catch (Exception e) {
            log.error("订单支付状态更新异常: orderId={}, payStatus={}", orderId, payStatus, e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
//    @CacheInvalidate(name = OrderCacheConstant.ORDER_DETAIL_CACHE,
//            key = OrderCacheConstant.ORDER_DETAIL_KEY + "#orderId")
//    @CacheInvalidate(name = OrderCacheConstant.USER_ORDER_CACHE)
    public boolean updatePaymentInfo(Long orderId, String payStatus, String payMethod, LocalDateTime payTime) {
        log.info("更新订单支付信息: orderId={}, payStatus={}, payMethod={}, payTime={}",
                orderId, payStatus, payMethod, payTime);

        try {
            int result = orderMapper.updatePaymentInfo(orderId, payStatus, payMethod, payTime);

            if (result > 0) {
                // 如果支付成功，更新订单状态
                if ("paid".equals(payStatus)) {
                    Order order = getOrderById(orderId);
                    if (order != null) {
                        String newStatus = order.isVirtualGoods() ?
                                Order.OrderStatus.COMPLETED.getCode() : Order.OrderStatus.PAID.getCode();
                        updateOrderStatus(orderId, newStatus);

                        // 处理支付成功后的业务逻辑
                        handlePaymentSuccess(order);
                    }
                }

                log.info("订单支付信息更新成功: orderId={}", orderId);
                return true;
            } else {
                log.warn("订单支付信息更新失败: orderId={}", orderId);
                return false;
            }
        } catch (Exception e) {
            log.error("订单支付信息更新异常: orderId={}", orderId, e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handlePaymentCallback(String orderNo, String payStatus, String payMethod,
                                         LocalDateTime payTime, Map<String, Object> extraInfo) {
        log.info("处理支付回调: orderNo={}, payStatus={}, payMethod={}", orderNo, payStatus, payMethod);

        Order order = getOrderByOrderNo(orderNo);
        if (order == null) {
            log.warn("订单不存在: orderNo={}", orderNo);
            return false;
        }

        if ("paid".equals(payStatus)) {
            return confirmPayment(order.getId(), payMethod);
        } else {
            log.warn("支付失败回调: orderNo={}, payStatus={}", orderNo, payStatus);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> requestRefund(Long orderId, String reason) {
        log.info("申请退款: orderId={}, reason={}", orderId, reason);

        Map<String, Object> result = new HashMap<>();

        try {
            // 验证是否可以退款
            Map<String, Object> validation = validateRefund(orderId);
            if (!(Boolean) validation.get("valid")) {
                result.put("success", false);
                result.put("message", validation.get("message"));
                return result;
            }

            Order order = (Order) validation.get("order");

            // 处理退款逻辑
            boolean success = processRefund(order, reason);

            result.put("success", success);
            result.put("message", success ? "退款申请成功" : "退款申请失败");

            return result;

        } catch (Exception e) {
            log.error("申请退款失败: orderId={}", orderId, e);
            result.put("success", false);
            result.put("message", "退款申请失败: " + e.getMessage());
            return result;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> requestRefund(Long orderId, String reason, Long userId) {
        log.info("申请退款（带用户验证）: orderId={}, reason={}, userId={}", orderId, reason, userId);

        Map<String, Object> result = new HashMap<>();

        try {
            // 验证订单是否存在
            Order order = getOrderById(orderId);
            if (order == null) {
                result.put("success", false);
                result.put("message", "订单不存在");
                return result;
            }

            // 验证用户权限
            if (!order.getUserId().equals(userId)) {
                result.put("success", false);
                result.put("message", "无权限操作此订单");
                return result;
            }

            // 调用原有的退款申请方法
            return requestRefund(orderId, reason);

        } catch (Exception e) {
            log.error("申请退款失败（带用户验证）: orderId={}, userId={}", orderId, userId, e);
            result.put("success", false);
            result.put("message", "退款申请失败: " + e.getMessage());
            return result;
        }
    }

    // =================== 订单状态管理 ===================

    @Override
    @Transactional(rollbackFor = Exception.class)
//    @CacheInvalidate(name = OrderCacheConstant.ORDER_DETAIL_CACHE,
//            key = OrderCacheConstant.ORDER_DETAIL_KEY + "#orderId")
//    @CacheInvalidate(name = OrderCacheConstant.USER_ORDER_CACHE)
    public boolean updateOrderStatus(Long orderId, String newStatus) {
        log.info("更新订单状态: orderId={}, newStatus={}", orderId, newStatus);

        LambdaUpdateWrapper<Order> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Order::getId, orderId)
                .set(Order::getStatus, newStatus)
                .set(Order::getUpdateTime, LocalDateTime.now());

        int result = orderMapper.update(null, updateWrapper);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
//    @CacheInvalidate(name = OrderCacheConstant.ORDER_LIST_CACHE)
//    @CacheInvalidate(name = OrderCacheConstant.USER_ORDER_CACHE)
    public boolean batchUpdateStatus(List<Long> orderIds, String newStatus) {
        log.info("批量更新订单状态: count={}, newStatus={}", orderIds.size(), newStatus);

        if (CollectionUtils.isEmpty(orderIds)) {
            return true;
        }

        int result = orderMapper.batchUpdateStatus(orderIds, newStatus);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean shipOrder(Long orderId, Map<String, Object> shippingInfo) {
        log.info("订单发货: orderId={}, shippingInfo={}", orderId, shippingInfo);

        // 更新为已发货状态
        boolean success = updateOrderStatus(orderId, Order.OrderStatus.SHIPPED.getCode());

        if (success) {
            // 处理物流信息（可以扩展到物流表）
            log.info("订单发货成功: orderId={}", orderId);
        }

        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean shipOrder(Long orderId, Map<String, Object> shippingInfo, Long operatorId) {
        log.info("订单发货（带操作者验证）: orderId={}, shippingInfo={}, operatorId={}", orderId, shippingInfo, operatorId);

        try {
            // 验证订单是否存在
            Order order = getOrderById(orderId);
            if (order == null) {
                log.warn("订单不存在: orderId={}", orderId);
                return false;
            }

            // 这里可以添加操作者权限验证逻辑
            // 例如：验证操作者是否有权限操作此订单

            // 调用原有的发货方法
            return shipOrder(orderId, shippingInfo);

        } catch (Exception e) {
            log.error("订单发货失败（带操作者验证）: orderId={}, operatorId={}", orderId, operatorId, e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmReceipt(Long orderId) {
        log.info("确认收货: orderId={}", orderId);

        return completeOrder(orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmReceipt(Long orderId, Long userId) {
        log.info("确认收货（带用户验证）: orderId={}, userId={}", orderId, userId);

        try {
            // 验证订单是否存在
            Order order = getOrderById(orderId);
            if (order == null) {
                log.warn("订单不存在: orderId={}", orderId);
                return false;
            }

            // 验证用户权限
            if (!order.getUserId().equals(userId)) {
                log.warn("无权限操作此订单: orderId={}, userId={}, orderUserId={}", orderId, userId, order.getUserId());
                return false;
            }

            // 调用原有的确认收货方法
            return confirmReceipt(orderId);

        } catch (Exception e) {
            log.error("确认收货失败（带用户验证）: orderId={}, userId={}", orderId, userId, e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completeOrder(Long orderId) {
        log.info("完成订单: orderId={}", orderId);

        boolean success = updateOrderStatus(orderId, Order.OrderStatus.COMPLETED.getCode());

        if (success) {
            // 处理订单完成后的业务逻辑
            handleOrderComplete(orderId);
        }

        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completeOrder(Long orderId, Long operatorId) {
        log.info("完成订单（带操作者验证）: orderId={}, operatorId={}", orderId, operatorId);

        try {
            // 验证订单是否存在
            Order order = getOrderById(orderId);
            if (order == null) {
                log.warn("订单不存在: orderId={}", orderId);
                return false;
            }

            // 这里可以添加操作者权限验证逻辑
            // 例如：验证操作者是否有权限操作此订单

            // 调用原有的完成订单方法
            return completeOrder(orderId);

        } catch (Exception e) {
            log.error("完成订单失败（带操作者验证）: orderId={}, operatorId={}", orderId, operatorId, e);
            return false;
        }
    }

    // =================== 定时任务相关 ===================

    @Override
    public List<Order> getTimeoutOrders(Integer timeoutMinutes) {
        log.debug("查询超时订单: timeoutMinutes={}", timeoutMinutes);

        if (timeoutMinutes == null || timeoutMinutes <= 0) {
            timeoutMinutes = 30; // 默认30分钟
        }

        LocalDateTime timeoutTime = LocalDateTime.now().minusMinutes(timeoutMinutes);
        return orderMapper.selectTimeoutOrders(timeoutTime);
    }

    @Override
    public List<Order> getTimeoutOrdersByTime(LocalDateTime timeoutTime) {
        log.debug("查询超时订单: timeoutTime={}", timeoutTime);

        if (timeoutTime == null) {
            timeoutTime = LocalDateTime.now().minusMinutes(30); // 默认30分钟前
        }

        return orderMapper.selectTimeoutOrders(timeoutTime);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int autoCancelTimeoutOrders(Integer timeoutMinutes) {
        log.info("自动取消超时订单: timeoutMinutes={}", timeoutMinutes);

        List<Order> timeoutOrders = getTimeoutOrders(timeoutMinutes);
        if (CollectionUtils.isEmpty(timeoutOrders)) {
            return 0;
        }

        List<Long> orderIds = timeoutOrders.stream()
                .map(Order::getId)
                .collect(Collectors.toList());

        boolean success = batchCancelOrders(orderIds, "系统自动取消（支付超时）");

        int cancelCount = success ? orderIds.size() : 0;
        log.info("自动取消超时订单完成: count={}", cancelCount);

        return cancelCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int autoCompleteShippedOrders(Integer days) {
        log.info("自动完成已发货订单: days={}", days);

        if (days == null || days <= 0) {
            days = 7; // 默认7天
        }

        // 查询超过指定天数的已发货订单
        LocalDateTime completionTime = LocalDateTime.now().minusDays(days);

        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getStatus, Order.OrderStatus.SHIPPED.getCode())
                .le(Order::getUpdateTime, completionTime);

        List<Order> shippedOrders = orderMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(shippedOrders)) {
            return 0;
        }

        List<Long> orderIds = shippedOrders.stream()
                .map(Order::getId)
                .collect(Collectors.toList());

        boolean success = batchUpdateStatus(orderIds, Order.OrderStatus.COMPLETED.getCode());

        int completeCount = success ? orderIds.size() : 0;
        log.info("自动完成已发货订单完成: count={}", completeCount);

        return completeCount;
    }

    // =================== 统计分析 ===================

    @Override
    public PageResponse<OrderResponse> getUserOrders(Long userId, String status, Integer currentPage, Integer pageSize) {
        try {
            log.info("查询用户订单: userId={}, status={}, page={}, size={}", userId, status, currentPage, pageSize);

            // 参数验证和默认值设置
            if (currentPage == null || currentPage <= 0) {
                currentPage = 1;
            }
            if (pageSize == null || pageSize <= 0) {
                pageSize = 10;
            }
            if (pageSize > 100) {
                pageSize = 100; // 限制最大页面大小
            }

            // 构建查询条件
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Order::getUserId, userId);

            if (StringUtils.hasText(status)) {
                wrapper.eq(Order::getStatus, status);
            }

            // 按创建时间倒序排列
            wrapper.orderByDesc(Order::getCreateTime);

            // 分页查询
            Page<Order> page = new Page<>(currentPage, pageSize);
            IPage<Order> orderPage = orderMapper.selectPage(page, wrapper);

            // 转换为响应
            List<OrderResponse> responses = orderConverter.toResponseList(orderPage.getRecords());

            PageResponse<OrderResponse> pageResponse = new PageResponse<>();
            pageResponse.setRecords(responses);
            pageResponse.setTotal(orderPage.getTotal());
            pageResponse.setCurrentPage(currentPage);
            pageResponse.setPageSize(pageSize);

            log.debug("用户订单查询成功: userId={}, total={}, page={}, size={}",
                    userId, orderPage.getTotal(), currentPage, pageSize);

            return pageResponse;

        } catch (Exception e) {
            log.error("查询用户订单失败: userId={}, status={}", userId, status, e);
            throw new RuntimeException("查询用户订单失败: " + e.getMessage());
        }
    }

    @Override
    public PageResponse<OrderResponse> getSellerOrders(Long sellerId, String status, Integer currentPage, Integer pageSize) {
        try {
            log.info("查询商家订单: sellerId={}, status={}, page={}, size={}", sellerId, status, currentPage, pageSize);

            // 参数验证和默认值设置
            if (currentPage == null || currentPage <= 0) {
                currentPage = 1;
            }
            if (pageSize == null || pageSize <= 0) {
                pageSize = 10;
            }
            if (pageSize > 100) {
                pageSize = 100;
            }

            // 由于Order实体中没有sellerId字段，我们需要通过goodsId来关联Goods表
            // 这里使用子查询的方式：先查询该商家的商品ID列表，再查询对应的订单
            // 注意：这是一个简化的实现，实际项目中可能需要使用连表查询或自定义SQL

            // 构建查询条件 - 这里暂时返回空结果，因为需要连表查询
            // 在实际项目中，应该使用自定义的Mapper方法或连表查询
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();

            // TODO: 实现真正的商家订单查询逻辑
            // 方案1: 使用自定义SQL查询
            // 方案2: 先查询商品ID列表，再查询订单
            // 方案3: 在Order实体中添加sellerId冗余字段

            // 临时实现：返回空结果
            PageResponse<OrderResponse> pageResponse = new PageResponse<>();
            pageResponse.setRecords(new ArrayList<>());
            pageResponse.setTotal(0L);
            pageResponse.setCurrentPage(currentPage);
            pageResponse.setPageSize(pageSize);

            log.warn("商家订单查询功能需要实现连表查询逻辑: sellerId={}", sellerId);

            return pageResponse;

        } catch (Exception e) {
            log.error("查询商家订单失败: sellerId={}, status={}", sellerId, status, e);
            throw new RuntimeException("查询商家订单失败: " + e.getMessage());
        }
    }

    @Override
    public PageResponse<OrderResponse> getPendingOrders(Long userId, Integer currentPage, Integer pageSize) {
        try {
            log.info("查询待处理订单: userId={}, page={}, size={}", userId, currentPage, pageSize);

            // 参数验证和默认值设置
            if (currentPage == null || currentPage <= 0) {
                currentPage = 1;
            }
            if (pageSize == null || pageSize <= 0) {
                pageSize = 10;
            }
            if (pageSize > 100) {
                pageSize = 100;
            }

            // 构建查询条件
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Order::getUserId, userId)
                    .eq(Order::getStatus, Order.OrderStatus.PENDING.getCode());

            // 按创建时间倒序排列
            wrapper.orderByDesc(Order::getCreateTime);

            // 分页查询
            Page<Order> page = new Page<>(currentPage, pageSize);
            IPage<Order> orderPage = orderMapper.selectPage(page, wrapper);

            // 转换为响应
            List<OrderResponse> responses = orderConverter.toResponseList(orderPage.getRecords());

            PageResponse<OrderResponse> pageResponse = new PageResponse<>();
            pageResponse.setRecords(responses);
            pageResponse.setTotal(orderPage.getTotal());
            pageResponse.setCurrentPage(currentPage);
            pageResponse.setPageSize(pageSize);

            log.debug("待处理订单查询成功: userId={}, total={}, page={}, size={}",
                    userId, orderPage.getTotal(), currentPage, pageSize);

            return pageResponse;

        } catch (Exception e) {
            log.error("查询待处理订单失败: userId={}", userId, e);
            throw new RuntimeException("查询待处理订单失败: " + e.getMessage());
        }
    }

    @Override
    public PageResponse<OrderResponse> getCompletedOrders(Long userId, Integer currentPage, Integer pageSize) {
        try {
            log.info("查询已完成订单: userId={}, page={}, size={}", userId, currentPage, pageSize);

            // 参数验证和默认值设置
            if (currentPage == null || currentPage <= 0) {
                currentPage = 1;
            }
            if (pageSize == null || pageSize <= 0) {
                pageSize = 10;
            }
            if (pageSize > 100) {
                pageSize = 100;
            }

            // 构建查询条件
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Order::getUserId, userId)
                    .eq(Order::getStatus, Order.OrderStatus.COMPLETED.getCode());

            // 按完成时间倒序排列
            wrapper.orderByDesc(Order::getUpdateTime);

            // 分页查询
            Page<Order> page = new Page<>(currentPage, pageSize);
            IPage<Order> orderPage = orderMapper.selectPage(page, wrapper);

            // 转换为响应
            List<OrderResponse> responses = orderConverter.toResponseList(orderPage.getRecords());

            PageResponse<OrderResponse> pageResponse = new PageResponse<>();
            pageResponse.setRecords(responses);
            pageResponse.setTotal(orderPage.getTotal());
            pageResponse.setCurrentPage(currentPage);
            pageResponse.setPageSize(pageSize);

            log.debug("已完成订单查询成功: userId={}, total={}, page={}, size={}",
                    userId, orderPage.getTotal(), currentPage, pageSize);

            return pageResponse;

        } catch (Exception e) {
            log.error("查询已完成订单失败: userId={}", userId, e);
            throw new RuntimeException("查询已完成订单失败: " + e.getMessage());
        }
    }

    @Override
    public PageResponse<OrderResponse> getTodayOrders(Integer currentPage, Integer pageSize) {
        try {
            log.info("查询今日订单: page={}, size={}", currentPage, pageSize);

            // 参数验证和默认值设置
            if (currentPage == null || currentPage <= 0) {
                currentPage = 1;
            }
            if (pageSize == null || pageSize <= 0) {
                pageSize = 10;
            }
            if (pageSize > 100) {
                pageSize = 100;
            }

            // 获取今天的开始和结束时间
            LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime todayEnd = todayStart.plusDays(1).minusNanos(1);

            // 构建查询条件
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.between(Order::getCreateTime, todayStart, todayEnd);

            // 按创建时间倒序排列
            wrapper.orderByDesc(Order::getCreateTime);

            // 分页查询
            Page<Order> page = new Page<>(currentPage, pageSize);
            IPage<Order> orderPage = orderMapper.selectPage(page, wrapper);

            // 转换为响应
            List<OrderResponse> responses = orderConverter.toResponseList(orderPage.getRecords());

            PageResponse<OrderResponse> pageResponse = new PageResponse<>();
            pageResponse.setRecords(responses);
            pageResponse.setTotal(orderPage.getTotal());
            pageResponse.setCurrentPage(currentPage);
            pageResponse.setPageSize(pageSize);

            log.debug("今日订单查询成功: total={}, page={}, size={}",
                    orderPage.getTotal(), currentPage, pageSize);

            return pageResponse;

        } catch (Exception e) {
            log.error("查询今日订单失败", e);
            throw new RuntimeException("查询今日订单失败: " + e.getMessage());
        }
    }

    @Override
//    @Cached(name = OrderCacheConstant.ORDER_STATISTICS_CACHE,
//            key = OrderCacheConstant.USER_STATS_KEY + "#userId",
//            expire = OrderCacheConstant.STATISTICS_EXPIRE,
//            timeUnit = TimeUnit.MINUTES)
    public Map<String, Object> getUserOrderStatistics(Long userId) {
        log.debug("统计用户订单数据: userId={}", userId);
        return orderMapper.selectUserOrderStatistics(userId);
    }

    @Override
//    @Cached(name = OrderCacheConstant.ORDER_STATISTICS_CACHE,
//            key = OrderCacheConstant.GOODS_STATS_KEY + "#goodsId",
//            expire = OrderCacheConstant.STATISTICS_EXPIRE,
//            timeUnit = TimeUnit.MINUTES)
    public Map<String, Object> getGoodsSalesStatistics(Long goodsId) {
        log.debug("统计商品销售数据: goodsId={}", goodsId);
        return orderMapper.selectGoodsSalesStatistics(goodsId);
    }

    @Override
//    @Cached(name = OrderCacheConstant.ORDER_STATISTICS_CACHE,
//            expire = OrderCacheConstant.STATISTICS_EXPIRE,
//            timeUnit = TimeUnit.MINUTES)
    public List<Map<String, Object>> getOrderStatisticsByType() {
        log.debug("按商品类型统计订单");
        return orderMapper.selectOrderStatisticsByType();
    }

    @Override
//    @Cached(name = OrderCacheConstant.HOT_GOODS_CACHE,
//            expire = OrderCacheConstant.HOT_GOODS_EXPIRE,
//            timeUnit = TimeUnit.MINUTES)
    public List<Map<String, Object>> getHotGoods(Integer limit) {
        log.debug("查询热门商品: limit={}", limit);

        if (limit == null || limit <= 0) {
            limit = 10;
        }

        return orderMapper.selectHotGoods(limit);
    }

    @Override
//    @Cached(name = OrderCacheConstant.REVENUE_CACHE,
//            key = "T(com.gig.collide.order.infrastructure.cache.OrderCacheConstant).buildRevenueKey(#startDate, #endDate)",
//            expire = OrderCacheConstant.REVENUE_EXPIRE,
//            timeUnit = TimeUnit.MINUTES)
    public List<Map<String, Object>> getDailyRevenue(String startDate, String endDate) {
        log.debug("查询日营收统计: start={}, end={}", startDate, endDate);
        return orderMapper.selectDailyRevenue(startDate, endDate);
    }

    @Override
//    @Cached(name = OrderCacheConstant.USER_PURCHASE_CACHE,
//            expire = OrderCacheConstant.PURCHASE_EXPIRE,
//            timeUnit = TimeUnit.MINUTES)
    public List<Order> getUserRecentOrders(Long userId, Integer limit) {
        log.debug("查询用户最近购买记录: userId={}, limit={}", userId, limit);

        if (limit == null || limit <= 0) {
            limit = 10;
        }

        return orderMapper.selectUserRecentOrders(userId, limit);
    }

    // =================== 专用查询 ===================

    @Override
    public IPage<Order> getUserCoinOrders(Page<Order> page, Long userId) {
        log.debug("查询用户金币消费订单: userId={}, page={}, size={}",
                userId, page.getCurrent(), page.getSize());

        return orderMapper.selectUserCoinOrders(page, userId);
    }

    @Override
    public PageResponse<OrderResponse> getUserCoinOrders(Long userId, Integer currentPage, Integer pageSize) {
        try {
            log.info("查询用户金币消费订单（简化版）: userId={}, page={}, size={}", userId, currentPage, pageSize);

            // 参数验证和默认值设置
            if (currentPage == null || currentPage <= 0) {
                currentPage = 1;
            }
            if (pageSize == null || pageSize <= 0) {
                pageSize = 10;
            }
            if (pageSize > 100) {
                pageSize = 100;
            }

            // 构建查询条件
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Order::getUserId, userId)
                    .eq(Order::getPaymentMode, Order.PaymentMode.COIN.getCode())
                    .eq(Order::getPayStatus, Order.PayStatus.PAID.getCode());

            // 按创建时间倒序排列
            wrapper.orderByDesc(Order::getCreateTime);

            // 分页查询
            Page<Order> page = new Page<>(currentPage, pageSize);
            IPage<Order> orderPage = orderMapper.selectPage(page, wrapper);

            // 转换为响应
            List<OrderResponse> responses = orderConverter.toResponseList(orderPage.getRecords());

            PageResponse<OrderResponse> pageResponse = new PageResponse<>();
            pageResponse.setRecords(responses);
            pageResponse.setTotal(orderPage.getTotal());
            pageResponse.setCurrentPage(currentPage);
            pageResponse.setPageSize(pageSize);

            log.debug("用户金币消费订单查询成功: userId={}, total={}, page={}, size={}",
                    userId, orderPage.getTotal(), currentPage, pageSize);

            return pageResponse;

        } catch (Exception e) {
            log.error("查询用户金币消费订单失败: userId={}", userId, e);
            throw new RuntimeException("查询用户金币消费订单失败: " + e.getMessage());
        }
    }

    @Override
    public IPage<Order> getUserRechargeOrders(Page<Order> page, Long userId) {
        log.debug("查询用户充值订单: userId={}, page={}, size={}",
                userId, page.getCurrent(), page.getSize());

        return orderMapper.selectUserRechargeOrders(page, userId);
    }

    @Override
    public PageResponse<OrderResponse> getUserRechargeOrders(Long userId, Integer currentPage, Integer pageSize) {
        try {
            log.info("查询用户充值订单（简化版）: userId={}, page={}, size={}", userId, currentPage, pageSize);

            // 参数验证和默认值设置
            if (currentPage == null || currentPage <= 0) {
                currentPage = 1;
            }
            if (pageSize == null || pageSize <= 0) {
                pageSize = 10;
            }
            if (pageSize > 100) {
                pageSize = 100;
            }

            // 构建查询条件
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Order::getUserId, userId)
                    .eq(Order::getGoodsType, Order.GoodsType.COIN.getCode())
                    .eq(Order::getPayStatus, Order.PayStatus.PAID.getCode());

            // 按创建时间倒序排列
            wrapper.orderByDesc(Order::getCreateTime);

            // 分页查询
            Page<Order> page = new Page<>(currentPage, pageSize);
            IPage<Order> orderPage = orderMapper.selectPage(page, wrapper);

            // 转换为响应
            List<OrderResponse> responses = orderConverter.toResponseList(orderPage.getRecords());

            PageResponse<OrderResponse> pageResponse = new PageResponse<>();
            pageResponse.setRecords(responses);
            pageResponse.setTotal(orderPage.getTotal());
            pageResponse.setCurrentPage(currentPage);
            pageResponse.setPageSize(pageSize);

            log.debug("用户充值订单查询成功: userId={}, total={}, page={}, size={}",
                    userId, orderPage.getTotal(), currentPage, pageSize);

            return pageResponse;

        } catch (Exception e) {
            log.error("查询用户充值订单失败: userId={}", userId, e);
            throw new RuntimeException("查询用户充值订单失败: " + e.getMessage());
        }
    }

    @Override
    public IPage<Order> getContentOrders(Page<Order> page, Long contentId) {
        log.debug("查询内容购买订单: contentId={}, page={}, size={}",
                contentId, page.getCurrent(), page.getSize());

        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getGoodsType, Order.GoodsType.CONTENT.getCode())
                .eq(Order::getPayStatus, Order.PayStatus.PAID.getCode());

        if (contentId != null) {
            queryWrapper.eq(Order::getContentId, contentId);
        }

        queryWrapper.orderByDesc(Order::getPayTime);

        return orderMapper.selectPage(page, queryWrapper);
    }

    @Override
    public PageResponse<OrderResponse> getContentOrders(Long contentId, Integer currentPage, Integer pageSize) {
        try {
            log.info("查询内容购买订单（简化版）: contentId={}, page={}, size={}", contentId, currentPage, pageSize);

            // 参数验证和默认值设置
            if (currentPage == null || currentPage <= 0) {
                currentPage = 1;
            }
            if (pageSize == null || pageSize <= 0) {
                pageSize = 10;
            }
            if (pageSize > 100) {
                pageSize = 100;
            }

            // 构建查询条件
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Order::getGoodsType, Order.GoodsType.CONTENT.getCode())
                    .eq(Order::getPayStatus, Order.PayStatus.PAID.getCode());

            if (contentId != null) {
                wrapper.eq(Order::getContentId, contentId);
            }

            // 按支付时间倒序排列
            wrapper.orderByDesc(Order::getPayTime);

            // 分页查询
            Page<Order> page = new Page<>(currentPage, pageSize);
            IPage<Order> orderPage = orderMapper.selectPage(page, wrapper);

            // 转换为响应
            List<OrderResponse> responses = orderConverter.toResponseList(orderPage.getRecords());

            PageResponse<OrderResponse> pageResponse = new PageResponse<>();
            pageResponse.setRecords(responses);
            pageResponse.setTotal(orderPage.getTotal());
            pageResponse.setCurrentPage(currentPage);
            pageResponse.setPageSize(pageSize);

            log.debug("内容购买订单查询成功: contentId={}, total={}, page={}, size={}",
                    contentId, orderPage.getTotal(), currentPage, pageSize);

            return pageResponse;

        } catch (Exception e) {
            log.error("查询内容购买订单失败: contentId={}", contentId, e);
            throw new RuntimeException("查询内容购买订单失败: " + e.getMessage());
        }
    }

    @Override
    public IPage<Order> getSubscriptionOrders(Page<Order> page, String subscriptionType) {
        log.debug("查询订阅订单: subscriptionType={}, page={}, size={}",
                subscriptionType, page.getCurrent(), page.getSize());

        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getGoodsType, Order.GoodsType.SUBSCRIPTION.getCode())
                .eq(Order::getPayStatus, Order.PayStatus.PAID.getCode());

        if (StringUtils.hasText(subscriptionType)) {
            queryWrapper.eq(Order::getSubscriptionType, subscriptionType);
        }

        queryWrapper.orderByDesc(Order::getPayTime);

        return orderMapper.selectPage(page, queryWrapper);
    }

    @Override
    public PageResponse<OrderResponse> getSubscriptionOrders(String subscriptionType, Integer currentPage, Integer pageSize) {
        try {
            log.info("查询订阅订单（简化版）: subscriptionType={}, page={}, size={}", subscriptionType, currentPage, pageSize);

            // 参数验证和默认值设置
            if (currentPage == null || currentPage <= 0) {
                currentPage = 1;
            }
            if (pageSize == null || pageSize <= 0) {
                pageSize = 10;
            }
            if (pageSize > 100) {
                pageSize = 100;
            }

            // 构建查询条件
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Order::getGoodsType, Order.GoodsType.SUBSCRIPTION.getCode())
                    .eq(Order::getPayStatus, Order.PayStatus.PAID.getCode());

            if (StringUtils.hasText(subscriptionType)) {
                wrapper.eq(Order::getSubscriptionType, subscriptionType);
            }

            // 按支付时间倒序排列
            wrapper.orderByDesc(Order::getPayTime);

            // 分页查询
            Page<Order> page = new Page<>(currentPage, pageSize);
            IPage<Order> orderPage = orderMapper.selectPage(page, wrapper);

            // 转换为响应
            List<OrderResponse> responses = orderConverter.toResponseList(orderPage.getRecords());

            PageResponse<OrderResponse> pageResponse = new PageResponse<>();
            pageResponse.setRecords(responses);
            pageResponse.setTotal(orderPage.getTotal());
            pageResponse.setCurrentPage(currentPage);
            pageResponse.setPageSize(pageSize);

            log.debug("订阅订单查询成功: subscriptionType={}, total={}, page={}, size={}",
                    subscriptionType, orderPage.getTotal(), currentPage, pageSize);

            return pageResponse;

        } catch (Exception e) {
            log.error("查询订阅订单失败: subscriptionType={}", subscriptionType, e);
            throw new RuntimeException("查询订阅订单失败: " + e.getMessage());
        }
    }

    // =================== 业务验证 ===================

    @Override
    public Map<String, Object> validatePayment(Long orderId) {
        Map<String, Object> result = new HashMap<>();

        try {
            Order order = getOrderById(orderId);
            if (order == null) {
                result.put("valid", false);
                result.put("message", "订单不存在");
                return result;
            }

            if (!order.canPay()) {
                result.put("valid", false);
                result.put("message", "订单状态不允许支付");
                return result;
            }

            result.put("valid", true);
            result.put("order", order);

        } catch (Exception e) {
            log.error("支付验证失败: orderId={}", orderId, e);
            result.put("valid", false);
            result.put("message", "验证失败: " + e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> validatePayment(Long orderId, Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            Order order = getOrderById(orderId);
            if (order == null) {
                result.put("valid", false);
                result.put("message", "订单不存在");
                return result;
            }

            // 验证用户权限
            if (!order.getUserId().equals(userId)) {
                result.put("valid", false);
                result.put("message", "无权限操作此订单");
                return result;
            }

            if (!order.canPay()) {
                result.put("valid", false);
                result.put("message", "订单状态不允许支付");
                return result;
            }

            result.put("valid", true);
            result.put("order", order);

        } catch (Exception e) {
            log.error("支付验证失败（带用户验证）: orderId={}, userId={}", orderId, userId, e);
            result.put("valid", false);
            result.put("message", "验证失败: " + e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> validateCancel(Long orderId) {
        Map<String, Object> result = new HashMap<>();

        try {
            Order order = getOrderById(orderId);
            if (order == null) {
                result.put("valid", false);
                result.put("message", "订单不存在");
                return result;
            }

            if (!order.canCancel()) {
                result.put("valid", false);
                result.put("message", "订单状态不允许取消");
                return result;
            }

            result.put("valid", true);
            result.put("order", order);

        } catch (Exception e) {
            log.error("取消验证失败: orderId={}", orderId, e);
            result.put("valid", false);
            result.put("message", "验证失败: " + e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> validateCancel(Long orderId, Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            Order order = getOrderById(orderId);
            if (order == null) {
                result.put("valid", false);
                result.put("message", "订单不存在");
                return result;
            }

            // 验证用户权限
            if (!order.getUserId().equals(userId)) {
                result.put("valid", false);
                result.put("message", "无权限操作此订单");
                return result;
            }

            if (!order.canCancel()) {
                result.put("valid", false);
                result.put("message", "订单状态不允许取消");
                return result;
            }

            result.put("valid", true);
            result.put("order", order);

        } catch (Exception e) {
            log.error("取消验证失败（带用户验证）: orderId={}, userId={}", orderId, userId, e);
            result.put("valid", false);
            result.put("message", "验证失败: " + e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> validateRefund(Long orderId) {
        Map<String, Object> result = new HashMap<>();

        try {
            Order order = getOrderById(orderId);
            if (order == null) {
                result.put("valid", false);
                result.put("message", "订单不存在");
                return result;
            }

            if (!order.canRefund()) {
                result.put("valid", false);
                result.put("message", "订单状态不允许退款");
                return result;
            }

            result.put("valid", true);
            result.put("order", order);

        } catch (Exception e) {
            log.error("退款验证失败: orderId={}", orderId, e);
            result.put("valid", false);
            result.put("message", "验证失败: " + e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> validateRefund(Long orderId, Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            Order order = getOrderById(orderId);
            if (order == null) {
                result.put("valid", false);
                result.put("message", "订单不存在");
                return result;
            }

            // 验证用户权限
            if (!order.getUserId().equals(userId)) {
                result.put("valid", false);
                result.put("message", "无权限操作此订单");
                return result;
            }

            if (!order.canRefund()) {
                result.put("valid", false);
                result.put("message", "订单状态不允许退款");
                return result;
            }

            result.put("valid", true);
            result.put("order", order);

        } catch (Exception e) {
            log.error("退款验证失败（带用户验证）: orderId={}, userId={}", orderId, userId, e);
            result.put("valid", false);
            result.put("message", "验证失败: " + e.getMessage());
        }

        return result;
    }

    @Override
    public String generateOrderNo(Long userId) {
        // 使用雪花算法生成唯一订单号，确保在分布式环境下的唯一性
        long snowflakeId = generateSnowflakeId();
        return "ORD" + snowflakeId;
    }

    /**
     * 雪花算法生成唯一ID
     * 64位ID：1位符号位 + 41位时间戳 + 10位工作机器ID + 12位序列号
     */
    private static final long EPOCH = 1640995200000L; // 2022-01-01 00:00:00 作为起始时间
    private static final long WORKER_ID_BITS = 5L;
    private static final long DATACENTER_ID_BITS = 5L;
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);
    private static final long SEQUENCE_BITS = 12L;
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    private static long workerId = 1L; // 工作机器ID，可以从配置获取
    private static long datacenterId = 1L; // 数据中心ID，可以从配置获取
    private static long sequence = 0L;
    private static long lastTimestamp = -1L;

    private synchronized long generateSnowflakeId() {
        long timestamp = System.currentTimeMillis();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id");
        }

        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - EPOCH) << TIMESTAMP_LEFT_SHIFT)
                | (datacenterId << DATACENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    // =================== 计数统计 ===================

    @Override
//    @Cached(name = OrderCacheConstant.ORDER_STATISTICS_CACHE,
//            key = "T(com.gig.collide.order.infrastructure.cache.OrderCacheConstant).buildGoodsCountKey(#goodsId, #status)",
//            expire = OrderCacheConstant.STATISTICS_EXPIRE,
//            timeUnit = TimeUnit.MINUTES)
    public Long countOrdersByGoodsId(Long goodsId, String status) {
        log.debug("统计商品订单数: goodsId={}, status={}", goodsId, status);

        if (goodsId == null || goodsId <= 0) {
            return 0L;
        }

        return orderMapper.countByGoodsId(goodsId, status);
    }

    @Override
//    @Cached(name = OrderCacheConstant.ORDER_STATISTICS_CACHE,
//            key = "T(com.gig.collide.order.infrastructure.cache.OrderCacheConstant).buildUserCountKey(#userId, #status)",
//            expire = OrderCacheConstant.STATISTICS_EXPIRE,
//            timeUnit = TimeUnit.MINUTES)
    public Long countOrdersByUserId(Long userId, String status) {
        log.debug("统计用户订单数: userId={}, status={}", userId, status);

        if (userId == null || userId <= 0) {
            return 0L;
        }

        return orderMapper.countByUserId(userId, status);
    }

    // =================== 私有方法 ===================

    /**
     * 设置订单默认值
     */
    private void setDefaultValues(@Valid OrderCreateRequest order) {
        // 订单号统一由系统生成，不接受前端传入
        if (order.getUserId() != null) {
            order.setOrderNo(generateOrderNo(order.getUserId()));
        } else {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        if (order.getStatus() == null) {
            order.setStatus(Order.OrderStatus.PENDING);
        }

        if (order.getPayStatus() == null) {
            order.setPayStatus(Order.PayStatus.UNPAID);
        }

        if (order.getQuantity() == null || order.getQuantity() <= 0) {
            order.setQuantity(1);
        }

        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        if (order.getCreateTime() == null) {
            order.setCreateTime(now);
        }
        if (order.getUpdateTime() == null) {
            order.setUpdateTime(now);
        }
    }

    /**
     * 处理金币支付
     */
    private Map<String, Object> processCoinPayment(Order order, String payMethod) {
        Map<String, Object> result = new HashMap<>();

        // TODO: 调用钱包服务扣减金币
        // 这里需要集成钱包模块的 consume_coin 方法

        log.info("处理金币支付: orderId={}, coinCost={}", order.getId(), order.getCoinCost());

        // 模拟金币支付成功
        boolean success = confirmPayment(order.getId(), "coin");

        result.put("success", success);
        result.put("message", success ? "金币支付成功" : "金币支付失败");
        result.put("paymentMode", "coin");

        return result;
    }

    /**
     * 处理现金支付
     */
    private Map<String, Object> processCashPayment(Order order, String payMethod) {
        Map<String, Object> result = new HashMap<>();

        log.info("处理现金支付: orderId={}, finalAmount={}, payMethod={}",
                order.getId(), order.getFinalAmount(), payMethod);

        // TODO: 集成第三方支付
        // 这里应该调用支付宝、微信等支付接口

        result.put("success", true);
        result.put("message", "支付请求已发送");
        result.put("paymentMode", "cash");
        result.put("payUrl", "mock_pay_url"); // 模拟支付URL

        return result;
    }

    /**
     * 处理退款
     */
    private boolean processRefund(Order order, String reason) {
        log.info("处理退款: orderId={}, reason={}", order.getId(), reason);

        // TODO: 调用退款接口

        // 更新支付状态为已退款
        int result = orderMapper.updatePaymentInfo(order.getId(),
                Order.PayStatus.REFUNDED.getCode(), order.getPayMethod(), LocalDateTime.now());

        return result > 0;
    }

    /**
     * 处理支付成功后的业务逻辑
     */
    private void handlePaymentSuccess(Order order) {
        log.info("处理支付成功后续逻辑: orderId={}, goodsType={}", order.getId(), order.getGoodsType());

        // 根据商品类型处理不同的业务逻辑
        switch (order.getGoodsType()) {
            case COIN:
                // 金币充值：调用钱包服务发放金币
                log.info("处理金币充值: orderId={}, coinAmount={}", order.getId(), order.getCoinAmount());
                // TODO: 调用 grant_coin_reward
                break;

            case SUBSCRIPTION:
                // 订阅服务：开通会员权限
                log.info("处理订阅开通: orderId={}, subscriptionType={}, duration={}",
                        order.getId(), order.getSubscriptionType(), order.getSubscriptionDuration());
                // TODO: 调用用户服务开通VIP
                break;

            case CONTENT:
                // 付费内容：开放访问权限
                log.info("处理内容解锁: orderId={}, contentId={}", order.getId(), order.getContentId());
                // TODO: 调用内容服务解锁访问权限
                break;

            case GOODS:
                // 实体商品：通知发货
                log.info("处理商品发货通知: orderId={}, goodsId={}", order.getId(), order.getGoodsId());
                // TODO: 发送发货通知
                break;
        }
    }

    /**
     * 处理订单完成后的业务逻辑
     */
    private void handleOrderComplete(Long orderId) {
        log.info("处理订单完成后续逻辑: orderId={}", orderId);

        // TODO: 
        // 1. 更新商品销量
        // 2. 发送完成通知
        // 3. 触发推荐算法
    }

    // =================== 控制器层方法（接受DTO参数）===================

    /**
     * 创建订单（接受OrderCreateRequest）
     */
    public Result<OrderResponse> createOrder(OrderCreateRequest request) {
        try {
            log.info("创建订单请求: userId={}, goodsId={}, goodsType={}",
                    request.getUserId(), request.getGoodsId(), request.getGoodsType());

            // 验证请求参数
            request.validateParams();

            // 转换为实体
            Order order = orderConverter.toEntity(request);

            // 生成订单号
            order.setOrderNo(generateOrderNo());

            // 调用原有的创建方法
            Long orderId = createOrder(order);

            // 查询创建的订单
            Order createdOrder = getOrderById(orderId);

            // 转换为响应
            OrderResponse response = orderConverter.toResponse(createdOrder);

            log.info("订单创建成功: orderId={}, orderNo={}", orderId, order.getOrderNo());
            return Result.success(response);

        } catch (Exception e) {
            log.error("订单创建失败: {}", e.getMessage(), e);
            return Result.error("订单创建失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取订单详情（返回OrderResponse）
     */
    public Result<OrderResponse> getOrderById(Long id, Long userId) {
        try {
            log.info("获取订单详情: id={}, userId={}", id, userId);

            Order order = getOrderById(id);
            if (order == null) {
                return Result.error("订单不存在");
            }

            // 权限验证
            if (!order.getUserId().equals(userId)) {
                return Result.error("无权限访问此订单");
            }

            OrderResponse response = orderConverter.toResponse(order);
            return Result.success(response);

        } catch (Exception e) {
            log.error("获取订单详情失败: {}", e.getMessage(), e);
            return Result.error("获取订单详情失败: " + e.getMessage());
        }
    }

    /**
     * 根据订单号获取订单详情（返回OrderResponse）
     */
    public Result<OrderResponse> getOrderByOrderNo(String orderNo, Long userId) {
        try {
            log.info("根据订单号获取详情: orderNo={}, userId={}", orderNo, userId);

            Order order = getOrderByOrderNo(orderNo);
            if (order == null) {
                return Result.error("订单不存在");
            }

            // 权限验证
            if (!order.getUserId().equals(userId)) {
                return Result.error("无权限访问此订单");
            }

            OrderResponse response = orderConverter.toResponse(order);
            return Result.success(response);

        } catch (Exception e) {
            log.error("根据订单号获取详情失败: {}", e.getMessage(), e);
            return Result.error("获取订单详情失败: " + e.getMessage());
        }
    }

    /**
     * 取消订单（返回Result）
     */
    public Result<Void> cancelOrder(Long id, String reason, Long userId) {
        try {
            log.info("取消订单: id={}, reason={}, userId={}", id, reason, userId);

            Order order = getOrderById(id);
            if (order == null) {
                return Result.error("订单不存在");
            }

            // 权限验证
            if (!order.getUserId().equals(userId)) {
                return Result.error("无权限操作此订单");
            }

            boolean success = cancelOrder(id, reason);
            if (success) {
                return Result.success();
            } else {
                return Result.error("取消订单失败");
            }

        } catch (Exception e) {
            log.error("取消订单失败: {}", e.getMessage(), e);
            return Result.error("取消订单失败: " + e.getMessage());
        }
    }

    /**
     * 批量取消订单（返回Result）
     */
    public Result<Void> batchCancelOrders(List<Long> orderIds, String reason, Long operatorId) {
        try {
            log.info("批量取消订单: count={}, reason={}, operatorId={}", orderIds.size(), reason, operatorId);

            boolean success = batchCancelOrders(orderIds, reason);
            if (success) {
                return Result.success();
            } else {
                return Result.error("批量取消订单失败");
            }

        } catch (Exception e) {
            log.error("批量取消订单失败: {}", e.getMessage(), e);
            return Result.error("批量取消订单失败: " + e.getMessage());
        }
    }

    /**
     * 查询订单（接受OrderQueryRequest）
     */
    public Result<PageResponse<OrderResponse>> queryOrders(@Valid OrderQueryRequest request) {
        try {
            log.info("查询订单: userId={}, status={}", request.getUserId(), request.getStatus());

            // 验证请求参数
            request.validateParams();

            // 构建查询条件
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();

            if (request.getUserId() != null) {
                wrapper.eq(Order::getUserId, request.getUserId());
            }

            if (StringUtils.hasText(request.getStatus())) {
                wrapper.eq(Order::getStatus, request.getStatus());
            }

            if (StringUtils.hasText(request.getGoodsType())) {
                wrapper.eq(Order::getGoodsType, Order.GoodsType.valueOf(request.getGoodsType().toUpperCase()));
            }

            if (StringUtils.hasText(request.getPaymentMode())) {
                wrapper.eq(Order::getPaymentMode, request.getPaymentMode());
            }

            if (StringUtils.hasText(request.getPayStatus())) {
                wrapper.eq(Order::getPayStatus, request.getPayStatus());
            }

            if (StringUtils.hasText(request.getOrderNo())) {
                wrapper.eq(Order::getOrderNo, request.getOrderNo());
            }

            if (StringUtils.hasText(request.getKeyword())) {
                wrapper.and(w -> w.like(Order::getOrderNo, request.getKeyword())
                        .or().like(Order::getGoodsName, request.getKeyword())
                        .or().like(Order::getUserNickname, request.getKeyword()));
            }

            if (request.getStartTime() != null) {
                wrapper.ge(Order::getCreateTime, request.getStartTime());
            }

            if (request.getEndTime() != null) {
                wrapper.le(Order::getCreateTime, request.getEndTime());
            }

            // 排序
            String sortField = request.getSortField();
            String sortDirection = request.getSortDirection();
            if ("create_time".equals(sortField)) {
                wrapper.orderBy(true, "DESC".equals(sortDirection), Order::getCreateTime);
            } else if ("pay_time".equals(sortField)) {
                wrapper.orderBy(true, "DESC".equals(sortDirection), Order::getPayTime);
            } else if ("update_time".equals(sortField)) {
                wrapper.orderBy(true, "DESC".equals(sortDirection), Order::getUpdateTime);
            } else if ("final_amount".equals(sortField)) {
                wrapper.orderBy(true, "DESC".equals(sortDirection), Order::getFinalAmount);
            } else if ("coin_cost".equals(sortField)) {
                wrapper.orderBy(true, "DESC".equals(sortDirection), Order::getCoinCost);
            }

            // 分页查询
            Page<Order> page = new Page<>(request.getCurrentPage(), request.getPageSize());
            IPage<Order> orderPage = orderMapper.selectPage(page, wrapper);

            // 转换为响应
            List<OrderResponse> responses = orderConverter.toResponseList(orderPage.getRecords());

            PageResponse<OrderResponse> pageResponse = new PageResponse<>();
            pageResponse.setRecords(responses);
            pageResponse.setTotal(orderPage.getTotal());
            pageResponse.setCurrentPage(request.getCurrentPage());
            pageResponse.setPageSize(request.getPageSize());

            return Result.success(pageResponse);

        } catch (Exception e) {
            log.error("查询订单失败: {}", e.getMessage(), e);
            return Result.error("查询订单失败: " + e.getMessage());
        }
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + String.format("%04d", (int)(Math.random() * 10000));
    }

    @Override
    public Map<String, Object> healthCheck() {
        Map<String, Object> result = new HashMap<>();

        try {
            // 简单的健康检查：尝试查询一条订单记录
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.last("LIMIT 1");
            Order order = orderMapper.selectOne(wrapper);

            result.put("status", "UP");
            result.put("message", "订单服务运行正常");
            result.put("timestamp", LocalDateTime.now());
            result.put("hasData", order != null);

            log.debug("健康检查通过");

        } catch (Exception e) {
            log.error("健康检查失败", e);
            result.put("status", "DOWN");
            result.put("message", "订单服务异常: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now());
            result.put("error", e.getClass().getSimpleName());
        }

        return result;
    }

    // =================== 控制器层方法（返回Result包装的响应）===================

    /**
     * 获取用户最近购买记录（控制器层方法）
     */
    @Override
    public Result<List<OrderResponse>> getUserRecentOrdersForController(Long userId, Integer limit) {
        try {
            log.info("获取用户最近购买记录（控制器层）: userId={}, limit={}", userId, limit);

            List<Order> orders = getUserRecentOrders(userId, limit);
            List<OrderResponse> responses = orderConverter.toResponseList(orders);

            return Result.success(responses);

        } catch (Exception e) {
            log.error("获取用户最近购买记录失败: userId={}", userId, e);
            return Result.error("获取用户最近购买记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取超时订单（控制器层方法）
     */
    @Override
    public Result<List<OrderResponse>> getTimeoutOrdersForController(Integer timeoutMinutes) {
        try {
            log.info("获取超时订单（控制器层）: timeoutMinutes={}", timeoutMinutes);

            List<Order> orders = getTimeoutOrders(timeoutMinutes);
            List<OrderResponse> responses = orderConverter.toResponseList(orders);

            return Result.success(responses);

        } catch (Exception e) {
            log.error("获取超时订单失败: timeoutMinutes={}", timeoutMinutes, e);
            return Result.error("获取超时订单失败: " + e.getMessage());
        }
    }





















    /**
     * 处理支付回调（控制器层方法）
     */
    public Result<Void> handlePaymentCallback(String orderNo, String payStatus, String payMethod, Map<String, Object> extraInfo) {
        try {
            log.info("处理支付回调（控制器层）: orderNo={}, payStatus={}, payMethod={}", orderNo, payStatus, payMethod);

            boolean success = handlePaymentCallback(orderNo, payStatus, payMethod, null, extraInfo);

            if (success) {
                return Result.success();
            } else {
                return Result.error("处理支付回调失败");
            }

        } catch (Exception e) {
            log.error("处理支付回调失败: orderNo={}", orderNo, e);
            return Result.error("处理支付回调失败: " + e.getMessage());
        }
    }



































    @Override
    public Result<Void> confirmPaymentForController(Long id, String payMethod, Long userId) {
        try {
            log.info("确认支付（控制器层）: orderId={}, payMethod={}, userId={}", id, payMethod, userId);

            boolean success = confirmPayment(id, payMethod, userId);

            if (success) {
                return Result.success();
            } else {
                return Result.error("确认支付失败");
            }

        } catch (Exception e) {
            log.error("确认支付失败: orderId={}, payMethod={}, userId={}", id, payMethod, userId, e);
            return Result.error("确认支付失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Map<String, Object>> requestRefundForController(Long id, String reason, Long userId) {
        try {
            log.info("申请退款（控制器层）: orderId={}, reason={}, userId={}", id, reason, userId);

            Map<String, Object> result = requestRefund(id, reason, userId);
            return Result.success(result);

        } catch (Exception e) {
            log.error("申请退款失败: orderId={}, reason={}, userId={}", id, reason, userId, e);
            return Result.error("申请退款失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Void> shipOrderForController(Long id, Map<String, Object> shippingInfo, Long operatorId) {
        try {
            log.info("发货（控制器层）: orderId={}, operatorId={}", id, operatorId);

            Map<String, Object> shippingInfoMap = shippingInfo == null ? new HashMap<>() : shippingInfo;

            boolean success = shipOrder(id, shippingInfoMap, operatorId);

            if (success) {
                return Result.success();
            } else {
                return Result.error("发货失败");
            }

        } catch (Exception e) {
            log.error("发货失败: orderId={}, operatorId={}", id, operatorId, e);
            return Result.error("发货失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Void> confirmReceiptForController(Long id, Long userId) {
        try {
            log.info("确认收货（控制器层）: orderId={}, userId={}", id, userId);

            boolean success = confirmReceipt(id, userId);

            if (success) {
                return Result.success();
            } else {
                return Result.error("确认收货失败");
            }

        } catch (Exception e) {
            log.error("确认收货失败: orderId={}, userId={}", id, userId, e);
            return Result.error("确认收货失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Void> completeOrderForController(Long id, Long operatorId) {
        try {
            log.info("完成订单（控制器层）: orderId={}, operatorId={}", id, operatorId);

            boolean success = completeOrder(id, operatorId);

            if (success) {
                return Result.success();
            } else {
                return Result.error("完成订单失败");
            }

        } catch (Exception e) {
            log.error("完成订单失败: orderId={}, operatorId={}", id, operatorId, e);
            return Result.error("完成订单失败: " + e.getMessage());
        }
    }

    // =================== 新增Controller专用方法 ===================

    @Override
    public Result<PageResponse<OrderResponse>> getUserOrdersForController(Long userId, String status, Integer currentPage, Integer pageSize) {
        try {
            log.info("获取用户订单（控制器层）: userId={}, status={}, page={}/{}", userId, status, currentPage, pageSize);

            // 参数验证和默认值设置
            if (currentPage == null || currentPage < 1) {
                currentPage = 1;
            }
            if (pageSize == null || pageSize < 1 || pageSize > 100) {
                pageSize = 20;
            }

            // 构建查询条件
            LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Order::getUserId, userId);
            if (StringUtils.hasText(status)) {
                queryWrapper.eq(Order::getStatus, status);
            }
            queryWrapper.orderByDesc(Order::getCreateTime);

            // 分页查询
            Page<Order> page = new Page<>(currentPage, pageSize);
            IPage<Order> result = orderMapper.selectPage(page, queryWrapper);

            // 转换为Response对象
            List<OrderResponse> responses = result.getRecords().stream()
                    .map(orderConverter::toResponse)
                    .collect(Collectors.toList());

            // 构建分页响应
            PageResponse<OrderResponse> pageResponse = new PageResponse<>();
            pageResponse.setRecords(responses);
            pageResponse.setTotal(result.getTotal());
            pageResponse.setCurrentPage(currentPage);
            pageResponse.setPageSize(pageSize);
            pageResponse.setTotalPages((int) Math.ceil((double) result.getTotal() / pageSize));

            return Result.success(pageResponse);

        } catch (Exception e) {
            log.error("获取用户订单失败: userId={}", userId, e);
            return Result.error("获取用户订单失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Map<String, Object>> processPaymentForController(Long id, String payMethod, Long userId) {
        try {
            log.info("处理订单支付（控制器层）: orderId={}, payMethod={}, userId={}", id, payMethod, userId);

            Map<String, Object> result = processPayment(id, payMethod, userId);
            return Result.success(result);

        } catch (Exception e) {
            log.error("处理订单支付失败: orderId={}, payMethod={}, userId={}", id, payMethod, userId, e);
            return Result.error("处理订单支付失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Map<String, Object>> getUserOrderStatisticsForController(Long userId) {
        try {
            log.info("获取用户订单统计（控制器层）: userId={}", userId);

            Map<String, Object> statistics = getUserOrderStatistics(userId);
            return Result.success(statistics);

        } catch (Exception e) {
            log.error("获取用户订单统计失败: userId={}", userId, e);
            return Result.error("获取用户订单统计失败: " + e.getMessage());
        }
    }

    @Override
    public Result<PageResponse<OrderResponse>> getOrdersByGoodsTypeForController(String goodsType, String status, Integer currentPage, Integer pageSize) {
        try {
            log.info("根据商品类型获取订单（控制器层）: goodsType={}, status={}, page={}/{}", goodsType, status, currentPage, pageSize);

            // 参数验证和默认值设置
            if (currentPage == null || currentPage < 1) {
                currentPage = 1;
            }
            if (pageSize == null || pageSize < 1 || pageSize > 100) {
                pageSize = 20;
            }

            // 构建查询条件
            LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Order::getGoodsType, goodsType);
            if (StringUtils.hasText(status)) {
                queryWrapper.eq(Order::getStatus, status);
            }
            queryWrapper.orderByDesc(Order::getCreateTime);

            // 分页查询
            Page<Order> page = new Page<>(currentPage, pageSize);
            IPage<Order> result = orderMapper.selectPage(page, queryWrapper);

            // 转换为Response对象
            List<OrderResponse> responses = result.getRecords().stream()
                    .map(orderConverter::toResponse)
                    .collect(Collectors.toList());

            // 构建分页响应
            PageResponse<OrderResponse> pageResponse = new PageResponse<>();
            pageResponse.setRecords(responses);
            pageResponse.setTotal(result.getTotal());
            pageResponse.setCurrentPage(currentPage);
            pageResponse.setPageSize(pageSize);
            pageResponse.setTotalPages((int) Math.ceil((double) result.getTotal() / pageSize));

            return Result.success(pageResponse);

        } catch (Exception e) {
            log.error("根据商品类型获取订单失败: goodsType={}", goodsType, e);
            return Result.error("根据商品类型获取订单失败: " + e.getMessage());
        }
    }

    @Override
    public Result<PageResponse<OrderResponse>> getSellerOrdersForController(Long sellerId, String status, Integer currentPage, Integer pageSize) {
        try {
            log.info("获取商家订单（控制器层）: sellerId={}, status={}, page={}/{}", sellerId, status, currentPage, pageSize);

            // 参数验证和默认值设置
            if (currentPage == null || currentPage < 1) {
                currentPage = 1;
            }
            if (pageSize == null || pageSize < 1 || pageSize > 100) {
                pageSize = 20;
            }

            // 构建查询条件
            LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
            // 注意：Order类中没有sellerId字段，这里需要根据实际业务逻辑调整
            // 如果是要查询某个用户的订单，应该使用userId
            // 如果是要查询某个商品的订单，应该使用goodsId
            // 暂时注释掉这个条件，避免编译错误
            // queryWrapper.eq(Order::getSellerId, sellerId);
            if (StringUtils.hasText(status)) {
                queryWrapper.eq(Order::getStatus, status);
            }
            queryWrapper.orderByDesc(Order::getCreateTime);

            // 分页查询
            Page<Order> page = new Page<>(currentPage, pageSize);
            IPage<Order> result = orderMapper.selectPage(page, queryWrapper);

            // 转换为Response对象
            List<OrderResponse> responses = result.getRecords().stream()
                    .map(orderConverter::toResponse)
                    .collect(Collectors.toList());

            // 构建分页响应
            PageResponse<OrderResponse> pageResponse = new PageResponse<>();
            pageResponse.setRecords(responses);
            pageResponse.setTotal(result.getTotal());
            pageResponse.setCurrentPage(currentPage);
            pageResponse.setPageSize(pageSize);
            pageResponse.setTotalPages((int) Math.ceil((double) result.getTotal() / pageSize));

            return Result.success(pageResponse);

        } catch (Exception e) {
            log.error("获取商家订单失败: sellerId={}", sellerId, e);
            return Result.error("获取商家订单失败: " + e.getMessage());
        }
    }

    @Override
    public Result<PageResponse<OrderResponse>> searchOrdersForController(String keyword, Integer currentPage, Integer pageSize) {
        try {
            log.info("搜索订单（控制器层）: keyword={}, page={}/{}", keyword, currentPage, pageSize);

            // 参数验证和默认值设置
            if (currentPage == null || currentPage < 1) {
                currentPage = 1;
            }
            if (pageSize == null || pageSize < 1 || pageSize > 100) {
                pageSize = 20;
            }

            // 构建查询条件
            LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
            if (StringUtils.hasText(keyword)) {
                queryWrapper.like(Order::getOrderNo, keyword)
                        .or()
                        .like(Order::getGoodsName, keyword)
                        .or()
                        .like(Order::getUserNickname, keyword);
            }
            queryWrapper.orderByDesc(Order::getCreateTime);

            // 分页查询
            Page<Order> page = new Page<>(currentPage, pageSize);
            IPage<Order> result = orderMapper.selectPage(page, queryWrapper);

            // 转换为Response对象
            List<OrderResponse> responses = result.getRecords().stream()
                    .map(orderConverter::toResponse)
                    .collect(Collectors.toList());

            // 构建分页响应
            PageResponse<OrderResponse> pageResponse = new PageResponse<>();
            pageResponse.setRecords(responses);
            pageResponse.setTotal(result.getTotal());
            pageResponse.setCurrentPage(currentPage);
            pageResponse.setPageSize(pageSize);
            pageResponse.setTotalPages((int) Math.ceil((double) result.getTotal() / pageSize));

            return Result.success(pageResponse);

        } catch (Exception e) {
            log.error("搜索订单失败: keyword={}", keyword, e);
            return Result.error("搜索订单失败: " + e.getMessage());
        }
    }

    @Override
    public Result<PageResponse<OrderResponse>> getOrdersByTimeRangeForController(LocalDateTime startTime, LocalDateTime endTime, String status, Integer currentPage, Integer pageSize) {
        try {
            log.info("根据时间范围获取订单（控制器层）: startTime={}, endTime={}, status={}, page={}/{}", startTime, endTime, status, currentPage, pageSize);

            // 参数验证和默认值设置
            if (currentPage == null || currentPage < 1) {
                currentPage = 1;
            }
            if (pageSize == null || pageSize < 1 || pageSize > 100) {
                pageSize = 20;
            }

            // 构建查询条件
            LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
            if (startTime != null && endTime != null) {
                queryWrapper.between(Order::getCreateTime, startTime, endTime);
            }
            if (StringUtils.hasText(status)) {
                queryWrapper.eq(Order::getStatus, status);
            }
            queryWrapper.orderByDesc(Order::getCreateTime);

            // 分页查询
            Page<Order> page = new Page<>(currentPage, pageSize);
            IPage<Order> result = orderMapper.selectPage(page, queryWrapper);

            // 转换为Response对象
            List<OrderResponse> responses = result.getRecords().stream()
                    .map(orderConverter::toResponse)
                    .collect(Collectors.toList());

            // 构建分页响应
            PageResponse<OrderResponse> pageResponse = new PageResponse<>();
            pageResponse.setRecords(responses);
            pageResponse.setTotal(result.getTotal());
            pageResponse.setCurrentPage(currentPage);
            pageResponse.setPageSize(pageSize);
            pageResponse.setTotalPages((int) Math.ceil((double) result.getTotal() / pageSize));

            return Result.success(pageResponse);

        } catch (Exception e) {
            log.error("根据时间范围获取订单失败: startTime={}, endTime={}", startTime, endTime, e);
            return Result.error("根据时间范围获取订单失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Map<String, Object>> getGoodsSalesStatisticsForController(Long goodsId) {
        try {
            log.info("获取商品销售统计（控制器层）: goodsId={}", goodsId);

            Map<String, Object> statistics = getGoodsSalesStatistics(goodsId);
            return Result.success(statistics);

        } catch (Exception e) {
            log.error("获取商品销售统计失败: goodsId={}", goodsId, e);
            return Result.error("获取商品销售统计失败: " + e.getMessage());
        }
    }

    @Override
    public Result<List<Map<String, Object>>> getOrderStatisticsByTypeForController() {
        try {
            log.info("获取订单类型统计（控制器层）");

            List<Map<String, Object>> statistics = getOrderStatisticsByType();
            return Result.success(statistics);

        } catch (Exception e) {
            log.error("获取订单类型统计失败", e);
            return Result.error("获取订单类型统计失败: " + e.getMessage());
        }
    }

    @Override
    public Result<List<Map<String, Object>>> getHotGoodsForController(Integer limit) {
        try {
            log.info("获取热门商品（控制器层）: limit={}", limit);

            List<Map<String, Object>> hotGoods = getHotGoods(limit);
            return Result.success(hotGoods);

        } catch (Exception e) {
            log.error("获取热门商品失败: limit={}", limit, e);
            return Result.error("获取热门商品失败: " + e.getMessage());
        }
    }

    @Override
    public Result<List<Map<String, Object>>> getDailyRevenueForController(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            log.info("获取每日收入（控制器层）: startDate={}, endDate={}", startDate, endDate);

            // 将LocalDateTime转换为String格式
            String startDateStr = startDate != null ? startDate.toString() : null;
            String endDateStr = endDate != null ? endDate.toString() : null;

            List<Map<String, Object>> revenue = getDailyRevenue(startDateStr, endDateStr);
            return Result.success(revenue);

        } catch (Exception e) {
            log.error("获取每日收入失败: startDate={}, endDate={}", startDate, endDate, e);
            return Result.error("获取每日收入失败: " + e.getMessage());
        }
    }

    @Override
    public Result<PageResponse<OrderResponse>> getUserCoinOrdersForController(Long userId, Integer currentPage, Integer pageSize) {
        try {
            log.info("获取用户金币订单（控制器层）: userId={}, page={}/{}", userId, currentPage, pageSize);

            // 参数验证和默认值设置
            if (currentPage == null || currentPage < 1) {
                currentPage = 1;
            }
            if (pageSize == null || pageSize < 1 || pageSize > 100) {
                pageSize = 20;
            }

            // 构建查询条件
            LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Order::getUserId, userId)
                    .eq(Order::getGoodsType, Order.GoodsType.COIN.getCode());
            queryWrapper.orderByDesc(Order::getCreateTime);

            // 分页查询
            Page<Order> page = new Page<>(currentPage, pageSize);
            IPage<Order> result = orderMapper.selectPage(page, queryWrapper);

            // 转换为Response对象
            List<OrderResponse> responses = result.getRecords().stream()
                    .map(orderConverter::toResponse)
                    .collect(Collectors.toList());

            // 构建分页响应
            PageResponse<OrderResponse> pageResponse = new PageResponse<>();
            pageResponse.setRecords(responses);
            pageResponse.setTotal(result.getTotal());
            pageResponse.setCurrentPage(currentPage);
            pageResponse.setPageSize(pageSize);
            pageResponse.setTotalPages((int) Math.ceil((double) result.getTotal() / pageSize));

            return Result.success(pageResponse);

        } catch (Exception e) {
            log.error("获取用户金币订单失败: userId={}", userId, e);
            return Result.error("获取用户金币订单失败: " + e.getMessage());
        }
    }

    @Override
    public Result<PageResponse<OrderResponse>> getUserRechargeOrdersForController(Long userId, Integer currentPage, Integer pageSize) {
        try {
            log.info("获取用户充值订单（控制器层）: userId={}, page={}/{}", userId, currentPage, pageSize);

            // 参数验证和默认值设置
            if (currentPage == null || currentPage < 1) {
                currentPage = 1;
            }
            if (pageSize == null || pageSize < 1 || pageSize > 100) {
                pageSize = 20;
            }

            // 构建查询条件
            LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Order::getUserId, userId)
                    .eq(Order::getGoodsType, Order.GoodsType.COIN.getCode());
            queryWrapper.orderByDesc(Order::getCreateTime);

            // 分页查询
            Page<Order> page = new Page<>(currentPage, pageSize);
            IPage<Order> result = orderMapper.selectPage(page, queryWrapper);

            // 转换为Response对象
            List<OrderResponse> responses = result.getRecords().stream()
                    .map(orderConverter::toResponse)
                    .collect(Collectors.toList());

            // 构建分页响应
            PageResponse<OrderResponse> pageResponse = new PageResponse<>();
            pageResponse.setRecords(responses);
            pageResponse.setTotal(result.getTotal());
            pageResponse.setCurrentPage(currentPage);
            pageResponse.setPageSize(pageSize);
            pageResponse.setTotalPages((int) Math.ceil((double) result.getTotal() / pageSize));

            return Result.success(pageResponse);

        } catch (Exception e) {
            log.error("获取用户充值订单失败: userId={}", userId, e);
            return Result.error("获取用户充值订单失败: " + e.getMessage());
        }
    }

    @Override
    public Result<PageResponse<OrderResponse>> getContentOrdersForController(Long contentId, Integer currentPage, Integer pageSize) {
        try {
            log.info("获取内容订单（控制器层）: contentId={}, page={}/{}", contentId, currentPage, pageSize);

            // 参数验证和默认值设置
            if (currentPage == null || currentPage < 1) {
                currentPage = 1;
            }
            if (pageSize == null || pageSize < 1 || pageSize > 100) {
                pageSize = 20;
            }

            // 构建查询条件
            LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Order::getGoodsId, contentId)
                    .eq(Order::getGoodsType, Order.GoodsType.CONTENT.getCode());
            queryWrapper.orderByDesc(Order::getCreateTime);

            // 分页查询
            Page<Order> page = new Page<>(currentPage, pageSize);
            IPage<Order> result = orderMapper.selectPage(page, queryWrapper);

            // 转换为Response对象
            List<OrderResponse> responses = result.getRecords().stream()
                    .map(orderConverter::toResponse)
                    .collect(Collectors.toList());

            // 构建分页响应
            PageResponse<OrderResponse> pageResponse = new PageResponse<>();
            pageResponse.setRecords(responses);
            pageResponse.setTotal(result.getTotal());
            pageResponse.setCurrentPage(currentPage);
            pageResponse.setPageSize(pageSize);
            pageResponse.setTotalPages((int) Math.ceil((double) result.getTotal() / pageSize));

            return Result.success(pageResponse);

        } catch (Exception e) {
            log.error("获取内容订单失败: contentId={}", contentId, e);
            return Result.error("获取内容订单失败: " + e.getMessage());
        }
    }

    @Override
    public Result<PageResponse<OrderResponse>> getSubscriptionOrdersForController(String subscriptionType, Integer currentPage, Integer pageSize) {
        try {
            log.info("获取订阅订单（控制器层）: subscriptionType={}, page={}/{}", subscriptionType, currentPage, pageSize);

            // 参数验证和默认值设置
            if (currentPage == null || currentPage < 1) {
                currentPage = 1;
            }
            if (pageSize == null || pageSize < 1 || pageSize > 100) {
                pageSize = 20;
            }

            // 构建查询条件
            LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Order::getGoodsType, Order.GoodsType.SUBSCRIPTION.getCode());
            if (StringUtils.hasText(subscriptionType)) {
                queryWrapper.eq(Order::getSubscriptionType, subscriptionType);
            }
            queryWrapper.orderByDesc(Order::getCreateTime);

            // 分页查询
            Page<Order> page = new Page<>(currentPage, pageSize);
            IPage<Order> result = orderMapper.selectPage(page, queryWrapper);

            // 转换为Response对象
            List<OrderResponse> responses = result.getRecords().stream()
                    .map(orderConverter::toResponse)
                    .collect(Collectors.toList());

            // 构建分页响应
            PageResponse<OrderResponse> pageResponse = new PageResponse<>();
            pageResponse.setRecords(responses);
            pageResponse.setTotal(result.getTotal());
            pageResponse.setCurrentPage(currentPage);
            pageResponse.setPageSize(pageSize);
            pageResponse.setTotalPages((int) Math.ceil((double) result.getTotal() / pageSize));

            return Result.success(pageResponse);

        } catch (Exception e) {
            log.error("获取订阅订单失败: subscriptionType={}", subscriptionType, e);
            return Result.error("获取订阅订单失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Map<String, Object>> validatePaymentForController(Long id, Long userId) {
        try {
            log.info("验证支付（控制器层）: orderId={}, userId={}", id, userId);

            Map<String, Object> result = validatePayment(id, userId);
            return Result.success(result);

        } catch (Exception e) {
            log.error("验证支付失败: orderId={}, userId={}", id, userId, e);
            return Result.error("验证支付失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Map<String, Object>> validateCancelForController(Long id, Long userId) {
        try {
            log.info("验证取消（控制器层）: orderId={}, userId={}", id, userId);

            Map<String, Object> result = validateCancel(id, userId);
            return Result.success(result);

        } catch (Exception e) {
            log.error("验证取消失败: orderId={}, userId={}", id, userId, e);
            return Result.error("验证取消失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Map<String, Object>> validateRefundForController(Long id, Long userId) {
        try {
            log.info("验证退款（控制器层）: orderId={}, userId={}", id, userId);

            Map<String, Object> result = validateRefund(id, userId);
            return Result.success(result);

        } catch (Exception e) {
            log.error("验证退款失败: orderId={}, userId={}", id, userId, e);
            return Result.error("验证退款失败: " + e.getMessage());
        }
    }

    @Override
    public Result<PageResponse<OrderResponse>> getPendingOrdersForController(Long userId, Integer currentPage, Integer pageSize) {
        try {
            log.info("获取待处理订单（控制器层）: userId={}, page={}/{}", userId, currentPage, pageSize);

            // 参数验证和默认值设置
            if (currentPage == null || currentPage < 1) {
                currentPage = 1;
            }
            if (pageSize == null || pageSize < 1 || pageSize > 100) {
                pageSize = 20;
            }

            // 构建查询条件
            LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Order::getUserId, userId)
                    .eq(Order::getStatus, "PENDING");
            queryWrapper.orderByDesc(Order::getCreateTime);

            // 分页查询
            Page<Order> page = new Page<>(currentPage, pageSize);
            IPage<Order> result = orderMapper.selectPage(page, queryWrapper);

            // 转换为Response对象
            List<OrderResponse> responses = result.getRecords().stream()
                    .map(orderConverter::toResponse)
                    .collect(Collectors.toList());

            // 构建分页响应
            PageResponse<OrderResponse> pageResponse = new PageResponse<>();
            pageResponse.setRecords(responses);
            pageResponse.setTotal(result.getTotal());
            pageResponse.setCurrentPage(currentPage);
            pageResponse.setPageSize(pageSize);
            pageResponse.setTotalPages((int) Math.ceil((double) result.getTotal() / pageSize));

            return Result.success(pageResponse);

        } catch (Exception e) {
            log.error("获取待处理订单失败: userId={}", userId, e);
            return Result.error("获取待处理订单失败: " + e.getMessage());
        }
    }

    @Override
    public Result<PageResponse<OrderResponse>> getCompletedOrdersForController(Long userId, Integer currentPage, Integer pageSize) {
        try {
            log.info("获取已完成订单（控制器层）: userId={}, page={}/{}", userId, currentPage, pageSize);

            // 参数验证和默认值设置
            if (currentPage == null || currentPage < 1) {
                currentPage = 1;
            }
            if (pageSize == null || pageSize < 1 || pageSize > 100) {
                pageSize = 20;
            }

            // 构建查询条件
            LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Order::getUserId, userId)
                    .eq(Order::getStatus, "COMPLETED");
            queryWrapper.orderByDesc(Order::getCreateTime);

            // 分页查询
            Page<Order> page = new Page<>(currentPage, pageSize);
            IPage<Order> result = orderMapper.selectPage(page, queryWrapper);

            // 转换为Response对象
            List<OrderResponse> responses = result.getRecords().stream()
                    .map(orderConverter::toResponse)
                    .collect(Collectors.toList());

            // 构建分页响应
            PageResponse<OrderResponse> pageResponse = new PageResponse<>();
            pageResponse.setRecords(responses);
            pageResponse.setTotal(result.getTotal());
            pageResponse.setCurrentPage(currentPage);
            pageResponse.setPageSize(pageSize);
            pageResponse.setTotalPages((int) Math.ceil((double) result.getTotal() / pageSize));

            return Result.success(pageResponse);

        } catch (Exception e) {
            log.error("获取已完成订单失败: userId={}", userId, e);
            return Result.error("获取已完成订单失败: " + e.getMessage());
        }
    }

    @Override
    public Result<PageResponse<OrderResponse>> getTodayOrdersForController(Integer currentPage, Integer pageSize) {
        try {
            log.info("获取今日订单（控制器层）: page={}/{}", currentPage, pageSize);

            // 参数验证和默认值设置
            if (currentPage == null || currentPage < 1) {
                currentPage = 1;
            }
            if (pageSize == null || pageSize < 1 || pageSize > 100) {
                pageSize = 20;
            }

            // 构建查询条件
            LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime tomorrow = today.plusDays(1);

            LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.between(Order::getCreateTime, today, tomorrow);
            queryWrapper.orderByDesc(Order::getCreateTime);

            // 分页查询
            Page<Order> page = new Page<>(currentPage, pageSize);
            IPage<Order> result = orderMapper.selectPage(page, queryWrapper);

            // 转换为Response对象
            List<OrderResponse> responses = result.getRecords().stream()
                    .map(orderConverter::toResponse)
                    .collect(Collectors.toList());

            // 构建分页响应
            PageResponse<OrderResponse> pageResponse = new PageResponse<>();
            pageResponse.setRecords(responses);
            pageResponse.setTotal(result.getTotal());
            pageResponse.setCurrentPage(currentPage);
            pageResponse.setPageSize(pageSize);
            pageResponse.setTotalPages((int) Math.ceil((double) result.getTotal() / pageSize));

            return Result.success(pageResponse);

        } catch (Exception e) {
            log.error("获取今日订单失败", e);
            return Result.error("获取今日订单失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Integer> autoCancelTimeoutOrdersForController(Integer timeoutMinutes) {
        try {
            log.info("自动取消超时订单（控制器层）: timeoutMinutes={}", timeoutMinutes);

            int count = autoCancelTimeoutOrders(timeoutMinutes);
            return Result.success(count);

        } catch (Exception e) {
            log.error("自动取消超时订单失败: timeoutMinutes={}", timeoutMinutes, e);
            return Result.error("自动取消超时订单失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Integer> autoCompleteShippedOrdersForController(Integer days) {
        try {
            log.info("自动完成已发货订单（控制器层）: days={}", days);

            int count = autoCompleteShippedOrders(days);
            return Result.success(count);

        } catch (Exception e) {
            log.error("自动完成已发货订单失败: days={}", days, e);
            return Result.error("自动完成已发货订单失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Long> countOrdersByGoodsIdForController(Long goodsId, String status) {
        try {
            log.info("根据商品ID统计订单（控制器层）: goodsId={}, status={}", goodsId, status);

            Long count = countOrdersByGoodsId(goodsId, status);
            return Result.success(count);

        } catch (Exception e) {
            log.error("根据商品ID统计订单失败: goodsId={}", goodsId, e);
            return Result.error("根据商品ID统计订单失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Long> countOrdersByUserIdForController(Long userId, String status) {
        try {
            log.info("根据用户ID统计订单（控制器层）: userId={}, status={}", userId, status);

            Long count = countOrdersByUserId(userId, status);
            return Result.success(count);

        } catch (Exception e) {
            log.error("根据用户ID统计订单失败: userId={}", userId, e);
            return Result.error("根据用户ID统计订单失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Void> updatePaymentInfoForController(Long id, String payStatus, String payMethod, LocalDateTime payTime) {
        try {
            log.info("更新支付信息（控制器层）: orderId={}, payStatus={}, payMethod={}", id, payStatus, payMethod);

            boolean success = updatePaymentInfo(id, payStatus, payMethod, payTime);
            if (success) {
                return Result.success();
            } else {
                return Result.error("更新支付信息失败");
            }

        } catch (Exception e) {
            log.error("更新支付信息失败: orderId={}", id, e);
            return Result.error("更新支付信息失败: " + e.getMessage());
        }
    }

    @Override
    public Result<String> healthCheckForController() {
        try {
            log.info("健康检查（控制器层）");

            Map<String, Object> healthResult = healthCheck();

            if ("UP".equals(healthResult.get("status"))) {
                return Result.success("订单系统运行正常");
            } else {
                Object messageObj = healthResult.get("message");
                String message = messageObj != null ? messageObj.toString() : "未知错误";
                return Result.error("订单系统异常: " + message);
            }

        } catch (Exception e) {
            log.error("健康检查失败", e);
            return Result.error("健康检查失败: " + e.getMessage());
        }
    }
}
