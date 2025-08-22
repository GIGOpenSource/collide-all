package com.gig.collide.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.domain.Like;
import com.gig.collide.domain.Favorite;
import com.gig.collide.domain.Follow;
import com.gig.collide.service.LikeService;
import com.gig.collide.service.FavoriteService;
import com.gig.collide.service.FollowService;
import com.gig.collide.service.CommentService;
import com.gig.collide.service.ContentService;
import com.gig.collide.mapper.UserMapper;
import com.gig.collide.domain.User;
import com.gig.collide.domain.Comment;
import com.gig.collide.domain.Content;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户互动管理控制器
 * 提供用户的点赞、收藏、关注等互动信息查询接口
 * 
 * @author GIG Team
 * @version 1.0.0
 * @since 2024-08-22
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/user-interactions")
@RequiredArgsConstructor
@Tag(name = "用户互动管理", description = "用户互动相关接口")
public class UserInteractionController {

    private final LikeService likeService;
    private final FavoriteService favoriteService;
    private final FollowService followService;
    private final CommentService commentService;
    private final ContentService contentService;
    private final UserMapper userMapper;

    /**
     * 获取用户所有互动信息
     * 按时间倒序返回统一的互动列表，前端根据type字段区分互动类型
     * 
     * 返回格式：
     * {
     *   "interactions": {
     *     "records": [
     *       {
     *         "id": 123,
     *         "type": "LIKE|FAVORITE|FOLLOW|COMMENT",
     *         "likeType": "CONTENT|COMMENT|DYNAMIC",     // type=LIKE时
     *         "favoriteType": "CONTENT|GOODS",           // type=FAVORITE时
     *         "interactionType": "COMMENT",               // type=COMMENT时
     *         "subType": "CONTENT|DYNAMIC",               // type=COMMENT时
     *         "commentContent": "评论内容",               // type=COMMENT时
     *         "targetId": 456,
     *         "targetTitle": "标题",
     *         "targetAuthorId": 789,
     *         "targetAuthorNickname": "作者昵称",        // 新增：作者昵称
     *         "createTime": "2024-08-22T15:30:00",
     *         "status": "active",
     *         // ...其他字段根据type不同而不同
     *       }
     *     ],
     *     "total": 45,
     *     "currentPage": 1,
     *     "pageSize": 20
     *   },
     *   "statistics": { "totalLikes": 25, "totalFavorites": 12, ... }
     * }
     */
    @GetMapping("/all")
    @Operation(summary = "获取用户所有互动", description = "获取用户的点赞、收藏、关注等互动信息，按时间倒序排列")
    public Result<Map<String, Object>> getAllUserInteractions(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "20") Integer pageSize) {
        
        try {
            log.info("REST请求 - 获取用户所有互动: userId={}, page={}/{}", userId, currentPage, pageSize);
            
            // 为了获取完整的时间排序，需要获取足够多的数据
            // 这里设置一个较大的数量来获取更多数据用于排序
            int fetchSize = Math.max(pageSize * 10, 100); // 至少获取100条，或者页面大小的10倍
            
            List<Map<String, Object>> allInteractions = new ArrayList<>();
            
            // 获取用户点赞列表
            try {
                IPage<Like> likePage = likeService.findUserLikes(1, fetchSize, userId, null, "active");
                for (Like like : likePage.getRecords()) {
                    Map<String, Object> interaction = new HashMap<>();
                    interaction.put("id", like.getId());
                    interaction.put("type", "LIKE");
                    interaction.put("likeType", like.getLikeType()); // CONTENT, COMMENT, DYNAMIC等
                    interaction.put("targetId", like.getTargetId());
                    interaction.put("targetTitle", like.getTargetTitle());
                    interaction.put("targetAuthorId", like.getTargetAuthorId());
                    interaction.put("createTime", like.getCreateTime());
                    interaction.put("status", like.getStatus());
                    allInteractions.add(interaction);
                }
                log.debug("用户点赞数据获取成功: 总数={}", likePage.getTotal());
            } catch (Exception e) {
                log.warn("获取用户点赞数据失败: userId={}", userId, e);
            }
            
            // 获取用户收藏列表  
            try {
                IPage<Favorite> favoritePage = favoriteService.getUserFavorites(userId, null, 1, fetchSize);
                for (Favorite favorite : favoritePage.getRecords()) {
                    Map<String, Object> interaction = new HashMap<>();
                    interaction.put("id", favorite.getId());
                    interaction.put("type", "FAVORITE");
                    interaction.put("favoriteType", favorite.getFavoriteType()); // CONTENT, GOODS等
                    interaction.put("targetId", favorite.getTargetId());
                    interaction.put("targetTitle", favorite.getTargetTitle());
                    interaction.put("targetCover", favorite.getTargetCover());
                    interaction.put("targetAuthorId", favorite.getTargetAuthorId());
                    interaction.put("createTime", favorite.getCreateTime());
                    interaction.put("status", favorite.getStatus());
                    allInteractions.add(interaction);
                }
                log.debug("用户收藏数据获取成功: 总数={}", favoritePage.getTotal());
            } catch (Exception e) {
                log.warn("获取用户收藏数据失败: userId={}", userId, e);
            }
            
            // 获取用户关注列表
            try {
                IPage<Follow> followingPage = followService.getFollowing(userId, 1, fetchSize);
                for (Follow follow : followingPage.getRecords()) {
                    Map<String, Object> interaction = new HashMap<>();
                    interaction.put("id", follow.getId());
                    interaction.put("type", "FOLLOW");
                    interaction.put("followerId", follow.getFollowerId());
                    interaction.put("followeeId", follow.getFolloweeId());
                    interaction.put("followeeNickname", follow.getFolloweeNickname());
                    interaction.put("followeeAvatar", follow.getFolloweeAvatar());
                    interaction.put("createTime", follow.getCreateTime());
                    interaction.put("status", follow.getStatus());
                    allInteractions.add(interaction);
                }
                log.debug("用户关注数据获取成功: 总数={}", followingPage.getTotal());
            } catch (Exception e) {
                log.warn("获取用户关注数据失败: userId={}", userId, e);
            }
            
            // 获取用户评论列表
            try {
                // 查询用户的评论数据
                var commentsResult = commentService.listCommentsForController(
                    null, null, userId, userId, null, "active", null, 
                    "createTime", "DESC", 1, fetchSize
                );
                
                if (commentsResult.getCode() == 200 && commentsResult.getData() != null) {
                    var commentPage = commentsResult.getData();
                    for (var commentResponse : commentPage.getRecords()) {
                        Map<String, Object> interaction = new HashMap<>();
                        interaction.put("id", commentResponse.getId());
                        interaction.put("type", "COMMENT");
                        interaction.put("interactionType", "COMMENT");
                        interaction.put("subType", commentResponse.getCommentType()); // CONTENT, DYNAMIC等
                        interaction.put("commentType", true);
                        interaction.put("commentContent", commentResponse.getContent());
                        interaction.put("targetId", commentResponse.getTargetId());
                        interaction.put("parentCommentId", commentResponse.getParentCommentId() != null ? commentResponse.getParentCommentId() : 0);
                        interaction.put("replyToUserId", commentResponse.getReplyToUserId());
                        interaction.put("replyToUserNickname", commentResponse.getReplyToUserNickname());
                        interaction.put("userId", commentResponse.getUserId());
                        interaction.put("userNickname", commentResponse.getUserNickname());
                        interaction.put("userAvatar", commentResponse.getUserAvatar());
                        interaction.put("likeCount", commentResponse.getLikeCount());
                        interaction.put("replyCount", commentResponse.getReplyCount());
                        interaction.put("isLike", commentResponse.getIsLiked() != null ? commentResponse.getIsLiked() : false);
                        interaction.put("createTime", commentResponse.getCreateTime());
                        interaction.put("updateTime", commentResponse.getUpdateTime());
                        interaction.put("status", commentResponse.getStatus());
                        
                        // 设置与评论相关的标识
                        interaction.put("commentRelated", false);
                        interaction.put("dynamicRelated", "DYNAMIC".equals(commentResponse.getCommentType()));
                        interaction.put("contentRelated", "CONTENT".equals(commentResponse.getCommentType()));
                        interaction.put("likeType", false);
                        
                        // 初始化需要补充的字段为null，稍后会填充
                        interaction.put("targetTitle", null);
                        interaction.put("targetAuthorId", null);
                        interaction.put("targetAuthorNickname", null);
                        interaction.put("targetAuthorAvatar", null);
                        interaction.put("contentDescription", null);
                        interaction.put("contentCoverUrl", new ArrayList<>());
                        interaction.put("contentLikeCount", null);
                        interaction.put("isFollowingAuthor", false);
                        
                        allInteractions.add(interaction);
                    }
                    log.debug("用户评论数据获取成功: 总数={}", commentPage.getTotal());
                }
            } catch (Exception e) {
                log.warn("获取用户评论数据失败: userId={}", userId, e);
            }
            
            // 批量查询内容信息并补充评论数据
            Set<Long> contentIds = allInteractions.stream()
                    .filter(interaction -> "COMMENT".equals(interaction.get("type")) && "CONTENT".equals(interaction.get("subType")))
                    .map(interaction -> (Long) interaction.get("targetId"))
                    .filter(contentId -> contentId != null)
                    .collect(Collectors.toSet());
            
            Map<Long, Content> contentMap = new HashMap<>();
            if (!contentIds.isEmpty()) {
                try {
                    log.debug("批量查询内容信息: contentIds.size={}", contentIds.size());
                    List<Content> contents = contentService.getContentsByConditions(
                        null, null, null, null, null, null, null,
                        "createTime", "DESC", 1, contentIds.size() * 2
                    );
                    
                    contentMap = contents.stream()
                            .filter(content -> contentIds.contains(content.getId()))
                            .collect(Collectors.toMap(Content::getId, content -> content, (existing, replacement) -> existing));
                    log.debug("内容信息查询完成: 找到{}个内容信息", contentMap.size());
                } catch (Exception e) {
                    log.warn("批量查询内容信息失败", e);
                }
            }
            
            // 填充评论的内容信息
            final Map<Long, Content> finalContentMap = contentMap;
            allInteractions.stream()
                    .filter(interaction -> "COMMENT".equals(interaction.get("type")) && "CONTENT".equals(interaction.get("subType")))
                    .forEach(interaction -> {
                        Long contentId = (Long) interaction.get("targetId");
                        if (contentId != null && finalContentMap.containsKey(contentId)) {
                            Content content = finalContentMap.get(contentId);
                            interaction.put("targetTitle", content.getTitle());
                            interaction.put("targetAuthorId", content.getAuthorId());
                            interaction.put("contentDescription", content.getDescription());
                            interaction.put("contentLikeCount", content.getLikeCount());
                            
                            // 处理封面URL
                            if (content.getCoverUrl() != null && !content.getCoverUrl().trim().isEmpty()) {
                                List<String> coverUrls = new ArrayList<>();
                                coverUrls.add(content.getCoverUrl());
                                interaction.put("contentCoverUrl", coverUrls);
                            } else {
                                interaction.put("contentCoverUrl", new ArrayList<>());
                            }
                        }
                    });

            // 批量查询作者昵称信息
            Set<Long> authorIds = allInteractions.stream()
                    .map(interaction -> (Long) interaction.get("targetAuthorId"))
                    .filter(authorId -> authorId != null)
                    .collect(Collectors.toSet());
            
            Map<Long, String> authorNicknameMap = new HashMap<>();
            if (!authorIds.isEmpty()) {
                try {
                    log.debug("批量查询作者昵称: authorIds.size={}", authorIds.size());
                    List<User> authors = userMapper.selectBatchIds(authorIds);
                    authorNicknameMap = authors.stream()
                            .collect(Collectors.toMap(User::getId, User::getNickname, (existing, replacement) -> existing));
                    log.debug("作者昵称查询完成: 找到{}个作者信息", authorNicknameMap.size());
                } catch (Exception e) {
                    log.warn("批量查询作者昵称失败", e);
                }
            }
            
            // 批量查询作者详细信息（昵称和头像）
            Map<Long, User> authorMap = new HashMap<>();
            if (!authorIds.isEmpty()) {
                try {
                    log.debug("批量查询作者详细信息: authorIds.size={}", authorIds.size());
                    List<User> authors = userMapper.selectBatchIds(authorIds);
                    authorMap = authors.stream()
                            .collect(Collectors.toMap(User::getId, user -> user, (existing, replacement) -> existing));
                    
                    // 保持向后兼容的昵称Map
                    authorNicknameMap = authors.stream()
                            .collect(Collectors.toMap(User::getId, User::getNickname, (existing, replacement) -> existing));
                    log.debug("作者详细信息查询完成: 找到{}个作者信息", authorMap.size());
                } catch (Exception e) {
                    log.warn("批量查询作者详细信息失败", e);
                }
            }
            
            // 填充作者详细信息
            final Map<Long, User> finalAuthorMap = authorMap;
            final Map<Long, String> finalAuthorNicknameMap = authorNicknameMap;
            allInteractions.forEach(interaction -> {
                Long authorId = (Long) interaction.get("targetAuthorId");
                if (authorId != null && finalAuthorMap.containsKey(authorId)) {
                    User author = finalAuthorMap.get(authorId);
                    interaction.put("targetAuthorNickname", author.getNickname());
                    interaction.put("targetAuthorAvatar", author.getAvatar());
                } else if (authorId != null) {
                    // 如果详细信息查询失败，至少尝试填充昵称
                    String authorNickname = finalAuthorNicknameMap.get(authorId);
                    interaction.put("targetAuthorNickname", authorNickname);
                }
            });
            
            // 填充关注状态（仅对评论类型）
            final Long finalUserId = userId;
            allInteractions.stream()
                    .filter(interaction -> "COMMENT".equals(interaction.get("type")))
                    .forEach(interaction -> {
                        Long authorId = (Long) interaction.get("targetAuthorId");
                        if (authorId != null && !authorId.equals(finalUserId)) {
                            try {
                                boolean isFollowing = followService.checkFollowStatus(finalUserId, authorId);
                                interaction.put("isFollowingAuthor", isFollowing);
                            } catch (Exception e) {
                                log.debug("查询关注状态失败: userId={}, authorId={}", finalUserId, authorId);
                                interaction.put("isFollowingAuthor", false);
                            }
                        } else {
                            interaction.put("isFollowingAuthor", false);
                        }
                    });
            
            // 按创建时间倒序排序（最新的在前）
            allInteractions.sort((a, b) -> {
                LocalDateTime timeA = (LocalDateTime) a.get("createTime");
                LocalDateTime timeB = (LocalDateTime) b.get("createTime");
                if (timeA == null && timeB == null) return 0;
                if (timeA == null) return 1;
                if (timeB == null) return -1;
                return timeB.compareTo(timeA); // 倒序：新的在前
            });
            
            // 应用分页
            int totalSize = allInteractions.size();
            int startIndex = (currentPage - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, totalSize);
            
            List<Map<String, Object>> pagedInteractions = new ArrayList<>();
            if (startIndex < totalSize) {
                pagedInteractions = allInteractions.subList(startIndex, endIndex);
            }
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            
            // 分页的互动列表
            Map<String, Object> pageInfo = new HashMap<>();
            pageInfo.put("records", pagedInteractions);
            pageInfo.put("total", totalSize);
            pageInfo.put("currentPage", currentPage);
            pageInfo.put("pageSize", pageSize);
            pageInfo.put("totalPages", (int) Math.ceil((double) totalSize / pageSize));
            result.put("interactions", pageInfo);
            
            // 统计信息
            Map<String, Long> statistics = new HashMap<>();
            try {
                statistics.put("totalLikes", likeService.countUserLikes(userId, null));
            } catch (Exception e) {
                log.warn("获取用户点赞统计失败: userId={}", userId, e);
                statistics.put("totalLikes", 0L);
            }
            
            try {
                statistics.put("totalFavorites", favoriteService.getUserFavoriteCount(userId, null));
            } catch (Exception e) {
                log.warn("获取用户收藏统计失败: userId={}", userId, e);
                statistics.put("totalFavorites", 0L);
            }
            
            try {
                Long followingCount = followService.getFollowingCount(userId);
                statistics.put("totalFollowing", followingCount);
                
                Long followersCount = followService.getFollowersCount(userId);
                statistics.put("totalFollowers", followersCount);
            } catch (Exception e) {
                log.warn("获取用户关注统计失败: userId={}", userId, e);
                statistics.put("totalFollowing", 0L);
                statistics.put("totalFollowers", 0L);
            }
            
            try {
                // 统计用户评论数量
                long totalComments = allInteractions.stream()
                        .filter(interaction -> "COMMENT".equals(interaction.get("type")))
                        .count();
                statistics.put("totalComments", totalComments);
            } catch (Exception e) {
                log.warn("获取用户评论统计失败: userId={}", userId, e);
                statistics.put("totalComments", 0L);
            }
            
            result.put("statistics", statistics);
            
            log.info("用户互动信息获取成功: userId={}, 总互动数={}, 当前页数据量={}, 统计={}", 
                    userId, totalSize, pagedInteractions.size(), statistics);
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("获取用户互动信息失败: userId={}", userId, e);
            return Result.error("获取用户互动信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户点赞统计
     */
    @GetMapping("/likes/statistics")
    @Operation(summary = "获取用户点赞统计", description = "获取用户的点赞统计信息")
    public Result<Map<String, Object>> getLikeStatistics(
            @Parameter(description = "用户ID") @RequestParam Long userId) {
        
        try {
            log.info("REST请求 - 获取用户点赞统计: userId={}", userId);
            
            Map<String, Object> statistics = new HashMap<>();
            
            // 总点赞数
            Long totalLikes = likeService.countUserLikes(userId, null);
            statistics.put("totalLikes", totalLikes);
            
            // 各类型点赞数
            statistics.put("contentLikes", likeService.countUserLikes(userId, "CONTENT"));
            statistics.put("commentLikes", likeService.countUserLikes(userId, "COMMENT"));
            statistics.put("dynamicLikes", likeService.countUserLikes(userId, "DYNAMIC"));
            
            log.info("用户点赞统计获取成功: userId={}, totalLikes={}", userId, totalLikes);
            return Result.success(statistics);
            
        } catch (Exception e) {
            log.error("获取用户点赞统计失败: userId={}", userId, e);
            return Result.error("获取用户点赞统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户收藏统计
     */
    @GetMapping("/favorites/statistics")
    @Operation(summary = "获取用户收藏统计", description = "获取用户的收藏统计信息")
    public Result<Map<String, Object>> getFavoriteStatistics(
            @Parameter(description = "用户ID") @RequestParam Long userId) {
        
        try {
            log.info("REST请求 - 获取用户收藏统计: userId={}", userId);
            
            Map<String, Object> statistics = favoriteService.getUserFavoriteStatistics(userId);
            
            log.info("用户收藏统计获取成功: userId={}", userId);
            return Result.success(statistics);
            
        } catch (Exception e) {
            log.error("获取用户收藏统计失败: userId={}", userId, e);
            return Result.error("获取用户收藏统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取关注数据的辅助方法
     */
    private Map<String, Object> getFollowData(Long userId, Integer currentPage, Integer pageSize) {
        Map<String, Object> followData = new HashMap<>();
        
        try {
            // 获取用户的关注列表（用户关注了哪些人）
            IPage<Follow> followingPage = followService.getFollowing(userId, currentPage, pageSize);
            followData.put("following", followingPage);
            log.debug("用户关注列表获取成功: 总数={}", followingPage.getTotal());
        } catch (Exception e) {
            log.warn("获取用户关注列表失败: userId={}", userId, e);
            followData.put("following", null);
        }
        
        try {
            // 获取用户的粉丝列表（哪些人关注了用户）
            IPage<Follow> followersPage = followService.getFollowers(userId, currentPage, pageSize);
            followData.put("followers", followersPage);
            log.debug("用户粉丝列表获取成功: 总数={}", followersPage.getTotal());
        } catch (Exception e) {
            log.warn("获取用户粉丝列表失败: userId={}", userId, e);
            followData.put("followers", null);
        }
        
        return followData;
    }

    /**
     * 获取用户关注统计
     */
    @GetMapping("/follows/statistics")
    @Operation(summary = "获取用户关注统计", description = "获取用户的关注和粉丝统计信息")
    public Result<Map<String, Object>> getFollowStatistics(
            @Parameter(description = "用户ID") @RequestParam Long userId) {
        
        try {
            log.info("REST请求 - 获取用户关注统计: userId={}", userId);
            
            Map<String, Object> statistics = new HashMap<>();
            
            // 关注数（用户关注了多少人）
            Long followingCount = followService.getFollowingCount(userId);
            statistics.put("followingCount", followingCount);
            
            // 粉丝数（多少人关注了用户）
            Long followersCount = followService.getFollowersCount(userId);
            statistics.put("followersCount", followersCount);
            
            // 互关数（可选，如果有相应接口）
            try {
                Map<String, Object> followStats = followService.getFollowStatistics(userId);
                statistics.putAll(followStats);
            } catch (Exception e) {
                log.debug("获取详细关注统计失败，仅返回基础统计", e);
            }
            
            log.info("用户关注统计获取成功: userId={}, following={}, followers={}", 
                    userId, followingCount, followersCount);
            return Result.success(statistics);
            
        } catch (Exception e) {
            log.error("获取用户关注统计失败: userId={}", userId, e);
            return Result.error("获取用户关注统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取关注数量的辅助方法
     */
    private Long getFollowCount(Long userId) {
        try {
            // 获取用户关注的人数（关注数）
            Long followingCount = followService.getFollowingCount(userId);
            log.debug("用户关注数量获取成功: userId={}, followingCount={}", userId, followingCount);
            return followingCount;
        } catch (Exception e) {
            log.warn("获取关注数量失败: userId={}", userId, e);
            return 0L;
        }
    }
}
