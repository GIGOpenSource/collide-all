package com.gig.collide.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gig.collide.domain.ContentTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 内容标签Mapper接口 - 简洁版
 *
 * @author GIG Team
 * @version 2.0.0
 */
@Mapper
public interface ContentTagMapper extends BaseMapper<ContentTag> {

    /**
     * 内容标签关联列表查询（Controller专用）
     * 支持多种条件查询和分页
     * 
     * @param page 分页对象
     * @param contentId 内容ID
     * @param tagId 标签ID
     * @param relationType 关联类型（预留参数）
     * @param status 关联状态（预留参数）
     * @param keyword 关键词搜索
     * @param orderBy 排序字段
     * @param orderDirection 排序方向
     * @return 分页内容标签关联列表
     */
    IPage<ContentTag> selectContentTagList(IPage<ContentTag> page,
                                         @Param("contentId") Long contentId,
                                         @Param("tagId") Long tagId,
                                         @Param("relationType") String relationType,
                                         @Param("status") String status,
                                         @Param("keyword") String keyword,
                                         @Param("orderBy") String orderBy,
                                         @Param("orderDirection") String orderDirection);

    /**
     * 获取内容的标签列表
     */
    List<ContentTag> selectByContentId(@Param("contentId") Long contentId);

    /**
     * 获取标签的内容列表
     */
    List<ContentTag> selectByTagId(@Param("tagId") Long tagId);

    /**
     * 检查内容是否已有此标签
     */
    int countByContentIdAndTagId(@Param("contentId") Long contentId, @Param("tagId") Long tagId);

    /**
     * 批量删除内容的所有标签
     */
    int deleteByContentId(@Param("contentId") Long contentId);

    /**
     * 批量删除标签的所有关联
     */
    int deleteByTagId(@Param("tagId") Long tagId);

    /**
     * 统计标签被使用的内容数量
     */
    int countContentsByTagId(@Param("tagId") Long tagId);

    /**
     * 统计内容的标签数量
     */
    int countTagsByContentId(@Param("contentId") Long contentId);

    /**
     * 批量获取内容标签关联信息（覆盖索引优化）
     */
    List<Map<String, Object>> getContentTagSummary(@Param("contentIds") List<Long> contentIds);

    /**
     * 获取最新的内容标签关联（覆盖索引优化）
     */
    List<Map<String, Object>> getRecentContentTags(@Param("limit") Integer limit);
} 