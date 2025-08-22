package com.gig.collide.service.Impl;

import com.gig.collide.domain.User;
import com.gig.collide.domain.UserRole;
import com.gig.collide.mapper.UserMapper;
import com.gig.collide.mapper.UserRoleMapper;
import com.gig.collide.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户业务服务实现类
 *
 * @author GIG Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User createUser(User user) {
        log.info("创建用户: username={}", user.getUsername());
        
        // 校验用户名唯一性
        User existingUserByUsername = userMapper.selectUserByUsername(user.getUsername());
        if (existingUserByUsername != null) {
            throw new IllegalArgumentException("用户名已存在: " + user.getUsername());
        }
        
        // 校验邮箱唯一性（如果邮箱不为空）
        if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            User existingUserByEmail = userMapper.selectUserByEmail(user.getEmail());
            if (existingUserByEmail != null) {
                throw new IllegalArgumentException("邮箱已存在: " + user.getEmail());
            }
        }
        
        // 插入用户
        userMapper.insert(user);
        
        // 分配默认角色（普通用户，角色ID为1）
        UserRole userRole = UserRole.create(user.getId(), 1);
        userRoleMapper.insert(userRole);
        log.info("用户角色分配成功: userId={}, roleId=1", user.getId());
        
        return userMapper.selectUserById(user.getId());
    }

    @Override
    public User getUserById(Long id) {
        log.info("根据ID查询用户: id={}", id);
        return userMapper.selectUserById(id);
    }

    @Override
    public User getUserByUsername(String username) {
        log.info("根据用户名查询用户: username={}", username);
        return userMapper.selectUserByUsername(username);
    }

    @Override
    public User updateUser(User user) {
        log.info("更新用户信息: id={}", user.getId());
        userMapper.updateUserById(user);
        return userMapper.selectUserById(user.getId());
    }

    @Override
    public java.util.List<User> getUserList(Integer currentPage, Integer pageSize, String condition) {
        log.info("分页查询用户列表: page={}, size={}, condition={}", currentPage, pageSize, condition);
        
        // 计算偏移量
        int offset = (currentPage - 1) * pageSize;
        
        return userMapper.selectUserList(condition, offset, pageSize);
    }

    @Override
    public Long countUsers(String condition) {
        log.info("统计用户总数: condition={}", condition);
        return userMapper.countUsers(condition);
    }

    @Override
    public void deleteUser(Long id) {
        log.info("删除用户: id={}", id);
        userMapper.deleteUserById(id);
    }
}
