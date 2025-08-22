package com.gig.collide.service;



import com.gig.collide.Apientry.api.common.response.PageResponse;
import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.Apientry.api.content.response.ContentPurchaseResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 内容购买服务接口 - 极简版
 * 基于12个核心方法设计的精简服务接口
 * 管理用户内容购买记录、访问权限验证和消费统计
 *
 * @author GIG Team
 * @version 2.0.0 (极简版)
 * @since 2024-01-31
 */
public interface ContentPurchaseService {

    // =================== 核心CRUD功能（2个方法）===================

    /**
     * 根据购买记录ID获取详情
     *
     * @param id 购买记录ID
     * @return 购买记录详情
     */
    Result<ContentPurchaseResponse> getPurchaseById(Long id);

    /**
     * 删除购买记录（逻辑删除）
     *
     * @param id 购买记录ID
     * @return 是否删除成功
     */
    Result<Boolean> deletePurchase(Long id);

    // =================== 万能查询功能（3个方法）===================

    /**
     * 万能条件查询购买记录列表
     * 可以替代所有具体查询方法：getUserPurchases, getContentPurchases等
     *
     * @param userId 用户ID（可选）
     * @param contentId 内容ID（可选）
     * @param contentType 内容类型（可选）
     * @param orderId 订单ID（可选）
     * @param orderNo 订单号（可选）
     * @param status 状态（可选）
     * @param isValid 是否有效（可选，true=未过期，false=已过期）
     * @param minAmount 最小金额（可选）
     * @param maxAmount 最大金额（可选）
     * @param orderBy 排序字段（可选：createTime、purchaseAmount、accessCount）
     * @param orderDirection 排序方向（可选：ASC、DESC）
     * @param currentPage 当前页码（可选，不分页时传null）
     * @param pageSize 页面大小（可选，不分页时传null）
     * @return 购买记录列表
     */
    Result<PageResponse<ContentPurchaseResponse>> getPurchasesByConditions(
            Long userId, Long contentId, String contentType, Long orderId, String orderNo,
            String status, Boolean isValid, Long minAmount, Long maxAmount,
            String orderBy, String orderDirection, Integer currentPage, Integer pageSize);

    /**
     * 推荐购买记录查询
     *
     * @param strategy 推荐策略（RECENT、HIGH_VALUE、MOST_ACCESSED、POPULAR）
     * @param userId 用户ID（可选）
     * @param contentType 内容类型筛选（可选）
     * @param excludeContentIds 排除的内容ID列表（可选）
     * @param limit 返回数量限制
     * @return 推荐购买记录列表
     */
    Result<List<ContentPurchaseResponse>> getRecommendedPurchases(
            String strategy, Long userId, String contentType,
            List<Long> excludeContentIds, Integer limit);

    /**
     * 过期相关查询
     *
     * @param type 查询类型（EXPIRING_SOON、EXPIRED）
     * @param beforeTime 时间界限（EXPIRING_SOON时使用）
     * @param userId 用户ID（可选）
     * @param limit 返回数量限制（可选）
     * @return 购买记录列表
     */
    Result<List<ContentPurchaseResponse>> getPurchasesByExpireStatus(
            String type, LocalDateTime beforeTime, Long userId, Integer limit);

    // =================== 权限验证功能（1个方法）===================

    /**
     * 检查用户是否有权限访问内容（已购买且未过期）
     *
     * @param userId 用户ID
     * @param contentId 内容ID
     * @return 是否有权限
     */
    Result<Boolean> checkAccessPermission(Long userId, Long contentId);

    // =================== 状态管理功能（2个方法）===================

    /**
     * 更新购买记录状态
     *
     * @param purchaseId 购买记录ID
     * @param status 新状态
     * @return 是否成功
     */
    Result<Boolean> updatePurchaseStatus(Long purchaseId, String status);

    /**
     * 批量更新购买记录状态
     *
     * @param ids 购买记录ID列表
     * @param status 新状态
     * @return 是否成功
     */
    Result<Boolean> batchUpdateStatus(List<Long> ids, String status);

    // =================== 统计功能（1个方法）===================

    /**
     * 获取购买统计信息
     *
     * @param statsType 统计类型（USER_SUMMARY、CONTENT_SUMMARY、DISCOUNT）
     * @param params 统计参数
     * @return 统计信息
     */
    Result<Map<String, Object>> getPurchaseStats(String statsType, Map<String, Object> params);

    // =================== 业务逻辑功能（3个方法）===================

    /**
     * 处理内容购买完成
     * 创建购买记录，更新相关统计
     *
     * @param userId 用户ID
     * @param contentId 内容ID
     * @param orderId 订单ID
     * @param orderNo 订单号
     * @param purchaseAmount 购买金额
     * @param originalPrice 原价
     * @param expireTime 过期时间（可选）
     * @return 购买记录
     */
    Result<ContentPurchaseResponse> completePurchase(Long userId, Long contentId, Long orderId, String orderNo,
                                                     Long purchaseAmount, Long originalPrice, LocalDateTime expireTime);

    /**
     * 处理退款
     *
     * @param purchaseId 购买记录ID
     * @param refundReason 退款原因
     * @param refundAmount 退款金额
     * @return 是否成功
     */
    Result<Boolean> processRefund(Long purchaseId, String refundReason, Long refundAmount);

    /**
     * 记录内容访问
     * 自动增加访问次数并更新最后访问时间
     *
     * @param userId 用户ID
     * @param contentId 内容ID
     * @return 是否成功
     */
    Result<Boolean> recordContentAccess(Long userId, Long contentId);
}
