package com.gig.collide.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gig.collide.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户数据访问层
 *
 * @author GIG Team
 * @since 1.0.0
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据ID查询用户
     *
     * @param id 用户ID
     * @return 用户信息
     */
    User selectUserById(@Param("id") Long id);

    /**
     * 分页查询用户列表
     *
     * @param condition 查询条件
     * @param offset 偏移量
     * @param pageSize 页面大小
     * @return 用户列表
     */
    List<User> selectUserList(@Param("condition") String condition, 
                             @Param("offset") Integer offset, 
                             @Param("pageSize") Integer pageSize);

    /**
     * 统计用户总数
     *
     * @param condition 查询条件
     * @return 用户总数
     */
    Long countUsers(@Param("condition") String condition);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    User selectUserByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户信息
     */
    User selectUserByEmail(@Param("email") String email);

    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号
     * @return 用户信息
     */
    User selectUserByPhone(@Param("phone") String phone);

    /**
     * 更新用户信息
     *
     * @param user 用户信息
     * @return 更新行数
     */
    int updateUserById(@Param("user") User user);

    /**
     * 逻辑删除用户
     *
     * @param id 用户ID
     * @return 删除行数
     */
    int deleteUserById(@Param("id") Long id);

    /**
     * 批量更新用户状态
     *
     * @param userIds 用户ID列表
     * @param status 状态
     * @return 更新行数
     */
    int batchUpdateUserStatus(@Param("userIds") List<Long> userIds, 
                             @Param("status") String status);

    /**
     * 根据VIP状态查询用户
     *
     * @param isVip VIP状态
     * @param offset 偏移量
     * @param pageSize 页面大小
     * @return 用户列表
     */
    List<User> selectUsersByVipStatus(@Param("isVip") String isVip, 
                                     @Param("offset") Integer offset, 
                                     @Param("pageSize") Integer pageSize);

    /**
     * 统计VIP用户数量
     *
     * @param isVip VIP状态
     * @return VIP用户数量
     */
    Long countVipUsers(@Param("isVip") String isVip);

    /**
     * 增加用户关注数
     *
     * @param userId 用户ID
     * @param increment 增量（正数增加，负数减少）
     * @return 更新行数
     */
    int updateFollowingCount(@Param("userId") Long userId, @Param("increment") int increment);

    /**
     * 增加用户粉丝数
     *
     * @param userId 用户ID
     * @param increment 增量（正数增加，负数减少）
     * @return 更新行数
     */
    int updateFollowerCount(@Param("userId") Long userId, @Param("increment") int increment);

    /**
     * 增加用户获得点赞数
     *
     * @param userId 用户ID
     * @param increment 增量（正数增加，负数减少）
     * @return 更新行数
     */
    int updateLikeCount(@Param("userId") Long userId, @Param("increment") int increment);

    /**
     * 增加用户内容数
     *
     * @param userId 用户ID
     * @param increment 增量（正数增加，负数减少）
     * @return 更新行数
     */
    int updateContentCount(@Param("userId") Long userId, @Param("increment") int increment);
}
