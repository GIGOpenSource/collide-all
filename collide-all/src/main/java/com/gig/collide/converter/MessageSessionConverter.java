package com.gig.collide.converter;

import com.gig.collide.Apientry.api.message.request.MessageSessionCreateRequest;
import com.gig.collide.Apientry.api.message.request.MessageSessionUpdateRequest;
import com.gig.collide.Apientry.api.message.response.MessageSessionResponse;
import com.gig.collide.domain.MessageSession;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 消息会话转换器
 * 用于在Request DTO、实体类和Response DTO之间进行转换
 *
 * @author GIG Team
 * @version 2.0.0
 * @since 2024-01-31
 */
@Component
public class MessageSessionConverter {

    /**
     * 将MessageSessionCreateRequest转换为MessageSession实体
     *
     * @param request 创建请求
     * @return MessageSession实体
     */
    public MessageSession toEntity(MessageSessionCreateRequest request) {
        if (request == null) {
            return null;
        }

        MessageSession session = new MessageSession();

        // 基本信息
        session.setUserId(request.getUserId());
        session.setOtherUserId(request.getOtherUserId());
        session.setLastMessageId(request.getLastMessageId());
        session.setLastMessageTime(request.getLastMessageTime());
        session.setUnreadCount(request.getUnreadCount() != null ? request.getUnreadCount() : 0);
        session.setIsArchived(request.getIsArchived() != null ? request.getIsArchived() : false);

        // 设置默认值
        session.setCreateTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());

        return session;
    }

    /**
     * 将MessageSessionUpdateRequest转换为MessageSession实体
     *
     * @param request 更新请求
     * @return MessageSession实体
     */
    public MessageSession toEntity(MessageSessionUpdateRequest request) {
        if (request == null) {
            return null;
        }

        MessageSession session = new MessageSession();

        // 基本信息
        session.setId(request.getSessionId());
        session.setUserId(request.getUserId());
        session.setOtherUserId(request.getOtherUserId());
        session.setLastMessageId(request.getLastMessageId());
        session.setLastMessageTime(request.getLastMessageTime());
        session.setUnreadCount(request.getUnreadCount());
        session.setIsArchived(request.getIsArchived());

        // 设置更新时间
        session.setUpdateTime(LocalDateTime.now());

        return session;
    }

    /**
     * 将MessageSession实体转换为MessageSessionResponse
     *
     * @param entity 实体
     * @return 响应对象
     */
    public MessageSessionResponse toResponse(MessageSession entity) {
        if (entity == null) {
            return null;
        }

        MessageSessionResponse response = new MessageSessionResponse();

        // 基本信息
        response.setId(entity.getId());
        response.setUserId(entity.getUserId());
        response.setOtherUserId(entity.getOtherUserId());
        response.setLastMessageId(entity.getLastMessageId());
        response.setLastMessageTime(entity.getLastMessageTime());
        response.setUnreadCount(entity.getUnreadCount());
        response.setIsArchived(entity.getIsArchived());

        // 时间相关
        response.setCreateTime(entity.getCreateTime());
        response.setUpdateTime(entity.getUpdateTime());

        return response;
    }

    /**
     * 批量转换实体列表为响应列表
     *
     * @param entities 实体列表
     * @return 响应列表
     */
    public List<MessageSessionResponse> toResponseList(List<MessageSession> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 创建新的MessageSession实体（工厂方法）
     *
     * @return 新的MessageSession实体
     */
    public MessageSession createNewMessageSession() {
        MessageSession session = new MessageSession();
        session.setUnreadCount(0);
        session.setIsArchived(false);
        session.setCreateTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());
        return session;
    }
}
