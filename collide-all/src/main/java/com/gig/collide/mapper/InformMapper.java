package com.gig.collide.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gig.collide.domain.Inform;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统通知Mapper接口
 * 基于t_inform表，处理系统通知的数据库操作
 *
 * @author GIG Team
 * @version 2.0.0
 * @since 2024-01-31
 */
@Mapper
public interface InformMapper extends BaseMapper<Inform> {

    /**
     * 分页查询通知
     *
     * @param page 分页参数
     * @param appName APP名称
     * @param typeRelation 类型关系
     * @param userType 用户类型
     * @param isDeleted 是否删除
     * @param isSent 是否已发送
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 通知分页
     */
    IPage<Inform> queryInforms(Page<Inform> page, @Param("appName") String appName,
                               @Param("typeRelation") String typeRelation, @Param("userType") String userType,
                               @Param("isDeleted") String isDeleted, @Param("isSent") String isSent,
                               @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 获取未发送的通知
     *
     * @param limit 限制数量
     * @return 未发送通知列表
     */
    List<Inform> getUnsentInforms(@Param("limit") Integer limit);

    /**
     * 获取已发送的通知
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param limit 限制数量
     * @return 已发送通知列表
     */
    List<Inform> getSentInforms(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime,
                                @Param("limit") Integer limit);

    /**
     * 统计通知数量
     *
     * @param appName APP名称
     * @param typeRelation 类型关系
     * @param userType 用户类型
     * @param isDeleted 是否删除
     * @param isSent 是否已发送
     * @return 通知数量
     */
    Long countInforms(@Param("appName") String appName, @Param("typeRelation") String typeRelation,
                      @Param("userType") String userType, @Param("isDeleted") String isDeleted,
                      @Param("isSent") String isSent);

    /**
     * 批量更新通知状态
     *
     * @param ids 通知ID列表
     * @param isSent 是否已发送
     * @param sendTime 发送时间
     * @return 更新行数
     */
    int batchUpdateSendStatus(@Param("ids") List<Long> ids, @Param("isSent") String isSent,
                              @Param("sendTime") LocalDateTime sendTime);

    /**
     * 获取通知统计信息
     *
     * @return 统计信息
     */
    List<java.util.Map<String, Object>> getInformStatistics();
}
