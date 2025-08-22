package com.gig.collide.service.Impl;

import com.gig.collide.domain.UserWallet;
import com.gig.collide.mapper.UserWalletMapper;
import com.gig.collide.service.UserWalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户钱包服务实现类
 * 
 * @author GIG Team
 * @version 1.0.0
 * @since 2024-01-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserWalletServiceImpl implements UserWalletService {

    private final UserWalletMapper userWalletMapper;

    @Override
    @Transactional
    public UserWallet getOrCreateWallet(Long userId) {
        log.debug("获取或创建用户钱包：userId={}", userId);
        
        // 先尝试查询现有钱包
        UserWallet wallet = userWalletMapper.selectByUserId(userId);
        
        // 如果钱包不存在，创建新钱包
        if (wallet == null) {
            log.info("用户钱包不存在，创建新钱包：userId={}", userId);
            int created = userWalletMapper.createWalletIfNotExists(userId);
            if (created > 0) {
                // 重新查询
                wallet = userWalletMapper.selectByUserId(userId);
                log.info("用户钱包创建成功：userId={}", userId);
            } else {
                log.warn("用户钱包创建失败：userId={}", userId);
            }
        }
        
        return wallet;
    }

    @Override
    @Transactional
    public boolean addCoins(Long userId, Long amount) {
        log.info("增加用户金币：userId={}, amount={}", userId, amount);
        
        if (amount == null || amount <= 0) {
            log.warn("金币数量无效：amount={}", amount);
            return false;
        }
        
        try {
            // 确保钱包存在
            getOrCreateWallet(userId);
            
            // 增加金币
            int updated = userWalletMapper.addCoinBalance(userId, amount);
            boolean success = updated > 0;
            
            if (success) {
                log.info("金币增加成功：userId={}, amount={}", userId, amount);
            } else {
                log.error("金币增加失败：userId={}, amount={}", userId, amount);
            }
            
            return success;
        } catch (Exception e) {
            log.error("增加金币异常：userId={}, amount={}", userId, amount, e);
            return false;
        }
    }

    @Override
    public Long getCoinBalance(Long userId) {
        log.debug("获取用户金币余额：userId={}", userId);
        
        UserWallet wallet = userWalletMapper.selectByUserId(userId);
        if (wallet == null) {
            log.debug("用户钱包不存在，返回0：userId={}", userId);
            return 0L;
        }
        
        Long balance = wallet.getCoinBalance();
        return balance != null ? balance : 0L;
    }
}
