package com.gig.collide.service;

import com.gig.collide.domain.TaskReward;
import com.gig.collide.domain.UserTaskRecord;

import java.util.List;
import java.util.Map;

/**
 * 用户任务服务接口
 * 提供用户任务相关的核心业务功能
 * 
 * @author GIG Team
 * @version 1.0.0
 * @since 2024-01-20
 */
public interface UserTaskService {

    /**
     * 获取用户今日任务列表
     * 
     * @param userId 用户ID
     * @return 今日任务列表
     */
    List<UserTaskRecord> getTodayTasks(Long userId);

    /**
     * 获取用户任务列表
     * 
     * @param userId 用户ID
     * @param taskType 任务类型（可选）：daily、weekly、achievement
     * @return 用户任务列表
     */
    List<UserTaskRecord> getUserTasks(Long userId, String taskType);

    /**
     * 更新任务进度
     * 
     * @param userId 用户ID
     * @param taskAction 任务动作：login、publish_content、like、comment、share、purchase
     * @param increment 进度增量
     * @return 更新的任务数量
     */
    int updateTaskProgress(Long userId, String taskAction, Integer increment);

    /**
     * 领取任务奖励
     * 
     * @param userId 用户ID
     * @param taskRecordId 任务记录ID
     * @return 奖励列表
     */
    List<TaskReward> claimTaskReward(Long userId, Long taskRecordId);

    /**
     * 获取已完成但未领取奖励的任务
     * 
     * @param userId 用户ID
     * @return 已完成任务列表
     */
    List<UserTaskRecord> getCompletedTasks(Long userId);

    /**
     * 初始化用户每日任务
     * 
     * @param userId 用户ID
     * @return 初始化的任务数量
     */
    int initDailyTasks(Long userId);

    /**
     * 获取用户任务统计
     * 
     * @param userId 用户ID
     * @return 统计信息
     */
    Map<String, Object> getTaskStatistics(Long userId);

    /**
     * 一键领取所有奖励
     * 
     * @param userId 用户ID
     * @return 领取结果：包含奖励总数、金币总数等信息
     */
    Map<String, Object> claimAllRewards(Long userId);

    /**
     * 检查并完成任务
     * 检查任务是否达到完成条件，如果达到则自动标记为完成
     * 
     * @param taskRecord 任务记录
     * @return 是否有任务被完成
     */
    boolean checkAndCompleteTask(UserTaskRecord taskRecord);

    /**
     * 重置每日任务（定时任务使用）
     * 
     * @return 重置的任务数量
     */
    int resetDailyTasks();
}
