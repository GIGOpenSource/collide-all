package com.gig.collide.service.Impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.gig.collide.domain.TaskReward;
import com.gig.collide.domain.TaskTemplate;
import com.gig.collide.domain.UserTaskRecord;
import com.gig.collide.mapper.UserTaskMapper;
import com.gig.collide.service.RewardService;
import com.gig.collide.service.UserTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 用户任务服务实现类
 * 
 * @author GIG Team
 * @version 1.0.0
 * @since 2024-01-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserTaskServiceImpl implements UserTaskService {

    private final UserTaskMapper userTaskMapper;
    private final RewardService rewardService;

    @Override
    public List<UserTaskRecord> getTodayTasks(Long userId) {
        log.debug("获取用户今日任务：userId={}", userId);
        LocalDate today = LocalDate.now();
        
        // 先尝试初始化今日任务
        try {
            initDailyTasks(userId);
        } catch (Exception e) {
            log.warn("初始化今日任务失败，继续获取已有任务：userId={}", userId, e);
        }
        
        return userTaskMapper.selectUserTasksByDate(userId, today);
    }

    @Override
    public List<UserTaskRecord> getUserTasks(Long userId, String taskType) {
        log.debug("获取用户任务列表：userId={}, taskType={}", userId, taskType);
        return userTaskMapper.selectUserTasks(userId, taskType);
    }

    @Override
    @Transactional
    public int updateTaskProgress(Long userId, String taskAction, Integer increment) {
        log.info("更新任务进度：userId={}, taskAction={}, increment={}", userId, taskAction, increment);
        
        // 查询该用户该动作的进行中任务
        List<UserTaskRecord> tasks = userTaskMapper.selectTasksByUserIdAndAction(userId, taskAction);
        if (tasks.isEmpty()) {
            log.debug("未找到进行中的任务：userId={}, taskAction={}", userId, taskAction);
            return 0;
        }
        
        int updatedCount = 0;
        for (UserTaskRecord task : tasks) {
            // 更新进度
            int newCount = (task.getCurrentCount() != null ? task.getCurrentCount() : 0) + increment;
            task.setCurrentCount(newCount);
            
            // 检查是否完成
            Boolean isCompleted = task.getIsCompleted();
            if (newCount >= task.getTargetCount()) {
                isCompleted = true;
                task.setIsCompleted(isCompleted);
                task.setCompleteTime(LocalDateTime.now());
                log.info("任务完成：userId={}, taskId={}, taskName={}", userId, task.getId(), task.getTaskName());
            }
            
            // 更新到数据库
            int updated = userTaskMapper.updateTaskProgress(task.getId(), newCount, isCompleted ? "1" : "0");
            if (updated > 0) {
                updatedCount++;
            }
        }
        
        return updatedCount;
    }

    @Override
    @Transactional
    public List<TaskReward> claimTaskReward(Long userId, Long taskRecordId) {
        log.info("领取任务奖励：userId={}, taskRecordId={}", userId, taskRecordId);
        
        // 查询任务记录
        UserTaskRecord taskRecord = userTaskMapper.selectById(taskRecordId);
        if (taskRecord == null) {
            throw new RuntimeException("任务记录不存在：" + taskRecordId);
        }
        
        if (!userId.equals(taskRecord.getUserId())) {
            throw new RuntimeException("任务记录不属于当前用户");
        }
        
        if (!Boolean.TRUE.equals(taskRecord.getIsCompleted())) {
            throw new RuntimeException("任务尚未完成，无法领取奖励");
        }
        
        // 检查是否已领取过奖励
        if (taskRecord.getIsRewarded() != null && taskRecord.getIsRewarded()) {
            throw new RuntimeException("奖励已领取过");
        }
        
        // 获取奖励配置
        List<TaskReward> rewards = rewardService.getRewardsByTaskTemplateId(taskRecord.getTaskId());
        if (rewards.isEmpty()) {
            log.warn("任务没有配置奖励：taskId={}", taskRecord.getTaskId());
            return Collections.emptyList();
        }
        
        // 发放奖励
        boolean success = rewardService.grantRewards(userId, rewards);
        if (success) {
            // 标记奖励已领取
            UpdateWrapper<UserTaskRecord> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", taskRecordId)
                        .set("is_rewarded", 1)
                        .set("reward_time", LocalDateTime.now());
            userTaskMapper.update(null, updateWrapper);
            
            log.info("奖励领取成功：userId={}, taskRecordId={}, 奖励数量={}", userId, taskRecordId, rewards.size());
            return rewards;
        } else {
            throw new RuntimeException("奖励发放失败");
        }
    }

    @Override
    public List<UserTaskRecord> getCompletedTasks(Long userId) {
        log.debug("获取已完成任务：userId={}", userId);
        return userTaskMapper.selectCompletedTasks(userId);
    }

    @Override
    @Transactional
    public int initDailyTasks(Long userId) {
        log.info("初始化用户每日任务：userId={}", userId);
        LocalDate today = LocalDate.now();
        
        // 获取需要初始化的每日任务模板
        List<TaskTemplate> templates = userTaskMapper.selectDailyTasksToInit(userId, today);
        if (templates.isEmpty()) {
            log.debug("没有需要初始化的每日任务：userId={}", userId);
            return 0;
        }
        
        // 构建用户任务记录
        List<UserTaskRecord> taskRecords = new ArrayList<>();
        for (TaskTemplate template : templates) {
            UserTaskRecord record = UserTaskRecord.create(
                userId, 
                template.getId(),
                template.getTaskType(),
                template.getTaskCategory(),
                template.getTaskAction(),
                template.getTargetCount(),
                today
            );
            record.setTaskName(template.getTaskName());
            record.setTaskDesc(template.getTaskDesc());
            taskRecords.add(record);
        }
        
        // 批量插入
        int insertCount = userTaskMapper.batchInsertUserTasks(taskRecords);
        log.info("初始化每日任务完成：userId={}, 初始化数量={}", userId, insertCount);
        
        return insertCount;
    }

    @Override
    public Map<String, Object> getTaskStatistics(Long userId) {
        log.debug("获取用户任务统计：userId={}", userId);
        Map<String, Object> statistics = userTaskMapper.selectTaskStatistics(userId);
        
        // 计算完成率
        if (statistics != null) {
            Long totalTasks = (Long) statistics.get("totalTasks");
            Long completedTasks = (Long) statistics.get("completedTasks");
            Long todayTasks = (Long) statistics.get("todayTasks");
            Long todayCompletedTasks = (Long) statistics.get("todayCompletedTasks");
            
            // 总完成率
            double completionRate = totalTasks > 0 ? (completedTasks.doubleValue() / totalTasks.doubleValue()) * 100 : 0;
            statistics.put("completionRate", Math.round(completionRate * 100.0) / 100.0);
            
            // 今日完成率
            double todayCompletionRate = todayTasks > 0 ? (todayCompletedTasks.doubleValue() / todayTasks.doubleValue()) * 100 : 0;
            statistics.put("todayCompletionRate", Math.round(todayCompletionRate * 100.0) / 100.0);
        }
        
        return statistics;
    }

    @Override
    @Transactional
    public Map<String, Object> claimAllRewards(Long userId) {
        log.info("一键领取所有奖励：userId={}", userId);
        
        // 获取所有已完成但未领取奖励的任务
        List<UserTaskRecord> completedTasks = getCompletedTasks(userId);
        if (completedTasks.isEmpty()) {
            return Map.of("message", "没有可领取的奖励", "rewardCount", 0);
        }
        
        int successCount = 0;
        int totalRewards = 0;
        List<String> errors = new ArrayList<>();
        
        for (UserTaskRecord task : completedTasks) {
            try {
                List<TaskReward> rewards = claimTaskReward(userId, task.getId());
                successCount++;
                totalRewards += rewards.size();
                log.debug("领取任务奖励成功：taskId={}, 奖励数量={}", task.getId(), rewards.size());
            } catch (Exception e) {
                log.error("领取任务奖励失败：taskId={}", task.getId(), e);
                errors.add("任务「" + task.getTaskName() + "」奖励领取失败：" + e.getMessage());
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "奖励领取完成");
        result.put("successCount", successCount);
        result.put("totalTasks", completedTasks.size());
        result.put("totalRewards", totalRewards);
        
        if (!errors.isEmpty()) {
            result.put("errors", errors);
        }
        
        return result;
    }

    @Override
    public boolean checkAndCompleteTask(UserTaskRecord taskRecord) {
        if (taskRecord.canComplete()) {
            taskRecord.complete();
            int updated = userTaskMapper.updateTaskProgress(
                taskRecord.getId(), 
                taskRecord.getCurrentCount(), 
                taskRecord.getIsCompleted() ? "1" : "0"
            );
            return updated > 0;
        }
        return false;
    }

    @Override
    @Transactional
    public int resetDailyTasks() {
        log.info("重置每日任务");
        LocalDate yesterday = LocalDate.now().minusDays(1);
        
        // 将昨天未完成的每日任务标记为过期
        int resetCount = userTaskMapper.resetExpiredDailyTasks(yesterday);
        log.info("重置每日任务完成：resetCount={}", resetCount);
        
        return resetCount;
    }
}
