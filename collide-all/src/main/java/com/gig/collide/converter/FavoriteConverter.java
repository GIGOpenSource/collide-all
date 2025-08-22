package com.gig.collide.converter;

import com.gig.collide.Apientry.api.favorite.request.FavoriteCreateRequest;
import com.gig.collide.Apientry.api.favorite.response.FavoriteResponse;
import com.gig.collide.domain.Favorite;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 收藏转换器
 * 用于在Request DTO、实体类和Response DTO之间进行转换
 *
 * @author GIG Team
 * @version 2.0.0
 * @since 2024-01-31
 */
@Component
public class FavoriteConverter {

    /**
     * 将FavoriteCreateRequest转换为Favorite实体
     *
     * @param request 创建请求
     * @return Favorite实体
     */
    public Favorite toEntity(FavoriteCreateRequest request) {
        if (request == null) {
            return null;
        }

        Favorite favorite = new Favorite();

        // 基本信息
        favorite.setUserId(request.getUserId());
        favorite.setFavoriteType(request.getFavoriteType());
        favorite.setTargetId(request.getTargetId());
        favorite.setTargetTitle(request.getTargetTitle());
        favorite.setTargetCover(request.getTargetCover());
        favorite.setTargetAuthorId(request.getTargetAuthorId());
        favorite.setUserNickname(request.getUserNickname());

        // 设置默认值
        favorite.setStatus("active");
        favorite.setCreateTime(LocalDateTime.now());
        favorite.setUpdateTime(LocalDateTime.now());

        return favorite;
    }

    /**
     * 将Favorite实体转换为FavoriteResponse
     *
     * @param entity 实体
     * @return 响应对象
     */
    public FavoriteResponse toResponse(Favorite entity) {
        if (entity == null) {
            return null;
        }

        FavoriteResponse response = new FavoriteResponse();

        // 基本信息
        response.setId(entity.getId());
        response.setUserId(entity.getUserId());
        response.setFavoriteType(entity.getFavoriteType());
        response.setTargetId(entity.getTargetId());
        response.setTargetTitle(entity.getTargetTitle());
        response.setTargetCover(entity.getTargetCover());
        response.setTargetAuthorId(entity.getTargetAuthorId());
        response.setUserNickname(entity.getUserNickname());
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
    public List<FavoriteResponse> toResponseList(List<Favorite> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 创建新的Favorite实体（工厂方法）
     *
     * @return 新的Favorite实体
     */
    public Favorite createNewFavorite() {
        Favorite favorite = new Favorite();
        favorite.setStatus("active");
        favorite.setCreateTime(LocalDateTime.now());
        favorite.setUpdateTime(LocalDateTime.now());
        return favorite;
    }
}
