package com.gig.collide.service.Impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gig.collide.domain.Comment;
import com.gig.collide.mapper.CommentMapper;
import com.gig.collide.service.CommentService;
import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.Apientry.api.common.response.PageResponse;
import com.gig.collide.Apientry.api.comment.response.CommentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * 评论业务服务实现 - C端简洁版
 * 只实现客户端使用的核心接口
 *
 * @author Collide
 * @version 2.0.0 (C端简洁版)
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;

    // =================== 基础CRUD ===================

    @Override
    @Transactional
    public Comment createComment(Comment comment) {
        log.info("创建评论: {}", comment);

        // 参数验证
        if (comment == null || !StringUtils.hasText(comment.getContent())) {
            throw new IllegalArgumentException("评论内容不能为空");
        }

        // 设置默认值
        comment.setStatus("NORMAL");
        comment.setLikeCount(0);
        comment.setReplyCount(0);
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());

        // 保存评论
        commentMapper.insert(comment);

        // 如果是回复评论，更新父评论的回复数
        if (comment.getParentCommentId() != null && comment.getParentCommentId() > 0) {
            commentMapper.increaseReplyCount(comment.getParentCommentId(), 1);
        }

        log.info("评论创建成功: {}", comment.getId());
        return comment;
    }

    @Override
    @Transactional
    public Comment updateComment(Comment comment) {
        log.info("更新评论: {}", comment.getId());

        // 参数验证
        if (comment == null || comment.getId() == null) {
            throw new IllegalArgumentException("评论ID不能为空");
        }

        // 获取原评论
        Comment existingComment = commentMapper.selectById(comment.getId());
        if (existingComment == null) {
            throw new IllegalArgumentException("评论不存在");
        }

        // 只更新允许的字段
        existingComment.setContent(comment.getContent());
        existingComment.setUpdateTime(LocalDateTime.now());

        // 更新评论
        commentMapper.updateById(existingComment);

        log.info("评论更新成功: {}", comment.getId());
        return existingComment;
    }

    @Override
    @Transactional
    public boolean deleteComment(Long commentId, Long userId) {
        log.info("删除评论: {}, 用户: {}", commentId, userId);

        // 参数验证
        if (commentId == null || userId == null) {
            throw new IllegalArgumentException("评论ID和用户ID不能为空");
        }

        // 获取评论
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new IllegalArgumentException("评论不存在");
        }

        // 权限验证（只能删除自己的评论）
        if (!comment.getUserId().equals(userId)) {
            throw new IllegalArgumentException("只能删除自己的评论");
        }

        // 逻辑删除
        comment.setStatus("DELETED");
        comment.setUpdateTime(LocalDateTime.now());
        commentMapper.updateById(comment);

        // 如果是回复评论，减少父评论的回复数
        if (comment.getParentCommentId() != null && comment.getParentCommentId() > 0) {
            commentMapper.increaseReplyCount(comment.getParentCommentId(), -1);
        }

        log.info("评论删除成功: {}", commentId);
        return true;
    }

    @Override
    public Comment getCommentById(Long commentId) {
        log.info("获取评论详情: {}", commentId);

        if (commentId == null) {
            throw new IllegalArgumentException("评论ID不能为空");
        }

        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || "DELETED".equals(comment.getStatus())) {
            return null;
        }

        return comment;
    }

    // =================== 查询功能 ===================

    @Override
    public IPage<Comment> getTargetComments(Long targetId, String commentType, Long parentCommentId,
                                            Integer currentPage, Integer pageSize) {
        log.info("获取目标评论: targetId={}, commentType={}, parentCommentId={}, page={}, size={}",
                targetId, commentType, parentCommentId, currentPage, pageSize);

        // 参数验证
        if (targetId == null) {
            throw new IllegalArgumentException("目标ID不能为空");
        }

        // 设置默认值
        currentPage = currentPage == null ? 1 : currentPage;
        pageSize = pageSize == null ? 10 : pageSize;
        parentCommentId = parentCommentId == null ? 0L : parentCommentId;

        // 创建分页对象
        Page<Comment> page = new Page<>(currentPage, pageSize);

        // 查询评论
        IPage<Comment> result = commentMapper.selectTargetCommentsPage(page, targetId, commentType,
                parentCommentId, "NORMAL", "create_time", "DESC", false);

        log.info("获取目标评论成功: 总数={}", result.getTotal());
        return result;
    }

    @Override
    public IPage<Comment> getCommentReplies(Long parentCommentId, Integer currentPage, Integer pageSize) {
        log.info("获取评论回复: parentCommentId={}, page={}, size={}", parentCommentId, currentPage, pageSize);

        // 参数验证
        if (parentCommentId == null || parentCommentId <= 0) {
            throw new IllegalArgumentException("父评论ID不能为空");
        }

        // 设置默认值
        currentPage = currentPage == null ? 1 : currentPage;
        pageSize = pageSize == null ? 10 : pageSize;

        // 创建分页对象
        Page<Comment> page = new Page<>(currentPage, pageSize);

        // 查询回复
        IPage<Comment> result = commentMapper.selectTargetCommentsPage(page, null, null,
                parentCommentId, "NORMAL", "create_time", "ASC", false);

        log.info("获取评论回复成功: 总数={}", result.getTotal());
        return result;
    }

    @Override
    public IPage<Comment> getCommentTree(Long targetId, String commentType, Integer maxDepth,
                                         Integer currentPage, Integer pageSize) {
        log.info("获取评论树: targetId={}, commentType={}, maxDepth={}, page={}, size={}",
                targetId, commentType, maxDepth, currentPage, pageSize);

        // 参数验证
        if (targetId == null) {
            throw new IllegalArgumentException("目标ID不能为空");
        }

        // 设置默认值
        currentPage = currentPage == null ? 1 : currentPage;
        pageSize = pageSize == null ? 10 : pageSize;
        maxDepth = maxDepth == null ? 3 : maxDepth;

        // 查询评论树
        List<Comment> treeComments = commentMapper.selectCommentTree(targetId, commentType,
                maxDepth, "NORMAL", "create_time", "ASC");

        // 构建分页结果
        IPage<Comment> result = new Page<>(currentPage, pageSize);
        result.setRecords(treeComments);
        result.setTotal(treeComments.size());

        log.info("获取评论树成功: 总数={}", result.getTotal());
        return result;
    }

    @Override
    public IPage<Comment> getUserComments(Long userId, String commentType, Integer currentPage, Integer pageSize) {
        log.info("获取用户评论: userId={}, commentType={}, page={}, size={}",
                userId, commentType, currentPage, pageSize);

        // 参数验证
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        // 设置默认值
        currentPage = currentPage == null ? 1 : currentPage;
        pageSize = pageSize == null ? 10 : pageSize;

        // 创建分页对象
        Page<Comment> page = new Page<>(currentPage, pageSize);

        // 查询用户评论
        IPage<Comment> result = commentMapper.selectUserCommentsPage(page, userId, commentType,
                "NORMAL", "create_time", "DESC", false);

        log.info("获取用户评论成功: 总数={}", result.getTotal());
        return result;
    }

    @Override
    public IPage<Comment> getUserReplies(Long userId, Integer currentPage, Integer pageSize) {
        log.info("获取用户回复: userId={}, page={}, size={}", userId, currentPage, pageSize);

        // 参数验证
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        // 设置默认值
        currentPage = currentPage == null ? 1 : currentPage;
        pageSize = pageSize == null ? 10 : pageSize;

        // 创建分页对象
        Page<Comment> page = new Page<>(currentPage, pageSize);

        // 查询用户回复
        IPage<Comment> result = commentMapper.selectUserRepliesPage(page, userId, "NORMAL",
                "create_time", "DESC", false);

        log.info("获取用户回复成功: 总数={}", result.getTotal());
        return result;
    }

    // =================== 统计功能 ===================

    @Override
    @Transactional
    public int increaseLikeCount(Long commentId, Integer increment) {
        log.info("增加评论点赞数: commentId={}, increment={}", commentId, increment);

        // 参数验证
        if (commentId == null || increment == null) {
            throw new IllegalArgumentException("评论ID和增量不能为空");
        }

        // 更新点赞数
        int result = commentMapper.increaseLikeCount(commentId, increment);

        log.info("增加评论点赞数成功: commentId={}, result={}", commentId, result);
        return result;
    }

    @Override
    @Transactional
    public int increaseReplyCount(Long commentId, Integer increment) {
        log.info("增加回复数: commentId={}, increment={}", commentId, increment);

        // 参数验证
        if (commentId == null || increment == null) {
            throw new IllegalArgumentException("评论ID和增量不能为空");
        }

        // 更新回复数
        int result = commentMapper.increaseReplyCount(commentId, increment);

        log.info("增加回复数成功: commentId={}, result={}", commentId, result);
        return result;
    }

    @Override
    public long countTargetComments(Long targetId, String commentType) {
        log.info("统计目标评论数: targetId={}, commentType={}", targetId, commentType);

        // 参数验证
        if (targetId == null) {
            throw new IllegalArgumentException("目标ID不能为空");
        }

        // 统计评论数
        Long count = commentMapper.countTargetComments(targetId, commentType, null, "NORMAL", false);

        log.info("统计目标评论数成功: count={}", count);
        return count != null ? count : 0L;
    }

    @Override
    public long countUserComments(Long userId, String commentType) {
        log.info("统计用户评论数: userId={}, commentType={}", userId, commentType);

        // 参数验证
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        // 统计评论数
        Long count = commentMapper.countUserComments(userId, commentType, "NORMAL", false);

        log.info("统计用户评论数成功: count={}", count);
        return count != null ? count : 0L;
    }

    // =================== 高级功能 ===================

    @Override
    public IPage<Comment> searchComments(String keyword, String commentType, Long targetId,
                                         Integer currentPage, Integer pageSize) {
        log.info("搜索评论: keyword={}, commentType={}, targetId={}, page={}, size={}",
                keyword, commentType, targetId, currentPage, pageSize);

        // 参数验证
        if (!StringUtils.hasText(keyword)) {
            throw new IllegalArgumentException("搜索关键词不能为空");
        }

        // 设置默认值
        currentPage = currentPage == null ? 1 : currentPage;
        pageSize = pageSize == null ? 10 : pageSize;

        // 创建分页对象
        Page<Comment> page = new Page<>(currentPage, pageSize);

        // 搜索评论
        IPage<Comment> result = commentMapper.searchComments(page, keyword, commentType,
                targetId, "NORMAL", "create_time", "DESC");

        log.info("搜索评论成功: 总数={}", result.getTotal());
        return result;
    }

    @Override
    public IPage<Comment> getPopularComments(Long targetId, String commentType, Integer timeRange,
                                             Integer currentPage, Integer pageSize) {
        log.info("获取热门评论: targetId={}, commentType={}, timeRange={}, page={}, size={}",
                targetId, commentType, timeRange, currentPage, pageSize);

        // 设置默认值
        currentPage = currentPage == null ? 1 : currentPage;
        pageSize = pageSize == null ? 10 : pageSize;
        timeRange = timeRange == null ? 7 : timeRange;

        // 创建分页对象
        Page<Comment> page = new Page<>(currentPage, pageSize);

        // 查询热门评论
        IPage<Comment> result = commentMapper.selectPopularComments(page, targetId, commentType,
                timeRange, 1, "NORMAL");

        log.info("获取热门评论成功: 总数={}", result.getTotal());
        return result;
    }

    @Override
    public IPage<Comment> getLatestComments(Long targetId, String commentType, Integer currentPage, Integer pageSize) {
        log.info("获取最新评论: targetId={}, commentType={}, page={}, size={}",
                targetId, commentType, currentPage, pageSize);

        // 设置默认值
        currentPage = currentPage == null ? 1 : currentPage;
        pageSize = pageSize == null ? 10 : pageSize;

        // 创建分页对象
        Page<Comment> page = new Page<>(currentPage, pageSize);

        // 查询最新评论
        IPage<Comment> result = commentMapper.selectLatestComments(page, targetId, commentType, "NORMAL");

        log.info("获取最新评论成功: 总数={}", result.getTotal());
        return result;
    }

    @Override
    public IPage<Comment> getCommentsByLikeCountRange(Integer minLikeCount, Integer maxLikeCount,
                                                      String commentType, Long targetId,
                                                      Integer currentPage, Integer pageSize) {
        log.info("根据点赞数范围查询评论: minLike={}, maxLike={}, commentType={}, targetId={}, page={}, size={}",
                minLikeCount, maxLikeCount, commentType, targetId, currentPage, pageSize);

        // 设置默认值
        currentPage = currentPage == null ? 1 : currentPage;
        pageSize = pageSize == null ? 10 : pageSize;

        // 创建分页对象
        Page<Comment> page = new Page<>(currentPage, pageSize);

        // 查询评论
        IPage<Comment> result = commentMapper.selectCommentsByLikeCountRange(page, minLikeCount, maxLikeCount,
                commentType, targetId, "NORMAL", false);

        log.info("根据点赞数范围查询评论成功: 总数={}", result.getTotal());
        return result;
    }

    @Override
    public IPage<Comment> getCommentsByTimeRange(LocalDateTime startTime, LocalDateTime endTime,
                                                 String commentType, Long targetId,
                                                 Integer currentPage, Integer pageSize) {
        log.info("根据时间范围查询评论: startTime={}, endTime={}, commentType={}, targetId={}, page={}, size={}",
                startTime, endTime, commentType, targetId, currentPage, pageSize);

        // 设置默认值
        currentPage = currentPage == null ? 1 : currentPage;
        pageSize = pageSize == null ? 10 : pageSize;

        // 查询评论
        List<Comment> comments = commentMapper.selectCommentsByTimeRange(startTime, endTime, commentType, "NORMAL");

        // 手动分页
        int start = (currentPage - 1) * pageSize;
        int end = Math.min(start + pageSize, comments.size());
        List<Comment> pageComments = start >= comments.size() ? List.of() : comments.subList(start, end);

        // 构建分页结果
        IPage<Comment> result = new Page<>(currentPage, pageSize);
        result.setRecords(pageComments);
        result.setTotal(comments.size());

        log.info("根据时间范围查询评论成功: 总数={}", result.getTotal());
        return result;
    }

    // =================== 管理功能 ===================

    @Override
    public Map<String, Object> getCommentStatistics(Long targetId, String commentType, Long userId,
                                                    LocalDateTime startTime, LocalDateTime endTime) {
        log.info("获取评论统计信息: targetId={}, commentType={}, userId={}, startTime={}, endTime={}",
                targetId, commentType, userId, startTime, endTime);

        // 获取统计信息
        Map<String, Object> statistics = commentMapper.selectCommentStatistics(targetId);

        log.info("获取评论统计信息成功: {}", statistics);
        return statistics;
    }

    @Override
    public List<Map<String, Object>> getUserReplyRelations(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("查询用户回复关系: userId={}, startTime={}, endTime={}", userId, startTime, endTime);

        // 查询回复关系
        List<Map<String, Object>> relations = commentMapper.selectUserReplyRelations(userId, startTime, endTime);

        log.info("查询用户回复关系成功: 总数={}", relations.size());
        return relations;
    }

    @Override
    public List<Map<String, Object>> getCommentHotRanking(String commentType, Long targetId,
                                                          LocalDateTime startTime, LocalDateTime endTime, Integer limit) {
        log.info("查询评论热度排行: commentType={}, targetId={}, startTime={}, endTime={}, limit={}",
                commentType, targetId, startTime, endTime, limit);

        // 设置默认值
        limit = limit == null ? 10 : limit;

        // 查询热度排行
        List<Map<String, Object>> ranking = commentMapper.selectCommentHotRanking(commentType, targetId,
                startTime, endTime, limit);

        log.info("查询评论热度排行成功: 总数={}", ranking.size());
        return ranking;
    }

    @Override
    @Transactional
    public int batchUpdateCommentStatus(List<Long> commentIds, String status) {
        log.info("批量更新评论状态: commentIds={}, status={}", commentIds, status);

        // 参数验证
        if (commentIds == null || commentIds.isEmpty()) {
            throw new IllegalArgumentException("评论ID列表不能为空");
        }
        if (!StringUtils.hasText(status)) {
            throw new IllegalArgumentException("状态不能为空");
        }

        // 批量更新状态
        int result = commentMapper.batchUpdateStatus(commentIds, status);

        log.info("批量更新评论状态成功: 影响行数={}", result);
        return result;
    }

    @Override
    @Transactional
    public int batchDeleteTargetComments(Long targetId, String commentType) {
        log.info("批量删除目标评论: targetId={}, commentType={}", targetId, commentType);

        // 参数验证
        if (targetId == null) {
            throw new IllegalArgumentException("目标ID不能为空");
        }

        // 批量删除评论
        int result = commentMapper.batchDeleteTargetComments(targetId, commentType, "DELETED");

        log.info("批量删除目标评论成功: 影响行数={}", result);
        return result;
    }

    @Override
    @Transactional
    public int updateUserInfo(Long userId, String nickname, String avatar) {
        log.info("更新用户信息: userId={}, nickname={}, avatar={}", userId, nickname, avatar);

        // 参数验证
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        // 更新用户信息
        int result = commentMapper.updateUserInfo(userId, nickname, avatar);

        log.info("更新用户信息成功: 影响行数={}", result);
        return result;
    }

    @Override
    @Transactional
    public int updateReplyToUserInfo(Long replyToUserId, String nickname, String avatar) {
        log.info("更新回复目标用户信息: replyToUserId={}, nickname={}, avatar={}", replyToUserId, nickname, avatar);

        // 参数验证
        if (replyToUserId == null) {
            throw new IllegalArgumentException("回复目标用户ID不能为空");
        }

        // 更新回复目标用户信息
        int result = commentMapper.updateReplyToUserInfo(replyToUserId, nickname, avatar);

        log.info("更新回复目标用户信息成功: 影响行数={}", result);
        return result;
    }

    @Override
    @Transactional
    public int cleanDeletedComments(Integer days, Integer limit) {
        log.info("清理已删除评论: days={}, limit={}", days, limit);

        // 参数验证
        if (days == null || days <= 0) {
            days = 30; // 默认清理30天前的数据
        }
        if (limit == null || limit <= 0) {
            limit = 1000; // 默认限制1000条
        }

        // 清理已删除评论
        int result = commentMapper.cleanDeletedComments(days, limit);

        log.info("清理已删除评论成功: 删除数量={}", result);
        return result;
    }

    // =================== Controller专用方法 ===================

    @Override
    public Result<PageResponse<CommentResponse>> listCommentsForController(
            String commentType, Long targetId, Long userId, Long parentId, String status, String keyword,
            String orderBy, String orderDirection, Integer currentPage, Integer pageSize) {
        
        log.info("Controller请求 - 评论列表查询: type={}, targetId={}, userId={}, parentId={}, status={}, keyword={}, orderBy={}, orderDirection={}, page={}/{}",
                commentType, targetId, userId, parentId, status, keyword, orderBy, orderDirection, currentPage, pageSize);

        try {
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

            // 构建查询条件
            Page<Comment> page = new Page<>(currentPage, pageSize);
            
            // 调用Mapper查询
            IPage<Comment> commentPage = commentMapper.selectCommentList(page, commentType, targetId, userId, parentId, status, keyword, orderBy, orderDirection);
            
            // 转换为Response对象
            List<CommentResponse> responses = commentPage.getRecords().stream()
                    .map(this::convertToResponse)
                    .toList();
            
            // 构建分页响应
            PageResponse<CommentResponse> pageResponse = new PageResponse<>();
            pageResponse.setRecords(responses);
            pageResponse.setTotal(commentPage.getTotal());
            pageResponse.setCurrentPage(currentPage);
            pageResponse.setPageSize(pageSize);
            pageResponse.setTotalPages((int) Math.ceil((double) commentPage.getTotal() / pageSize));
            
            log.info("评论列表查询成功: 总数={}, 当前页={}, 页面大小={}", commentPage.getTotal(), currentPage, pageSize);
            return Result.success(pageResponse);
            
        } catch (Exception e) {
            log.error("评论列表查询失败", e);
            return Result.error("评论列表查询失败: " + e.getMessage());
        }
    }

    /**
     * 将Comment实体转换为CommentResponse
     */
    private CommentResponse convertToResponse(Comment comment) {
        if (comment == null) {
            return null;
        }
        
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setCommentType(comment.getCommentType());
        response.setTargetId(comment.getTargetId());
        response.setParentCommentId(comment.getParentCommentId());
        response.setContent(comment.getContent());
        response.setUserId(comment.getUserId());
        response.setUserNickname(comment.getUserNickname());
        response.setUserAvatar(comment.getUserAvatar());
        response.setReplyToUserId(comment.getReplyToUserId());
        response.setReplyToUserNickname(comment.getReplyToUserNickname());
        response.setReplyToUserAvatar(comment.getReplyToUserAvatar());
        response.setStatus(comment.getStatus());
        response.setLikeCount(comment.getLikeCount());
        response.setReplyCount(comment.getReplyCount());
        response.setCreateTime(comment.getCreateTime());
        response.setUpdateTime(comment.getUpdateTime());
        response.setChildren(new ArrayList<>());
        response.setLevel(0);
        response.setCommentPath(String.valueOf(comment.getId()));
        
        return response;
    }
}
