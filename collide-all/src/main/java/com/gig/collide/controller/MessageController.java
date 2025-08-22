package com.gig.collide.controller;


import com.gig.collide.Apientry.api.common.response.PageResponse;
import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.Apientry.api.message.MessageFacadeService;
import com.gig.collide.Apientry.api.message.request.MessageCreateRequest;
import com.gig.collide.Apientry.api.message.request.MessageQueryRequest;
import com.gig.collide.Apientry.api.message.request.MessageUpdateRequest;
import com.gig.collide.Apientry.api.message.response.MessageResponse;
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
}