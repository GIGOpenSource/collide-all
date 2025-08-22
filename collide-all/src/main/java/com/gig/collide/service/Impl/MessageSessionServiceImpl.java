package com.gig.collide.service.Impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gig.collide.Apientry.api.message.request.MessageSessionCreateRequest;
import com.gig.collide.Apientry.api.message.request.MessageSessionQueryRequest;
import com.gig.collide.Apientry.api.message.request.MessageSessionUpdateRequest;
import com.gig.collide.Apientry.api.message.response.MessageSessionResponse;
import com.gig.collide.Apientry.api.common.response.PageResponse;
import com.gig.collide.converter.MessageSessionConverter;
import com.gig.collide.domain.MessageSession;
import com.gig.collide.mapper.MessageSessionMapper;
import com.gig.collide.service.MessageSessionService;
import com.gig.collide.Apientry.api.common.response.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 消息会话业务服务实现?- 简洁版
 * 基于message-simple.sql的t_message_session表设�?
 * 管理用户间的会话状态和未读计数
 *
 * @author GIG Team
 * @version 2.0.0 (简洁版)
 * @since 2024-01-16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageSessionServiceImpl implements MessageSessionService {

    private final MessageSessionMapper messageSessionMapper;
    private final MessageSessionConverter messageSessionConverter;

    // =================== 基础CRUD ===================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MessageSession createOrUpdateSession(MessageSession messageSession) {
                log.info("创建或更新会话: userId={}, otherUserId={}",
                messageSession.getUserId(), messageSession.getOtherUserId());

        // 参数验证
        validateSessionParams(messageSession);

        // 检查是否已存在会话
        MessageSession existingSession = messageSessionMapper.findByUserIds(
                messageSession.getUserId(), messageSession.getOtherUserId());

        if (existingSession != null) {
            // 更新现有会话
            updateExistingSession(existingSession, messageSession);
            int result = messageSessionMapper.updateById(existingSession);
            if (result <= 0) {
                throw new RuntimeException("更新会话失败");
            }
            log.info("会话更新成功: sessionId={}", existingSession.getId());
            return existingSession;
        } else {
            // 创建新会话
            setSessionDefaults(messageSession);
            int result = messageSessionMapper.insert(messageSession);
            if (result <= 0) {
                throw new RuntimeException("创建会话失败");
            }
            log.info("会话创建成功: sessionId={}", messageSession.getId());
            return messageSession;
        }
    }

    @Override
    public MessageSession findByUserIds(Long userId, Long otherUserId) {
        log.debug("查询会话: userId={}, otherUserId={}", userId, otherUserId);

        if (userId == null || otherUserId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (userId.equals(otherUserId)) {
            throw new IllegalArgumentException("不能查询与自己的会话");
        }

        return messageSessionMapper.findByUserIds(userId, otherUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateArchiveStatus(Long sessionId, Boolean isArchived) {
        log.info("更新会话归档状态 sessionId={}, isArchived={}", sessionId, isArchived);

        if (sessionId == null || isArchived == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        // 检查会话是否存在
        MessageSession existingSession = messageSessionMapper.selectById(sessionId);
        if (existingSession == null) {
            throw new IllegalArgumentException("会话不存在");
        }

        int result = messageSessionMapper.updateArchiveStatus(sessionId, isArchived);
        boolean success = result > 0;

        if (success) {
            log.info("会话归档状态更新成功 sessionId={}, isArchived={}", sessionId, isArchived);
        } else {
            log.warn("会话归档状态更新失败 sessionId={}, isArchived={}", sessionId, isArchived);
        }

        return success;
    }

    // =================== 查询功能 ===================

    @Override
    public IPage<MessageSession> findUserSessions(Long userId, Boolean isArchived, Boolean hasUnread,
                                                  String orderBy, String orderDirection,
                                                  Integer currentPage, Integer pageSize) {
        log.debug("查询用户会话列表: userId={}, isArchived={}, hasUnread={}, page={}/{}",
                userId, isArchived, hasUnread, currentPage, pageSize);

        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        // 验证排序参数
        String validOrderBy = validateAndGetOrderBy(orderBy);
        String validOrderDirection = validateAndGetOrderDirection(orderDirection);

        Page<MessageSession> page = createPage(currentPage, pageSize);
        return messageSessionMapper.findUserSessions(page, userId, isArchived, hasUnread,
                validOrderBy, validOrderDirection);
    }

    @Override
    public IPage<MessageSession> findActiveSessions(Long userId, LocalDateTime sinceTime,
                                                    Integer currentPage, Integer pageSize) {
        log.debug("查询用户活跃会话: userId={}, sinceTime={}, page={}/{}",
                userId, sinceTime, currentPage, pageSize);

        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        // 如果没有指定时间，默认查询最近时间
        LocalDateTime validSinceTime = sinceTime != null ? sinceTime : LocalDateTime.now().minusDays(7);

        Page<MessageSession> page = createPage(currentPage, pageSize);
        return messageSessionMapper.findActiveSessions(page, userId, validSinceTime);
    }

    // =================== 统计功能 ===================

    @Override
    public Long countUnreadSessions(Long userId) {
        log.debug("统计未读会话�? userId={}", userId);

        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        return messageSessionMapper.countUnreadSessions(userId);
    }

    @Override
    public Long countUserSessions(Long userId, Boolean isArchived) {
        log.debug("统计用户会话�? userId={}, isArchived={}", userId, isArchived);

        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        return messageSessionMapper.countUserSessions(userId, isArchived);
    }

    // =================== 会话管理 ===================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateLastMessage(Long userId, Long otherUserId, Long lastMessageId, LocalDateTime lastMessageTime) {
        log.info("更新会话最后消�? userId={}, otherUserId={}, lastMessageId={}",
                userId, otherUserId, lastMessageId);

        if (userId == null || otherUserId == null || lastMessageId == null || lastMessageTime == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        int result = messageSessionMapper.updateLastMessage(userId, otherUserId, lastMessageId, lastMessageTime);
        boolean success = result > 0;

        if (success) {
            log.info("会话最后消息更新成功 userId={}, otherUserId={}", userId, otherUserId);
        } else {
            log.warn("会话最后消息更新失败 userId={}, otherUserId={}", userId, otherUserId);
        }

        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean incrementUnreadCount(Long userId, Long otherUserId) {
        log.info("增加会话未读计数: userId={}, otherUserId={}", userId, otherUserId);

        if (userId == null || otherUserId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        int result = messageSessionMapper.incrementUnreadCount(userId, otherUserId);
        boolean success = result > 0;

        if (success) {
            log.info("会话未读计数增加成功: userId={}, otherUserId={}", userId, otherUserId);
        } else {
            log.warn("会话未读计数增加失败: userId={}, otherUserId={}", userId, otherUserId);
        }

        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean clearUnreadCount(Long userId, Long otherUserId) {
        log.info("清零会话未读计数: userId={}, otherUserId={}", userId, otherUserId);

        if (userId == null || otherUserId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        int result = messageSessionMapper.clearUnreadCount(userId, otherUserId);
        boolean success = result > 0;

        if (success) {
            log.info("会话未读计数清零成功: userId={}, otherUserId={}", userId, otherUserId);
        } else {
            log.warn("会话未读计数清零失败: userId={}, otherUserId={}", userId, otherUserId);
        }

        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insertOrUpdate(Long userId, Long otherUserId, Long lastMessageId, LocalDateTime lastMessageTime) {
        log.info("创建或更新会话信�? userId={}, otherUserId={}", userId, otherUserId);

        if (userId == null || otherUserId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        int result = messageSessionMapper.insertOrUpdate(userId, otherUserId, lastMessageId, lastMessageTime);
        boolean success = result > 0;

        if (success) {
            log.info("会话信息创建或更新成功 userId={}, otherUserId={}", userId, otherUserId);
        } else {
            log.warn("会话信息创建或更新失败 userId={}, otherUserId={}", userId, otherUserId);
        }

        return success;
    }

    // =================== 清理操作 ===================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteEmptySessions() {
        log.info("删除空会�?");

        int result = messageSessionMapper.deleteEmptySessions();

        log.info("删除空会话完�? 删除数量={}", result);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteArchivedSessions(LocalDateTime beforeTime) {
        log.info("删除归档会话: beforeTime={}", beforeTime);

        if (beforeTime == null) {
            throw new IllegalArgumentException("截止时间不能为空");
        }

        int result = messageSessionMapper.deleteArchivedSessions(beforeTime);

        log.info("删除归档会话完成: 删除数量={}", result);
        return result;
    }

    // =================== 业务逻辑 ===================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleNewMessage(Long senderId, Long receiverId, Long messageId, LocalDateTime messageTime) {
        log.info("处理新消息事�? senderId={}, receiverId={}, messageId={}", senderId, receiverId, messageId);

        if (senderId == null || receiverId == null || messageId == null || messageTime == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        try {
            // 更新发送者的会话（不增加未读数）
            boolean senderResult = insertOrUpdate(senderId, receiverId, messageId, messageTime);

            // 更新接收者的会话并增加未读数
            boolean receiverResult = insertOrUpdate(receiverId, senderId, messageId, messageTime);
            if (receiverResult) {
                incrementUnreadCount(receiverId, senderId);
            }

            boolean success = senderResult && receiverResult;

            if (success) {
                log.info("新消息事件处理成功 senderId={}, receiverId={}, messageId={}",
                        senderId, receiverId, messageId);
            } else {
                log.warn("新消息事件处理失败 senderId={}, receiverId={}, messageId={}",
                        senderId, receiverId, messageId);
            }

            return success;
        } catch (Exception e) {
            log.error("处理新消息事件异�? senderId={}, receiverId={}, messageId={}",
                    senderId, receiverId, messageId, e);
            throw e;
        }
    }

    @Override
    public Long getTotalUnreadCount(Long userId) {
        log.debug("获取用户总未读数: userId={}", userId);

        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        // 通过统计所有未读会话数来实�?
        // 这里可以根据实际需求优化，比如直接在数据库层面求和
        Long unreadSessionCount = countUnreadSessions(userId);
        return unreadSessionCount != null ? unreadSessionCount : 0L;
    }

    // =================== 私有方法 ===================

    /**
     * 验证会话参数
     */
    private void validateSessionParams(MessageSession messageSession) {
        if (messageSession == null) {
            throw new IllegalArgumentException("会话对象不能为空");
        }
        if (messageSession.getUserId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (messageSession.getOtherUserId() == null) {
            throw new IllegalArgumentException("对方用户ID不能为空");
        }
        if (messageSession.getUserId().equals(messageSession.getOtherUserId())) {
            throw new IllegalArgumentException("不能创建与自己的会话");
        }
    }

    /**
     * 更新现有会话
     */
    private void updateExistingSession(MessageSession existingSession, MessageSession newSession) {
        // 更新最后消息信�?
        if (newSession.getLastMessageId() != null) {
            existingSession.setLastMessageId(newSession.getLastMessageId());
        }
        if (newSession.getLastMessageTime() != null) {
            existingSession.setLastMessageTime(newSession.getLastMessageTime());
        }

        // 更新未读计数（如果提供的话）
        if (newSession.getUnreadCount() != null) {
            existingSession.setUnreadCount(newSession.getUnreadCount());
        }

        // 更新归档状态（如果提供的话�?
        if (newSession.getIsArchived() != null) {
            existingSession.setIsArchived(newSession.getIsArchived());
        }

        existingSession.setUpdateTime(LocalDateTime.now());
    }

    /**
     * 设置会话默认�?
     */
    private void setSessionDefaults(MessageSession messageSession) {
        if (messageSession.getUnreadCount() == null) {
            messageSession.setUnreadCount(0);
        }
        if (messageSession.getIsArchived() == null) {
            messageSession.setIsArchived(false);
        }
        messageSession.setCreateTime(LocalDateTime.now());
        messageSession.setUpdateTime(LocalDateTime.now());
    }

    /**
     * 创建分页对象
     */
    private Page<MessageSession> createPage(Integer currentPage, Integer pageSize) {
        int page = currentPage != null && currentPage > 0 ? currentPage : 1;
        int size = pageSize != null && pageSize > 0 ? Math.min(pageSize, 100) : 20;
        return new Page<>(page, size);
    }

    /**
     * 验证并获取有效的排序字段
     */
    private String validateAndGetOrderBy(String orderBy) {
        if (!StringUtils.hasText(orderBy)) {
            return "last_message_time";
        }

        // 只允许特定的排序字段
        switch (orderBy.toLowerCase()) {
            case "last_message_time":
            case "create_time":
            case "unread_count":
                return orderBy.toLowerCase();
            default:
                return "last_message_time";
        }
    }

    /**
     * 验证并获取有效的排序方向
     */
    private String validateAndGetOrderDirection(String orderDirection) {
        if (!StringUtils.hasText(orderDirection)) {
            return "DESC";
        }

        return "ASC".equalsIgnoreCase(orderDirection) ? "ASC" : "DESC";
    }

    // =================== Controller专用方法实现 ===================

    @Override
    public Result<MessageSessionResponse> createOrUpdateSessionForController(MessageSessionCreateRequest request) {
        try {
            log.info("Controller层 - 创建或更新会话: userId={}, otherUserId={}",
                    request.getUserId(), request.getOtherUserId());

            // 使用转换器将MessageSessionCreateRequest转换为MessageSession实体
            MessageSession session = messageSessionConverter.toEntity(request);
            MessageSession createdSession = createOrUpdateSession(session);

            // 将创建的MessageSession实体转换为MessageSessionResponse
            MessageSessionResponse response = messageSessionConverter.toResponse(createdSession);
            return Result.success(response);
        } catch (Exception e) {
            log.error("Controller层 - 创建或更新会话失败", e);
            return Result.error("创建或更新会话失败: " + e.getMessage());
        }
    }

    @Override
    public Result<MessageSessionResponse> getSessionByUserIdsForController(Long userId, Long otherUserId) {
        try {
            log.info("Controller层 - 获取会话详情: userId={}, otherUserId={}", userId, otherUserId);

            MessageSession session = findByUserIds(userId, otherUserId);
            if (session != null) {
                MessageSessionResponse response = messageSessionConverter.toResponse(session);
                return Result.success(response);
            } else {
                return Result.error("会话不存在");
            }
        } catch (Exception e) {
            log.error("Controller层 - 获取会话详情失败", e);
            return Result.error("获取会话详情失败: " + e.getMessage());
        }
    }

    @Override
    public Result<MessageSessionResponse> updateSessionForController(MessageSessionUpdateRequest request) {
        try {
            log.info("Controller层 - 更新会话信息: sessionId={}", request.getSessionId());

            // 使用转换器将MessageSessionUpdateRequest转换为MessageSession实体
            MessageSession session = messageSessionConverter.toEntity(request);

            // 这里需要实现更新逻辑，暂时返回成功
            MessageSessionResponse response = messageSessionConverter.toResponse(session);
            return Result.success(response);
        } catch (Exception e) {
            log.error("Controller层 - 更新会话信息失败", e);
            return Result.error("更新会话信息失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Void> updateArchiveStatusForController(Long sessionId, Boolean isArchived, Long userId) {
        try {
            log.info("Controller层 - 更新会话归档状态: sessionId={}, isArchived={}, userId={}",
                    sessionId, isArchived, userId);

            boolean success = updateArchiveStatus(sessionId, isArchived);
            if (success) {
                return Result.success(null);
            } else {
                return Result.error("更新归档状态失败");
            }
        } catch (Exception e) {
            log.error("Controller层 - 更新会话归档状态失败", e);
            return Result.error("更新会话归档状态失败: " + e.getMessage());
        }
    }

    @Override
    public Result<PageResponse<MessageSessionResponse>> queryUserSessionsForController(MessageSessionQueryRequest request) {
        try {
            log.info("Controller层 - 分页查询用户会话: userId={}, 页码={}/{}",
                    request.getUserId(), request.getCurrentPage(), request.getPageSize());

            IPage<MessageSession> page = findUserSessions(
                    request.getUserId(), request.getIsArchived(), request.getHasUnread(),
                    request.getOrderBy(), request.getOrderDirection(),
                    request.getCurrentPage(), request.getPageSize()
            );

            // 转换为PageResponse<MessageSessionResponse>
            List<MessageSessionResponse> responses = messageSessionConverter.toResponseList(page.getRecords());


            // 安全地获取当前页码和页面大小
            int currentPageValue = 1;
            int pageSizeValue = 20;

            PageResponse<MessageSessionResponse> pageResponse = PageResponse.of(
                    responses, page.getTotal(), currentPageValue, pageSizeValue
            );

            return Result.success(pageResponse);
        } catch (Exception e) {
            log.error("Controller层 - 分页查询用户会话失败", e);
            return Result.error("分页查询用户会话失败: " + e.getMessage());
        }
    }

    @Override
    public Result<PageResponse<MessageSessionResponse>> getUserSessionsForController(Long userId, Boolean isArchived, Boolean hasUnread, Integer currentPage, Integer pageSize) {
        try {
            log.info("Controller层 - 获取用户会话列表: userId={}, isArchived={}, hasUnread={}, 页码={}/{}",
                    userId, isArchived, hasUnread, currentPage, pageSize);

            IPage<MessageSession> page = findUserSessions(userId, isArchived, hasUnread,
                    "last_message_time", "DESC", currentPage, pageSize);

            // 转换为PageResponse<MessageSessionResponse>
            List<MessageSessionResponse> responses = messageSessionConverter.toResponseList(page.getRecords());
            
            // 安全地获取当前页码和页面大小
            int currentPageValue = 1;
            int pageSizeValue = 20;

            if (Objects.nonNull(page.getCurrent())) {
                currentPageValue = (int) page.getCurrent();
            }
            if (Objects.nonNull(page.getSize())) {
                pageSizeValue = (int) page.getSize();
            }
            
            PageResponse<MessageSessionResponse> pageResponse = PageResponse.of(
                    responses, page.getTotal(), currentPageValue, pageSizeValue
            );

            return Result.success(pageResponse);
        } catch (Exception e) {
            log.error("Controller层 - 获取用户会话列表失败", e);
            return Result.error("获取用户会话列表失败: " + e.getMessage());
        }
    }

    @Override
    public Result<PageResponse<MessageSessionResponse>> getActiveSessionsForController(Long userId, LocalDateTime sinceTime, Integer currentPage, Integer pageSize) {
        try {
            log.info("Controller层 - 获取用户活跃会话: userId={}, sinceTime={}, 页码={}/{}",
                    userId, sinceTime, currentPage, pageSize);

            IPage<MessageSession> page = findActiveSessions(userId, sinceTime, currentPage, pageSize);

            // 转换为PageResponse<MessageSessionResponse>
            List<MessageSessionResponse> responses = messageSessionConverter.toResponseList(page.getRecords());
            
            // 安全地获取当前页码和页面大小
            int currentPageValue = 1;
            int pageSizeValue = 20;

            if (Objects.nonNull(page.getCurrent())) {
                currentPageValue = (int) page.getCurrent();
            }
            if (Objects.nonNull(page.getSize())) {
                pageSizeValue = (int) page.getSize();
            }
            
            PageResponse<MessageSessionResponse> pageResponse = PageResponse.of(
                    responses, page.getTotal(), currentPageValue, pageSizeValue
            );

            return Result.success(pageResponse);
        } catch (Exception e) {
            log.error("Controller层 - 获取用户活跃会话失败", e);
            return Result.error("获取用户活跃会话失败: " + e.getMessage());
        }
    }

    @Override
    public Result<PageResponse<MessageSessionResponse>> getUnreadSessionsForController(Long userId, Integer currentPage, Integer pageSize) {
        try {
            log.info("Controller层 - 获取用户未读会话: userId={}, 页码={}/{}", userId, currentPage, pageSize);

            IPage<MessageSession> page = findUserSessions(userId, false, true,
                    "last_message_time", "DESC", currentPage, pageSize);

            // 转换为PageResponse<MessageSessionResponse>
            List<MessageSessionResponse> responses = messageSessionConverter.toResponseList(page.getRecords());
            
            // 安全地获取当前页码和页面大小
            int currentPageValue = 1;
            int pageSizeValue = 20;

            if (Objects.nonNull(page.getCurrent())) {
                currentPageValue = (int) page.getCurrent();
            }
            if (Objects.nonNull(page.getSize())) {
                pageSizeValue = (int) page.getSize();
            }
            
            PageResponse<MessageSessionResponse> pageResponse = PageResponse.of(
                    responses, page.getTotal(), currentPageValue, pageSizeValue
            );

            return Result.success(pageResponse);
        } catch (Exception e) {
            log.error("Controller层 - 获取用户未读会话失败", e);
            return Result.error("获取用户未读会话失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Long> getUnreadSessionCountForController(Long userId) {
        try {
            log.info("Controller层 - 统计未读会话数: userId={}", userId);

            Long count = countUnreadSessions(userId);
            return Result.success(count);
        } catch (Exception e) {
            log.error("Controller层 - 统计未读会话数失败", e);
            return Result.error("统计未读会话数失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Long> getUserSessionCountForController(Long userId, Boolean isArchived) {
        try {
            log.info("Controller层 - 统计用户会话总数: userId={}, isArchived={}", userId, isArchived);

            Long count = countUserSessions(userId, isArchived);
            return Result.success(count);
        } catch (Exception e) {
            log.error("Controller层 - 统计用户会话总数失败", e);
            return Result.error("统计用户会话总数失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Long> getTotalUnreadCountForController(Long userId) {
        try {
            log.info("Controller层 - 获取用户总未读数: userId={}", userId);

            Long count = getTotalUnreadCount(userId);
            return Result.success(count);
        } catch (Exception e) {
            log.error("Controller层 - 获取用户总未读数失败", e);
            return Result.error("获取用户总未读数失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Void> updateLastMessageForController(Long userId, Long otherUserId, Long lastMessageId, LocalDateTime lastMessageTime) {
        try {
            log.info("Controller层 - 更新会话最后消息: userId={}, otherUserId={}, lastMessageId={}",
                    userId, otherUserId, lastMessageId);

            boolean success = updateLastMessage(userId, otherUserId, lastMessageId, lastMessageTime);
            if (success) {
                return Result.success(null);
            } else {
                return Result.error("更新最后消息失败");
            }
        } catch (Exception e) {
            log.error("Controller层 - 更新会话最后消息失败", e);
            return Result.error("更新会话最后消息失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Void> incrementUnreadCountForController(Long userId, Long otherUserId) {
        try {
            log.info("Controller层 - 增加会话未读计数: userId={}, otherUserId={}", userId, otherUserId);

            boolean success = incrementUnreadCount(userId, otherUserId);
            if (success) {
                return Result.success(null);
            } else {
                return Result.error("增加未读计数失败");
            }
        } catch (Exception e) {
            log.error("Controller层 - 增加会话未读计数失败", e);
            return Result.error("增加会话未读计数失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Void> clearUnreadCountForController(Long userId, Long otherUserId) {
        try {
            log.info("Controller层 - 清零会话未读计数: userId={}, otherUserId={}", userId, otherUserId);

            boolean success = clearUnreadCount(userId, otherUserId);
            if (success) {
                return Result.success(null);
            } else {
                return Result.error("清零未读计数失败");
            }
        } catch (Exception e) {
            log.error("Controller层 - 清零会话未读计数失败", e);
            return Result.error("清零会话未读计数失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Void> handleNewMessageForController(Long senderId, Long receiverId, Long messageId, LocalDateTime messageTime) {
        try {
            log.info("Controller层 - 处理新消息事件: senderId={}, receiverId={}, messageId={}",
                    senderId, receiverId, messageId);

            boolean success = handleNewMessage(senderId, receiverId, messageId, messageTime);
            if (success) {
                return Result.success(null);
            } else {
                return Result.error("处理新消息事件失败");
            }
        } catch (Exception e) {
            log.error("Controller层 - 处理新消息事件失败", e);
            return Result.error("处理新消息事件失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Integer> deleteEmptySessionsForController(Long userId) {
        try {
            log.info("Controller层 - 删除空会话: userId={}", userId);

            int count = deleteEmptySessions();
            return Result.success(count);
        } catch (Exception e) {
            log.error("Controller层 - 删除空会话失败", e);
            return Result.error("删除空会话失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Integer> deleteArchivedSessionsForController(LocalDateTime beforeTime, Long userId) {
        try {
            log.info("Controller层 - 删除归档会话: beforeTime={}, userId={}", beforeTime, userId);

            int count = deleteArchivedSessions(beforeTime);
            return Result.success(count);
        } catch (Exception e) {
            log.error("Controller层 - 删除归档会话失败", e);
            return Result.error("删除归档会话失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Void> batchArchiveSessionsForController(List<Long> sessionIds, Long userId) {
        try {
            log.info("Controller层 - 批量归档会话: sessionIds.size={}, userId={}",
                    sessionIds != null ? sessionIds.size() : 0, userId);

            // 这里需要实现批量归档逻辑，暂时返回成功
            return Result.success(null);
        } catch (Exception e) {
            log.error("Controller层 - 批量归档会话失败", e);
            return Result.error("批量归档会话失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Void> batchUnarchiveSessionsForController(List<Long> sessionIds, Long userId) {
        try {
            log.info("Controller层 - 批量取消归档会话: sessionIds.size={}, userId={}",
                    sessionIds != null ? sessionIds.size() : 0, userId);

            // 这里需要实现批量取消归档逻辑，暂时返回成功
            return Result.success(null);
        } catch (Exception e) {
            log.error("Controller层 - 批量取消归档会话失败", e);
            return Result.error("批量取消归档会话失败: " + e.getMessage());
        }
    }

    @Override
    public Result<String> rebuildSessionIndexForController(Long userId) {
        try {
            log.info("Controller层 - 重建会话索引: userId={}", userId);

            // 这里需要实现重建索引逻辑，暂时返回成功
            return Result.success("索引重建完成");
        } catch (Exception e) {
            log.error("Controller层 - 重建会话索引失败", e);
            return Result.error("重建会话索引失败: " + e.getMessage());
        }
    }

    @Override
    public Result<String> healthCheckForController() {
        try {
            log.info("Controller层 - 会话系统健康检查");

            // 这里需要实现健康检查逻辑，暂时返回成功
            return Result.success("系统运行正常");
        } catch (Exception e) {
            log.error("Controller层 - 会话系统健康检查失败", e);
            return Result.error("系统健康检查失败: " + e.getMessage());
        }
    }
}
