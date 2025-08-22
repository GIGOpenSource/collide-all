package com.gig.collide.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gig.collide.domain.Inform;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统通知服务接口
 * 基于t_inform表，处理系统通知的发送和管理
 *
 * @author GIG Team
 * @version 2.0.0
 * @since 2024-01-31
 */
public interface InformService {

    // =================== 基础CRUD ===================

    /**
     * 创建通知
     *
     * @param inform 通知实体
     * @return 创建的通知
     */
    Inform createInform(Inform inform);

    /**
     * 根据ID获取通知
     *
     * @param id 通知ID
     * @return 通知详情
     */
    Inform getInformById(Long id);

    /**
     * 更新通知
     *
     * @param inform 通知实体
     * @return 是否成功
     */
    boolean updateInform(Inform inform);

    /**
     * 删除通知（逻辑删除）
     *
     * @param id 通知ID
     * @return 是否成功
     */
    boolean deleteInform(Long id);

    // =================== 查询功能 ===================

    /**
     * 分页查询通知
     *
     * @param appName APP名称
     * @param typeRelation 类型关系
     * @param userType 用户类型
     * @param isDeleted 是否删除
     * @param isSent 是否已发送
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param currentPage 当前页码
     * @param pageSize 页面大小
     * @return 通知分页
     */
    IPage<Inform> queryInforms(String appName, String typeRelation, String userType,
                               String isDeleted, String isSent, LocalDateTime startTime, LocalDateTime endTime,
                               Integer currentPage, Integer pageSize);

    /**
     * 获取未发送的通知
     *
     * @param limit 限制数量
     * @return 未发送通知列表
     */
    List<Inform> getUnsentInforms(Integer limit);

    /**
     * 获取已发送的通知
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param limit 限制数量
     * @return 已发送通知列表
     */
    List<Inform> getSentInforms(LocalDateTime startTime, LocalDateTime endTime, Integer limit);

    // =================== 业务功能 ===================

    /**
     * 发送通知
     *
     * @param id 通知ID
     * @return 是否成功
     */
    boolean sendInform(Long id);

    /**
     * 批量发送通知
     *
     * @param ids 通知ID列表
     * @return 成功发送的数量
     */
    int batchSendInforms(List<Long> ids);

    /**
     * 创建并发送通知
     *
     * @param appName APP名称
     * @param typeRelation 类型关系
     * @param userType 用户类型
     * @param notificationContent 通知内容
     * @return 是否成功
     */
    boolean createAndSendInform(String appName, String typeRelation, String userType, String notificationContent);

    // =================== 点赞评论通知 ===================

    /**
     * 发送点赞通知
     *
     * @param likerId 点赞者ID
     * @param likerName 点赞者昵称
     * @param targetUserId 被点赞内容作者ID
     * @param targetType 目标类型（CONTENT、COMMENT、DYNAMIC）
     * @param targetId 目标ID
     * @param targetTitle 目标标题
     * @return 是否成功
     */
    boolean sendLikeNotification(Long likerId, String likerName, Long targetUserId, 
                                String targetType, Long targetId, String targetTitle);

    /**
     * 发送评论通知
     *
     * @param commenterId 评论者ID
     * @param commenterName 评论者昵称
     * @param targetUserId 被评论内容作者ID
     * @param targetType 目标类型（CONTENT、DYNAMIC）
     * @param targetId 目标ID
     * @param targetTitle 目标标题
     * @param commentContent 评论内容
     * @return 是否成功
     */
    boolean sendCommentNotification(Long commenterId, String commenterName, Long targetUserId,
                                   String targetType, Long targetId, String targetTitle, String commentContent);

    /**
     * 发送回复评论通知
     *
     * @param replierId 回复者ID
     * @param replierName 回复者昵称
     * @param targetUserId 被回复评论作者ID
     * @param targetType 目标类型（CONTENT、DYNAMIC）
     * @param targetId 目标ID
     * @param targetTitle 目标标题
     * @param commentContent 回复内容
     * @return 是否成功
     */
    boolean sendReplyNotification(Long replierId, String replierName, Long targetUserId,
                                 String targetType, Long targetId, String targetTitle, String commentContent);

    // =================== 统计功能 ===================

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
    Long countInforms(String appName, String typeRelation, String userType, String isDeleted, String isSent);

    /**
     * 获取通知统计信息
     *
     * @return 统计信息Map
     */
    java.util.Map<String, Object> getInformStatistics();
}
