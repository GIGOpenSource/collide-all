package com.gig.collide.service.Impl;

import com.gig.collide.domain.TaskReward;
import com.gig.collide.mapper.TaskRewardMapper;
import com.gig.collide.service.RewardService;
import com.gig.collide.service.UserWalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 奖励服务实现类
 * 
 * @author GIG Team
 * @version 1.0.0
 * @since 2024-01-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RewardServiceImpl implements RewardService {

    private final TaskRewardMapper taskRewardMapper;
    private final UserWalletService userWalletService;

    @Override
    public List<TaskReward> getRewardsByTaskTemplateId(Long taskTemplateId) {
        log.debug("获取任务奖励配置：taskTemplateId={}", taskTemplateId);
        return taskRewardMapper.selectActiveRewardsByTaskTemplateId(taskTemplateId);
    }

    @Override
    public boolean grantRewards(Long userId, List<TaskReward> taskRewards) {
        log.info("批量发放奖励：userId={}, 奖励数量={}", userId, taskRewards.size());
        
        boolean allSuccess = true;
        for (TaskReward reward : taskRewards) {
            boolean success = grantSingleReward(userId, reward);
            if (!success) {
                allSuccess = false;
                log.error("奖励发放失败：userId={}, rewardId={}, rewardType={}", 
                    userId, reward.getId(), reward.getRewardType());
            }
        }
        
        return allSuccess;
    }

    @Override
    public boolean grantSingleReward(Long userId, TaskReward taskReward) {
        log.info("发放单个奖励：userId={}, rewardType={}, amount={}", 
            userId, taskReward.getRewardType(), taskReward.getRewardAmount());
        
        try {
            switch (taskReward.getRewardType().toLowerCase()) {
                case "coin":
                    return grantCoinReward(userId, taskReward);
                case "cash":
                    return grantCashReward(userId, taskReward);
                case "experience":
                    return grantExperienceReward(userId, taskReward);
                case "item":
                    return grantItemReward(userId, taskReward);
                default:
                    log.warn("未知的奖励类型：{}", taskReward.getRewardType());
                    return false;
            }
        } catch (Exception e) {
            log.error("奖励发放异常：userId={}, rewardType={}", userId, taskReward.getRewardType(), e);
            return false;
        }
    }

    /**
     * 发放金币奖励
     */
    private boolean grantCoinReward(Long userId, TaskReward taskReward) {
        log.info("发放金币奖励：userId={}, amount={}", userId, taskReward.getRewardAmount());
        
        try {
            // 调用钱包服务增加金币
            boolean success = userWalletService.addCoins(userId, taskReward.getRewardAmount().longValue());
            
            if (success) {
                log.info("金币奖励发放成功：userId={}, amount={}", userId, taskReward.getRewardAmount());
            } else {
                log.error("金币奖励发放失败：userId={}, amount={}", userId, taskReward.getRewardAmount());
            }
            
            return success;
        } catch (Exception e) {
            log.error("金币奖励发放异常：userId={}, amount={}", userId, taskReward.getRewardAmount(), e);
            return false;
        }
    }

    /**
     * 发放现金奖励
     */
    private boolean grantCashReward(Long userId, TaskReward taskReward) {
        log.info("发放现金奖励：userId={}, amount={}", userId, taskReward.getRewardAmount());
        // TODO: 调用用户钱包服务增加现金余额
        // walletService.addBalance(userId, taskReward.getRewardAmount());
        return true; // 暂时返回true，等待实际的钱包服务实现
    }

    /**
     * 发放经验奖励
     */
    private boolean grantExperienceReward(Long userId, TaskReward taskReward) {
        log.info("发放经验奖励：userId={}, amount={}", userId, taskReward.getRewardAmount());
        // TODO: 调用用户经验服务增加经验值
        // userService.addExperience(userId, taskReward.getRewardAmount().intValue());
        return true; // 暂时返回true，等待实际的用户经验系统实现
    }

    /**
     * 发放道具奖励
     */
    private boolean grantItemReward(Long userId, TaskReward taskReward) {
        log.info("发放道具奖励：userId={}, amount={}", userId, taskReward.getRewardAmount());
        // TODO: 调用道具服务发放道具
        // itemService.grantItem(userId, taskReward.getRewardData(), taskReward.getRewardAmount().intValue());
        return true; // 暂时返回true，等待实际的道具系统实现
    }
}
