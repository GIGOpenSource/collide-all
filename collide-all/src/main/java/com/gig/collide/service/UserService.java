package com.gig.collide.service;

import com.gig.collide.domain.User;

import java.util.List;

/**
 * 用户服务接口
 *
 * @author GIG Team
 * @since 1.0.0
 */
public interface UserService {

    /**
     * 创建用户
     */
    User createUser(User user);

    /**
     * 根据ID查询用户
     */
    User getUserById(Long id);

    /**
     * 根据用户名查询用户
     */
    User getUserByUsername(String username);

    /**
     * 更新用户信息
     */
    User updateUser(User user);

    /**
     * 分页查询用户列表
     */
    List<User> getUserList(Integer currentPage, Integer pageSize, String condition);

    /**
     * 统计用户总数
     */
    Long countUsers(String condition);

    /**
     * 删除用户
     */
    void deleteUser(Long id);
}
