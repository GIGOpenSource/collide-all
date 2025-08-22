package com.gig.collide.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.domain.Content;
import com.gig.collide.domain.Comment;
import com.gig.collide.service.ContentService;
import com.gig.collide.service.CommentService;
import com.gig.collide.service.ContentPaymentService;
import com.gig.collide.service.Impl.ContentServiceImpl;
import com.gig.collide.service.Impl.CommentServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * XXWF内容详情控制器
 * 提供内容详情查询功能
 * 
 * @author Collide
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/xxwf")
@RequiredArgsConstructor
@Tag(name = "XXWF内容详情", description = "XXWF内容详情相关接口")
public class XXWFController {

    private final ContentService contentService;
    private final CommentService commentService;
    private final ContentPaymentService contentPaymentService;

    /**
     * 根据内容ID查询内容详情
     * 默认类型为长视频LONGVIDEO
     * 新增：付费内容权限校验
     */
    @GetMapping("/content/{contentId}")
    @Operation(summary = "查询内容详情", description = "根据内容ID查询内容详情，包含视频URL、简介、发布者信息、标签、推荐内容和评论列表")
    public Result<XXWFContentDetailResponse> getContentDetail(
            @Parameter(description = "内容ID") @PathVariable Long contentId,
            @Parameter(description = "用户ID（用于付费权限校验）") @RequestParam(required = false) Long userId) {
        try {
            log.info("REST请求 - 查询内容详情: contentId={}, userId={}", contentId, userId);
            
            // 查询内容信息
            Content content = contentService.getContentById(contentId, false);
            if (content == null) {
                return Result.error("内容不存在");
            }
            
            // 验证内容类型是否为LONGVIDEO
            if (!"LONGVIDEO".equals(content.getContentType())) {
                return Result.error("内容类型不是长视频");
            }
            
            // 新增：付费内容权限校验
            if (userId != null) {
                boolean hasPermission = contentPaymentService.checkAccessPermission(userId, contentId);
                if (!hasPermission) {
                    log.warn("用户无权访问付费内容: userId={}, contentId={}", userId, contentId);
                    return Result.error("需要付费才能访问此内容");
                }
                log.debug("用户付费权限验证通过: userId={}, contentId={}", userId, contentId);
            } else {
                log.debug("用户未提供userId，跳过付费权限校验: contentId={}", contentId);
            }
            
            // 构建响应对象
            XXWFContentDetailResponse response = new XXWFContentDetailResponse();
            
            // 设置基本信息
            response.setContentId(content.getId());
            response.setVideoUrl(extractVideoUrl(content.getContentData()));
            response.setDescription(content.getDescription());
            
            // 设置发布者信息
            XXWFPublisherInfo publisherInfo = new XXWFPublisherInfo();
            publisherInfo.setUserId(content.getAuthorId());
            publisherInfo.setNickname(content.getAuthorNickname());
            publisherInfo.setAvatar(content.getAuthorAvatar());
            response.setPublisherInfo(publisherInfo);
            
            // 设置发布者标签（从tags字段解析）
            response.setPublisherTags(parseTags(content.getTags()));
            
            // 获取随机5个其他内容的ID
            response.setRecommendContentIds(getRandomContentIds(contentId, 5));
            
            // 获取评论列表
            response.setComments(getCommentList(contentId));
            
            return Result.success(response);
        } catch (Exception e) {
            log.error("查询内容详情失败: contentId={}", contentId, e);
            return Result.error("查询内容详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 从contentData中提取视频URL
     */
    private String extractVideoUrl(String contentData) {
        if (contentData == null || contentData.trim().isEmpty()) {
            log.warn("contentData为空");
            return null;
        }
        
        try {
            log.info("开始解析contentData: {}", contentData);
            
            // 简单的JSON解析，假设格式为{"videoUrl": "xxx"}
            if (contentData.contains("\"videoUrl\"")) {
                int startIndex = contentData.indexOf("\"videoUrl\"") + 11;
                int endIndex = contentData.indexOf("\"", startIndex + 1);
                if (endIndex > startIndex) {
                    String videoUrl = contentData.substring(startIndex, endIndex);
                    log.info("解析到视频URL: {}", videoUrl);
                    return videoUrl;
                } else {
                    log.warn("解析videoUrl失败，startIndex={}, endIndex={}", startIndex, endIndex);
                }
            } else {
                log.warn("contentData中未找到videoUrl字段");
            }
            
            // 如果没有找到videoUrl字段，直接返回contentData
            log.warn("未找到videoUrl字段，返回原始contentData: {}", contentData);
            return contentData;
        } catch (Exception e) {
            log.error("解析视频URL失败: contentData={}", contentData, e);
            return contentData;
        }
    }
    
    /**
     * 解析标签字符串为数组
     */
    private List<String> parseTags(String tags) {
        List<String> tagList = new ArrayList<>();
        if (tags == null || tags.trim().isEmpty()) {
            return tagList;
        }
        
        try {
            // 如果是JSON数组格式
            if (tags.startsWith("[") && tags.endsWith("]")) {
                String tagsStr = tags.substring(1, tags.length() - 1);
                if (!tagsStr.trim().isEmpty()) {
                    String[] tagArray = tagsStr.split(",");
                    for (String tag : tagArray) {
                        String cleanTag = tag.trim().replace("\"", "");
                        if (!cleanTag.isEmpty()) {
                            tagList.add(cleanTag);
                        }
                    }
                }
            } else {
                // 如果是逗号分隔的格式
                String[] tagArray = tags.split(",");
                for (String tag : tagArray) {
                    String cleanTag = tag.trim();
                    if (!cleanTag.isEmpty()) {
                        tagList.add(cleanTag);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("解析标签失败: tags={}", tags, e);
        }
        
        return tagList;
    }
    
    /**
     * 获取随机5个其他内容的ID
     */
    private List<Long> getRandomContentIds(Long excludeContentId, int count) {
        try {
            log.info("开始获取随机内容ID: excludeContentId={}, count={}", excludeContentId, count);
            
            // 简化查询逻辑，直接使用MyBatis-Plus的查询
            List<Long> contentIds = new ArrayList<>();
            
            // 手动添加一些测试ID（临时方案）
            if (excludeContentId != 107) contentIds.add(107L);
            if (excludeContentId != 108) contentIds.add(108L);
            if (excludeContentId != 109) contentIds.add(109L);
            if (excludeContentId != 110) contentIds.add(110L);
            if (excludeContentId != 111) contentIds.add(111L);
            
            log.info("可用内容ID列表: {}", contentIds);
            
            // 随机选择指定数量的ID
            List<Long> randomIds = new ArrayList<>();
            Random random = new Random();
            int availableCount = Math.min(count, contentIds.size());
            
            for (int i = 0; i < availableCount; i++) {
                int randomIndex = random.nextInt(contentIds.size());
                randomIds.add(contentIds.get(randomIndex));
                contentIds.remove(randomIndex);
            }
            
            log.info("最终返回随机ID数量: {}", randomIds.size());
            return randomIds;
        } catch (Exception e) {
            log.error("获取随机内容ID失败", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 获取评论列表
     */
    private List<XXWFCommentInfo> getCommentList(Long contentId) {
        try {
            // 查询该内容的所有评论
            IPage<Comment> commentPage = commentService.getTargetComments(contentId, "CONTENT", 0L, 1, 1000);
            List<Comment> comments = commentPage.getRecords();
            
            return comments.stream()
                    .map(comment -> {
                        XXWFCommentInfo commentInfo = new XXWFCommentInfo();
                        commentInfo.setCommentId(comment.getId());
                        commentInfo.setContent(comment.getContent());
                        commentInfo.setUserId(comment.getUserId());
                        commentInfo.setUserNickname(comment.getUserNickname());
                        commentInfo.setUserAvatar(comment.getUserAvatar());
                        commentInfo.setCreateTime(comment.getCreateTime());
                        return commentInfo;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("获取评论列表失败: contentId={}", contentId, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * XXWF内容详情响应对象
     */
    public static class XXWFContentDetailResponse {
        private Long contentId;
        private String videoUrl;
        private String description;
        private XXWFPublisherInfo publisherInfo;
        private List<String> publisherTags;
        private List<Long> recommendContentIds;
        private List<XXWFCommentInfo> comments;
        
        // Getters and Setters
        public Long getContentId() { return contentId; }
        public void setContentId(Long contentId) { this.contentId = contentId; }
        
        public String getVideoUrl() { return videoUrl; }
        public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public XXWFPublisherInfo getPublisherInfo() { return publisherInfo; }
        public void setPublisherInfo(XXWFPublisherInfo publisherInfo) { this.publisherInfo = publisherInfo; }
        
        public List<String> getPublisherTags() { return publisherTags; }
        public void setPublisherTags(List<String> publisherTags) { this.publisherTags = publisherTags; }
        
        public List<Long> getRecommendContentIds() { return recommendContentIds; }
        public void setRecommendContentIds(List<Long> recommendContentIds) { this.recommendContentIds = recommendContentIds; }
        
        public List<XXWFCommentInfo> getComments() { return comments; }
        public void setComments(List<XXWFCommentInfo> comments) { this.comments = comments; }
    }
    
    /**
     * 发布者信息对象
     */
    public static class XXWFPublisherInfo {
        private Long userId;
        private String nickname;
        private String avatar;
        
        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
        
        public String getAvatar() { return avatar; }
        public void setAvatar(String avatar) { this.avatar = avatar; }
    }
    
    /**
     * 评论信息对象
     */
    public static class XXWFCommentInfo {
        private Long commentId;
        private String content;
        private Long userId;
        private String userNickname;
        private String userAvatar;
        private java.time.LocalDateTime createTime;
        
        // Getters and Setters
        public Long getCommentId() { return commentId; }
        public void setCommentId(Long commentId) { this.commentId = commentId; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getUserNickname() { return userNickname; }
        public void setUserNickname(String userNickname) { this.userNickname = userNickname; }
        
        public String getUserAvatar() { return userAvatar; }
        public void setUserAvatar(String userAvatar) { this.userAvatar = userAvatar; }
        
        public java.time.LocalDateTime getCreateTime() { return createTime; }
        public void setCreateTime(java.time.LocalDateTime createTime) { this.createTime = createTime; }
    }
}
