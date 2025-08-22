package com.gig.collide.service.Impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gig.collide.domain.SocialDynamic;
import com.gig.collide.domain.Like;
import com.gig.collide.mapper.SocialDynamicMapper;
import com.gig.collide.service.SocialDynamicService;
import com.gig.collide.service.LikeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 社交动态服务实现类 - 严格对应接口定义
 * 实现SocialDynamicService接口的全部15个方法
 *
 * @author GIG Team
 * @version 3.0.0
 */
@Slf4j
@Service
public class SocialDynamicServiceImpl implements SocialDynamicService {

    @Autowired
    private SocialDynamicMapper socialDynamicMapper;
    
    @Autowired
    private LikeService likeService;

    // =================== 动态创建方法（对应Mapper1个） ===================

    @Override
    @Transactional
    public SocialDynamic createDynamic(SocialDynamic dynamic) {
        log.debug("创建动态: 用户ID={}, 内容={}", dynamic.getUserId(), dynamic.getContent());

        // 设置默认值
        if (dynamic.getLikeCount() == null) {
            dynamic.setLikeCount(0L);
        }
        if (dynamic.getCommentCount() == null) {
            dynamic.setCommentCount(0L);
        }
        if (dynamic.getShareCount() == null) {
            dynamic.setShareCount(0L);
        }
        if (!StringUtils.hasText(dynamic.getStatus())) {
            dynamic.setStatus("normal");
        }

        // 使用Mapper专门的创建方法
        int result = socialDynamicMapper.insertDynamic(dynamic);
        if (result > 0) {
            log.info("动态创建成功: ID={}", dynamic.getId());
            return dynamic;
        } else {
            throw new RuntimeException("动态创建失败");
        }
    }

    @Override
    @Transactional
    public int batchCreateDynamics(List<SocialDynamic> dynamics) {
        log.debug("批量创建动态: 数量={}", dynamics.size());

        // 批量设置默认值
        dynamics.forEach(dynamic -> {
            if (dynamic.getLikeCount() == null) {
                dynamic.setLikeCount(0L);
            }
            if (dynamic.getCommentCount() == null) {
                dynamic.setCommentCount(0L);
            }
            if (dynamic.getShareCount() == null) {
                dynamic.setShareCount(0L);
            }
            if (!StringUtils.hasText(dynamic.getStatus())) {
                dynamic.setStatus("normal");
            }
        });

        int result = socialDynamicMapper.batchInsertDynamics(dynamics);
        log.info("批量创建动态完成: 成功创建{}条", result);
        return result;
    }

    @Override
    @Transactional
    public SocialDynamic createShareDynamic(SocialDynamic dynamic) {
        log.debug("创建分享动态: 用户ID={}, 分享目标ID={}", dynamic.getUserId(), dynamic.getShareTargetId());

        // 验证分享目标是否存在
        if (dynamic.getShareTargetId() != null && "dynamic".equals(dynamic.getShareTargetType())) {
            SocialDynamic originalDynamic = socialDynamicMapper.selectById(dynamic.getShareTargetId());
            if (originalDynamic == null) {
                throw new RuntimeException("分享的原动态不存在");
            }
            // 更新原动态的分享数
            socialDynamicMapper.increaseShareCount(dynamic.getShareTargetId());
        }

        // 设置分享动态的默认值
        dynamic.setDynamicType("share");
        if (!StringUtils.hasText(dynamic.getStatus())) {
            dynamic.setStatus("normal");
        }
        if (dynamic.getLikeCount() == null) {
            dynamic.setLikeCount(0L);
        }
        if (dynamic.getCommentCount() == null) {
            dynamic.setCommentCount(0L);
        }
        if (dynamic.getShareCount() == null) {
            dynamic.setShareCount(0L);
        }

        int result = socialDynamicMapper.insertShareDynamic(dynamic);
        if (result > 0) {
            log.info("分享动态创建成功: ID={}", dynamic.getId());
            return dynamic;
        } else {
            throw new RuntimeException("分享动态创建失败");
        }
    }

    // =================== 核心查询方法（对应Mapper的7个） ===================

    @Override
    public SocialDynamic selectById(Long id) {
        log.debug("根据ID查询动态: ID={}", id);
        return socialDynamicMapper.selectById(id);
    }

    @Override
    @Transactional
    public int updateById(SocialDynamic dynamic) {
        log.debug("更新动态: ID={}", dynamic.getId());
        int result = socialDynamicMapper.updateById(dynamic);
        if (result > 0) {
            log.info("动态更新成功: ID={}", dynamic.getId());
        }
        return result;
    }

    @Override
    public IPage<SocialDynamic> selectByUserId(Page<SocialDynamic> page, Long userId, String status, String dynamicType) {
        log.debug("根据用户ID查询动态: 用户ID={}, 状态={}, 类型={}", userId, status, dynamicType);
        return socialDynamicMapper.selectByUserId(page, userId, status, dynamicType);
    }

    @Override
    public IPage<SocialDynamic> selectByDynamicType(Page<SocialDynamic> page, String dynamicType, String status) {
        log.debug("根据动态类型查询: 类型={}, 状态={}", dynamicType, status);
        return socialDynamicMapper.selectByDynamicType(page, dynamicType, status);
    }

    @Override
    public IPage<SocialDynamic> selectByStatus(Page<SocialDynamic> page, String status) {
        log.debug("根据状态查询动态: 状态={}", status);
        return socialDynamicMapper.selectByStatus(page, status);
    }

    @Override
    public IPage<SocialDynamic> selectFollowingDynamics(Page<SocialDynamic> page, List<Long> userIds, String status) {
        log.debug("查询关注用户动态: 用户数量={}, 状态={}", userIds.size(), status);
        return socialDynamicMapper.selectFollowingDynamics(page, userIds, status);
    }

    @Override
    public IPage<SocialDynamic> searchByContent(Page<SocialDynamic> page, String keyword, String status) {
        log.debug("搜索动态内容: 关键词={}, 状态={}", keyword, status);
        return socialDynamicMapper.searchByContent(page, keyword, status);
    }

    @Override
    public IPage<SocialDynamic> selectHotDynamics(Page<SocialDynamic> page, String status, String dynamicType) {
        log.debug("查询热门动态: 状态={}, 类型={}", status, dynamicType);
        return socialDynamicMapper.selectHotDynamics(page, status, dynamicType);
    }

    @Override
    public IPage<SocialDynamic> selectByShareTarget(Page<SocialDynamic> page, String shareTargetType, Long shareTargetId, String status) {
        log.debug("根据分享目标查询: 类型={}, 目标ID={}, 状态={}", shareTargetType, shareTargetId, status);
        return socialDynamicMapper.selectByShareTarget(page, shareTargetType, shareTargetId, status);
    }

    // =================== 统计计数方法（对应Mapper的3个） ===================

    @Override
    public Long countByUserId(Long userId, String status, String dynamicType) {
        log.debug("统计用户动态数量: 用户ID={}, 状态={}, 类型={}", userId, status, dynamicType);
        return socialDynamicMapper.countByUserId(userId, status, dynamicType);
    }

    @Override
    public Long countByDynamicType(String dynamicType, String status) {
        log.debug("统计动态类型数量: 类型={}, 状态={}", dynamicType, status);
        return socialDynamicMapper.countByDynamicType(dynamicType, status);
    }

    @Override
    public Long countByTimeRange(LocalDateTime startTime, LocalDateTime endTime, String status) {
        log.debug("统计时间范围内动态数量: 开始时间={}, 结束时间={}, 状态={}", startTime, endTime, status);
        return socialDynamicMapper.countByTimeRange(startTime, endTime, status);
    }

    // =================== 互动统计更新（对应Mapper的5个） ===================

    @Override
    @Transactional
    public int increaseLikeCount(Long dynamicId, Long operatorId) {
        log.info("增加点赞数: 动态ID={}, 操作者ID={}", dynamicId, operatorId);
        
        // 检查是否已经点赞过
        boolean alreadyLiked = likeService.checkLikeStatus(operatorId, "DYNAMIC", dynamicId);
        if (alreadyLiked) {
            log.warn("用户已点赞该动态，忽略重复点赞: 动态ID={}, 操作者ID={}", dynamicId, operatorId);
            return 0; // 返回0表示重复点赞
        }
        
        // 先创建点赞记录
        Like like = new Like();
        like.setUserId(operatorId);
        like.setLikeType("DYNAMIC");
        like.setTargetId(dynamicId);
        like.setStatus("active");
        
        // 获取动态信息用于冗余字段
        SocialDynamic dynamic = socialDynamicMapper.selectById(dynamicId);
        if (dynamic != null) {
            like.setTargetTitle(dynamic.getTitle());
            like.setTargetAuthorId(dynamic.getUserId());
        }
        
        // 添加点赞记录
        Like savedLike = likeService.addLike(like);
        if (savedLike == null) {
            log.error("创建点赞记录失败: 动态ID={}, 操作者ID={}", dynamicId, operatorId);
            throw new RuntimeException("创建点赞记录失败");
        }
        
        // 增加动态表中的点赞数
        int result = socialDynamicMapper.increaseLikeCount(dynamicId);
        if (result > 0) {
            log.info("点赞数增加成功: 动态ID={}, 操作者ID={}, 点赞记录ID={}", dynamicId, operatorId, savedLike.getId());
            return result;
        } else {
            log.error("增加动态点赞数失败: 动态ID={}, 操作者ID={}", dynamicId, operatorId);
            throw new RuntimeException("增加动态点赞数失败");
        }
    }

    @Override
    @Transactional
    public int decreaseLikeCount(Long dynamicId, Long operatorId) {
        log.debug("减少点赞数: 动态ID={}, 操作者ID={}", dynamicId, operatorId);
        
        // 检查是否已经点赞过
        boolean alreadyLiked = likeService.checkLikeStatus(operatorId, "DYNAMIC", dynamicId);
        if (!alreadyLiked) {
            log.warn("用户未点赞该动态，无法取消点赞: 动态ID={}, 操作者ID={}", dynamicId, operatorId);
            return 0; // 返回0表示未点赞，无法取消
        }
        
        // 取消点赞记录
        boolean likeDeleted = likeService.cancelLike(operatorId, "DYNAMIC", dynamicId);
        if (!likeDeleted) {
            log.error("取消点赞记录失败: 动态ID={}, 操作者ID={}", dynamicId, operatorId);
            throw new RuntimeException("取消点赞记录失败");
        }
        
        // 减少动态表中的点赞数
        int result = socialDynamicMapper.decreaseLikeCount(dynamicId);
        if (result > 0) {
            log.info("点赞数减少成功: 动态ID={}, 操作者ID={}", dynamicId, operatorId);
            return result;
        } else {
            log.error("减少动态点赞数失败: 动态ID={}, 操作者ID={}", dynamicId, operatorId);
            throw new RuntimeException("减少动态点赞数失败");
        }
    }

    @Override
    @Transactional
    public int increaseCommentCount(Long dynamicId, Long operatorId) {
        log.debug("增加评论数: 动态ID={}, 操作者ID={}", dynamicId, operatorId);
        int result = socialDynamicMapper.increaseCommentCount(dynamicId);
        if (result > 0) {
            log.info("评论数增加成功: 动态ID={}, 操作者ID={}", dynamicId, operatorId);
        }
        return result;
    }

    @Override
    @Transactional
    public int increaseShareCount(Long dynamicId, Long operatorId) {
        log.debug("增加分享数: 动态ID={}, 操作者ID={}", dynamicId, operatorId);
        int result = socialDynamicMapper.increaseShareCount(dynamicId);
        if (result > 0) {
            log.info("分享数增加成功: 动态ID={}, 操作者ID={}", dynamicId, operatorId);
        }
        return result;
    }

    @Override
    @Transactional
    public int updateStatistics(Long dynamicId, Long likeCount, Long commentCount, Long shareCount, Long operatorId) {
        log.debug("更新统计数据: 动态ID={}, 点赞数={}, 评论数={}, 分享数={}, 操作者ID={}", 
                 dynamicId, likeCount, commentCount, shareCount, operatorId);
        int result = socialDynamicMapper.updateStatistics(dynamicId, likeCount, commentCount, shareCount);
        if (result > 0) {
            log.info("统计数据更新成功: 动态ID={}, 操作者ID={}", dynamicId, operatorId);
        }
        return result;
    }

    // =================== 状态管理（对应Mapper的2个） ===================

    @Override
    @Transactional
    public int updateStatus(Long dynamicId, String status, Long operatorId) {
        log.debug("更新动态状态: 动态ID={}, 状态={}, 操作者ID={}", dynamicId, status, operatorId);
        int result = socialDynamicMapper.updateStatus(dynamicId, status);
        if (result > 0) {
            log.info("动态状态更新成功: 动态ID={}, 状态={}, 操作者ID={}", dynamicId, status, operatorId);
        }
        return result;
    }

    @Override
    @Transactional
    public int batchUpdateStatus(List<Long> dynamicIds, String status, Long operatorId) {
        log.debug("批量更新动态状态: 动态数量={}, 状态={}, 操作者ID={}", dynamicIds.size(), status, operatorId);
        int result = socialDynamicMapper.batchUpdateStatus(dynamicIds, status);
        if (result > 0) {
            log.info("批量状态更新成功: 更新数量={}, 操作者ID={}", result, operatorId);
        }
        return result;
    }

    // =================== 用户信息同步（对应Mapper的1个） ===================

    @Override
    @Transactional
    public int updateUserInfo(Long userId, String userNickname, String userAvatar, Long operatorId) {
        log.debug("更新用户信息: 用户ID={}, 昵称={}, 操作者ID={}", userId, userNickname, operatorId);
        int result = socialDynamicMapper.updateUserInfo(userId, userNickname, userAvatar);
        if (result > 0) {
            log.info("用户信息更新成功: 用户ID={}, 操作者ID={}", userId, operatorId);
        }
        return result;
    }

    // =================== 数据清理（对应Mapper的1个） ===================

    @Override
    @Transactional
    public int deleteByStatusAndTime(String status, LocalDateTime beforeTime, Integer limit, Long operatorId) {
        log.debug("删除历史动态: 状态={}, 时间={}, 限制数量={}, 操作者ID={}", status, beforeTime, limit, operatorId);
        int result = socialDynamicMapper.deleteByStatusAndTime(status, beforeTime, limit);
        if (result > 0) {
            log.info("历史动态删除成功: 删除数量={}, 操作者ID={}", result, operatorId);
        }
        return result;
    }

    // =================== 特殊查询（对应Mapper的3个） ===================

    @Override
    public List<SocialDynamic> selectLatestDynamics(Integer limit, String status) {
        log.debug("查询最新动态: 限制数量={}, 状态={}", limit, status);
        return socialDynamicMapper.selectLatestDynamics(limit, status);
    }

    @Override
    public List<SocialDynamic> selectUserLatestDynamics(Long userId, Integer limit, String status) {
        log.debug("查询用户最新动态: 用户ID={}, 限制数量={}, 状态={}", userId, limit, status);
        return socialDynamicMapper.selectUserLatestDynamics(userId, limit, status);
    }

    @Override
    public List<SocialDynamic> selectShareDynamics(String shareTargetType, Integer limit, String status) {
        log.debug("查询分享动态列表: 目标类型={}, 限制数量={}, 状态={}", shareTargetType, limit, status);
        return socialDynamicMapper.selectShareDynamics(shareTargetType, limit, status);
    }
}

