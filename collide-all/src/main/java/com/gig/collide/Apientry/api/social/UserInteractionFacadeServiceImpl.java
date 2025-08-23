package com.gig.collide.Apientry.api.social;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gig.collide.Apientry.api.common.response.PageResponse;
import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.Apientry.api.social.request.UserInteractionQueryRequest;
import com.gig.collide.Apientry.api.social.response.UserInteractionResponse;
import com.gig.collide.domain.Like;
import com.gig.collide.domain.Comment;
import com.gig.collide.domain.Content;
import com.gig.collide.mapper.LikeMapper;
import com.gig.collide.mapper.CommentMapper;
import com.gig.collide.service.ContentService;
import com.gig.collide.service.FollowService;
import com.gig.collide.service.LikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户互动查询Facade服务实现类
 * 整合用户的点赞和评论数据查询功能
 * 
 * @author GIG Team
 * @version 1.0.0
 * @since 2024-01-16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserInteractionFacadeServiceImpl implements UserInteractionFacadeService {

    private final LikeMapper likeMapper;
    private final CommentMapper commentMapper;
    private final ContentService contentService;
    private final FollowService followService;
    private final LikeService likeService;

    @Override
    public Result<PageResponse<UserInteractionResponse>> getUserInteractions(UserInteractionQueryRequest request) {
        log.info("查询用户互动数据: {}", request);
        
        // 参数验证
        if (request.getUserId() == null) {
            return Result.error("用户ID不能为空");
        }
        
        // 参数默认值设置
        if (request.getInteractionType() == null) {
            request.setInteractionType("ALL");
        }
        if (request.getDirection() == null) {
            request.setDirection("ALL");
        }
        if (request.getOrderBy() == null) {
            request.setOrderBy("createTime");
        }
        if (request.getOrderDirection() == null) {
            request.setOrderDirection("DESC");
        }
        if (request.getCurrentPage() == null || request.getCurrentPage() < 1) {
            request.setCurrentPage(1);
        }
        if (request.getPageSize() == null || request.getPageSize() < 1) {
            request.setPageSize(20);
        }

        // 根据互动类型分别查询
        List<UserInteractionResponse> allInteractions = new ArrayList<>();


        try {
            // 查询点赞数据
            if ("ALL".equals(request.getInteractionType()) || "LIKES".equals(request.getInteractionType())) {
                List<UserInteractionResponse> likes = queryUserLikes(request);
                allInteractions.addAll(likes);
            }
            
            // 查询评论数据
            if ("ALL".equals(request.getInteractionType()) || "COMMENTS".equals(request.getInteractionType())) {
                List<UserInteractionResponse> comments = queryUserComments(request);
                allInteractions.addAll(comments);
            }
            
            // 排序和分页
            List<UserInteractionResponse> sortedInteractions = sortInteractions(allInteractions, request.getOrderBy(), request.getOrderDirection());
            PageResponse<UserInteractionResponse> pageResponse = paginateInteractions(sortedInteractions, request.getCurrentPage(), request.getPageSize());
            
            // 为缺失的目标字段补充赋值
            if (pageResponse.getRecords() != null) {
                for (UserInteractionResponse response : pageResponse.getRecords()) {
                    fillMissingTargetFields(response);
                }
            }
            
            return Result.success(pageResponse);
        } catch (Exception e) {
            log.error("查询用户互动数据失败: userId={}, error={}", request.getUserId(), e.getMessage(), e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    @Override
    public Result<PageResponse<UserInteractionResponse>> getUserInteractions(
            Long userId, String interactionType, String likeType, String commentType,
            String direction, String orderBy, String orderDirection,
            Integer currentPage, Integer pageSize) {
        
        UserInteractionQueryRequest request = new UserInteractionQueryRequest();
        request.setUserId(userId);
        request.setInteractionType(interactionType != null ? interactionType : "ALL");
        request.setLikeType(likeType);
        request.setCommentType(commentType);
        request.setDirection(direction != null ? direction : "ALL");
        request.setOrderBy(orderBy != null ? orderBy : "createTime");
        request.setOrderDirection(orderDirection != null ? orderDirection : "DESC");
        request.setCurrentPage(currentPage != null ? currentPage : 1);
        request.setPageSize(pageSize != null ? pageSize : 20);


        
        return getUserInteractions(request);
    }

    @Override
    public Result<PageResponse<UserInteractionResponse>> getAllInteractions(Long userId, Integer currentPage, Integer pageSize) {
        log.info("查询所有互动数据: userId={}, page={}/{}", userId, currentPage, pageSize);
        
        // 参数验证
        if (userId == null) {
            return Result.error("用户ID不能为空");
        }
        
        // 参数默认值设置
        if (currentPage == null || currentPage < 1) {
            currentPage = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 20;
        }

        try {
            // 查询所有互动数据：我评论的，我点赞的，评论我的
            List<UserInteractionResponse> allInteractions = new ArrayList<>();

            // 1. 查询我点赞的内容
            List<UserInteractionResponse> myLikedContents = queryMyLikedContents(userId);
            allInteractions.addAll(myLikedContents);
            
            // 2. 查询我点赞的评论
            List<UserInteractionResponse> myLikedComments = queryMyLikedComments(userId);
            allInteractions.addAll(myLikedComments);
            
            // 3. 查询我发出的评论
            List<UserInteractionResponse> myComments = queryMyComments(userId);
            allInteractions.addAll(myComments);
            
            // 4. 查询我收到的评论（评论我的）
            List<UserInteractionResponse> commentsToMe = queryCommentsToMe(userId);
            allInteractions.addAll(commentsToMe);
            
            // 按时间排序
            allInteractions.sort((a, b) -> {
                if (a.getCreateTime() == null && b.getCreateTime() == null) return 0;
                if (a.getCreateTime() == null) return 1;
                if (b.getCreateTime() == null) return -1;
                return b.getCreateTime().compareTo(a.getCreateTime());
            });
            
            // 分页处理
            PageResponse<UserInteractionResponse> pageResponse = paginateInteractions(allInteractions, currentPage, pageSize);
            
            // 填充缺失字段
            if (pageResponse.getRecords() != null) {
                for (UserInteractionResponse response : pageResponse.getRecords()) {
                    enhanceInteractionResponse(response, userId);
                }
            }
            
            return Result.success(pageResponse);
        } catch (Exception e) {
            log.error("查询所有互动数据失败: userId={}, error={}", userId, e.getMessage(), e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询用户点赞数据
     */
    private List<UserInteractionResponse> queryUserLikes(UserInteractionQueryRequest request) {
        List<UserInteractionResponse> likes = new ArrayList<>();
        
        // 查询我点赞的内容
        if ("ALL".equals(request.getDirection()) || "GIVE".equals(request.getDirection())) {
            if (request.getLikeType() == null || "CONTENT".equals(request.getLikeType())) {
                Page<Like> page = new Page<>(1, 1000); // 限制最大查询数量，避免性能问题
                IPage<Like> contentLikesPage = likeMapper.findUserLikes(page, request.getUserId(), "CONTENT", "active");
                likes.addAll(convertLikesToInteractions(contentLikesPage.getRecords(), "CONTENT"));
            }
            
            if (request.getLikeType() == null || "COMMENT".equals(request.getLikeType())) {
                Page<Like> page = new Page<>(1, 1000); // 限制最大查询数量，避免性能问题
                IPage<Like> commentLikesPage = likeMapper.findUserLikes(page, request.getUserId(), "COMMENT", "active");
                likes.addAll(convertLikesToInteractions(commentLikesPage.getRecords(), "COMMENT"));
            }
        }
        
        // 查询我收到的点赞（我发布的评论被点赞）
        if ("ALL".equals(request.getDirection()) || "RECEIVE".equals(request.getDirection())) {
            if (request.getLikeType() == null || "COMMENT".equals(request.getLikeType())) {
                Page<Like> page = new Page<>(1, 1000); // 限制最大查询数量，避免性能问题
                IPage<Like> receivedCommentLikesPage = likeMapper.findAuthorLikes(page, request.getUserId(), "COMMENT", "active");
                likes.addAll(convertReceivedLikesToInteractions(receivedCommentLikesPage.getRecords(), "COMMENT"));
            }
        }
        
        return likes;
    }

    /**
     * 查询用户评论数据
     */
    private List<UserInteractionResponse> queryUserComments(UserInteractionQueryRequest request) {
        List<UserInteractionResponse> comments = new ArrayList<>();
        
        // 查询我发出的评论
        if ("ALL".equals(request.getDirection()) || "GIVE".equals(request.getDirection())) {
            if (request.getCommentType() == null || "CONTENT".equals(request.getCommentType())) {
                IPage<Comment> page = new Page<>(1, 1000); // 限制最大查询数量，避免性能问题
                IPage<Comment> contentCommentsPage = commentMapper.selectUserCommentsPage(
                    page, request.getUserId(), "COMMUNITY", "NORMAL", "createTime", "DESC", true);
                comments.addAll(convertCommentsToInteractions(contentCommentsPage.getRecords(), "CONTENT"));
            }
            
            if (request.getCommentType() == null || "DYNAMIC".equals(request.getCommentType())) {
                IPage<Comment> page = new Page<>(1, 1000); // 限制最大查询数量，避免性能问题
                IPage<Comment> dynamicCommentsPage = commentMapper.selectUserCommentsPage(
                    page, request.getUserId(), "VIDEO", "NORMAL", "createTime", "DESC", true);
                comments.addAll(convertCommentsToInteractions(dynamicCommentsPage.getRecords(), "DYNAMIC"));
            }
        }
        
        // 查询我收到的评论（回复我的评论）
        if ("ALL".equals(request.getDirection()) || "RECEIVE".equals(request.getDirection())) {
            IPage<Comment> page = new Page<>(1, 1000); // 限制最大查询数量，避免性能问题
            IPage<Comment> receivedCommentsPage = commentMapper.selectUserRepliesPage(
                page, request.getUserId(), "NORMAL", "createTime", "DESC", true);
            comments.addAll(convertReceivedCommentsToInteractions(receivedCommentsPage.getRecords()));
        }
        
        return comments;
    }

    /**
     * 将点赞数据转换为互动响应对象
     */
    private List<UserInteractionResponse> convertLikesToInteractions(List<Like> likes, String subType) {
        if (likes == null || likes.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 获取所有内容ID和作者ID
        Set<Long> contentIds = likes.stream()
                .filter(like -> "CONTENT".equals(subType))
                .map(Like::getTargetId)
                .collect(Collectors.toSet());
        
        Set<Long> authorIds = likes.stream()
                .map(Like::getTargetAuthorId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        
        // 批量获取内容信息
        Map<Long, Content> contentMap = new java.util.HashMap<>();
        if (!contentIds.isEmpty()) {
            contentIds.forEach(contentId -> {
                try {
                    Content content = contentService.getContentById(contentId, false);
                    if (content != null) {
                        contentMap.put(contentId, content);
                    }
                } catch (Exception e) {
                    log.warn("获取内容信息失败: contentId={}", contentId, e);
                }
            });
        }
        
        // 批量获取关注状态
        Map<Long, Boolean> followStatusMap = new java.util.HashMap<>();
        if (!authorIds.isEmpty() && likes.get(0).getUserId() != null) {
            Long currentUserId = likes.get(0).getUserId();
            authorIds.forEach(authorId -> {
                try {
                    boolean isFollowing = followService.checkFollowStatus(currentUserId, authorId);
                    followStatusMap.put(authorId, isFollowing);
                } catch (Exception e) {
                    log.warn("获取关注状态失败: userId={}, authorId={}", currentUserId, authorId, e);
                    followStatusMap.put(authorId, false);
                }
            });
        }
        
        // 批量获取内容点赞数量
        Map<Long, Long> contentLikeCountMap = new java.util.HashMap<>();
        if (!contentIds.isEmpty()) {
            contentIds.forEach(contentId -> {
                try {
                    Long likeCount = likeService.countTargetLikes(contentId, "CONTENT");
                    contentLikeCountMap.put(contentId, likeCount);
                } catch (Exception e) {
                    log.warn("获取内容点赞数量失败: contentId={}", contentId, e);
                    contentLikeCountMap.put(contentId, 0L);
                }
            });
        }
        
        return likes.stream().map(like -> {
            UserInteractionResponse response = new UserInteractionResponse();
            response.setId(like.getId());
            response.setInteractionType("LIKE");
            response.setSubType(subType);
            response.setTargetId(like.getTargetId());
            
            // 设置内容作者信息
            if ("CONTENT".equals(subType) && contentMap.containsKey(like.getTargetId())) {
                Content content = contentMap.get(like.getTargetId());
                response.setTitle(content.getTitle());
                response.setAuthorId(content.getAuthorId());
                response.setAuthorNickname(content.getAuthorNickname());
                response.setAuthorAvatar(content.getAuthorAvatar());
            } else {
                // 如果无法获取内容信息，使用冗余字段的值
                response.setTitle(like.getTargetTitle());
                response.setAuthorId(like.getTargetAuthorId());
                response.setAuthorNickname(null);
                response.setAuthorAvatar(null);
            }
            
            // 设置关注状态
            if (response.getAuthorId() != null) {
                response.setIsFollowingAuthor(followStatusMap.getOrDefault(response.getAuthorId(), false));
            } else {
                response.setIsFollowingAuthor(false);
            }
            
            // 设置内容点赞数量
            if ("CONTENT".equals(subType)) {
                response.setContentLikeCount(contentLikeCountMap.getOrDefault(like.getTargetId(), 0L));
            } else {
                response.setContentLikeCount(null);
            }
            
            // 设置是否点赞该内容（对于点赞记录，总是true）
            response.setIsLike(true);
            
            // 设置内容封面图片和描述
            if ("CONTENT".equals(subType) && contentMap.containsKey(like.getTargetId())) {
                Content content = contentMap.get(like.getTargetId());
                response.setContentCoverUrl(convertCoverUrlToArray(content.getCoverUrl()));
                response.setContentDescription(content.getDescription());
            } else {
                response.setContentCoverUrl(new ArrayList<>());
                response.setContentDescription(null);
            }
            
            response.setUserId(like.getUserId());
            response.setUserNickname(like.getUserNickname());
            response.setUserAvatar(like.getUserAvatar());
            response.setStatus(like.getStatus());
            response.setCreateTime(like.getCreateTime());
            response.setUpdateTime(like.getUpdateTime());
            return response;
        }).collect(Collectors.toList());
    }

    /**
     * 将收到的点赞数据转换为互动响应对象
     */
    private List<UserInteractionResponse> convertReceivedLikesToInteractions(List<Like> likes, String subType) {
        if (likes == null || likes.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 获取所有内容ID和作者ID
        Set<Long> contentIds = likes.stream()
                .filter(like -> "CONTENT".equals(subType))
                .map(Like::getTargetId)
                .collect(Collectors.toSet());
        
        Set<Long> authorIds = likes.stream()
                .map(Like::getTargetAuthorId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        
        // 批量获取内容信息
        Map<Long, Content> contentMap = new java.util.HashMap<>();
        if (!contentIds.isEmpty()) {
            contentIds.forEach(contentId -> {
                try {
                    Content content = contentService.getContentById(contentId, false);
                    if (content != null) {
                        contentMap.put(contentId, content);
                    }
                } catch (Exception e) {
                    log.warn("获取内容信息失败: contentId={}", contentId, e);
                }
            });
        }
        
        // 批量获取关注状态
        Map<Long, Boolean> followStatusMap = new java.util.HashMap<>();
        if (!authorIds.isEmpty() && likes.get(0).getUserId() != null) {
            Long currentUserId = likes.get(0).getUserId();
            authorIds.forEach(authorId -> {
                try {
                    boolean isFollowing = followService.checkFollowStatus(currentUserId, authorId);
                    followStatusMap.put(authorId, isFollowing);
                } catch (Exception e) {
                    log.warn("获取关注状态失败: userId={}, authorId={}", currentUserId, authorId, e);
                    followStatusMap.put(authorId, false);
                }
            });
        }
        
        // 批量获取内容点赞数量
        Map<Long, Long> contentLikeCountMap = new java.util.HashMap<>();
        if (!contentIds.isEmpty()) {
            contentIds.forEach(contentId -> {
                try {
                    Long likeCount = likeService.countTargetLikes(contentId, "CONTENT");
                    contentLikeCountMap.put(contentId, likeCount);
                } catch (Exception e) {
                    log.warn("获取内容点赞数量失败: contentId={}", contentId, e);
                    contentLikeCountMap.put(contentId, 0L);
                }
            });
        }
        
        return likes.stream().map(like -> {
            UserInteractionResponse response = new UserInteractionResponse();
            response.setId(like.getId());
            response.setInteractionType("LIKE");
            response.setSubType(subType);
            response.setTargetId(like.getTargetId());
            
            // 设置内容作者信息
            if ("CONTENT".equals(subType) && contentMap.containsKey(like.getTargetId())) {
                Content content = contentMap.get(like.getTargetId());
                response.setTitle(content.getTitle());
                response.setAuthorId(content.getAuthorId());
                response.setAuthorNickname(content.getAuthorNickname());
                response.setAuthorAvatar(content.getAuthorAvatar());
            } else {
                // 如果无法获取内容信息，使用冗余字段的值
                response.setTitle(like.getTargetTitle());
                response.setAuthorId(like.getTargetAuthorId());
                response.setAuthorNickname(null);
                response.setAuthorAvatar(null);
            }
            
            // 设置关注状态
            if (like.getTargetAuthorId() != null) {
                response.setIsFollowingAuthor(followStatusMap.getOrDefault(like.getTargetAuthorId(), false));
            } else {
                response.setIsFollowingAuthor(false);
            }
            
            // 设置内容点赞数量
            if ("CONTENT".equals(subType)) {
                response.setContentLikeCount(contentLikeCountMap.getOrDefault(like.getTargetId(), 0L));
            } else {
                response.setContentLikeCount(null);
            }
            
            // 设置是否点赞该内容（对于收到的点赞记录，总是true）
            response.setIsLike(true);
            
            // 设置内容封面图片和描述
            if ("CONTENT".equals(subType) && contentMap.containsKey(like.getTargetId())) {
                Content content = contentMap.get(like.getTargetId());
                response.setContentCoverUrl(convertCoverUrlToArray(content.getCoverUrl()));
                response.setContentDescription(content.getDescription());
            } else {
                response.setContentCoverUrl(new ArrayList<>());
                response.setContentDescription(null);
            }
            
            response.setUserId(like.getUserId());
            response.setUserNickname(like.getUserNickname());
            response.setUserAvatar(like.getUserAvatar());
            response.setStatus(like.getStatus());
            response.setCreateTime(like.getCreateTime());
            response.setUpdateTime(like.getUpdateTime());
            return response;
        }).collect(Collectors.toList());
    }

    /**
     * 将评论数据转换为互动响应对象
     */
    private List<UserInteractionResponse> convertCommentsToInteractions(List<Comment> comments, String subType) {
        if (comments == null || comments.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 获取所有内容ID
        Set<Long> contentIds = comments.stream()
                .filter(comment -> "CONTENT".equals(subType))
                .map(Comment::getTargetId)
                .collect(Collectors.toSet());
        
        // 批量获取内容信息
        Map<Long, Content> contentMap = new java.util.HashMap<>();
        if (!contentIds.isEmpty()) {
            contentIds.forEach(contentId -> {
                try {
                    Content content = contentService.getContentById(contentId, false);
                    if (content != null) {
                        contentMap.put(contentId, content);
                    }
                } catch (Exception e) {
                    log.warn("获取内容信息失败: contentId={}", contentId, e);
                }
            });
        }
        
        // 批量获取关注状态
        Map<Long, Boolean> followStatusMap = new java.util.HashMap<>();
        if (!contentIds.isEmpty() && comments.get(0).getUserId() != null) {
            Long currentUserId = comments.get(0).getUserId();
            contentMap.values().forEach(content -> {
                if (content.getAuthorId() != null) {
                    try {
                        boolean isFollowing = followService.checkFollowStatus(currentUserId, content.getAuthorId());
                        followStatusMap.put(content.getAuthorId(), isFollowing);
                    } catch (Exception e) {
                        log.warn("获取关注状态失败: userId={}, authorId={}", currentUserId, content.getAuthorId(), e);
                        followStatusMap.put(content.getAuthorId(), false);
                    }
                }
            });
        }
        
        // 批量获取内容点赞数量
        Map<Long, Long> contentLikeCountMap = new java.util.HashMap<>();
        if (!contentIds.isEmpty()) {
            contentIds.forEach(contentId -> {
                try {
                    Long likeCount = likeService.countTargetLikes(contentId, "CONTENT");
                    contentLikeCountMap.put(contentId, likeCount);
                } catch (Exception e) {
                    log.warn("获取内容点赞数量失败: contentId={}", contentId, e);
                    contentLikeCountMap.put(contentId, 0L);
                }
            });
        }
        
        // 批量获取用户对内容的点赞状态
        Map<Long, Boolean> userLikeStatusMap = new java.util.HashMap<>();
        if (!contentIds.isEmpty() && comments.get(0).getUserId() != null) {
            Long currentUserId = comments.get(0).getUserId();
            contentIds.forEach(contentId -> {
                try {
                    boolean isLiked = likeService.checkLikeStatus(currentUserId, "CONTENT", contentId);
                    userLikeStatusMap.put(contentId, isLiked);
                } catch (Exception e) {
                    log.warn("获取用户点赞状态失败: userId={}, contentId={}", currentUserId, contentId, e);
                    userLikeStatusMap.put(contentId, false);
                }
            });
        }
        
        return comments.stream().map(comment -> {
            UserInteractionResponse response = new UserInteractionResponse();
            response.setId(comment.getId());
            response.setInteractionType("COMMENT");
            response.setSubType(subType);
            response.setTargetId(comment.getTargetId());
            
            // 设置内容作者信息
            if ("CONTENT".equals(subType) && contentMap.containsKey(comment.getTargetId())) {
                Content content = contentMap.get(comment.getTargetId());
                response.setTitle(content.getTitle());
                response.setAuthorId(content.getAuthorId());
                response.setAuthorNickname(content.getAuthorNickname());
                response.setAuthorAvatar(content.getAuthorAvatar());
                
                // 设置关注状态
                if (content.getAuthorId() != null) {
                    response.setIsFollowingAuthor(followStatusMap.getOrDefault(content.getAuthorId(), false));
                } else {
                    response.setIsFollowingAuthor(false);
                }
                
                // 设置内容点赞数量
                response.setContentLikeCount(contentLikeCountMap.getOrDefault(comment.getTargetId(), 0L));
                
                // 设置是否点赞该内容
                response.setIsLike(userLikeStatusMap.getOrDefault(comment.getTargetId(), false));
                
                // 设置内容封面图片和描述
                response.setContentCoverUrl(convertCoverUrlToArray(content.getCoverUrl()));
                response.setContentDescription(content.getDescription());
            } else {
                // 如果无法获取内容信息，设置默认值
                response.setTitle("未知内容");
                response.setAuthorId(null);
                response.setAuthorNickname(null);
                response.setAuthorAvatar(null);
                response.setIsFollowingAuthor(false);
                response.setContentLikeCount(0L);
                response.setIsLike(false);
                response.setContentCoverUrl(new ArrayList<>());
                response.setContentDescription(null);
            }
            
            response.setUserId(comment.getUserId());
            response.setUserNickname(comment.getUserNickname());
            response.setUserAvatar(comment.getUserAvatar());
            response.setCommentContent(comment.getContent());
            response.setParentCommentId(comment.getParentCommentId());
            response.setReplyToUserId(comment.getReplyToUserId());
            response.setReplyToUserNickname(comment.getReplyToUserNickname());
            response.setLikeCount(comment.getLikeCount());
            response.setReplyCount(comment.getReplyCount());
            response.setStatus(comment.getStatus());
            response.setCreateTime(comment.getCreateTime());
            response.setUpdateTime(comment.getUpdateTime());
            return response;
        }).collect(Collectors.toList());
    }

    /**
     * 将收到的评论数据转换为互动响应对象
     */
    private List<UserInteractionResponse> convertReceivedCommentsToInteractions(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 获取所有内容ID
        Set<Long> contentIds = comments.stream()
                .filter(comment -> "CONTENT".equals(comment.getCommentType()))
                .map(Comment::getTargetId)
                .collect(Collectors.toSet());
        
        // 批量获取内容信息
        Map<Long, Content> contentMap = new java.util.HashMap<>();
        if (!contentIds.isEmpty()) {
            contentIds.forEach(contentId -> {
                try {
                    Content content = contentService.getContentById(contentId, false);
                    if (content != null) {
                        contentMap.put(contentId, content);
                    }
                } catch (Exception e) {
                    log.warn("获取内容信息失败: contentId={}", contentId, e);
                }
            });
        }
        
        // 批量获取关注状态
        Map<Long, Boolean> followStatusMap = new java.util.HashMap<>();
        if (!contentIds.isEmpty() && comments.get(0).getUserId() != null) {
            Long currentUserId = comments.get(0).getUserId();
            contentMap.values().forEach(content -> {
                if (content.getAuthorId() != null) {
                    try {
                        boolean isFollowing = followService.checkFollowStatus(currentUserId, content.getAuthorId());
                        followStatusMap.put(content.getAuthorId(), isFollowing);
                    } catch (Exception e) {
                        log.warn("获取关注状态失败: userId={}, authorId={}", currentUserId, content.getAuthorId(), e);
                        followStatusMap.put(content.getAuthorId(), false);
                    }
                }
            });
        }
        
        // 批量获取内容点赞数量
        Map<Long, Long> contentLikeCountMap = new java.util.HashMap<>();
        if (!contentIds.isEmpty()) {
            contentIds.forEach(contentId -> {
                try {
                    Long likeCount = likeService.countTargetLikes(contentId, "CONTENT");
                    contentLikeCountMap.put(contentId, likeCount);
                } catch (Exception e) {
                    log.warn("获取内容点赞数量失败: contentId={}", contentId, e);
                    contentLikeCountMap.put(contentId, 0L);
                }
            });
        }
        
        // 批量获取用户对内容的点赞状态
        Map<Long, Boolean> userLikeStatusMap = new java.util.HashMap<>();
        if (!contentIds.isEmpty() && comments.get(0).getUserId() != null) {
            Long currentUserId = comments.get(0).getUserId();
            contentIds.forEach(contentId -> {
                try {
                    boolean isLiked = likeService.checkLikeStatus(currentUserId, "CONTENT", contentId);
                    userLikeStatusMap.put(contentId, isLiked);
                } catch (Exception e) {
                    log.warn("获取用户点赞状态失败: userId={}, contentId={}", currentUserId, contentId, e);
                    userLikeStatusMap.put(contentId, false);
                }
            });
        }
        
        return comments.stream().map(comment -> {
            UserInteractionResponse response = new UserInteractionResponse();
            response.setId(comment.getId());
            response.setInteractionType("COMMENT");
            response.setSubType(comment.getCommentType());
            response.setTargetId(comment.getTargetId());
            
            // 设置内容作者信息
            if ("CONTENT".equals(comment.getCommentType()) && contentMap.containsKey(comment.getTargetId())) {
                Content content = contentMap.get(comment.getTargetId());
                response.setTitle(content.getTitle());
                response.setAuthorId(content.getAuthorId());
                response.setAuthorNickname(content.getAuthorNickname());
                response.setAuthorAvatar(content.getAuthorAvatar());
                
                // 设置关注状态
                if (content.getAuthorId() != null) {
                    response.setIsFollowingAuthor(followStatusMap.getOrDefault(content.getAuthorId(), false));
                } else {
                    response.setIsFollowingAuthor(false);
                }
                
                // 设置内容点赞数量
                response.setContentLikeCount(contentLikeCountMap.getOrDefault(comment.getTargetId(), 0L));
                
                // 设置是否点赞该内容
                response.setIsLike(userLikeStatusMap.getOrDefault(comment.getTargetId(), false));
                
                // 设置内容封面图片和描述
                response.setContentCoverUrl(convertCoverUrlToArray(content.getCoverUrl()));
                response.setContentDescription(content.getDescription());
            } else {
                // 如果无法获取内容信息，设置默认值
                response.setTitle("未知内容");
                response.setAuthorId(null);
                response.setAuthorNickname(null);
                response.setAuthorAvatar(null);
                response.setIsFollowingAuthor(false);
                response.setContentLikeCount(0L);
                response.setIsLike(false);
                response.setContentCoverUrl(new ArrayList<>());
                response.setContentDescription(null);
            }
            
            response.setUserId(comment.getUserId());
            response.setUserNickname(comment.getUserNickname());
            response.setUserAvatar(comment.getUserAvatar());
            response.setCommentContent(comment.getContent());
            response.setParentCommentId(comment.getParentCommentId());
            response.setReplyToUserId(comment.getReplyToUserId());
            response.setReplyToUserNickname(comment.getReplyToUserNickname());
            response.setLikeCount(comment.getLikeCount());
            response.setReplyCount(comment.getReplyCount());
            response.setStatus(comment.getStatus());
            response.setCreateTime(comment.getCreateTime());
            response.setUpdateTime(comment.getUpdateTime());
            return response;
        }).collect(Collectors.toList());
    }

    /**
     * 对互动数据进行排序
     */
    private List<UserInteractionResponse> sortInteractions(List<UserInteractionResponse> interactions, String orderBy, String orderDirection) {
        if (interactions == null || interactions.isEmpty()) {
            return new ArrayList<>();
        }
        
        if (orderBy == null) {
            orderBy = "createTime";
        }
        if (orderDirection == null) {
            orderDirection = "DESC";
        }
        
        if ("updateTime".equals(orderBy)) {
            if ("ASC".equals(orderDirection)) {
                interactions.sort((a, b) -> {
                    if (a.getUpdateTime() == null && b.getUpdateTime() == null) return 0;
                    if (a.getUpdateTime() == null) return -1;
                    if (b.getUpdateTime() == null) return 1;
                    return a.getUpdateTime().compareTo(b.getUpdateTime());
                });
            } else {
                interactions.sort((a, b) -> {
                    if (a.getUpdateTime() == null && b.getUpdateTime() == null) return 0;
                    if (a.getUpdateTime() == null) return 1;
                    if (b.getUpdateTime() == null) return -1;
                    return b.getUpdateTime().compareTo(a.getUpdateTime());
                });
            }
        } else {
            if ("ASC".equals(orderDirection)) {
                interactions.sort((a, b) -> {
                    if (a.getCreateTime() == null && b.getCreateTime() == null) return 0;
                    if (a.getCreateTime() == null) return -1;
                    if (b.getCreateTime() == null) return 1;
                    return a.getCreateTime().compareTo(b.getCreateTime());
                });
            } else {
                interactions.sort((a, b) -> {
                    if (a.getCreateTime() == null && b.getCreateTime() == null) return 0;
                    if (a.getCreateTime() == null) return 1;
                    if (b.getCreateTime() == null) return -1;
                    return b.getCreateTime().compareTo(a.getCreateTime());
                });
            }
        }
        return interactions;
    }

    /**
     * 对互动数据进行分页
     */
    private PageResponse<UserInteractionResponse> paginateInteractions(List<UserInteractionResponse> interactions, Integer currentPage, Integer pageSize) {
        if (interactions == null) {
            interactions = new ArrayList<>();
        }
        if (currentPage == null || currentPage < 1) {
            currentPage = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 20;
        }
        
        int total = interactions.size();
        int startIndex = (currentPage - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, total);
        
        List<UserInteractionResponse> pageData = new ArrayList<>();
        if (startIndex < total) {
            pageData = interactions.subList(startIndex, endIndex);
        }
        
        PageResponse<UserInteractionResponse> response = new PageResponse<>();
        response.setRecords(pageData);
        response.setTotal((long) total);
        response.setCurrentPage(currentPage);
        response.setPageSize(pageSize);
        response.setTotalPages((int) Math.ceil((double) total / pageSize));
        
        return response;
    }

    /**
     * 将单个URL转换为URL数组
     * 如果URL包含逗号分隔符，则按逗号分割
     * 如果URL为空或null，则返回空数组
     * 
     * @param coverUrl 封面URL字符串
     * @return URL数组
     */
    private List<String> convertCoverUrlToArray(String coverUrl) {
        if (!StringUtils.hasText(coverUrl)) {
            return new ArrayList<>();
        }
        
        // 如果URL包含逗号，按逗号分割
        if (coverUrl.contains(",")) {
            return Arrays.stream(coverUrl.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toList());
        }
        
        // 单个URL，包装成数组
        return Arrays.asList(coverUrl.trim());
    }

    /**
     * 填充缺失的目标字段信息
     * 根据互动类型和目标ID，从相应的服务中获取目标对象的详细信息
     * 
     * @param response 用户互动响应对象
     */
    private void fillMissingTargetFields(UserInteractionResponse response) {
        if (response.getTargetId() == null) {
            return;
        }

        try {
            // 根据子类型判断是内容还是动态
            if ("CONTENT".equals(response.getSubType())) {
                // 获取内容信息
                Content content = contentService.getContentById(response.getTargetId(), false);
                if (content != null) {
                    if (response.getTitle() == null) {
                        response.setTitle(content.getTitle());
                    }
                    if (response.getAuthorId() == null) {
                        response.setAuthorId(content.getAuthorId());
                    }
                    if (response.getAuthorNickname() == null) {
                        response.setAuthorNickname(content.getAuthorNickname());
                    }
                    if (response.getAuthorAvatar() == null) {
                        response.setAuthorAvatar(content.getAuthorAvatar());
                    }
                }
            } else if ("DYNAMIC".equals(response.getSubType())) {
                // 对于动态类型，暂时只记录日志
                log.debug("DYNAMIC类型的目标字段填充: targetId={}", response.getTargetId());
                // 注意：如果需要支持DYNAMIC类型，需要注入SocialDynamicService
            }
        } catch (Exception e) {
            log.warn("填充目标字段信息失败: targetId={}, subType={}, error={}", 
                    response.getTargetId(), response.getSubType(), e.getMessage());
        }
    }

    /**
     * 查询我点赞的内容
     */
    private List<UserInteractionResponse> queryMyLikedContents(Long userId) {
        try {
            Page<Like> page = new Page<>(1, 1000);
            IPage<Like> likesPage = likeMapper.findUserLikes(page, userId, "COMMENT", "active");
            return convertLikesToInteractions(likesPage.getRecords(), "COMMENT");
        } catch (Exception e) {
            log.warn("查询我点赞的内容失败: userId={}", userId, e);
            return new ArrayList<>();
        }
    }

    /**
     * 查询我点赞的评论
     */
    private List<UserInteractionResponse> queryMyLikedComments(Long userId) {
        try {
            Page<Like> page = new Page<>(1, 1000);
            IPage<Like> likesPage = likeMapper.findUserLikes(page, userId, "COMMENT", "active");
            return convertLikesToInteractions(likesPage.getRecords(), "COMMENT");
        } catch (Exception e) {
            log.warn("查询我点赞的评论失败: userId={}", userId, e);
            return new ArrayList<>();
        }
    }

    /**
     * 查询我发出的评论
     */
    private List<UserInteractionResponse> queryMyComments(Long userId) {
        List<UserInteractionResponse> allComments = new ArrayList<>();
        try {
            // 查询我对内容的评论
            IPage<Comment> contentCommentsPage = new Page<>(1, 1000);
            contentCommentsPage = commentMapper.selectUserCommentsPage(
                contentCommentsPage, userId, "CONTENT", "NORMAL", "createTime", "DESC", true);
            allComments.addAll(convertCommentsToInteractions(contentCommentsPage.getRecords(), "CONTENT"));
            
            // 查询我对动态的评论
            IPage<Comment> dynamicCommentsPage = new Page<>(1, 1000);
            dynamicCommentsPage = commentMapper.selectUserCommentsPage(
                dynamicCommentsPage, userId, "DYNAMIC", "NORMAL", "createTime", "DESC", true);
            allComments.addAll(convertCommentsToInteractions(dynamicCommentsPage.getRecords(), "DYNAMIC"));
            
        } catch (Exception e) {
            log.warn("查询我发出的评论失败: userId={}", userId, e);
        }
        return allComments;
    }

    /**
     * 查询我收到的评论（评论我的）
     */
    private List<UserInteractionResponse> queryCommentsToMe(Long userId) {
        try {
            IPage<Comment> page = new Page<>(1, 1000);
            IPage<Comment> commentsPage = commentMapper.selectUserRepliesPage(
                page, userId, "NORMAL", "createTime", "DESC", true);
            return convertReceivedCommentsToInteractions(commentsPage.getRecords());
        } catch (Exception e) {
            log.warn("查询我收到的评论失败: userId={}", userId, e);
            return new ArrayList<>();
        }
    }

    /**
     * 增强互动响应数据，填充所有必要字段
     */
    private void enhanceInteractionResponse(UserInteractionResponse response, Long currentUserId) {
        if (response == null) {
            return;
        }

        try {
            // 根据互动类型和子类型，填充不同的字段
            if ("COMMENT".equals(response.getInteractionType())) {
                enhanceCommentResponse(response, currentUserId);
            } else if ("LIKE".equals(response.getInteractionType())) {
                enhanceLikeResponse(response, currentUserId);
            }
        } catch (Exception e) {
            log.warn("增强互动响应数据失败: interactionType={}, targetId={}", 
                response.getInteractionType(), response.getTargetId(), e);
        }
    }

    /**
     * 增强评论响应数据
     */
    private void enhanceCommentResponse(UserInteractionResponse response, Long currentUserId) {
        // 填充内容相关信息
        if ("CONTENT".equals(response.getSubType()) && response.getTargetId() != null) {
            try {
                Content content = contentService.getContentById(response.getTargetId(), false);
                if (content != null) {
                    response.setTitle(content.getTitle());
                    response.setAuthorId(content.getAuthorId());
                    response.setAuthorNickname(content.getAuthorNickname());
                    response.setAuthorAvatar(content.getAuthorAvatar());
                    response.setContentCoverUrl(convertCoverUrlToArray(content.getCoverUrl()));
                    response.setContentDescription(content.getDescription());
                    
                    // 获取内容点赞数
                    Long contentLikeCount = likeService.countTargetLikes(response.getTargetId(), "CONTENT");
                    response.setContentLikeCount(contentLikeCount);
                    
                    // 检查当前用户是否点赞了该内容
                    boolean isLiked = likeService.checkLikeStatus(currentUserId, "CONTENT", response.getTargetId());
                    response.setIsLike(isLiked);
                    
                    // 检查是否关注内容作者
                    if (content.getAuthorId() != null && !content.getAuthorId().equals(currentUserId)) {
                        boolean isFollowing = followService.checkFollowStatus(currentUserId, content.getAuthorId());
                        response.setIsFollowingAuthor(isFollowing);
                    } else {
                        response.setIsFollowingAuthor(false);
                    }
                }
            } catch (Exception e) {
                log.warn("增强评论内容信息失败: targetId={}", response.getTargetId(), e);
            }
        }
        
        // 评论相关标识会根据subType和interactionType自动计算，无需手动设置
    }

    /**
     * 增强点赞响应数据
     */
    private void enhanceLikeResponse(UserInteractionResponse response, Long currentUserId) {
        // 填充内容相关信息
        if ("CONTENT".equals(response.getSubType()) && response.getTargetId() != null) {
            try {
                Content content = contentService.getContentById(response.getTargetId(), false);
                if (content != null) {
                    response.setTitle(content.getTitle());
                    response.setAuthorId(content.getAuthorId());
                    response.setAuthorNickname(content.getAuthorNickname());
                    response.setAuthorAvatar(content.getAuthorAvatar());
                    response.setContentCoverUrl(convertCoverUrlToArray(content.getCoverUrl()));
                    response.setContentDescription(content.getDescription());
                    
                    // 获取内容点赞数
                    Long contentLikeCount = likeService.countTargetLikes(response.getTargetId(), "CONTENT");
                    response.setContentLikeCount(contentLikeCount);
                    
                    // 对于点赞记录，当前用户肯定已经点赞了
                    response.setIsLike(true);
                    
                    // 检查是否关注内容作者
                    if (content.getAuthorId() != null && !content.getAuthorId().equals(currentUserId)) {
                        boolean isFollowing = followService.checkFollowStatus(currentUserId, content.getAuthorId());
                        response.setIsFollowingAuthor(isFollowing);
                    } else {
                        response.setIsFollowingAuthor(false);
                    }
                }
            } catch (Exception e) {
                log.warn("增强点赞内容信息失败: targetId={}", response.getTargetId(), e);
            }
        }
        
        // 点赞相关标识会根据subType和interactionType自动计算，无需手动设置
    }
}
