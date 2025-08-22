package com.gig.collide.controller;

import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.domain.TaskReward;
import com.gig.collide.domain.UserTaskRecord;
import com.gig.collide.service.UserTaskService;
import com.gig.collide.service.UserWalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户任务控制器 - C端用户专用
 * 提供用户任务相关的核心功能
 * 
 * @author GIG Team
 * @version 1.0.0
 * @since 2024-01-20
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/user-task")
@RequiredArgsConstructor
@Tag(name = "用户任务", description = "用户任务相关功能")
public class UserTaskController {

    private final UserTaskService userTaskService;
    private final UserWalletService userWalletService;

    /**
     * 获取用户今日任务列表
     */
    @GetMapping("/today")
    @Operation(summary = "获取今日任务", description = "获取当前用户的今日任务列表")
    public Result<List<UserTaskRecord>> getTodayTasks(
            @Parameter(description = "用户ID") @RequestParam Long userId) {
        log.debug("获取用户今日任务：userId={}", userId);
        try {
            List<UserTaskRecord> tasks = userTaskService.getTodayTasks(userId);
            return Result.success(tasks);
        } catch (Exception e) {
            log.error("获取今日任务失败：userId={}", userId, e);
            return Result.error("获取今日任务失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户任务列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取用户任务列表", description = "获取用户的任务列表，支持按类型筛选")
    public Result<List<UserTaskRecord>> getUserTasks(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "任务类型：daily-每日，weekly-每周，achievement-成就") @RequestParam(required = false) String taskType) {
        log.debug("获取用户任务列表：userId={}, taskType={}", userId, taskType);
        try {
            List<UserTaskRecord> tasks = userTaskService.getUserTasks(userId, taskType);
            return Result.success(tasks);
        } catch (Exception e) {
            log.error("获取用户任务列表失败：userId={}, taskType={}", userId, taskType, e);
            return Result.error("获取用户任务列表失败：" + e.getMessage());
        }
    }

    /**
     * 更新任务进度
     */
    @PostMapping("/progress")
    @Operation(summary = "更新任务进度", description = "根据用户行为更新任务进度")
    public Result<String> updateTaskProgress(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "任务动作：login、publish_content、like、comment、share、purchase") @RequestParam String taskAction,
            @Parameter(description = "进度增量") @RequestParam(defaultValue = "1") Integer increment) {
        log.info("更新任务进度：userId={}, taskAction={}, increment={}", userId, taskAction, increment);
        try {
            int updatedCount = userTaskService.updateTaskProgress(userId, taskAction, increment);
            return Result.success("更新了 " + updatedCount + " 个任务的进度");
        } catch (Exception e) {
            log.error("更新任务进度失败：userId={}, taskAction={}", userId, taskAction, e);
            return Result.error("更新任务进度失败：" + e.getMessage());
        }
    }

    /**
     * 领取任务奖励
     */
    @PostMapping("/claim-reward")
    @Operation(summary = "领取任务奖励", description = "领取已完成任务的奖励")
    public Result<List<TaskReward>> claimTaskReward(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "任务记录ID") @RequestParam Long taskRecordId) {
        log.info("领取任务奖励：userId={}, taskRecordId={}", userId, taskRecordId);
        try {
            List<TaskReward> rewards = userTaskService.claimTaskReward(userId, taskRecordId);
            return Result.success("奖励领取成功", rewards);
        } catch (Exception e) {
            log.error("领取任务奖励失败：userId={}, taskRecordId={}", userId, taskRecordId, e);
            return Result.error("领取任务奖励失败：" + e.getMessage());
        }
    }

    /**
     * 获取可领取奖励的任务
     */
    @GetMapping("/completed")
    @Operation(summary = "获取已完成任务", description = "获取已完成但未领取奖励的任务")
    public Result<List<UserTaskRecord>> getCompletedTasks(
            @Parameter(description = "用户ID") @RequestParam Long userId) {
        log.debug("获取已完成任务：userId={}", userId);
        try {
            List<UserTaskRecord> tasks = userTaskService.getCompletedTasks(userId);
            return Result.success(tasks);
        } catch (Exception e) {
            log.error("获取已完成任务失败：userId={}", userId, e);
            return Result.error("获取已完成任务失败：" + e.getMessage());
        }
    }

    /**
     * 初始化用户每日任务
     */
    @PostMapping("/init-daily")
    @Operation(summary = "初始化每日任务", description = "为用户初始化今日的每日任务")
    public Result<String> initDailyTasks(
            @Parameter(description = "用户ID") @RequestParam Long userId) {
        log.info("初始化用户每日任务：userId={}", userId);
        try {
            int initCount = userTaskService.initDailyTasks(userId);
            return Result.success("初始化了 " + initCount + " 个每日任务");
        } catch (Exception e) {
            log.error("初始化每日任务失败：userId={}", userId, e);
            return Result.error("初始化每日任务失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户任务统计
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取任务统计", description = "获取用户的任务完成统计信息")
    public Result<Map<String, Object>> getTaskStatistics(
            @Parameter(description = "用户ID") @RequestParam Long userId) {
        log.debug("获取用户任务统计：userId={}", userId);
        try {
            Map<String, Object> statistics = userTaskService.getTaskStatistics(userId);
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取任务统计失败：userId={}", userId, e);
            return Result.error("获取任务统计失败：" + e.getMessage());
        }
    }

    /**
     * 一键领取所有奖励
     */
    @PostMapping("/claim-all")
    @Operation(summary = "一键领取所有奖励", description = "领取所有已完成任务的奖励")
    public Result<Map<String, Object>> claimAllRewards(
            @Parameter(description = "用户ID") @RequestParam Long userId) {
        log.info("一键领取所有奖励：userId={}", userId);
        try {
            Map<String, Object> result = userTaskService.claimAllRewards(userId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("一键领取所有奖励失败：userId={}", userId, e);
            return Result.error("一键领取所有奖励失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户金币余额
     */
    @GetMapping("/coin-balance")
    @Operation(summary = "获取金币余额", description = "获取用户当前的金币余额")
    public Result<Long> getCoinBalance(
            @Parameter(description = "用户ID") @RequestParam Long userId) {
        log.debug("获取用户金币余额：userId={}", userId);
        try {
            Long balance = userWalletService.getCoinBalance(userId);
            return Result.success(balance);
        } catch (Exception e) {
            log.error("获取金币余额失败：userId={}", userId, e);
            return Result.error("获取金币余额失败：" + e.getMessage());
        }
    }
}
