package com.gig.collide.service;

import com.gig.collide.domain.TaskReward;

import java.util.List;

/**
 * 奖励服务接口
 * 处理奖励发放相关的业务逻辑
 * 
 * @author GIG Team
 * @version 1.0.0
 * @since 2024-01-20
 */
public interface RewardService {

    /**
     * 根据任务模板ID获取奖励配置
     * 
     * @param taskTemplateId 任务模板ID
     * @return 奖励配置列表
     */
    List<TaskReward> getRewardsByTaskTemplateId(Long taskTemplateId);

    /**
     * 发放任务奖励给用户
     * 
     * @param userId 用户ID
     * @param taskRewards 奖励列表
     * @return 是否发放成功
     */
    boolean grantRewards(Long userId, List<TaskReward> taskRewards);

    /**
     * 发放单个奖励给用户
     * 
     * @param userId 用户ID
     * @param taskReward 奖励配置
     * @return 是否发放成功
     */
    boolean grantSingleReward(Long userId, TaskReward taskReward);
}
