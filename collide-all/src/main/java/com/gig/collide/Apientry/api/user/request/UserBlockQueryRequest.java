package com.gig.collide.Apientry.api.user.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * 用户拉黑查询请求 - 简洁版
 *
 * @author GIG Team
 * @version 2.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class UserBlockQueryRequest {

    /**
     * 拉黑者用户ID
     */
    private Long userId;

    /**
     * 被拉黑用户ID
     */
    private Long blockedUserId;

    /**
     * 拉黑者用户名（模糊搜索）
     */
    private String userUsername;

    /**
     * 被拉黑用户名（模糊搜索）
     */
    private String blockedUsername;

    /**
     * 拉黑状态：active、cancelled
     */
    private String status;

    /**
     * 页码
     */
    private int page = 0;

    /**
     * 每页大小
     */
    private int size = 10;

    /**
     * 转换为Pageable对象
     */
    public Pageable toPageable() {
        return PageRequest.of(page, size);
    }
}
