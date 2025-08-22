package com.gig.collide.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gig.collide.domain.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户角色关联Mapper接口
 * 支持用户与角色的多对多关系管理
 *
 * @author GIG Team
 * @version 2.0.0 (简洁版)
 * @since 2024-01-01
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {

    /**
     * 根据用户ID查询角色列表
     */
    List<UserRole> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID查询用户列表
     */
    List<UserRole> selectByRoleId(@Param("roleId") Integer roleId);

    /**
     * 检查用户是否拥有指定角色
     */
    int countByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Integer roleId);

    /**
     * 删除用户的所有角色
     */
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 批量插入用户角色关联
     */
    int batchInsert(@Param("userRoles") List<UserRole> userRoles);
}
