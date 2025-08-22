package com.gig.collide.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gig.collide.domain.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 标签Mapper接口 - 简洁版
 *
 * @author GIG Team
 * @version 2.0.0
 */
@Mapper
public interface TagMapper extends BaseMapper<Tag> {

    /**
     * 根据类型查询标签列表
     */
    List<Tag> selectByTagType(@Param("tagType") String tagType);

    /**
     * 按名称模糊搜索标签（全文搜索）
     */
    List<Tag> searchByName(@Param("keyword") String keyword, @Param("limit") Integer limit);

    /**
     * 按名称精确搜索标签（大小写不敏感）
     */
    List<Tag> searchByNameExact(@Param("keyword") String keyword, @Param("limit") Integer limit);

    /**
     * 获取热门标签（按使用次数排序）
     */
    List<Tag> selectHotTags(@Param("limit") Integer limit);

    /**
     * 增加标签使用次数
     */
    int increaseUsageCount(@Param("tagId") Long tagId);

    /**
     * 检查标签名称是否存在
     */
    int countByNameAndType(@Param("name") String name, @Param("tagType") String tagType);

    /**
     * 批量更新标签状态
     */
    int batchUpdateStatus(@Param("tagIds") List<Long> tagIds, @Param("status") String status);

    /**
     * 减少标签使用次数
     */
    int decreaseUsageCount(@Param("tagId") Long tagId);

    /**
     * 根据分类查询标签
     */
    List<Tag> selectByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 获取标签使用统计（只返回计数，性能优化）
     */
    List<Map<String, Object>> getTagUsageStats(@Param("tagType") String tagType, @Param("limit") Integer limit);

    /**
     * 批量获取标签基本信息（覆盖索引优化）
     */
    List<Map<String, Object>> selectTagSummary(@Param("tagIds") List<Long> tagIds);

    /**
     * 检查标签名称是否存在
     */
    int countByTagName(@Param("tagName") String tagName);

    /**
     * 统计标签数量（按类型）
     */
    Long countByTagType(@Param("tagType") String tagType);

    /**
     * 统计标签数量（按状态）
     */
    Long countByStatus(@Param("status") String status);

    /**
     * 获取标签统计信息
     */
    Map<String, Object> getTagStatistics();

    /**
     * 更新标签状态
     */
    int updateTagStatus(@Param("tagId") Long tagId, @Param("status") String status);

    /**
     * 增加标签使用次数（别名）
     */
    int incrementUsageCount(@Param("tagId") Long tagId);

    /**
     * 减少标签使用次数（别名）
     */
    int decrementUsageCount(@Param("tagId") Long tagId);

    /**
     * 根据分类查询活跃标签
     */
    List<Tag> selectActiveTagsByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 根据类型查询活跃标签
     */
    List<Tag> selectActiveTagsByType(@Param("tagType") String tagType);

    /**
     * 根据类型查询热门标签
     */
    List<Tag> selectPopularTagsByType(@Param("tagType") String tagType, @Param("limit") Integer limit);

    /**
     * 搜索活跃标签
     */
    List<Tag> searchActiveTags(@Param("keyword") String keyword);

    /**
     * 根据类型搜索标签
     */
    List<Tag> searchTagsByType(@Param("keyword") String keyword, @Param("tagType") String tagType);

    /**
     * 根据使用次数范围查询标签
     */
    List<Tag> selectTagsByUsageRange(@Param("minUsage") Long minUsage, @Param("maxUsage") Long maxUsage);

    /**
     * 根据使用次数范围和类型查询标签
     */
    List<Tag> selectTagsByUsageRangeAndType(@Param("minUsage") Long minUsage, @Param("maxUsage") Long maxUsage, @Param("tagType") String tagType);

    /**
     * 获取标签使用统计
     */
    List<Map<String, Object>> getTagUsageStatistics();

    /**
     * 根据类型获取标签使用统计
     */
    List<Map<String, Object>> getTagUsageStatisticsByType(@Param("tagType") String tagType);

    /**
     * 批量删除标签
     */
    int batchDeleteTags(@Param("tagIds") List<Long> tagIds);

    /**
     * 根据ID列表查询标签
     */
    List<Tag> selectTagsByIds(@Param("tagIds") List<Long> tagIds);

    /**
     * 根据ID列表查询活跃标签
     */
    List<Tag> selectActiveTagsByIds(@Param("tagIds") List<Long> tagIds);

    /**
     * 搜索标签
     */
    List<Tag> searchTags(@Param("keyword") String keyword);
} 