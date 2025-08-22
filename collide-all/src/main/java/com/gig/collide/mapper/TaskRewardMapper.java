package com.gig.collide.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gig.collide.domain.TaskReward;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 任务奖励 Mapper 接口
 * 
 * @author GIG Team
 * @version 1.0.0
 * @since 2024-01-20
 */
@Mapper
public interface TaskRewardMapper extends BaseMapper<TaskReward> {

    /**
     * 根据任务模板ID查询奖励配置
     * 
     * @param taskTemplateId 任务模板ID
     * @return 奖励配置列表
     */
    List<TaskReward> selectRewardsByTaskTemplateId(@Param("taskTemplateId") Long taskTemplateId);

    /**
     * 根据任务模板ID查询启用的奖励配置
     * 
     * @param taskTemplateId 任务模板ID
     * @return 启用的奖励配置列表
     */
    List<TaskReward> selectActiveRewardsByTaskTemplateId(@Param("taskTemplateId") Long taskTemplateId);
}
