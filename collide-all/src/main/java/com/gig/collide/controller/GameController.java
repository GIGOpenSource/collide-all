package com.gig.collide.controller;

import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.domain.Ad;
import com.gig.collide.service.AdService;
import com.gig.collide.service.ContentPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 新鲜玩法模块控制器
 * 提供广告相关的查询和管理功能
 * 
 * @author GIG Team
 * @version 1.0.0
 * @since 2025-01-27
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/game")
@RequiredArgsConstructor
@Validated
@Tag(name = "新鲜玩法", description = "新鲜玩法模块相关接口")
public class GameController {

    private final AdService adService;
    private final ContentPaymentService contentPaymentService;

    /**
     * 根据广告ID查询广告详情
     * 包含广告基本信息、是否收费等
     * 
     * @param adId 广告ID
     * @param userId 用户ID（可选，用于付费权限校验）
     * @return 广告详情信息
     */
    @GetMapping("/detail/{adId}")
    @Operation(summary = "查询广告详情", description = "根据广告ID查询广告详情，检查当前广告是否收费")
    public Result<AdDetailResponse> getAdDetail(
            @Parameter(description = "广告ID", required = true) @PathVariable Long adId,
            @Parameter(description = "用户ID（用于付费权限校验）") @RequestParam(required = false) Long userId) {
        
        try {
            log.info("REST请求 - 查询广告详情: adId={}, userId={}", adId, userId);
            
            // 1. 查询广告信息
            Ad ad = adService.getAdById(adId);
            if (ad == null) {
                log.warn("广告不存在: adId={}", adId);
                return Result.error("广告不存在");
            }
            
            // 2. 检查广告是否启用
            if (ad.getIsActive() == null || ad.getIsActive() != 1) {
                log.warn("广告未启用: adId={}, isActive={}", adId, ad.getIsActive());
                return Result.error("广告暂未开放");
            }
            
            // 3. 构建广告详情响应
            AdDetailResponse response = new AdDetailResponse();
            
            // 设置基本信息
            response.setAdId(ad.getId());
            response.setAdName(ad.getAdName());
            response.setAdTitle(ad.getAdTitle());
            response.setAdDescription(ad.getAdDescription());
            response.setAdType(ad.getAdType());
            response.setImageUrl(ad.getImageUrl());
            response.setClickUrl(ad.getClickUrl());
            response.setAltText(ad.getAltText());
            response.setTargetType(ad.getTargetType());
            response.setSortOrder(ad.getSortOrder());
            response.setCreateTime(ad.getCreateTime());
            response.setUpdateTime(ad.getUpdateTime());
            response.setIsActive(ad.getIsActive());
            
            // 4. 检查是否收费
            boolean isPaid = false;
            String paymentType = "FREE";
            String paymentMessage = "免费广告";
            
            if (userId != null) {
                // 如果有用户ID，检查付费权限
                boolean hasPermission = contentPaymentService.checkAccessPermission(userId, adId);
                if (!hasPermission) {
                    isPaid = true;
                    paymentType = "PAID";
                    paymentMessage = "需要付费才能访问";
                }
            }
            
            response.setIsPaid(isPaid);
            response.setPaymentType(paymentType);
            response.setPaymentMessage(paymentMessage);
            
            // 5. 设置访问权限
            response.setCanAccess(userId == null || !isPaid);
            response.setNeedLogin(userId == null);
            
            log.info("广告详情查询成功: adId={}, adName={}, isPaid={}", 
                    adId, ad.getAdName(), isPaid);
            
            return Result.success(response);
            
        } catch (Exception e) {
            log.error("查询广告详情失败: adId={}", adId, e);
            return Result.error("查询广告详情失败: " + e.getMessage());
        }
    }

    /**
     * 广告详情响应对象
     */
    public static class AdDetailResponse {
        // 基本信息
        private Long adId;
        private String adName;
        private String adTitle;
        private String adDescription;
        private String adType;
        private String imageUrl;
        private String clickUrl;
        private String altText;
        private String targetType;
        private Integer sortOrder;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
        private Integer isActive;
        
        // 付费信息
        private Boolean isPaid;
        private String paymentType;
        private String paymentMessage;
        
        // 访问权限
        private Boolean canAccess;
        private Boolean needLogin;
        
        // Getters and Setters
        public Long getAdId() { return adId; }
        public void setAdId(Long adId) { this.adId = adId; }
        
        public String getAdName() { return adName; }
        public void setAdName(String adName) { this.adName = adName; }
        
        public String getAdTitle() { return adTitle; }
        public void setAdTitle(String adTitle) { this.adTitle = adTitle; }
        
        public String getAdDescription() { return adDescription; }
        public void setAdDescription(String adDescription) { this.adDescription = adDescription; }
        
        public String getAdType() { return adType; }
        public void setAdType(String adType) { this.adType = adType; }
        
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        
        public String getClickUrl() { return clickUrl; }
        public void setClickUrl(String clickUrl) { this.clickUrl = clickUrl; }
        
        public String getAltText() { return altText; }
        public void setAltText(String altText) { this.altText = altText; }
        
        public String getTargetType() { return targetType; }
        public void setTargetType(String targetType) { this.targetType = targetType; }
        
        public Integer getSortOrder() { return sortOrder; }
        public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
        
        public LocalDateTime getCreateTime() { return createTime; }
        public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
        
        public LocalDateTime getUpdateTime() { return updateTime; }
        public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
        
        public Integer getIsActive() { return isActive; }
        public void setIsActive(Integer isActive) { this.isActive = isActive; }
        
        public Boolean getIsPaid() { return isPaid; }
        public void setIsPaid(Boolean isPaid) { this.isPaid = isPaid; }
        
        public String getPaymentType() { return paymentType; }
        public void setPaymentType(String paymentType) { this.paymentType = paymentType; }
        
        public String getPaymentMessage() { return paymentMessage; }
        public void setPaymentMessage(String paymentMessage) { this.paymentMessage = paymentMessage; }
        
        public Boolean getCanAccess() { return canAccess; }
        public void setCanAccess(Boolean canAccess) { this.canAccess = canAccess; }
        
        public Boolean getNeedLogin() { return needLogin; }
        public void setNeedLogin(Boolean needLogin) { this.needLogin = needLogin; }
    }
}
