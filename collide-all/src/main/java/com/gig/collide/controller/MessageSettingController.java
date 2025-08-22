package com.gig.collide.controller;


import com.gig.collide.Apientry.api.common.response.PageResponse;
import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.Apientry.api.message.MessageSettingFacadeService;
import com.gig.collide.Apientry.api.message.request.MessageSettingCreateRequest;
import com.gig.collide.Apientry.api.message.request.MessageSettingUpdateRequest;
import com.gig.collide.Apientry.api.message.response.MessageSettingResponse;
import com.gig.collide.service.MessageSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 消息设置REST控制器 - 简洁版
 * 基于message-simple.sql的t_message_setting表设计
 * 管理用户的消息偏好设置和权限控制
 * 
 * 主要功能：
 * - 设置管理：创建/更新用户设置、查询设置、初始化默认设置
 * - 单项设置：陌生人消息、已读回执、消息通知单独控制
 * - 权限验证：发送权限检查、各种设置状态查询
 * - 设置模板：重置默认、复制设置、批量初始化
 * - 设置分析：统计信息、设置历史记录
 * - 系统功能：设置同步、健康检查
 * 
 * @author GIG Team
 * @version 2.0.0 (简洁版)
 * @since 2024-01-16
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/message-settings")
@RequiredArgsConstructor
@Tag(name = "消息设置管理", description = "消息设置相关的API接口")
@Validated
public class MessageSettingController {

    private final MessageSettingService messageSettingService;

    /**
     * 消息设置列表查询
     * 支持多种条件查询和分页
     */
    @GetMapping("/list")
    @Operation(summary = "消息设置列表查询", description = "支持按用户、设置类型、状态等条件查询消息设置列表")
    public Result<PageResponse<MessageSettingResponse>> listMessageSettings(
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "设置类型") @RequestParam(required = false) String settingType,
            @Parameter(description = "设置状态") @RequestParam(required = false) String status,
            @Parameter(description = "是否启用") @RequestParam(required = false) Boolean isEnabled,
            @Parameter(description = "关键词搜索") @RequestParam(required = false) String keyword,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createTime") String orderBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "DESC") String orderDirection,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("REST请求 - 消息设置列表查询: userId={}, type={}, status={}, page={}/{}", 
                userId, settingType, status, currentPage, pageSize);
        return messageSettingService.listMessageSettingsForController(userId, settingType, status, isEnabled, keyword,
                orderBy, orderDirection, currentPage, pageSize);
    }
}