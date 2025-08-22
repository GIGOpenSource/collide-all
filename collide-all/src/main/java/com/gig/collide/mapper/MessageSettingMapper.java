package com.gig.collide.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gig.collide.domain.MessageSetting;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 消息设置数据访问接口 - 简洁版
 * 基于message-simple.sql的t_message_setting表设计
 * 
 * @author GIG Team
 * @version 2.0.0 (简洁版)
 * @since 2024-01-16
 */
@Mapper
public interface MessageSettingMapper extends BaseMapper<MessageSetting> {

    /**
     * 消息设置列表查询（Controller专用）
     * 支持多种条件查询和分页
     * 
     * @param page 分页对象
     * @param userId 用户ID
     * @param settingType 设置类型
     * @param status 设置状态
     * @param isEnabled 是否启用
     * @param keyword 关键词搜索
     * @param orderBy 排序字段
     * @param orderDirection 排序方向
     * @return 分页消息设置列表
     */
    IPage<MessageSetting> selectMessageSettingList(IPage<MessageSetting> page,
                                                 @Param("userId") Long userId,
                                                 @Param("settingType") String settingType,
                                                 @Param("status") String status,
                                                 @Param("isEnabled") Boolean isEnabled,
                                                 @Param("keyword") String keyword,
                                                 @Param("orderBy") String orderBy,
                                                 @Param("orderDirection") String orderDirection);

    // =================== 基础查询 ===================

    /**
     * 根据用户ID查询消息设置
     */
    MessageSetting findByUserId(@Param("userId") Long userId);

    // =================== 更新操作 ===================

    /**
     * 更新陌生人消息设置
     */
    int updateStrangerMessageSetting(@Param("userId") Long userId,
                                   @Param("allowStrangerMsg") Boolean allowStrangerMsg);

    /**
     * 更新已读回执设置
     */
    int updateReadReceiptSetting(@Param("userId") Long userId,
                               @Param("autoReadReceipt") Boolean autoReadReceipt);

    /**
     * 更新消息通知设置
     */
    int updateNotificationSetting(@Param("userId") Long userId,
                                @Param("messageNotification") Boolean messageNotification);

    /**
     * 批量更新用户设置
     */
    int updateUserSettings(@Param("userId") Long userId,
                         @Param("allowStrangerMsg") Boolean allowStrangerMsg,
                         @Param("autoReadReceipt") Boolean autoReadReceipt,
                         @Param("messageNotification") Boolean messageNotification);

    // =================== 创建或更新 ===================

    /**
     * 创建或更新用户设置
     * 如果设置不存在则创建，存在则更新
     */
    int insertOrUpdate(@Param("userId") Long userId,
                      @Param("allowStrangerMsg") Boolean allowStrangerMsg,
                      @Param("autoReadReceipt") Boolean autoReadReceipt,
                      @Param("messageNotification") Boolean messageNotification);
}