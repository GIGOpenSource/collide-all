package com.gig.collide.service;

import com.gig.collide.domain.UserWallet;

/**
 * 用户钱包服务接口
 * 提供钱包相关的基础操作
 * 
 * @author GIG Team
 * @version 1.0.0
 * @since 2024-01-20
 */
public interface UserWalletService {

    /**
     * 根据用户ID获取钱包信息
     * 如果钱包不存在会自动创建
     * 
     * @param userId 用户ID
     * @return 钱包信息
     */
    UserWallet getOrCreateWallet(Long userId);

    /**
     * 增加用户金币
     * 
     * @param userId 用户ID
     * @param amount 金币数量
     * @return 是否成功
     */
    boolean addCoins(Long userId, Long amount);

    /**
     * 获取用户金币余额
     * 
     * @param userId 用户ID
     * @return 金币余额
     */
    Long getCoinBalance(Long userId);
}
