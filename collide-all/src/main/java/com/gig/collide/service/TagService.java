package com.gig.collide.service;


import com.gig.collide.domain.Tag;

import java.util.List;
import java.util.Map;

/**
 * 标签服务接口 - 严格对应TagMapper
 * 基于TagMapper的所有方法，提供标签相关的业务逻辑
 *
 * @author GIG Team
 * @version 3.0.0
 */
public interface TagService {

    // =================== 基础CRUD操作（继承自BaseMapper） ===================

    /**
     * 创建标签
     */
    Tag createTag(Tag tag);

    /**
     * 更新标签
     */
    Tag updateTag(Tag tag);

    /**
     * 根据ID删除标签
     */
    boolean deleteTagById(Long tagId);

    /**
     * 根据ID查询标签
     */
    Tag getTagById(Long tagId);

    /**
     * 查询所有标签
     */
    List<Tag> getAllTags();

    // =================== 核心查询方法（对应Mapper自定义方法） ===================

    /**
     * 根据类型查询标签列表
     * 对应Mapper: selectByTagType
     */
    List<Tag> selectByTagType(String tagType);

    /**
     * 按名称模糊搜索标签（全文搜索）
     * 对应Mapper: searchByName
     */
    List<Tag> searchByName(String keyword, Integer limit);

    /**
     * 按名称精确搜索标签（大小写不敏感）
     * 对应Mapper: searchByNameExact
     */
    List<Tag> searchByNameExact(String keyword, Integer limit);

    /**
     * 获取热门标签（按使用次数排序）
     * 对应Mapper: selectHotTags
     */
    List<Tag> selectHotTags(Integer limit);

    /**
     * 搜索标签
     * 对应Mapper: searchTags
     */
    List<Tag> searchTags(String keyword);

    /**
     * 根据分类查询标签
     * 对应Mapper: selectByCategoryId
     */
    List<Tag> selectByCategoryId(Long categoryId);

    /**
     * 根据状态查询标签
     * 对应Mapper: selectByStatus
     */
    List<Tag> selectByStatus(String status);

    // =================== 统计和计数方法 ===================

    /**
     * 检查标签名称是否存在
     * 对应Mapper: countByNameAndType
     */
    boolean existsByNameAndType(String name, String tagType);

    /**
     * 检查标签名称是否存在
     * 对应Mapper: countByTagName
     */
    boolean existsByTagName(String tagName);

    /**
     * 获取标签使用统计（性能优化）
     * 对应Mapper: getTagUsageStats
     */
    List<Map<String, Object>> getTagUsageStats(String tagType, Integer limit);

    /**
     * 统计标签数量（按类型）
     * 对应Mapper: countByTagType
     */
    Long countByTagType(String tagType);

    /**
     * 统计标签数量（按状态）
     * 对应Mapper: countByStatus
     */
    Long countByStatus(String status);

    /**
     * 获取标签统计信息
     * 对应Mapper: getTagStatistics
     */
    Map<String, Object> getTagStatistics();

    /**
     * 批量获取标签基本信息（覆盖索引优化）
     * 对应Mapper: selectTagSummary
     */
    List<Map<String, Object>> selectTagSummary(List<Long> tagIds);

    // =================== 更新操作方法 ===================

    /**
     * 增加标签使用次数
     * 对应Mapper: increaseUsageCount
     */
    boolean increaseUsageCount(Long tagId);

    /**
     * 增加标签使用次数（别名）
     * 对应Mapper: incrementUsageCount
     */
    boolean incrementUsageCount(Long tagId);

    /**
     * 减少标签使用次数
     * 对应Mapper: decreaseUsageCount
     */
    boolean decreaseUsageCount(Long tagId);

    /**
     * 减少标签使用次数（别名）
     * 对应Mapper: decrementUsageCount
     */
    boolean decrementUsageCount(Long tagId);

    /**
     * 批量更新标签状态
     * 对应Mapper: batchUpdateStatus
     */
    int batchUpdateStatus(List<Long> tagIds, String status);

    /**
     * 更新标签状态
     * 对应Mapper: updateTagStatus
     */
    boolean updateTagStatus(Long tagId, String status);

    // =================== 业务逻辑方法 ===================

    /**
     * 创建标签（带唯一性检查）
     */
    Tag createTagSafely(String name, String tagType, String description, Long categoryId);

    /**
     * 创建标签（带唯一性检查，传入Tag对象）
     */
    Tag createTagSafely(Tag tag);

    /**
     * 激活标签
     */
    boolean activateTag(Long tagId);

    /**
     * 停用标签
     */
    boolean deactivateTag(Long tagId);

    /**
     * 获取分类下的活跃标签
     */
    List<Tag> getActiveTags(Long categoryId);

    /**
     * 获取所有活跃标签
     */
    List<Tag> getActiveTags();

    /**
     * 根据类型获取活跃标签
     */
    List<Tag> getActiveTagsByType(String tagType);

    /**
     * 获取热门标签
     */
    List<Tag> getPopularTags(Integer limit);

    /**
     * 根据类型获取热门标签
     */
    List<Tag> getPopularTagsByType(String tagType, Integer limit);

    /**
     * 搜索活跃标签
     */
    List<Tag> searchActiveTags(String keyword);

    /**
     * 根据类型搜索标签
     */
    List<Tag> searchTagsByType(String keyword, String tagType);

    /**
     * 根据使用次数范围查询标签
     */
    List<Tag> getTagsByUsageRange(Long minUsage, Long maxUsage);

    /**
     * 根据使用次数范围和类型查询标签
     */
    List<Tag> getTagsByUsageRangeAndType(Long minUsage, Long maxUsage, String tagType);

    /**
     * 获取标签使用统计
     */
    List<Map<String, Object>> getTagUsageStatistics();

    /**
     * 根据类型获取标签使用统计
     */
    List<Map<String, Object>> getTagUsageStatisticsByType(String tagType);

    /**
     * 批量删除标签
     */
    int batchDeleteTags(List<Long> tagIds);

    /**
     * 根据ID列表查询标签
     */
    List<Tag> getTagsByIds(List<Long> tagIds);

    /**
     * 根据ID列表查询活跃标签
     */
    List<Tag> getActiveTagsByIds(List<Long> tagIds);

    /**
     * 搜索标签（智能搜索，先精确后模糊）
     */
    List<Tag> intelligentSearch(String keyword, Integer limit);

    // =================== Controller专用方法 ===================

    /**
     * 标签列表查询（Controller专用）
     * 支持多种条件查询和分页，返回Result包装的分页响应
     *
     * @param tagType 标签类型
     * @param status 标签状态
     * @param keyword 关键词搜索
     * @param isHot 是否热门
     * @param minUsageCount 最小使用次数
     * @param orderBy 排序字段
     * @param orderDirection 排序方向
     * @param currentPage 当前页码
     * @param pageSize 页面大小
     * @return 分页响应结果
     */
    com.gig.collide.Apientry.api.common.response.Result<com.gig.collide.Apientry.api.common.response.PageResponse<com.gig.collide.Apientry.api.tag.response.TagResponse>> listTagsForController(
            String tagType, String status, String keyword, Boolean isHot, Long minUsageCount,
            String orderBy, String orderDirection, Integer currentPage, Integer pageSize);
} 