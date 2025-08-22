package com.gig.collide.Apientry.api.user.request;

import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;


/**
 * 用户查询请求 - 简洁版
 *
 * @author GIG Team
 * @version 2.0.0
 */
@Data
public class UserQueryRequest {

    /**
     * 当前页码
     */
    private int page = 0;

    /**
     * 每页大小
     */
    private int size = 10;

    /**
     * 排序条件
     */
    private Sort sort = Sort.unsorted();

    /**
     * 用户名（模糊搜索）
     */
    private String username;

    /**
     * 昵称（模糊搜索）
     */
    private String nickname;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 用户状态：active、inactive、suspended、banned
     */
    private String status;

    public Pageable toPageable() {
        return PageRequest.of(page, size, sort);
    }
}
