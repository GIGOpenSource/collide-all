package com.gig.collide.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gig.collide.domain.TaskTemplate;
import com.gig.collide.domain.UserTaskRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 用户任务记录 Mapper 接口
 * 
 * @author GIG Team
 * @version 1.0.0
 * @since 2024-01-20
 */
@Mapper
public interface UserTaskMapper extends BaseMapper<UserTaskRecord> {

    /**
     * 获取用户指定日期的任务记录（包含任务模板信息）
     * 
     * @param userId 用户ID
     * @param taskDate 任务日期
     * @return 任务记录列表
     */
    List<UserTaskRecord> selectUserTasksByDate(@Param("userId") Long userId, @Param("taskDate") LocalDate taskDate);

    /**
     * 获取用户任务记录列表（包含任务模板信息）
     * 
     * @param userId 用户ID
     * @param taskType 任务类型（可选）
     * @return 任务记录列表
     */
    List<UserTaskRecord> selectUserTasks(@Param("userId") Long userId, @Param("taskType") String taskType);

    /**
     * 根据用户ID和任务动作查询进行中的任务
     * 
     * @param userId 用户ID
     * @param taskAction 任务动作
     * @return 任务记录列表
     */
    List<UserTaskRecord> selectTasksByUserIdAndAction(@Param("userId") Long userId, @Param("taskAction") String taskAction);

    /**
     * 获取用户已完成但未领取奖励的任务
     * 
     * @param userId 用户ID
     * @return 已完成任务列表
     */
    List<UserTaskRecord> selectCompletedTasks(@Param("userId") Long userId);

    /**
     * 获取用户任务统计信息
     * 
     * @param userId 用户ID
     * @return 统计信息
     */
    Map<String, Object> selectTaskStatistics(@Param("userId") Long userId);

    /**
     * 更新任务进度
     * 
     * @param id 任务记录ID
     * @param currentCount 当前完成次数
     * @param isCompleted 是否完成（"1"表示完成，"0"表示未完成）
     * @return 影响行数
     */
    int updateTaskProgress(@Param("id") Long id, @Param("currentCount") Integer currentCount, @Param("isCompleted") String isCompleted);

    /**
     * 批量更新任务进度
     * 
     * @param taskRecords 任务记录列表
     * @return 影响行数
     */
    int batchUpdateTaskProgress(@Param("taskRecords") List<UserTaskRecord> taskRecords);

    /**
     * 获取用户今日需要初始化的任务模板
     * 查询活跃的每日任务模板，但用户今日还没有对应的任务记录
     * 
     * @param userId 用户ID
     * @param taskDate 任务日期
     * @return 任务模板列表
     */
    List<TaskTemplate> selectDailyTasksToInit(@Param("userId") Long userId, @Param("taskDate") LocalDate taskDate);

    /**
     * 批量插入用户任务记录
     * 
     * @param taskRecords 任务记录列表
     * @return 影响行数
     */
    int batchInsertUserTasks(@Param("taskRecords") List<UserTaskRecord> taskRecords);

    /**
     * 重置每日任务（将昨天的每日任务标记为过期，准备生成新的）
     * 
     * @param yesterday 昨天的日期
     * @return 影响行数
     */
    int resetExpiredDailyTasks(@Param("yesterday") LocalDate yesterday);

    /**
     * 检查用户是否已有指定日期的任务记录
     * 
     * @param userId 用户ID
     * @param taskTemplateId 任务模板ID
     * @param taskDate 任务日期
     * @return 记录数量
     */
    int countUserTaskByTemplateAndDate(@Param("userId") Long userId, 
                                      @Param("taskTemplateId") Long taskTemplateId, 
                                      @Param("taskDate") LocalDate taskDate);
}
