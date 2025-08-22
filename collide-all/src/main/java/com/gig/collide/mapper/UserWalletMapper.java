package com.gig.collide.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gig.collide.domain.UserWallet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户钱包 Mapper 接口
 * 
 * @author GIG Team
 * @version 1.0.0
 * @since 2024-01-20
 */
@Mapper
public interface UserWalletMapper extends BaseMapper<UserWallet> {

    /**
     * 根据用户ID查询钱包信息
     * 
     * @param userId 用户ID
     * @return 钱包信息
     */
    UserWallet selectByUserId(@Param("userId") Long userId);

    /**
     * 增加用户金币余额
     * 
     * @param userId 用户ID
     * @param amount 金币数量
     * @return 影响行数
     */
    int addCoinBalance(@Param("userId") Long userId, @Param("amount") Long amount);

    /**
     * 创建用户钱包（如果不存在）
     * 
     * @param userId 用户ID
     * @return 影响行数
     */
    int createWalletIfNotExists(@Param("userId") Long userId);
}
