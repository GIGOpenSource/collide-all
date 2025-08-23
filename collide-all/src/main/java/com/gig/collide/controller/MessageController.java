package com.gig.collide.controller;


import com.gig.collide.Apientry.api.common.response.PageResponse;
import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.Apientry.api.message.MessageFacadeService;
import com.gig.collide.Apientry.api.message.request.MessageCreateRequest;
import com.gig.collide.Apientry.api.message.request.MessageQueryRequest;
import com.gig.collide.Apientry.api.message.request.MessageUpdateRequest;
import com.gig.collide.Apientry.api.message.response.MessageResponse;
import com.gig.collide.domain.Message;
import com.gig.collide.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息REST控制器 - 简洁版
 * 基于message-simple.sql的无连表设计，提供完整的消息HTTP接口
 * 支持私信、留言板、消息回复等功能
 * 
 * 主要功能：
 * - 消息发送：发送私信、回复消息、留言板消息
 * - 消息查询：聊天记录、条件查询、搜索、回复列表
 * - 消息管理：更新状态、删除、置顶、标记已读
 * - 批量操作：批量已读、批量删除、会话全部已读
 * - 统计功能：未读数统计、发送/接收统计
 * - 会话管理：最近聊天用户、最新消息
 * 
 * @author GIG Team
 * @version 2.0.0 (简洁版)
 * @since 2024-01-16
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
@Tag(name = "消息管理", description = "消息相关的API接口")
@Validated
public class MessageController {

    private final MessageService messageService;

    /**
     * 消息列表查询
     * 支持多种条件查询和分页
     */
    @GetMapping("/list")
    @Operation(summary = "消息列表查询", description = "支持按发送者、接收者、类型等条件查询消息列表")
    public Result<PageResponse<MessageResponse>> listMessages(
            @Parameter(description = "发送者ID") @RequestParam(required = false) Long senderId,
            @Parameter(description = "接收者ID") @RequestParam(required = false) Long receiverId,
            @Parameter(description = "消息类型") @RequestParam(required = false) String messageType,
            @Parameter(description = "消息状态") @RequestParam(required = false) String status,
            @Parameter(description = "是否已读") @RequestParam(required = false) Boolean isRead,
            @Parameter(description = "关键词搜索") @RequestParam(required = false) String keyword,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createTime") String orderBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "DESC") String orderDirection,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("REST请求 - 消息列表查询: senderId={}, receiverId={}, type={}, page={}/{}", 
                senderId, receiverId, messageType, currentPage, pageSize);
        return messageService.listMessagesForController(senderId, receiverId, messageType, status, isRead, keyword,
                orderBy, orderDirection, currentPage, pageSize);
    }

    /**
     * 查询t_message表数据（包含用户信息）
     * 提供基础的消息查询功能，并返回发送者和接收者的姓名、头像
     * 如果发送人和同一个接收人有多条消息，将消息合并并且倒序排序返回给前端
     * 实现聊天会话功能，防止为每条消息建立单独的会话
     */
    @GetMapping("/query")
    @Operation(summary = "查询t_message表数据", description = "查询t_message表中的消息数据，包含发送者和接收者的姓名、头像。如果发送人和同一个接收人有多条消息，将消息合并并且倒序排序返回给前端，实现聊天会话功能")
    public Result<List<MessageResponse>> queryMessages(
            @Parameter(description = "消息ID") @RequestParam(required = false) Long id,
            @Parameter(description = "发送者ID") @RequestParam(required = false) Long senderId,
            @Parameter(description = "接收者ID") @RequestParam(required = false) Long receiverId,
            @Parameter(description = "消息类型") @RequestParam(required = false) String messageType,
            @Parameter(description = "消息状态") @RequestParam(required = false) String status,
            @Parameter(description = "开始时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("REST请求 - 查询t_message表数据: id={}, senderId={}, receiverId={}, type={}, page={}/{}", 
                id, senderId, receiverId, messageType, currentPage, pageSize);
        
        // 如果发送人和接收人ID都存在，查询两个用户之间的完整聊天记录
        if (senderId != null && receiverId != null) {
            log.info("检测到发送人和接收人ID都存在，查询完整聊天记录: senderId={}, receiverId={}", senderId, receiverId);
            return queryChatHistory(senderId, receiverId, messageType, status, startTime, endTime, currentPage, pageSize);
        }
        
        // 其他情况使用原有的查询逻辑
        Message queryCondition = new Message();
        queryCondition.setId(id);
        queryCondition.setSenderId(senderId);
        queryCondition.setReceiverId(receiverId);
        queryCondition.setMessageType(messageType);
        queryCondition.setStatus(status);
        
        // 调用Service层查询并转换为包含用户信息的响应
        List<MessageResponse> messages = messageService.queryMessagesWithUserInfo(queryCondition, startTime, endTime, currentPage, pageSize);
        
        return Result.success(messages);
    }

    /**
     * 写入t_message表数据
     * 提供基础的消息写入功能
     */
    @PostMapping("/write")
    @Operation(summary = "写入t_message表数据", description = "向t_message表中写入消息数据")
    public Result<Message> writeMessage(@RequestBody @Validated MessageCreateRequest request) {
        log.info("REST请求 - 写入t_message表数据: senderId={}, receiverId={}, content={}", 
                request.getSenderId(), request.getReceiverId(), request.getContent());
        
        // 设置默认值
        request.initDefaults();
        
        // 验证内容长度
        if (!request.isContentLengthValid()) {
            return Result.error("消息内容长度超出限制");
        }
        
        // 构建Message实体
        Message message = new Message();
        message.setSenderId(request.getSenderId());
        message.setReceiverId(request.getReceiverId());
        message.setContent(request.getContent());
        message.setMessageType(request.getMessageType());
        message.setExtraData(request.getExtraData());
        message.setReplyToId(request.getReplyToId());
        message.setIsPinned(request.getIsPinned());
        message.setStatus("sent"); // 默认状态为已发送
        
        // 调用Service层保存
        Message savedMessage = messageService.sendMessage(message);
        return Result.success(savedMessage);
    }
    
    /**
     * 查询两个用户之间的完整聊天记录
     * 实现聊天会话功能，防止为每条消息建立单独的会话
     * 
     * @param senderId 发送者ID
     * @param receiverId 接收者ID
     * @param messageType 消息类型（可选）
     * @param status 消息状态（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param currentPage 当前页码
     * @param pageSize 页面大小
     * @return 聊天记录列表
     */
    private Result<List<MessageResponse>> queryChatHistory(Long senderId, Long receiverId, 
                                                          String messageType, String status,
                                                          LocalDateTime startTime, LocalDateTime endTime,
                                                          Integer currentPage, Integer pageSize) {
        log.info("查询聊天记录: senderId={}, receiverId={}, type={}, status={}, page={}/{}", 
                senderId, receiverId, messageType, status, currentPage, pageSize);
        
        try {
            // 查询两个用户之间的所有消息（双向查询）
            List<MessageResponse> chatMessages = messageService.queryChatHistoryWithUserInfo(
                    senderId, receiverId, messageType, status, startTime, endTime, currentPage, pageSize);
            
            // 按创建时间倒序排序（最新的消息在最后，符合聊天界面的习惯）
            List<MessageResponse> sortedMessages = chatMessages.stream()
                    .sorted((m1, m2) -> {
                        if (m1.getCreateTime() == null && m2.getCreateTime() == null) {
                            return 0;
                        }
                        if (m1.getCreateTime() == null) {
                            return 1; // null值排在后面
                        }
                        if (m2.getCreateTime() == null) {
                            return -1;
                        }
                        return m1.getCreateTime().compareTo(m2.getCreateTime()); // 正序，最新的在最后
                    })
                    .toList();
            
            log.info("聊天记录查询完成: 消息数量={}, senderId={}, receiverId={}", 
                    sortedMessages.size(), senderId, receiverId);
            
            return Result.success(sortedMessages);
            
        } catch (Exception e) {
            log.error("查询聊天记录失败: senderId={}, receiverId={}", senderId, receiverId, e);
            return Result.error("查询聊天记录失败: " + e.getMessage());
        }
    }

    /**
     * 合并和排序消息
     * 将发送人和接收人之间的多条消息进行合并，并按创建时间倒序排序
     * 
     * @param messages 原始消息列表
     * @return 合并和排序后的消息列表
     */
    private List<MessageResponse> mergeAndSortMessages(List<MessageResponse> messages) {
        if (messages == null || messages.isEmpty()) {
            return messages;
        }
        
        log.debug("开始合并和排序消息，原始消息数量: {}", messages.size());
        
        // 按创建时间倒序排序
        List<MessageResponse> sortedMessages = messages.stream()
                .sorted((m1, m2) -> {
                    if (m1.getCreateTime() == null && m2.getCreateTime() == null) {
                        return 0;
                    }
                    if (m1.getCreateTime() == null) {
                        return 1; // null值排在后面
                    }
                    if (m2.getCreateTime() == null) {
                        return -1;
                    }
                    return m2.getCreateTime().compareTo(m1.getCreateTime()); // 倒序
                })
                .toList();
        
        log.debug("消息合并和排序完成，处理后消息数量: {}", sortedMessages.size());
        return sortedMessages;
    }
}