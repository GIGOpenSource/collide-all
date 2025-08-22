package com.gig.collide.service.Impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gig.collide.domain.Inform;
import com.gig.collide.mapper.InformMapper;
import com.gig.collide.service.InformService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统通知服务实现类
 * 基于t_inform表，处理系统通知的发送和管理
 *
 * @author GIG Team
 * @version 2.0.0
 * @since 2024-01-31
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InformServiceImpl implements InformService {

    private final InformMapper informMapper;

    // =================== 基础CRUD ===================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Inform createInform(Inform inform) {
        log.info("创建通知: appName={}, typeRelation={}, userType={}", 
                inform.getAppName(), inform.getTypeRelation(), inform.getUserType());

        if (inform == null) {
            throw new IllegalArgumentException("通知对象不能为空");
        }

        // 设置默认值
        if (inform.getIsDeleted() == null) {
            inform.setIsDeleted("N");
        }
        if (inform.getIsSent() == null) {
            inform.setIsSent("N");
        }

        int result = informMapper.insert(inform);
        if (result <= 0) {
            throw new RuntimeException("创建通知失败");
        }

        log.info("通知创建成功: id={}", inform.getId());
        return inform;
    }

    @Override
    public Inform getInformById(Long id) {
        log.debug("根据ID获取通知: id={}", id);

        if (id == null) {
            return null;
        }

        return informMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateInform(Inform inform) {
        log.info("更新通知: id={}", inform.getId());

        if (inform == null || inform.getId() == null) {
            throw new IllegalArgumentException("通知ID不能为空");
        }

        int result = informMapper.updateById(inform);
        boolean success = result > 0;

        if (success) {
            log.info("通知更新成功: id={}", inform.getId());
        } else {
            log.warn("通知更新失败: id={}", inform.getId());
        }

        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteInform(Long id) {
        log.info("删除通知: id={}", id);

        if (id == null) {
            throw new IllegalArgumentException("通知ID不能为空");
        }

        Inform inform = new Inform();
        inform.setId(id);
        inform.setIsDeleted("Y");

        int result = informMapper.updateById(inform);
        boolean success = result > 0;

        if (success) {
            log.info("通知删除成功: id={}", id);
        } else {
            log.warn("通知删除失败: id={}", id);
        }

        return success;
    }

    // =================== 查询功能 ===================

    @Override
    public IPage<Inform> queryInforms(String appName, String typeRelation, String userType,
                                      String isDeleted, String isSent, LocalDateTime startTime, LocalDateTime endTime,
                                      Integer currentPage, Integer pageSize) {
        log.debug("分页查询通知: appName={}, typeRelation={}, userType={}, page={}/{}", 
                appName, typeRelation, userType, currentPage, pageSize);

        // 参数验证和默认值设置
        currentPage = currentPage == null ? 1 : currentPage;
        pageSize = pageSize == null ? 20 : pageSize;

        Page<Inform> page = new Page<>(currentPage, pageSize);
        return informMapper.queryInforms(page, appName, typeRelation, userType, isDeleted, isSent, startTime, endTime);
    }

    @Override
    public List<Inform> getUnsentInforms(Integer limit) {
        log.debug("获取未发送通知: limit={}", limit);

        limit = limit == null ? 100 : limit;
        return informMapper.getUnsentInforms(limit);
    }

    @Override
    public List<Inform> getSentInforms(LocalDateTime startTime, LocalDateTime endTime, Integer limit) {
        log.debug("获取已发送通知: startTime={}, endTime={}, limit={}", startTime, endTime, limit);

        limit = limit == null ? 100 : limit;
        return informMapper.getSentInforms(startTime, endTime, limit);
    }

    // =================== 业务功能 ===================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean sendInform(Long id) {
        log.info("发送通知: id={}", id);

        if (id == null) {
            throw new IllegalArgumentException("通知ID不能为空");
        }

        Inform inform = getInformById(id);
        if (inform == null) {
            log.warn("通知不存在: id={}", id);
            return false;
        }

        if (inform.isSent()) {
            log.warn("通知已发送: id={}", id);
            return true;
        }

        // 标记为已发送
        inform.markAsSent();
        boolean success = updateInform(inform);

        if (success) {
            log.info("通知发送成功: id={}", id);
            // TODO: 这里可以集成实际的推送服务（如极光推送、个推等）
        } else {
            log.error("通知发送失败: id={}", id);
        }

        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchSendInforms(List<Long> ids) {
        log.info("批量发送通知: count={}", ids.size());

        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        LocalDateTime sendTime = LocalDateTime.now();
        int result = informMapper.batchUpdateSendStatus(ids, "Y", sendTime);

        if (result > 0) {
            log.info("批量发送通知成功: count={}", result);
            // TODO: 这里可以集成实际的推送服务
        } else {
            log.warn("批量发送通知失败: count=0");
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createAndSendInform(String appName, String typeRelation, String userType, String notificationContent) {
        log.info("创建并发送通知: appName={}, typeRelation={}, userType={}", appName, typeRelation, userType);

        if (!StringUtils.hasText(appName) || !StringUtils.hasText(typeRelation) || 
            !StringUtils.hasText(userType) || !StringUtils.hasText(notificationContent)) {
            throw new IllegalArgumentException("参数不能为空");
        }

        // 创建通知
        Inform inform = Inform.create(appName, typeRelation, userType, notificationContent);
        inform = createInform(inform);

        // 发送通知
        return sendInform(inform.getId());
    }

    // =================== 点赞评论通知 ===================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean sendLikeNotification(Long likerId, String likerName, Long targetUserId, 
                                       String targetType, Long targetId, String targetTitle) {
        log.info("发送点赞通知: likerId={}, targetUserId={}, targetType={}, targetId={}", 
                likerId, targetUserId, targetType, targetId);

        if (likerId == null || targetUserId == null || !StringUtils.hasText(targetType)) {
            log.warn("点赞通知参数不完整");
            return false;
        }

        // 自己给自己点赞不发送通知
        if (likerId.equals(targetUserId)) {
            log.debug("自己给自己点赞，不发送通知");
            return true;
        }

        String notificationContent = String.format("用户 %s 点赞了你的%s", 
                likerName != null ? likerName : "用户" + likerId, 
                getTargetTypeDescription(targetType));

        if (StringUtils.hasText(targetTitle)) {
            notificationContent += String.format("《%s》", targetTitle);
        }

        return createAndSendInform("collide-app", "LIKE", "user", notificationContent);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean sendCommentNotification(Long commenterId, String commenterName, Long targetUserId,
                                          String targetType, Long targetId, String targetTitle, String commentContent) {
        log.info("发送评论通知: commenterId={}, targetUserId={}, targetType={}, targetId={}", 
                commenterId, targetUserId, targetType, targetId);

        if (commenterId == null || targetUserId == null || !StringUtils.hasText(targetType)) {
            log.warn("评论通知参数不完整");
            return false;
        }

        // 自己给自己评论不发送通知
        if (commenterId.equals(targetUserId)) {
            log.debug("自己给自己评论，不发送通知");
            return true;
        }

        String notificationContent = String.format("用户 %s 评论了你的%s", 
                commenterName != null ? commenterName : "用户" + commenterId, 
                getTargetTypeDescription(targetType));

        if (StringUtils.hasText(targetTitle)) {
            notificationContent += String.format("《%s》", targetTitle);
        }

        if (StringUtils.hasText(commentContent)) {
            // 截取评论内容前50个字符
            String shortContent = commentContent.length() > 50 ? 
                    commentContent.substring(0, 50) + "..." : commentContent;
            notificationContent += String.format("：%s", shortContent);
        }

        return createAndSendInform("collide-app", "COMMENT", "user", notificationContent);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean sendReplyNotification(Long replierId, String replierName, Long targetUserId,
                                        String targetType, Long targetId, String targetTitle, String commentContent) {
        log.info("发送回复通知: replierId={}, targetUserId={}, targetType={}, targetId={}", 
                replierId, targetUserId, targetType, targetId);

        if (replierId == null || targetUserId == null || !StringUtils.hasText(targetType)) {
            log.warn("回复通知参数不完整");
            return false;
        }

        // 自己回复自己不发送通知
        if (replierId.equals(targetUserId)) {
            log.debug("自己回复自己，不发送通知");
            return true;
        }

        String notificationContent = String.format("用户 %s 回复了你在%s下的评论", 
                replierName != null ? replierName : "用户" + replierId, 
                getTargetTypeDescription(targetType));

        if (StringUtils.hasText(targetTitle)) {
            notificationContent += String.format("《%s》", targetTitle);
        }

        if (StringUtils.hasText(commentContent)) {
            // 截取回复内容前50个字符
            String shortContent = commentContent.length() > 50 ? 
                    commentContent.substring(0, 50) + "..." : commentContent;
            notificationContent += String.format("：%s", shortContent);
        }

        return createAndSendInform("collide-app", "REPLY", "user", notificationContent);
    }

    // =================== 统计功能 ===================

    @Override
    public Long countInforms(String appName, String typeRelation, String userType, String isDeleted, String isSent) {
        log.debug("统计通知数量: appName={}, typeRelation={}, userType={}", appName, typeRelation, userType);

        return informMapper.countInforms(appName, typeRelation, userType, isDeleted, isSent);
    }

    @Override
    public Map<String, Object> getInformStatistics() {
        log.debug("获取通知统计信息");

        List<Map<String, Object>> statistics = informMapper.getInformStatistics();
        Map<String, Object> result = new HashMap<>();

        if (statistics != null && !statistics.isEmpty()) {
            for (Map<String, Object> stat : statistics) {
                result.putAll(stat);
            }
        }

        return result;
    }

    // =================== 私有方法 ===================

    /**
     * 获取目标类型描述
     */
    private String getTargetTypeDescription(String targetType) {
        switch (targetType) {
            case "CONTENT":
                return "内容";
            case "COMMENT":
                return "评论";
            case "DYNAMIC":
                return "动态";
            default:
                return "内容";
        }
    }
}
