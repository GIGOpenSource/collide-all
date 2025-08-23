package com.gig.collide.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gig.collide.domain.Message;
import com.gig.collide.domain.User;
import com.gig.collide.mapper.MessageMapper;
import com.gig.collide.service.MessageService;
import com.gig.collide.service.UserService;
import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.Apientry.api.common.response.PageResponse;
import com.gig.collide.Apientry.api.message.response.MessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * 消息业务服务实现类 - 简洁版
 * 基于message-simple.sql的无连表设计，实现核心消息功能
 * 支持私信、留言板、消息回复等功能
 *
 * @author GIG Team
 * @version 2.0.0 (简洁版)
 * @since 2024-01-16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;
    private final UserService userService;

    // =================== 基础CRUD ===================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Message sendMessage(Message message) {
        log.info("发送消息: senderId={}, receiverId={}, messageType={}",
                message.getSenderId(), message.getReceiverId(), message.getMessageType());

        // 参数验证
        validateSendMessageParams(message);

        // 业务验证
        validateSendMessageBusiness(message);

        // 设置默认值
        setMessageDefaults(message);

        // 保存消息
        int result = messageMapper.insert(message);
        if (result <= 0) {
            throw new RuntimeException("消息发送失败");
        }

        log.info("消息发送成功: messageId={}", message.getId());
        return message;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Message replyMessage(Message message) {
        log.info("回复消息: replyToId={}, senderId={}, receiverId={}",
                message.getReplyToId(), message.getSenderId(), message.getReceiverId());

        // 参数验证
        validateReplyMessageParams(message);

        // 验证原消息存在性
        Message originalMessage = messageMapper.selectById(message.getReplyToId());
        if (originalMessage == null) {
            throw new IllegalArgumentException("原消息不存在");
        }

        // 验证回复权限
        validateReplyPermission(message, originalMessage);

        // 设置默认值
        setMessageDefaults(message);

        // 保存回复消息
        int result = messageMapper.insert(message);
        if (result <= 0) {
            throw new RuntimeException("回复消息失败");
        }

        log.info("回复消息成功: messageId={}, replyToId={}", message.getId(), message.getReplyToId());
        return message;
    }

    @Override
    public Message getMessageById(Long messageId) {
        log.debug("查询消息详情: messageId={}", messageId);

        if (messageId == null) {
            throw new IllegalArgumentException("消息ID不能为空");
        }

        Message message = messageMapper.selectById(messageId);
        if (message == null) {
            log.warn("消息不存在: messageId={}", messageId);
        }

        return message;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMessageStatus(Long messageId, String status, LocalDateTime readTime) {
        log.info("更新消息状态: messageId={}, status={}", messageId, status);

        // 参数验证
        if (messageId == null) {
            throw new IllegalArgumentException("消息ID不能为空");
        }
        if (!StringUtils.hasText(status)) {
            throw new IllegalArgumentException("消息状态不能为空");
        }
        if (!isValidStatus(status)) {
            throw new IllegalArgumentException("无效的消息状态: " + status);
        }

        // 检查消息是否存在
        Message existingMessage = messageMapper.selectById(messageId);
        if (existingMessage == null) {
            throw new IllegalArgumentException("消息不存在");
        }

        // 更新状态
        int result = messageMapper.updateMessageStatus(messageId, status, readTime);
        boolean success = result > 0;

        if (success) {
            log.info("消息状态更新成功: messageId={}, status={}", messageId, status);
        } else {
            log.warn("消息状态更新失败: messageId={}, status={}", messageId, status);
        }

        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMessage(Long messageId, Long userId) {
        log.info("删除消息: messageId={}, userId={}", messageId, userId);

        // 参数验证
        if (messageId == null || userId == null) {
            throw new IllegalArgumentException("消息ID和用户ID不能为空");
        }

        // 检查消息是否存在
        Message existingMessage = messageMapper.selectById(messageId);
        if (existingMessage == null) {
            throw new IllegalArgumentException("消息不存在");
        }

        // 验证删除权限（发送者或接收者才能删除）
        if (!userId.equals(existingMessage.getSenderId()) && !userId.equals(existingMessage.getReceiverId())) {
            throw new IllegalArgumentException("无权限删除该消息");
        }

        // 逻辑删除
        int result = messageMapper.updateMessageStatus(messageId, "deleted", LocalDateTime.now());
        boolean success = result > 0;

        if (success) {
            log.info("消息删除成功: messageId={}, userId={}", messageId, userId);
        } else {
            log.warn("消息删除失败: messageId={}, userId={}", messageId, userId);
        }

        return success;
    }

    // =================== 查询功能 ===================

    @Override
    public IPage<Message> findChatHistory(Long userId1, Long userId2, String status,
                                          Integer currentPage, Integer pageSize) {
        log.debug("查询聊天记录: userId1={}, userId2={}, status={}, page={}/{}",
                userId1, userId2, status, currentPage, pageSize);

        // 参数验证
        if (userId1 == null || userId2 == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (userId1.equals(userId2)) {
            throw new IllegalArgumentException("不能查询与自己的聊天记录");
        }

        Page<Message> page = createPage(currentPage, pageSize);
        return messageMapper.findChatHistory(page, userId1, userId2, status);
    }

    @Override
    public IPage<Message> findWithConditions(Long senderId, Long receiverId, String messageType,
                                             String status, Boolean isPinned, Long replyToId,
                                             String keyword, LocalDateTime startTime, LocalDateTime endTime,
                                             String orderBy, String orderDirection,
                                             Integer currentPage, Integer pageSize) {
        log.debug("条件查询消息: senderId={}, receiverId={}, messageType={}, page={}/{}",
                senderId, receiverId, messageType, currentPage, pageSize);

        Page<Message> page = createPage(currentPage, pageSize);
        return messageMapper.findWithConditions(page, senderId, receiverId, messageType,
                status, isPinned, replyToId, keyword, startTime, endTime, orderBy, orderDirection);
    }

    @Override
    public IPage<Message> findWallMessages(Long receiverId, String status,
                                           Integer currentPage, Integer pageSize) {
        log.debug("查询留言板消息: receiverId={}, status={}, page={}/{}",
                receiverId, status, currentPage, pageSize);

        if (receiverId == null) {
            throw new IllegalArgumentException("接收者ID不能为空");
        }

        Page<Message> page = createPage(currentPage, pageSize);
        return messageMapper.findWallMessages(page, receiverId, status);
    }

    @Override
    public IPage<Message> findReplies(Long replyToId, String status,
                                      Integer currentPage, Integer pageSize) {
        log.debug("查询消息回复: replyToId={}, status={}, page={}/{}",
                replyToId, status, currentPage, pageSize);

        if (replyToId == null) {
            throw new IllegalArgumentException("原消息ID不能为空");
        }

        Page<Message> page = createPage(currentPage, pageSize);
        return messageMapper.findReplies(page, replyToId, status);
    }

    @Override
    public IPage<Message> searchMessages(Long userId, String keyword, String status,
                                         Integer currentPage, Integer pageSize) {
        log.debug("搜索用户消息: userId={}, keyword={}, status={}, page={}/{}",
                userId, keyword, status, currentPage, pageSize);

        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (!StringUtils.hasText(keyword)) {
            throw new IllegalArgumentException("搜索关键词不能为空");
        }

        Page<Message> page = createPage(currentPage, pageSize);
        return messageMapper.searchMessages(page, userId, keyword, status);
    }

    // =================== 统计功能 ===================

    @Override
    public Long countUnreadMessages(Long receiverId) {
        log.debug("统计未读消息数: receiverId={}", receiverId);

        if (receiverId == null) {
            throw new IllegalArgumentException("接收者ID不能为空");
        }

        return messageMapper.countUnreadMessages(receiverId);
    }

    @Override
    public Long countUnreadWithUser(Long receiverId, Long senderId) {
        log.debug("统计与用户的未读消息数: receiverId={}, senderId={}", receiverId, senderId);

        if (receiverId == null || senderId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        return messageMapper.countUnreadWithUser(receiverId, senderId);
    }

    @Override
    public Long countSentMessages(Long senderId, LocalDateTime startTime, LocalDateTime endTime) {
        log.debug("统计发送消息数: senderId={}, startTime={}, endTime={}", senderId, startTime, endTime);

        if (senderId == null) {
            throw new IllegalArgumentException("发送者ID不能为空");
        }

        return messageMapper.countSentMessages(senderId, startTime, endTime);
    }

    @Override
    public Long countReceivedMessages(Long receiverId, LocalDateTime startTime, LocalDateTime endTime) {
        log.debug("统计接收消息数: receiverId={}, startTime={}, endTime={}", receiverId, startTime, endTime);

        if (receiverId == null) {
            throw new IllegalArgumentException("接收者ID不能为空");
        }

        return messageMapper.countReceivedMessages(receiverId, startTime, endTime);
    }

    // =================== 批量操作 ===================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchMarkAsRead(List<Long> messageIds, Long receiverId) {
        log.info("批量标记已读: messageIds.size={}, receiverId={}",
                messageIds != null ? messageIds.size() : 0, receiverId);

        if (messageIds == null || messageIds.isEmpty()) {
            throw new IllegalArgumentException("消息ID列表不能为空");
        }
        if (receiverId == null) {
            throw new IllegalArgumentException("接收者ID不能为空");
        }

        LocalDateTime readTime = LocalDateTime.now();
        int result = messageMapper.batchMarkAsRead(messageIds, receiverId, readTime);

        log.info("批量标记已读完成: 成功数量={}", result);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteMessages(List<Long> messageIds, Long userId) {
        log.info("批量删除消息: messageIds.size={}, userId={}",
                messageIds != null ? messageIds.size() : 0, userId);

        if (messageIds == null || messageIds.isEmpty()) {
            throw new IllegalArgumentException("消息ID列表不能为空");
        }
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        int result = messageMapper.batchDeleteMessages(messageIds, userId);

        log.info("批量删除消息完成: 成功数量={}", result);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int markSessionMessagesAsRead(Long receiverId, Long senderId) {
        log.info("标记会话消息已读: receiverId={}, senderId={}", receiverId, senderId);

        if (receiverId == null || senderId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        LocalDateTime readTime = LocalDateTime.now();
        int result = messageMapper.markSessionMessagesAsRead(receiverId, senderId, readTime);

        log.info("标记会话消息已读完成: 成功数量={}", result);
        return result;
    }

    // =================== 高级功能 ===================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePinnedStatus(Long messageId, Boolean isPinned, Long receiverId) {
        log.info("更新消息置顶状态: messageId={}, isPinned={}, receiverId={}",
                messageId, isPinned, receiverId);

        // 参数验证
        if (messageId == null || isPinned == null || receiverId == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        // 检查消息是否存在
        Message existingMessage = messageMapper.selectById(messageId);
        if (existingMessage == null) {
            throw new IllegalArgumentException("消息不存在");
        }

        // 验证权限（只有接收者可以置顶）
        if (!receiverId.equals(existingMessage.getReceiverId())) {
            throw new IllegalArgumentException("只有接收者可以设置置顶状态");
        }

        int result = messageMapper.updatePinnedStatus(messageId, isPinned, receiverId);
        boolean success = result > 0;

        if (success) {
            log.info("消息置顶状态更新成功: messageId={}, isPinned={}", messageId, isPinned);
        } else {
            log.warn("消息置顶状态更新失败: messageId={}, isPinned={}", messageId, isPinned);
        }

        return success;
    }

    @Override
    public Message getLatestMessageBetweenUsers(Long userId1, Long userId2) {
        log.debug("获取用户间最新消息: userId1={}, userId2={}", userId1, userId2);

        if (userId1 == null || userId2 == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        return messageMapper.getLatestMessageBetweenUsers(userId1, userId2);
    }

    @Override
    public List<Long> getRecentChatUsers(Long userId, Integer limit) {
        log.debug("获取最近聊天用户: userId={}, limit={}", userId, limit);

        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        int validLimit = limit != null && limit > 0 ? Math.min(limit, 100) : 20;
        return messageMapper.getRecentChatUsers(userId, validLimit);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanupExpiredMessages(LocalDateTime beforeTime) {
        log.info("清理过期删除消息: beforeTime={}", beforeTime);

        if (beforeTime == null) {
            throw new IllegalArgumentException("截止时间不能为空");
        }

        int result = messageMapper.physicalDeleteExpiredMessages(beforeTime);

        log.info("清理过期删除消息完成: 清理数量={}", result);
        return result;
    }

    // =================== 私有方法 ===================

    /**
     * 验证发送消息参数
     */
    private void validateSendMessageParams(Message message) {
        if (message == null) {
            throw new IllegalArgumentException("消息对象不能为空");
        }
        if (message.getSenderId() == null) {
            throw new IllegalArgumentException("发送者ID不能为空");
        }
        if (message.getReceiverId() == null) {
            throw new IllegalArgumentException("接收者ID不能为空");
        }
        if (!StringUtils.hasText(message.getContent())) {
            throw new IllegalArgumentException("消息内容不能为空");
        }
        if (!StringUtils.hasText(message.getMessageType())) {
            throw new IllegalArgumentException("消息类型不能为空");
        }
    }

    /**
     * 验证发送消息业务规则
     */
    private void validateSendMessageBusiness(Message message) {
        // 不能给自己发消息
        if (message.getSenderId().equals(message.getReceiverId())) {
            throw new IllegalArgumentException("不能给自己发送消息");
        }

        // 验证消息类型
        if (!isValidMessageType(message.getMessageType())) {
            throw new IllegalArgumentException("无效的消息类型: " + message.getMessageType());
        }

        // 验证消息内容长度
        if (message.getContent().length() > getMaxContentLength(message.getMessageType())) {
            throw new IllegalArgumentException("消息内容超出长度限制");
        }
    }

    /**
     * 验证回复消息参数
     */
    private void validateReplyMessageParams(Message message) {
        validateSendMessageParams(message);
        if (message.getReplyToId() == null) {
            throw new IllegalArgumentException("回复消息ID不能为空");
        }
    }

    /**
     * 验证回复权限
     */
    private void validateReplyPermission(Message replyMessage, Message originalMessage) {
        // 只能回复给原消息的参与者
        Long senderId = replyMessage.getSenderId();
        Long receiverId = replyMessage.getReceiverId();
        Long originalSenderId = originalMessage.getSenderId();
        Long originalReceiverId = originalMessage.getReceiverId();

        boolean isValidReply = (senderId.equals(originalSenderId) && receiverId.equals(originalReceiverId)) ||
                (senderId.equals(originalReceiverId) && receiverId.equals(originalSenderId));

        if (!isValidReply) {
            throw new IllegalArgumentException("回复消息的参与者必须是原消息的参与者");
        }
    }

    /**
     * 设置消息默认值
     */
    private void setMessageDefaults(Message message) {
        if (message.getStatus() == null) {
            message.setStatus("sent");
        }
        if (message.getIsPinned() == null) {
            message.setIsPinned(false);
        }
        message.setCreateTime(LocalDateTime.now());
        message.setUpdateTime(LocalDateTime.now());
    }

    /**
     * 创建分页对象
     */
    private Page<Message> createPage(Integer currentPage, Integer pageSize) {
        int page = currentPage != null && currentPage > 0 ? currentPage : 1;
        int size = pageSize != null && pageSize > 0 ? Math.min(pageSize, 100) : 20;
        return new Page<>(page, size);
    }

    /**
     * 验证消息状态
     */
    private boolean isValidStatus(String status) {
        return "sent".equals(status) || "delivered".equals(status) ||
                "read".equals(status) || "deleted".equals(status);
    }

    /**
     * 验证消息类型
     */
    private boolean isValidMessageType(String messageType) {
        return "text".equals(messageType) || "image".equals(messageType) ||
                "file".equals(messageType) || "system".equals(messageType);
    }

    /**
     * 获取消息内容最大长度
     */
    private int getMaxContentLength(String messageType) {
        switch (messageType) {
            case "text":
                return 1000;
            case "image":
            case "file":
                return 200;
            case "system":
                return 500;
            default:
                return 1000;
        }
    }

    // =================== Controller专用方法 ===================

    @Override
    public Result<PageResponse<MessageResponse>> listMessagesForController(
            Long senderId, Long receiverId, String messageType, String status, Boolean isRead, String keyword,
            String orderBy, String orderDirection, Integer currentPage, Integer pageSize) {
        
        log.info("Controller请求 - 消息列表查询: senderId={}, receiverId={}, messageType={}, status={}, isRead={}, keyword={}, orderBy={}, orderDirection={}, page={}/{}",
                senderId, receiverId, messageType, status, isRead, keyword, orderBy, orderDirection, currentPage, pageSize);

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
            Page<Message> page = new Page<>(currentPage, pageSize);
            
            // 调用Mapper查询
            IPage<Message> messagePage = messageMapper.selectMessageList(page, senderId, receiverId, messageType, status, isRead, keyword, orderBy, orderDirection);
            
            // 转换为Response对象，包含用户信息
            List<MessageResponse> responses = messagePage.getRecords().stream()
                    .map(this::convertToResponseWithUserInfo)
                    .toList();
            
            // 新增逻辑：将相同发送者和接收者的消息合并为对话数组
            List<MessageResponse> mergedResponses = mergeConversations(responses);
            
            // 构建分页响应
            PageResponse<MessageResponse> pageResponse = new PageResponse<>();
            pageResponse.setRecords(mergedResponses);
            pageResponse.setTotal(messagePage.getTotal());
            pageResponse.setCurrentPage(currentPage);
            pageResponse.setPageSize(pageSize);
            pageResponse.setTotalPages((int) Math.ceil((double) messagePage.getTotal() / pageSize));
            
            log.info("消息列表查询成功: 总数={}, 当前页={}, 页面大小={}, 合并后对话数量={}", 
                    messagePage.getTotal(), currentPage, pageSize, mergedResponses.size());
            return Result.success(pageResponse);
            
        } catch (Exception e) {
            log.error("消息列表查询失败", e);
            return Result.error("消息列表查询失败: " + e.getMessage());
        }
    }

    /**
     * 将相同发送者和接收者的消息合并为对话数组
     * A为发送者，B为接收者，将两者的对话（相同的发送者和接收者）合并为一个数组
     * 修改：只返回每个对话的最新一条消息
     * 
     * @param messages 原始消息列表
     * @return 合并后的对话数组（每个对话只保留最新一条消息）
     */
    private List<MessageResponse> mergeConversations(List<MessageResponse> messages) {
        if (messages == null || messages.isEmpty()) {
            return messages;
        }
        
        log.debug("开始合并对话，原始消息数量: {}", messages.size());
        
        // 使用Map来分组相同发送者和接收者的消息，只保留每个对话的最新一条消息
        Map<String, MessageResponse> conversationLatestMessages = new HashMap<>();
        
        for (MessageResponse message : messages) {
            // 创建对话键：确保发送者和接收者的顺序一致（较小的ID在前）
            String conversationKey = createConversationKey(message.getSenderId(), message.getReceiverId());
            
            // 检查是否已有该对话的消息，如果有则比较时间，保留最新的
            MessageResponse existingMessage = conversationLatestMessages.get(conversationKey);
            
            if (existingMessage == null) {
                // 如果该对话还没有消息，直接添加
                conversationLatestMessages.put(conversationKey, message);
            } else {
                // 比较时间，保留最新的消息
                if (isMessageNewer(message, existingMessage)) {
                    conversationLatestMessages.put(conversationKey, message);
                }
            }
        }
        
        // 将每个对话的最新消息转换为列表，并按时间倒序排序
        List<MessageResponse> latestMessages = new ArrayList<>(conversationLatestMessages.values());
        
        // 按最新消息时间倒序排序对话组
        latestMessages.sort((m1, m2) -> {
            if (m1.getCreateTime() == null && m2.getCreateTime() == null) {
                return 0;
            }
            if (m1.getCreateTime() == null) {
                return 1;
            }
            if (m2.getCreateTime() == null) {
                return -1;
            }
            return m2.getCreateTime().compareTo(m1.getCreateTime()); // 倒序，最新的对话在前
        });
        
        log.debug("对话合并完成，原始消息数量: {}，合并后对话数量: {}", messages.size(), latestMessages.size());
        return latestMessages;
    }
    
    /**
     * 比较两条消息哪个更新
     * 
     * @param message1 消息1
     * @param message2 消息2
     * @return 如果消息1比消息2新，返回true
     */
    private boolean isMessageNewer(MessageResponse message1, MessageResponse message2) {
        if (message1.getCreateTime() == null && message2.getCreateTime() == null) {
            return false; // 两个都为null，认为不更新
        }
        if (message1.getCreateTime() == null) {
            return false; // message1时间为null，认为message2更新
        }
        if (message2.getCreateTime() == null) {
            return true; // message2时间为null，认为message1更新
        }
        return message1.getCreateTime().isAfter(message2.getCreateTime());
    }
    
    /**
     * 创建对话键，确保发送者和接收者的顺序一致
     * 使用较小的用户ID在前，较大的在后，确保相同的对话有相同的键
     * 
     * @param senderId 发送者ID
     * @param receiverId 接收者ID
     * @return 对话键
     */
    private String createConversationKey(Long senderId, Long receiverId) {
        if (senderId == null || receiverId == null) {
            return "null_" + (senderId != null ? senderId : "null") + "_" + (receiverId != null ? receiverId : "null");
        }
        
        // 确保相同的对话有相同的键，不管谁是发送者谁是接收者
        if (senderId.compareTo(receiverId) <= 0) {
            return senderId + "_" + receiverId;
        } else {
            return receiverId + "_" + senderId;
        }
    }

    /**
     * 将Message实体转换为MessageResponse
     */
    private MessageResponse convertToResponse(Message message) {
        if (message == null) {
            return null;
        }
        
        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        response.setSenderId(message.getSenderId());
        response.setReceiverId(message.getReceiverId());
        response.setMessageType(message.getMessageType());
        response.setContent(message.getContent());
        response.setStatus(message.getStatus());
        response.setReadTime(message.getReadTime());
        response.setIsPinned(message.getIsPinned());
        response.setReplyToId(message.getReplyToId());
        response.setCreateTime(message.getCreateTime());
        response.setUpdateTime(message.getUpdateTime());
        response.setExtraData(message.getExtraData());
        
        return response;
    }

    @Override
    public List<Message> queryMessages(Message queryCondition, LocalDateTime startTime, LocalDateTime endTime, 
                                     Integer currentPage, Integer pageSize) {
        log.info("查询t_message表数据: queryCondition={}, startTime={}, endTime={}, page={}/{}", 
                queryCondition, startTime, endTime, currentPage, pageSize);
        
        try {
            // 参数验证和默认值设置
            if (currentPage == null || currentPage < 1) {
                currentPage = 1;
            }
            if (pageSize == null || pageSize < 1 || pageSize > 100) {
                pageSize = 20;
            }
            
            // 构建查询条件
            QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
            
            if (queryCondition != null) {
                if (queryCondition.getId() != null) {
                    queryWrapper.eq("id", queryCondition.getId());
                }
                if (queryCondition.getSenderId() != null) {
                    queryWrapper.eq("sender_id", queryCondition.getSenderId());
                }
                if (queryCondition.getReceiverId() != null) {
                    queryWrapper.eq("receiver_id", queryCondition.getReceiverId());
                }
                if (queryCondition.getMessageType() != null && !queryCondition.getMessageType().trim().isEmpty()) {
                    queryWrapper.eq("message_type", queryCondition.getMessageType());
                }
                if (queryCondition.getStatus() != null && !queryCondition.getStatus().trim().isEmpty()) {
                    queryWrapper.eq("status", queryCondition.getStatus());
                }
            }
            
            // 添加时间范围条件
            if (startTime != null) {
                queryWrapper.ge("create_time", startTime);
            }
            if (endTime != null) {
                queryWrapper.le("create_time", endTime);
            }
            
            // 排序
            queryWrapper.orderByDesc("create_time");
            
            // 分页查询
            Page<Message> page = new Page<>(currentPage, pageSize);
            IPage<Message> messagePage = messageMapper.selectPage(page, queryWrapper);
            
            log.info("查询t_message表数据成功: 总数={}, 当前页={}, 页面大小={}", 
                    messagePage.getTotal(), currentPage, pageSize);
            
            return messagePage.getRecords();
            
        } catch (Exception e) {
            log.error("查询t_message表数据失败", e);
            throw new RuntimeException("查询t_message表数据失败: " + e.getMessage());
        }
    }

    @Override
    public List<MessageResponse> queryMessagesWithUserInfo(Message queryCondition, LocalDateTime startTime, LocalDateTime endTime,
                                                          Integer currentPage, Integer pageSize) {
        log.info("查询消息并包含用户信息: queryCondition={}, startTime={}, endTime={}, page={}/{}", 
                queryCondition, startTime, endTime, currentPage, pageSize);
        
        try {
            // 先查询消息列表
            List<Message> messages = queryMessages(queryCondition, startTime, endTime, currentPage, pageSize);
            
            // 转换为MessageResponse并填充用户信息
            List<MessageResponse> responses = messages.stream()
                    .map(this::convertToResponseWithUserInfo)
                    .toList();
            
            log.info("查询消息并包含用户信息成功: 消息数量={}", responses.size());
            return responses;
            
        } catch (Exception e) {
            log.error("查询消息并包含用户信息失败", e);
            throw new RuntimeException("查询消息并包含用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 将Message实体转换为MessageResponse并填充用户信息
     */
    private MessageResponse convertToResponseWithUserInfo(Message message) {
        if (message == null) {
            return null;
        }
        
        MessageResponse response = convertToResponse(message);
        
        // 填充发送者信息
        if (message.getSenderId() != null) {
            try {
                User sender = userService.getUserById(message.getSenderId());
                if (sender != null) {
                    response.setSenderNickname(sender.getNickname());
                    response.setSenderAvatar(sender.getAvatar());
                }
            } catch (Exception e) {
                log.warn("获取发送者信息失败: senderId={}, error={}", message.getSenderId(), e.getMessage());
            }
        }
        
        // 填充接收者信息
        if (message.getReceiverId() != null) {
            try {
                User receiver = userService.getUserById(message.getReceiverId());
                if (receiver != null) {
                    response.setReceiverNickname(receiver.getNickname());
                    response.setReceiverAvatar(receiver.getAvatar());
                }
            } catch (Exception e) {
                log.warn("获取接收者信息失败: receiverId={}, error={}", message.getReceiverId(), e.getMessage());
            }
        }
        
        return response;
    }

    @Override
    public List<MessageResponse> queryChatHistoryWithUserInfo(Long userId1, Long userId2, String messageType, String status,
                                                             LocalDateTime startTime, LocalDateTime endTime,
                                                             Integer currentPage, Integer pageSize) {
        log.info("查询两个用户之间的聊天记录并包含用户信息: userId1={}, userId2={}, type={}, status={}, page={}/{}", 
                userId1, userId2, messageType, status, currentPage, pageSize);
        
        try {
            // 构建查询条件：查询两个用户之间的所有消息（双向查询）
            QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
            
            // 查询条件：(senderId = userId1 AND receiverId = userId2) OR (senderId = userId2 AND receiverId = userId1)
            queryWrapper.and(wrapper -> wrapper
                    .and(innerWrapper -> innerWrapper
                            .eq("sender_id", userId1)
                            .eq("receiver_id", userId2))
                    .or(innerWrapper -> innerWrapper
                            .eq("sender_id", userId2)
                            .eq("receiver_id", userId1)));
            
            // 添加消息类型过滤
            if (messageType != null && !messageType.trim().isEmpty()) {
                queryWrapper.eq("message_type", messageType);
            }
            
            // 添加状态过滤
            if (status != null && !status.trim().isEmpty()) {
                queryWrapper.eq("status", status);
            }
            
            // 添加时间范围条件
            if (startTime != null) {
                queryWrapper.ge("create_time", startTime);
            }
            if (endTime != null) {
                queryWrapper.le("create_time", endTime);
            }
            
            // 排除已删除的消息
            queryWrapper.ne("status", "deleted");
            
            // 按创建时间排序（正序，最新的在最后）
            queryWrapper.orderByAsc("create_time");
            
            // 分页查询
            Page<Message> page = new Page<>(currentPage, pageSize);
            IPage<Message> messagePage = messageMapper.selectPage(page, queryWrapper);
            
            // 转换为MessageResponse并填充用户信息
            List<MessageResponse> responses = messagePage.getRecords().stream()
                    .map(this::convertToResponseWithUserInfo)
                    .toList();
            
            log.info("查询聊天记录成功: 总数={}, 当前页={}, 页面大小={}, 实际返回数量={}", 
                    messagePage.getTotal(), currentPage, pageSize, responses.size());
            
            return responses;
            
        } catch (Exception e) {
            log.error("查询聊天记录失败: userId1={}, userId2={}", userId1, userId2, e);
            throw new RuntimeException("查询聊天记录失败: " + e.getMessage());
        }
    }
}
