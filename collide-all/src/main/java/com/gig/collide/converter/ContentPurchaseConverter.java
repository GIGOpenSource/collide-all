package com.gig.collide.converter;

import com.gig.collide.Apientry.api.content.response.ContentPurchaseResponse;
import com.gig.collide.domain.UserContentPurchase;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 内容购买记录转换器
 * 用于在实体类和响应类之间进行转换
 *
 * @author GIG Team
 * @version 2.0.0 (极简版)
 * @since 2024-01-31
 */
@Component
public class ContentPurchaseConverter {

    /**
     * 将实体类转换为响应类
     *
     * @param entity 实体类
     * @return 响应类
     */
    public ContentPurchaseResponse toResponse(UserContentPurchase entity) {
        if (entity == null) {
            return null;
        }

        ContentPurchaseResponse response = new ContentPurchaseResponse();

        // 基础信息
        response.setId(entity.getId());
        response.setUserId(entity.getUserId());
        response.setContentId(entity.getContentId());

        // 内容信息
        response.setContentTitle(entity.getContentTitle());
        response.setContentType(entity.getContentType());
        response.setContentCoverUrl(entity.getContentCoverUrl());

        // 作者信息
        response.setAuthorId(entity.getAuthorId());
        response.setAuthorNickname(entity.getAuthorNickname());

        // 购买信息
        response.setOrderId(entity.getOrderId());
        response.setOrderNo(entity.getOrderNo());
        response.setCoinAmount(entity.getCoinAmount());
        response.setOriginalPrice(entity.getOriginalPrice());
        response.setDiscountAmount(entity.getDiscountAmount());
        response.setActualPaidAmount(entity.getActualPaidAmount());
        response.setDiscountRate(entity.getDiscountRate());

        // 状态信息
        response.setStatus(entity.getStatus());
        response.setPurchaseTime(entity.getPurchaseTime());
        response.setExpireTime(entity.getExpireTime());
        response.setIsExpired(entity.isExpired());
        response.setRemainingDays(entity.getRemainingDays());

        // 访问统计
        response.setAccessCount(entity.getAccessCount());
        response.setLastAccessTime(entity.getLastAccessTime());
        response.setHasAccessed(entity.getAccessCount() != null && entity.getAccessCount() > 0);

        // 权限信息
        response.setHasAccessPermission(entity.hasAccessPermission());

        // 时间信息
        response.setCreateTime(entity.getCreateTime());
        response.setUpdateTime(entity.getUpdateTime());

        return response;
    }

    /**
     * 将实体类列表转换为响应类列表
     *
     * @param entities 实体类列表
     * @return 响应类列表
     */
    public List<ContentPurchaseResponse> toResponseList(List<UserContentPurchase> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 将响应类转换为实体类
     *
     * @param response 响应类
     * @return 实体类
     */
    public UserContentPurchase toEntity(ContentPurchaseResponse response) {
        if (response == null) {
            return null;
        }

        UserContentPurchase entity = new UserContentPurchase();

        // 基础信息
        entity.setId(response.getId());
        entity.setUserId(response.getUserId());
        entity.setContentId(response.getContentId());

        // 内容信息
        entity.setContentTitle(response.getContentTitle());
        entity.setContentType(response.getContentType());
        entity.setContentCoverUrl(response.getContentCoverUrl());

        // 作者信息
        entity.setAuthorId(response.getAuthorId());
        entity.setAuthorNickname(response.getAuthorNickname());

        // 购买信息
        entity.setOrderId(response.getOrderId());
        entity.setOrderNo(response.getOrderNo());
        entity.setCoinAmount(response.getCoinAmount());
        entity.setOriginalPrice(response.getOriginalPrice());
        entity.setDiscountAmount(response.getDiscountAmount());

        // 状态信息
        entity.setStatus(response.getStatus());
        entity.setPurchaseTime(response.getPurchaseTime());
        entity.setExpireTime(response.getExpireTime());

        // 访问统计
        entity.setAccessCount(response.getAccessCount());
        entity.setLastAccessTime(response.getLastAccessTime());

        // 时间信息
        entity.setCreateTime(response.getCreateTime());
        entity.setUpdateTime(response.getUpdateTime());

        return entity;
    }

    /**
     * 创建新的购买记录实体
     *
     * @param userId 用户ID
     * @param contentId 内容ID
     * @param orderId 订单ID
     * @param orderNo 订单号
     * @param purchaseAmount 购买金额
     * @param originalPrice 原价
     * @param expireTime 过期时间
     * @return 实体类
     */
    public UserContentPurchase createPurchaseEntity(Long userId, Long contentId, Long orderId,
                                                    String orderNo, Long purchaseAmount,
                                                    Long originalPrice, LocalDateTime expireTime) {
        UserContentPurchase entity = new UserContentPurchase();

        entity.setUserId(userId);
        entity.setContentId(contentId);
        entity.setOrderId(orderId);
        entity.setOrderNo(orderNo);
        entity.setCoinAmount(purchaseAmount);
        entity.setOriginalPrice(originalPrice);
        entity.setDiscountAmount(originalPrice - purchaseAmount);
        entity.setStatus("ACTIVE");
        entity.setPurchaseTime(LocalDateTime.now());
        entity.setExpireTime(expireTime);
        entity.setAccessCount(0);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());

        return entity;
    }
}
