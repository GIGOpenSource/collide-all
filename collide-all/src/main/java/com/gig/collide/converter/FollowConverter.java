package com.gig.collide.converter;

import com.gig.collide.Apientry.api.follow.request.FollowCreateRequest;
import com.gig.collide.Apientry.api.follow.response.FollowResponse;
import com.gig.collide.domain.Follow;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 关注转换器
 * 用于在Request DTO、实体类和Response DTO之间进行转换
 *
 * @author GIG Team
 * @version 2.0.0
 * @since 2024-01-31
 */
@Component
public class FollowConverter {

    /**
     * 将FollowCreateRequest转换为Follow实体
     *
     * @param request 创建请求
     * @return Follow实体
     */
    public Follow toEntity(FollowCreateRequest request) {
        if (request == null) {
            return null;
        }

        Follow follow = new Follow();

        // 基本信息
        follow.setFollowerId(request.getFollowerId());
        follow.setFolloweeId(request.getFolloweeId());
        follow.setFollowerNickname(request.getFollowerNickname());
        follow.setFollowerAvatar(request.getFollowerAvatar());
        follow.setFolloweeNickname(request.getFolloweeNickname());
        follow.setFolloweeAvatar(request.getFolloweeAvatar());

        // 设置默认值
        follow.setStatus(request.getStatus() != null ? request.getStatus() : "active");
        follow.setCreateTime(LocalDateTime.now());
        follow.setUpdateTime(LocalDateTime.now());

        return follow;
    }

    /**
     * 将Follow实体转换为FollowResponse
     *
     * @param entity 实体
     * @return 响应对象
     */
    public FollowResponse toResponse(Follow entity) {
        if (entity == null) {
            return null;
        }

        FollowResponse response = new FollowResponse();

        // 基本信息
        response.setId(entity.getId());
        response.setFollowerId(entity.getFollowerId());
        response.setFolloweeId(entity.getFolloweeId());
        response.setFollowerNickname(entity.getFollowerNickname());
        response.setFollowerAvatar(entity.getFollowerAvatar());
        response.setFolloweeNickname(entity.getFolloweeNickname());
        response.setFolloweeAvatar(entity.getFolloweeAvatar());
        response.setStatus(entity.getStatus());

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
    public List<FollowResponse> toResponseList(List<Follow> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 创建新的Follow实体（工厂方法）
     *
     * @return 新的Follow实体
     */
    public Follow createNewFollow() {
        Follow follow = new Follow();
        follow.setStatus("active");
        follow.setCreateTime(LocalDateTime.now());
        follow.setUpdateTime(LocalDateTime.now());
        return follow;
    }


}
