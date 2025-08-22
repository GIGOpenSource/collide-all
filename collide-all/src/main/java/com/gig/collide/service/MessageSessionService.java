package com.gig.collide.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gig.collide.Apientry.api.message.request.MessageSessionCreateRequest;
import com.gig.collide.Apientry.api.message.request.MessageSessionQueryRequest;
import com.gig.collide.Apientry.api.message.request.MessageSessionUpdateRequest;
import com.gig.collide.Apientry.api.message.response.MessageSessionResponse;
import com.gig.collide.Apientry.api.common.response.PageResponse;
import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.domain.MessageSession;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息会话业务服务接口 - 简洁版
 * 基于message-simple.sql的t_message_session表设计
 * 管理用户间的会话状态和未读计数
 *
 * @author GIG Team
 * @version 2.0.0 (简洁版)
 * @since 2024-01-16
 */
public interface MessageSessionService {

    // =================== 核心业务方法 ===================

    /**
     * 创建或更新会话
     * 如果会话不存在则创建，存在则更新最后消息信息
     *
     * @param messageSession 会话实体
     * @return 会话记录
     */
    MessageSession createOrUpdateSession(MessageSession messageSession);

    /**
     * 根据用户ID和对方用户ID查询会话
     *
     * @param userId 用户ID
     * @param otherUserId 对方用户ID
     * @return 会话信息
     */
    MessageSession findByUserIds(Long userId, Long otherUserId);

    /**
     * 更新会话归档状态
     *
     * @param sessionId 会话ID
     * @param isArchived 是否归档
     * @return 是否成功
     */
    boolean updateArchiveStatus(Long sessionId, Boolean isArchived);

    // =================== 查询功能 ===================

    /**
     * 查询用户的会话列表
     * 支持按归档状态、未读状态筛选和排序
     *
     * @param userId 用户ID
     * @param isArchived 是否归档（可选）
     * @param hasUnread 是否有未读（可选）
     * @param orderBy 排序字段
     * @param orderDirection 排序方向
     * @param currentPage 当前页码
     * @param pageSize 页面大小
     * @return 会话列表分页
     */
    IPage<MessageSession> findUserSessions(Long userId, Boolean isArchived, Boolean hasUnread,
                                           String orderBy, String orderDirection,
                                           Integer currentPage, Integer pageSize);

    /**
     * 查询用户的活跃会话
     * 查询指定时间后有消息交互的会话
     *
     * @param userId 用户ID
     * @param sinceTime 起始时间
     * @param currentPage 当前页码
     * @param pageSize 页面大小
     * @return 活跃会话分页
     */
    IPage<MessageSession> findActiveSessions(Long userId, LocalDateTime sinceTime,
                                             Integer currentPage, Integer pageSize);

    // =================== 统计功能 ===================

    /**
     * 统计用户的未读会话数
     *
     * @param userId 用户ID
     * @return 未读会话数
     */
    Long countUnreadSessions(Long userId);

    /**
     * 统计用户的总会话数
     *
     * @param userId 用户ID
     * @param isArchived 是否归档（可选）
     * @return 总会话数
     */
    Long countUserSessions(Long userId, Boolean isArchived);

    // =================== 会话管理 ===================

    /**
     * 更新会话的最后消息信息
     * 在新消息到达时更新会话状态
     *
     * @param userId 用户ID
     * @param otherUserId 对方用户ID
     * @param lastMessageId 最后消息ID
     * @param lastMessageTime 最后消息时间
     * @return 是否成功
     */
    boolean updateLastMessage(Long userId, Long otherUserId, Long lastMessageId, LocalDateTime lastMessageTime);

    /**
     * 增加会话的未读计数
     *
     * @param userId 用户ID
     * @param otherUserId 对方用户ID
     * @return 是否成功
     */
    boolean incrementUnreadCount(Long userId, Long otherUserId);

    /**
     * 清零会话的未读计数
     * 用户查看消息时调用
     *
     * @param userId 用户ID
     * @param otherUserId 对方用户ID
     * @return 是否成功
     */
    boolean clearUnreadCount(Long userId, Long otherUserId);

    /**
     * 创建或更新会话信息（便捷方法）
     *
     * @param userId 用户ID
     * @param otherUserId 对方用户ID
     * @param lastMessageId 最后消息ID
     * @param lastMessageTime 最后消息时间
     * @return 是否成功
     */
    boolean insertOrUpdate(Long userId, Long otherUserId, Long lastMessageId, LocalDateTime lastMessageTime);

    // =================== 清理操作 ===================

    /**
     * 删除空会话
     * 删除没有消息记录的会话
     *
     * @return 删除的会话数量
     */
    int deleteEmptySessions();

    /**
     * 删除指定时间前的归档会话
     *
     * @param beforeTime 截止时间
     * @return 删除的会话数量
     */
    int deleteArchivedSessions(LocalDateTime beforeTime);

    // =================== 业务逻辑 ===================

    /**
     * 处理新消息事件
     * 自动创建或更新双方的会话状态
     *
     * @param senderId 发送者ID
     * @param receiverId 接收者ID
     * @param messageId 消息ID
     * @param messageTime 消息时间
     * @return 是否处理成功
     */
    boolean handleNewMessage(Long senderId, Long receiverId, Long messageId, LocalDateTime messageTime);

    /**
     * 获取用户所有会话的未读总数
     *
     * @param userId 用户ID
     * @return 未读总数
     */
    Long getTotalUnreadCount(Long userId);

    // =================== Controller专用方法 ===================

    /**
     * 创建或更新会话（Controller专用）
     *
     * @param request 会话创建请求
     * @return 会话操作结果
     */
    Result<MessageSessionResponse> createOrUpdateSessionForController(MessageSessionCreateRequest request);

    /**
     * 根据用户ID获取会话详情（Controller专用）
     *
     * @param userId 用户ID
     * @param otherUserId 对方用户ID
     * @return 会话详情
     */
    Result<MessageSessionResponse> getSessionByUserIdsForController(Long userId, Long otherUserId);

    /**
     * 更新会话信息（Controller专用）
     *
     * @param request 更新请求
     * @return 更新后的会话信息
     */
    Result<MessageSessionResponse> updateSessionForController(MessageSessionUpdateRequest request);

    /**
     * 更新会话归档状态（Controller专用）
     *
     * @param sessionId 会话ID
     * @param isArchived 是否归档
     * @param userId 操作用户ID
     * @return 操作结果
     */
    Result<Void> updateArchiveStatusForController(Long sessionId, Boolean isArchived, Long userId);

    /**
     * 分页查询用户会话（Controller专用）
     *
     * @param request 查询请求
     * @return 会话列表分页
     */
    Result<PageResponse<MessageSessionResponse>> queryUserSessionsForController(MessageSessionQueryRequest request);

    /**
     * 获取用户会话列表（Controller专用）
     *
     * @param userId 用户ID
     * @param isArchived 是否归档
     * @param hasUnread 是否有未读
     * @param currentPage 当前页码
     * @param pageSize 页面大小
     * @return 会话列表分页
     */
    Result<PageResponse<MessageSessionResponse>> getUserSessionsForController(Long userId, Boolean isArchived, Boolean hasUnread, Integer currentPage, Integer pageSize);

    /**
     * 获取用户活跃会话（Controller专用）
     *
     * @param userId 用户ID
     * @param sinceTime 起始时间
     * @param currentPage 当前页码
     * @param pageSize 页面大小
     * @return 活跃会话分页
     */
    Result<PageResponse<MessageSessionResponse>> getActiveSessionsForController(Long userId, LocalDateTime sinceTime, Integer currentPage, Integer pageSize);

    /**
     * 获取用户未读会话（Controller专用）
     *
     * @param userId 用户ID
     * @param currentPage 当前页码
     * @param pageSize 页面大小
     * @return 未读会话分页
     */
    Result<PageResponse<MessageSessionResponse>> getUnreadSessionsForController(Long userId, Integer currentPage, Integer pageSize);

    /**
     * 统计未读会话数（Controller专用）
     *
     * @param userId 用户ID
     * @return 未读会话数
     */
    Result<Long> getUnreadSessionCountForController(Long userId);

    /**
     * 统计用户会话总数（Controller专用）
     *
     * @param userId 用户ID
     * @param isArchived 是否归档
     * @return 总会话数
     */
    Result<Long> getUserSessionCountForController(Long userId, Boolean isArchived);

    /**
     * 获取用户总未读数（Controller专用）
     *
     * @param userId 用户ID
     * @return 未读总数
     */
    Result<Long> getTotalUnreadCountForController(Long userId);

    /**
     * 更新会话最后消息（Controller专用）
     *
     * @param userId 用户ID
     * @param otherUserId 对方用户ID
     * @param lastMessageId 最后消息ID
     * @param lastMessageTime 最后消息时间
     * @return 操作结果
     */
    Result<Void> updateLastMessageForController(Long userId, Long otherUserId, Long lastMessageId, LocalDateTime lastMessageTime);

    /**
     * 增加会话未读计数（Controller专用）
     *
     * @param userId 用户ID
     * @param otherUserId 对方用户ID
     * @return 操作结果
     */
    Result<Void> incrementUnreadCountForController(Long userId, Long otherUserId);

    /**
     * 清零会话未读计数（Controller专用）
     *
     * @param userId 用户ID
     * @param otherUserId 对方用户ID
     * @return 操作结果
     */
    Result<Void> clearUnreadCountForController(Long userId, Long otherUserId);

    /**
     * 处理新消息事件（Controller专用）
     *
     * @param senderId 发送者ID
     * @param receiverId 接收者ID
     * @param messageId 消息ID
     * @param messageTime 消息时间
     * @return 处理结果
     */
    Result<Void> handleNewMessageForController(Long senderId, Long receiverId, Long messageId, LocalDateTime messageTime);

    /**
     * 删除空会话（Controller专用）
     *
     * @param userId 用户ID（可选）
     * @return 删除的会话数量
     */
    Result<Integer> deleteEmptySessionsForController(Long userId);

    /**
     * 删除归档会话（Controller专用）
     *
     * @param beforeTime 截止时间
     * @param userId 用户ID（可选）
     * @return 删除的会话数量
     */
    Result<Integer> deleteArchivedSessionsForController(LocalDateTime beforeTime, Long userId);

    /**
     * 批量归档会话（Controller专用）
     *
     * @param sessionIds 会话ID列表
     * @param userId 操作用户ID
     * @return 操作结果
     */
    Result<Void> batchArchiveSessionsForController(List<Long> sessionIds, Long userId);

    /**
     * 批量取消归档会话（Controller专用）
     *
     * @param sessionIds 会话ID列表
     * @param userId 操作用户ID
     * @return 操作结果
     */
    Result<Void> batchUnarchiveSessionsForController(List<Long> sessionIds, Long userId);

    /**
     * 重建会话索引（Controller专用）
     *
     * @param userId 用户ID（可选）
     * @return 处理结果
     */
    Result<String> rebuildSessionIndexForController(Long userId);

    /**
     * 会话系统健康检查（Controller专用）
     *
     * @return 系统状态
     */
    Result<String> healthCheckForController();
}