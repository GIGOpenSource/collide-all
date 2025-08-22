package com.gig.collide.Apientry.api.follow;

import com.gig.collide.Apientry.api.common.response.PageResponse;
import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.Apientry.api.follow.request.FollowCreateRequest;
import com.gig.collide.Apientry.api.follow.request.FollowDeleteRequest;
import com.gig.collide.Apientry.api.follow.request.FollowQueryRequest;
import com.gig.collide.Apientry.api.follow.response.FollowResponse;


/**
 * 关注门面服务接口 - 简洁版
 * 基于follow-simple.sql的单表设计，实现核心关注功能
 * 
 * @author Collide
 * @version 2.0.0 (简洁版)
 * @since 2024-01-01
 */
public interface FollowFacadeService {
    
    /**
     * 关注用户
     * 支持关注者和被关注者信息冗余存储
     * 
     * @param request 关注请求
     * @return 关注结果
     */
    Result<FollowResponse> followUser(FollowCreateRequest request);
    
    /**
     * 取消关注
     * 将关注状态更新为cancelled
     * 
     * @param request 取消关注请求
     * @return 取消结果
     */
    Result<Void> unfollowUser(FollowDeleteRequest request);
    
    /**
     * 检查关注状态
     * 查询用户是否已关注目标用户
     * 
     * @param followerId 关注者ID
     * @param followeeId 被关注者ID
     * @return 关注状态
     */
    Result<Boolean> checkFollowStatus(Long followerId, Long followeeId);
    
    /**
     * 获取关注关系详情
     * 
     * @param followerId 关注者ID
     * @param followeeId 被关注者ID
     * @return 关注关系详情
     */
    Result<FollowResponse> getFollowRelation(Long followerId, Long followeeId);
    
    /**
     * 分页查询关注记录
     * 支持按关注者、被关注者、状态等条件查询
     * 
     * @param request 查询请求
     * @return 关注记录列表
     */
    Result<PageResponse<FollowResponse>> queryFollows(FollowQueryRequest request);
    
    /**
     * 获取用户的关注列表
     * 查询某用户关注的所有人
     * 
     * @param followerId 关注者ID
     * @param currentPage 页码
     * @param pageSize 页面大小
     * @return 关注列表
     */
    Result<PageResponse<FollowResponse>> getFollowing(Long followerId, Integer currentPage, Integer pageSize);
    
    /**
     * 获取用户的粉丝列表
     * 查询关注某用户的所有人
     * 
     * @param followeeId 被关注者ID
     * @param currentPage 页码
     * @param pageSize 页面大小
     * @return 粉丝列表
     */
    Result<PageResponse<FollowResponse>> getFollowers(Long followeeId, Integer currentPage, Integer pageSize);
    
    /**
     * 获取用户关注数量
     * 统计用户关注的人数
     * 
     * @param followerId 关注者ID
     * @return 关注数量
     */
    Result<Long> getFollowingCount(Long followerId);
    
    /**
     * 获取用户粉丝数量
     * 统计关注某用户的人数
     * 
     * @param followeeId 被关注者ID
     * @return 粉丝数量
     */
    Result<Long> getFollowersCount(Long followeeId);
    
    /**
     * 获取用户关注统计信息
     * 包含关注数和粉丝数
     * 
     * @param userId 用户ID
     * @return 统计信息Map (following_count、followers_count)
     */
    Result<java.util.Map<String, Object>> getFollowStatistics(Long userId);
    
    /**
     * 批量检查关注状态
     * 检查用户对多个目标用户的关注状态
     * 
     * @param followerId 关注者ID
     * @param followeeIds 被关注者ID列表
     * @return 关注状态Map (followeeId -> isFollowing)
     */
    Result<java.util.Map<Long, Boolean>> batchCheckFollowStatus(Long followerId, java.util.List<Long> followeeIds);
    
    /**
     * 获取互相关注的好友
     * 查询双向关注关系
     * 
     * @param userId 用户ID
     * @param currentPage 页码
     * @param pageSize 页面大小
     * @return 互关好友列表
     */
    Result<PageResponse<FollowResponse>> getMutualFollows(Long userId, Integer currentPage, Integer pageSize);
    
    /**
     * 清理已取消的关注记录
     * 物理删除cancelled状态的记录（可选功能）
     * 
     * @param days 保留天数
     * @return 清理数量
     */
    Result<Integer> cleanCancelledFollows(Integer days);
    
    /**
     * 根据昵称搜索关注关系
     * 根据关注者或被关注者昵称进行模糊搜索
     * 
     * @param followerId 关注者ID（可选）
     * @param followeeId 被关注者ID（可选）
     * @param nicknameKeyword 昵称关键词
     * @param currentPage 页码
     * @param pageSize 页面大小
     * @return 搜索结果
     */
    Result<PageResponse<FollowResponse>> searchByNickname(Long followerId, Long followeeId, String nicknameKeyword,
                                                          Integer currentPage, Integer pageSize);
    
    /**
     * 更新用户信息（冗余字段同步）
     * 当用户信息变更时，同步更新关注表中的冗余信息
     * 
     * @param userId 用户ID
     * @param nickname 新昵称
     * @param avatar 新头像
     * @return 更新成功的记录数
     */
    Result<Integer> updateUserInfo(Long userId, String nickname, String avatar);
    
    /**
     * 查询用户间的关注关系链
     * 检查两个用户之间的双向关注关系
     * 
     * @param userIdA 用户A ID
     * @param userIdB 用户B ID
     * @return 关注关系链列表
     */
    Result<java.util.List<FollowResponse>> getRelationChain(Long userIdA, Long userIdB);
    
    /**
     * 验证关注请求参数
     * 校验请求参数的有效性
     * 
     * @param request 关注请求
     * @return 验证结果信息
     */
    Result<String> validateFollowRequest(FollowCreateRequest request);
    
    /**
     * 检查是否可以关注
     * 检查业务规则是否允许关注
     * 
     * @param followerId 关注者ID
     * @param followeeId 被关注者ID
     * @return 检查结果信息
     */
    Result<String> checkCanFollow(Long followerId, Long followeeId);
    
    /**
     * 检查是否已经存在关注关系
     * 包括已取消的关注关系
     * 
     * @param followerId 关注者ID
     * @param followeeId 被关注者ID
     * @return 是否存在关注关系
     */
    Result<Boolean> existsFollowRelation(Long followerId, Long followeeId);
    
    /**
     * 重新激活已取消的关注关系
     * 将cancelled状态的关注重新设置为active
     * 
     * @param followerId 关注者ID
     * @param followeeId 被关注者ID
     * @return 是否成功重新激活
     */
    Result<Boolean> reactivateFollow(Long followerId, Long followeeId);
} 
