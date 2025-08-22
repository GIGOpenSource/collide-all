package com.gig.collide.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gig.collide.Apientry.api.common.response.PageResponse;
import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.Apientry.api.payment.response.PaymentResponse;
import com.gig.collide.domain.Payment;
import com.gig.collide.mapper.PaymentMapper;
import com.gig.collide.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 支付服务实现类 - 简洁版
 *
 * @author GIG Team
 * @version 2.0.0
 */
@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentMapper paymentMapper;

    private final Random random = new Random();

    @Override
    @Transactional
    public Payment createPayment(Payment payment) {
        // 生成支付单号
        if (!StringUtils.hasText(payment.getPaymentNo())) {
            payment.setPaymentNo(generatePaymentNo());
        }

        // 设置默认状态
        if (!StringUtils.hasText(payment.getStatus())) {
            payment.setStatus("pending");
        }

        paymentMapper.insert(payment);
        return payment;
    }

    @Override
    public Payment getPaymentById(Long paymentId) {
        return paymentMapper.selectById(paymentId);
    }

    @Override
    public Payment getPaymentByNo(String paymentNo) {
        return paymentMapper.selectByPaymentNo(paymentNo);
    }

    @Override
    public IPage<Payment> queryPayments(Long userId, String payMethod, String status,
                                        String paymentNo, String orderNo, Long orderId,
                                        String thirdPartyNo, BigDecimal minAmount, BigDecimal maxAmount,
                                        LocalDateTime startTime, LocalDateTime endTime,
                                        int pageNum, int pageSize, String sortBy, String sortDirection) {
        Page<Payment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Payment> queryWrapper = new LambdaQueryWrapper<>();

        // 动态查询条件
        if (userId != null) {
            queryWrapper.eq(Payment::getUserId, userId);
        }
        if (StringUtils.hasText(payMethod)) {
            queryWrapper.eq(Payment::getPayMethod, payMethod);
        }
        if (StringUtils.hasText(status)) {
            queryWrapper.eq(Payment::getStatus, status);
        }
        if (StringUtils.hasText(paymentNo)) {
            queryWrapper.like(Payment::getPaymentNo, paymentNo);
        }
        if (StringUtils.hasText(orderNo)) {
            queryWrapper.like(Payment::getOrderNo, orderNo);
        }
        if (orderId != null) {
            queryWrapper.eq(Payment::getOrderId, orderId);
        }
        if (StringUtils.hasText(thirdPartyNo)) {
            queryWrapper.eq(Payment::getThirdPartyNo, thirdPartyNo);
        }
        if (minAmount != null) {
            queryWrapper.ge(Payment::getAmount, minAmount);
        }
        if (maxAmount != null) {
            queryWrapper.le(Payment::getAmount, maxAmount);
        }
        if (startTime != null) {
            queryWrapper.ge(Payment::getCreateTime, startTime);
        }
        if (endTime != null) {
            queryWrapper.le(Payment::getCreateTime, endTime);
        }

        // 排序
        if ("amount".equals(sortBy)) {
            if ("asc".equals(sortDirection)) {
                queryWrapper.orderByAsc(Payment::getAmount);
            } else {
                queryWrapper.orderByDesc(Payment::getAmount);
            }
        } else if ("pay_time".equals(sortBy)) {
            if ("asc".equals(sortDirection)) {
                queryWrapper.orderByAsc(Payment::getPayTime);
            } else {
                queryWrapper.orderByDesc(Payment::getPayTime);
            }
        } else {
            // 默认按创建时间排序
            if ("asc".equals(sortDirection)) {
                queryWrapper.orderByAsc(Payment::getCreateTime);
            } else {
                queryWrapper.orderByDesc(Payment::getCreateTime);
            }
        }

        return paymentMapper.selectPage(page, queryWrapper);
    }

    @Override
    @Transactional
    public void handlePaymentCallback(String paymentNo, String status, String thirdPartyNo,
                                      LocalDateTime payTime, LocalDateTime notifyTime) {
        Payment payment = getPaymentByNo(paymentNo);
        if (payment == null) {
            log.warn("支付回调失败：未找到支付记录，支付单号: {}", paymentNo);
            return;
        }

        // 更新支付状态
        paymentMapper.updatePaymentStatus(paymentNo, status, thirdPartyNo, payTime, notifyTime);
        log.info("支付回调处理成功：支付单号:{}, 状态:{}", paymentNo, status);
    }

    @Override
    @Transactional
    public void cancelPayment(Long paymentId) {
        Payment payment = getPaymentById(paymentId);
        if (payment == null) {
            throw new RuntimeException("支付记录不存在");
        }

        if (!"pending".equals(payment.getStatus())) {
            throw new RuntimeException("只能取消待支付的订单");
        }

        payment.setStatus("cancelled");
        paymentMapper.updateById(payment);
    }

    @Override
    public Payment syncPaymentStatus(String paymentNo) {
        // 这里应该调用第三方支付接口查询状态
        // 暂时返回当前状态
        return getPaymentByNo(paymentNo);
    }

    @Override
    public List<Payment> getUserPayments(Long userId, Integer limit) {
        return paymentMapper.selectByUserId(userId, limit);
    }

    @Override
    public List<Payment> getPaymentsByOrderId(Long orderId) {
        return paymentMapper.selectByOrderId(orderId);
    }

    @Override
    public boolean verifyPaymentStatus(String paymentNo) {
        Payment payment = getPaymentByNo(paymentNo);
        return payment != null && "success".equals(payment.getStatus());
    }

    @Override
    public String generatePaymentNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomSuffix = String.format("%06d", random.nextInt(1000000));
        return "PAY" + timestamp + randomSuffix;
    }

    @Override
    public boolean canCancelPayment(Long paymentId) {
        Payment payment = getPaymentById(paymentId);
        return payment != null && "pending".equals(payment.getStatus());
    }

    @Override
    public Payment getUserPaymentStatistics(Long userId) {
        BigDecimal totalAmount = paymentMapper.sumAmountByUserId(userId);
        int totalCount = paymentMapper.countByUserId(userId);

        Payment statistics = new Payment();
        statistics.setUserId(userId);
        statistics.setAmount(totalAmount != null ? totalAmount : BigDecimal.ZERO);
        // 使用ID字段存储总次数（仅用于统计展示）
        statistics.setId((long) totalCount);

        return statistics;
    }

    @Override
    @Transactional
    public void updateExpiredPayments() {
        LocalDateTime expireTime = LocalDateTime.now().minusHours(24); // 24小时过期
        int count = paymentMapper.updateExpiredPayments(expireTime);
        log.info("更新过期支付订单数量: {}", count);
    }

    // =================== Controller专用方法 ===================

    @Override
    public Result<PageResponse<PaymentResponse>> listPaymentsForController(
            Long userId, Long orderId, String status, String paymentMethod, String paymentType,
            String keyword, String orderBy, String orderDirection, Integer currentPage, Integer pageSize) {
        try {
            log.info("Controller层 - 支付记录列表查询: userId={}, orderId={}, status={}, page={}/{}", 
                    userId, orderId, status, currentPage, pageSize);

            // 参数验证和默认值设置
            if (currentPage == null || currentPage < 1) {
                currentPage = 1;
            }
            if (pageSize == null || pageSize < 1 || pageSize > 100) {
                pageSize = 20;
            }
            if (orderBy == null || orderBy.trim().isEmpty()) {
                orderBy = "createTime";
            }
            if (orderDirection == null || orderDirection.trim().isEmpty()) {
                orderDirection = "DESC";
            }

            // 使用现有的分页查询方法
            IPage<Payment> result = queryPayments(userId, paymentMethod, status, null, null, orderId,
                    null, null, null, null, null, currentPage, pageSize, orderBy, orderDirection);

            // 转换为Response对象
            List<PaymentResponse> responses = result.getRecords().stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            // 构建分页响应
            PageResponse<PaymentResponse> pageResponse = new PageResponse<>();
            pageResponse.setRecords(responses);
            pageResponse.setTotal(result.getTotal());
            pageResponse.setCurrentPage(currentPage);
            pageResponse.setPageSize(pageSize);
            pageResponse.setTotalPages((int) Math.ceil((double) result.getTotal() / pageSize));

            log.info("支付记录列表查询成功: 总数={}, 当前页={}, 页面大小={}", result.getTotal(), currentPage, pageSize);
            return Result.success(pageResponse);

        } catch (Exception e) {
            log.error("Controller层 - 支付记录列表查询失败", e);
            return Result.error("支付记录列表查询失败: " + e.getMessage());
        }
    }

    /**
     * 将Payment实体转换为PaymentResponse
     */
    private PaymentResponse convertToResponse(Payment payment) {
        if (payment == null) {
            return null;
        }

        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setUserId(payment.getUserId());
        response.setOrderId(payment.getOrderId());
        response.setOrderNo(payment.getOrderNo());
        response.setPaymentNo(payment.getPaymentNo());
        response.setAmount(payment.getAmount());
        response.setPayMethod(payment.getPayMethod());
        response.setStatus(payment.getStatus());
        response.setThirdPartyNo(payment.getThirdPartyNo());
        response.setPayTime(payment.getPayTime());
        response.setCreateTime(payment.getCreateTime());
        response.setUpdateTime(payment.getUpdateTime());

        return response;
    }
} 
