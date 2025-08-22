package com.gig.collide.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gig.collide.Apientry.api.common.response.PageResponse;
import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.Apientry.api.tag.response.TagResponse;
import com.gig.collide.domain.Tag;
import com.gig.collide.mapper.TagMapper;
import com.gig.collide.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 标签服务实现类 - 严格对应TagMapper
 *
 * @author GIG Team
 * @version 3.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagMapper tagMapper;

    // =================== 基础CRUD操作 ===================

    @Override
    @Transactional
    public Tag createTag(Tag tag) {
        log.debug("创建标签: 名称={}, 类型={}", tag.getName(), tag.getTagType());

        // 设置默认值
        if (!StringUtils.hasText(tag.getStatus())) {
            tag.setStatus("active");
        }
        if (tag.getUsageCount() == null) {
            tag.setUsageCount(0L);
        }
        if (!StringUtils.hasText(tag.getTagType())) {
            tag.setTagType("content");
        }

        int result = tagMapper.insert(tag);
        if (result > 0) {
            log.info("标签创建成功: ID={}, 名称={}", tag.getId(), tag.getName());
            return tag;
        } else {
            throw new RuntimeException("标签创建失败");
        }
    }

    @Override
    @Transactional
    public Tag updateTag(Tag tag) {
        log.debug("更新标签: ID={}, 名称={}", tag.getId(), tag.getName());

        int result = tagMapper.updateById(tag);
        if (result > 0) {
            log.info("标签更新成功: ID={}", tag.getId());
            return tag;
        } else {
            throw new RuntimeException("标签更新失败");
        }
    }

    @Override
    @Transactional
    public boolean deleteTagById(Long tagId) {
        log.debug("删除标签: ID={}", tagId);

        int result = tagMapper.deleteById(tagId);
        boolean success = result > 0;
        if (success) {
            log.info("标签删除成功: ID={}", tagId);
        } else {
            log.warn("标签删除失败: ID={}", tagId);
        }
        return success;
    }

    @Override
    public Tag getTagById(Long tagId) {
        log.debug("查询标签: ID={}", tagId);
        return tagMapper.selectById(tagId);
    }

    @Override
    public List<Tag> getAllTags() {
        log.debug("查询所有标签");
        return tagMapper.selectList(null);
    }

    // =================== 核心查询方法 ===================

    @Override
    public List<Tag> selectByTagType(String tagType) {
        log.debug("根据类型查询标签: 类型={}", tagType);
        return tagMapper.selectByTagType(tagType);
    }

    @Override
    public List<Tag> selectByCategoryId(Long categoryId) {
        log.debug("根据分类查询标签: 分类ID={}", categoryId);
        return tagMapper.selectByCategoryId(categoryId);
    }

    @Override
    public List<Tag> selectByStatus(String status) {
        log.debug("根据状态查询标签: 状态={}", status);
        // 使用 MyBatis-Plus 的通用查询方法
        return tagMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Tag>()
                .eq("status", status));
    }

    @Override
    public List<Tag> selectHotTags(Integer limit) {
        log.debug("查询热门标签: 限制数量={}", limit);
        return tagMapper.selectHotTags(limit);
    }

    @Override
    public List<Tag> searchTags(String keyword) {
        log.debug("搜索标签: 关键词={}", keyword);
        return tagMapper.searchTags(keyword);
    }

    @Override
    public List<Tag> searchByName(String keyword, Integer limit) {
        log.debug("按名称模糊搜索标签: 关键词={}, 限制数量={}", keyword, limit);
        return tagMapper.searchByName(keyword, limit);
    }

    @Override
    public List<Tag> searchByNameExact(String keyword, Integer limit) {
        log.debug("按名称精确搜索标签: 关键词={}, 限制数量={}", keyword, limit);
        return tagMapper.searchByNameExact(keyword, limit);
    }

    // =================== 统计和计数方法 ===================

    @Override
    public boolean existsByTagName(String tagName) {
        log.debug("检查标签是否存在: 名称={}", tagName);
        int count = tagMapper.countByTagName(tagName);
        return count > 0;
    }

    @Override
    public boolean existsByNameAndType(String name, String tagType) {
        log.debug("检查标签是否存在: 名称={}, 类型={}", name, tagType);
        int count = tagMapper.countByNameAndType(name, tagType);
        return count > 0;
    }

    @Override
    public Long countByTagType(String tagType) {
        log.debug("统计标签数量: 类型={}", tagType);
        return tagMapper.countByTagType(tagType);
    }

    @Override
    public Long countByStatus(String status) {
        log.debug("统计标签数量: 状态={}", status);
        return tagMapper.countByStatus(status);
    }

    @Override
    public Map<String, Object> getTagStatistics() {
        log.debug("获取标签统计信息");
        return tagMapper.getTagStatistics();
    }

    @Override
    public List<Map<String, Object>> getTagUsageStats(String tagType, Integer limit) {
        log.debug("获取标签使用统计: 类型={}, 限制数量={}", tagType, limit);
        return tagMapper.getTagUsageStats(tagType, limit);
    }

    @Override
    public List<Map<String, Object>> selectTagSummary(List<Long> tagIds) {
        log.debug("批量获取标签基本信息: 标签数量={}", tagIds.size());
        return tagMapper.selectTagSummary(tagIds);
    }

    // =================== 更新操作方法 ===================

    @Override
    @Transactional
    public boolean updateTagStatus(Long tagId, String status) {
        log.debug("更新标签状态: ID={}, 状态={}", tagId, status);

        int result = tagMapper.updateTagStatus(tagId, status);
        boolean success = result > 0;
        if (success) {
            log.info("标签状态更新成功: ID={}, 状态={}", tagId, status);
        } else {
            log.warn("标签状态更新失败: ID={}, 状态={}", tagId, status);
        }
        return success;
    }

    @Override
    @Transactional
    public boolean incrementUsageCount(Long tagId) {
        log.debug("增加标签使用次数: ID={}", tagId);

        int result = tagMapper.incrementUsageCount(tagId);
        boolean success = result > 0;
        if (success) {
            log.debug("标签使用次数增加成功: ID={}", tagId);
        } else {
            log.warn("标签使用次数增加失败: ID={}", tagId);
        }
        return success;
    }

    @Override
    @Transactional
    public boolean decrementUsageCount(Long tagId) {
        log.debug("减少标签使用次数: ID={}", tagId);

        int result = tagMapper.decrementUsageCount(tagId);
        boolean success = result > 0;
        if (success) {
            log.debug("标签使用次数减少成功: ID={}", tagId);
        } else {
            log.warn("标签使用次数减少失败: ID={}", tagId);
        }
        return success;
    }

    @Override
    @Transactional
    public boolean increaseUsageCount(Long tagId) {
        log.debug("增加标签使用次数: ID={}", tagId);
        return incrementUsageCount(tagId);
    }

    @Override
    @Transactional
    public boolean decreaseUsageCount(Long tagId) {
        log.debug("减少标签使用次数: ID={}", tagId);
        return decrementUsageCount(tagId);
    }

    // =================== 业务逻辑方法 ===================

    @Override
    @Transactional
    public Tag createTagSafely(Tag tag) {
        log.debug("安全创建标签: 名称={}, 类型={}", tag.getName(), tag.getTagType());

        // 检查标签是否已存在
        if (existsByTagName(tag.getName())) {
            throw new RuntimeException("标签已存在: " + tag.getName());
        }

        return createTag(tag);
    }

    @Override
    @Transactional
    public Tag createTagSafely(String name, String tagType, String description, Long categoryId) {
        log.debug("安全创建标签: 名称={}, 类型={}, 描述={}, 分类ID={}", name, tagType, description, categoryId);

        // 检查标签是否已存在
        if (existsByNameAndType(name, tagType)) {
            throw new RuntimeException("标签已存在: 名称=" + name + ", 类型=" + tagType);
        }

        // 创建新的标签对象
        Tag tag = new Tag();
        tag.setName(name);
        tag.setTagType(tagType);
        tag.setDescription(description);
        tag.setCategoryId(categoryId);
        tag.setStatus("active");
        tag.setUsageCount(0L);

        return createTag(tag);
    }

    @Override
    @Transactional
    public boolean activateTag(Long tagId) {
        log.debug("激活标签: ID={}", tagId);
        return updateTagStatus(tagId, "active");
    }

    @Override
    @Transactional
    public boolean deactivateTag(Long tagId) {
        log.debug("停用标签: ID={}", tagId);
        return updateTagStatus(tagId, "inactive");
    }

    @Override
    public List<Tag> getActiveTags() {
        log.debug("获取活跃标签");
        return selectByStatus("active");
    }

    @Override
    public List<Tag> getActiveTags(Long categoryId) {
        log.debug("获取分类下的活跃标签: 分类ID={}", categoryId);
        if (categoryId == null) {
            log.warn("分类ID为空，返回所有活跃标签");
            return getActiveTags();
        }
        return tagMapper.selectActiveTagsByCategoryId(categoryId);
    }

    @Override
    public List<Tag> getActiveTagsByType(String tagType) {
        log.debug("获取活跃标签: 类型={}", tagType);
        return tagMapper.selectActiveTagsByType(tagType);
    }

    @Override
    public List<Tag> getPopularTags(Integer limit) {
        log.debug("获取热门标签: 限制数量={}", limit);
        return selectHotTags(limit);
    }

    @Override
    public List<Tag> getPopularTagsByType(String tagType, Integer limit) {
        log.debug("获取热门标签: 类型={}, 限制数量={}", tagType, limit);
        return tagMapper.selectPopularTagsByType(tagType, limit);
    }

    @Override
    public List<Tag> searchActiveTags(String keyword) {
        log.debug("搜索活跃标签: 关键词={}", keyword);
        return tagMapper.searchActiveTags(keyword);
    }

    @Override
    public List<Tag> searchTagsByType(String keyword, String tagType) {
        log.debug("搜索标签: 关键词={}, 类型={}", keyword, tagType);
        return tagMapper.searchTagsByType(keyword, tagType);
    }

    @Override
    public List<Tag> getTagsByUsageRange(Long minUsage, Long maxUsage) {
        log.debug("根据使用次数范围查询标签: 最小={}, 最大={}", minUsage, maxUsage);
        return tagMapper.selectTagsByUsageRange(minUsage, maxUsage);
    }

    @Override
    public List<Tag> getTagsByUsageRangeAndType(Long minUsage, Long maxUsage, String tagType) {
        log.debug("根据使用次数范围和类型查询标签: 最小={}, 最大={}, 类型={}", minUsage, maxUsage, tagType);
        return tagMapper.selectTagsByUsageRangeAndType(minUsage, maxUsage, tagType);
    }

    @Override
    public List<Map<String, Object>> getTagUsageStatistics() {
        log.debug("获取标签使用统计");
        return tagMapper.getTagUsageStatistics();
    }

    @Override
    public List<Map<String, Object>> getTagUsageStatisticsByType(String tagType) {
        log.debug("获取标签使用统计: 类型={}", tagType);
        return tagMapper.getTagUsageStatisticsByType(tagType);
    }

    @Override
    @Transactional
    public int batchUpdateStatus(List<Long> tagIds, String status) {
        log.debug("批量更新标签状态: 标签数量={}, 状态={}", tagIds.size(), status);

        int result = tagMapper.batchUpdateStatus(tagIds, status);
        log.info("批量更新标签状态完成: 更新数量={}", result);
        return result;
    }

    @Override
    @Transactional
    public int batchDeleteTags(List<Long> tagIds) {
        log.debug("批量删除标签: 标签数量={}", tagIds.size());

        int result = tagMapper.batchDeleteTags(tagIds);
        log.info("批量删除标签完成: 删除数量={}", result);
        return result;
    }

    @Override
    public List<Tag> getTagsByIds(List<Long> tagIds) {
        log.debug("根据ID列表查询标签: 数量={}", tagIds.size());
        return tagMapper.selectTagsByIds(tagIds);
    }

    @Override
    public List<Tag> getActiveTagsByIds(List<Long> tagIds) {
        log.debug("根据ID列表查询活跃标签: 数量={}", tagIds.size());
        return tagMapper.selectActiveTagsByIds(tagIds);
    }

    @Override
    public List<Tag> intelligentSearch(String keyword, Integer limit) {
        log.debug("智能搜索标签: 关键词={}, 限制数量={}", keyword, limit);

        if (!StringUtils.hasText(keyword)) {
            log.warn("搜索关键词为空");
            return List.of();
        }

        // 先尝试精确搜索
        List<Tag> exactResults = searchByNameExact(keyword, limit);
        if (!exactResults.isEmpty()) {
            log.debug("精确搜索找到 {} 个结果", exactResults.size());
            return exactResults;
        }

        // 精确搜索无结果时，进行模糊搜索
        List<Tag> fuzzyResults = searchByName(keyword, limit);
        log.debug("模糊搜索找到 {} 个结果", fuzzyResults.size());
        return fuzzyResults;
    }

    // =================== Controller专用方法 ===================

    @Override
    public Result<PageResponse<TagResponse>> listTagsForController(
            String tagType, String status, String keyword, Boolean isHot, Long minUsageCount,
            String orderBy, String orderDirection, Integer currentPage, Integer pageSize) {
        try {
            log.info("Controller层 - 标签列表查询: tagType={}, status={}, keyword={}, page={}/{}", 
                    tagType, status, keyword, currentPage, pageSize);

            // 参数验证和默认值设置
            if (currentPage == null || currentPage < 1) {
                currentPage = 1;
            }
            if (pageSize == null || pageSize < 1 || pageSize > 100) {
                pageSize = 20;
            }
            if (orderBy == null || orderBy.trim().isEmpty()) {
                orderBy = "usageCount";
            }
            if (orderDirection == null || orderDirection.trim().isEmpty()) {
                orderDirection = "DESC";
            }

            // 构建查询条件
            LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();

            // 动态查询条件
            if (StringUtils.hasText(tagType)) {
                queryWrapper.eq(Tag::getTagType, tagType);
            }
            if (StringUtils.hasText(status)) {
                queryWrapper.eq(Tag::getStatus, status);
            }
            if (StringUtils.hasText(keyword)) {
                queryWrapper.like(Tag::getName, keyword);
            }
            if (isHot != null && isHot) {
                queryWrapper.gt(Tag::getUsageCount, 0);
            }
            if (minUsageCount != null && minUsageCount > 0) {
                queryWrapper.ge(Tag::getUsageCount, minUsageCount);
            }

            // 排序
            if ("name".equals(orderBy)) {
                if ("ASC".equals(orderDirection)) {
                    queryWrapper.orderByAsc(Tag::getName);
                } else {
                    queryWrapper.orderByDesc(Tag::getName);
                }
            } else if ("createTime".equals(orderBy)) {
                if ("ASC".equals(orderDirection)) {
                    queryWrapper.orderByAsc(Tag::getCreateTime);
                } else {
                    queryWrapper.orderByDesc(Tag::getCreateTime);
                }
            } else if ("updateTime".equals(orderBy)) {
                if ("ASC".equals(orderDirection)) {
                    queryWrapper.orderByAsc(Tag::getUpdateTime);
                } else {
                    queryWrapper.orderByDesc(Tag::getUpdateTime);
                }
            } else {
                // 默认按使用次数排序
                if ("ASC".equals(orderDirection)) {
                    queryWrapper.orderByAsc(Tag::getUsageCount);
                } else {
                    queryWrapper.orderByDesc(Tag::getUsageCount);
                }
            }

            // 使用MyBatis-Plus分页
            Page<Tag> page = new Page<>(currentPage, pageSize);
            IPage<Tag> result = tagMapper.selectPage(page, queryWrapper);

            // 转换为Response对象
            List<TagResponse> responses = result.getRecords().stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            // 构建分页响应
            PageResponse<TagResponse> pageResponse = new PageResponse<>();
            pageResponse.setRecords(responses);
            pageResponse.setTotal(result.getTotal());
            pageResponse.setCurrentPage(currentPage);
            pageResponse.setPageSize(pageSize);
            pageResponse.setTotalPages((int) Math.ceil((double) result.getTotal() / pageSize));

            log.info("标签列表查询成功: 总数={}, 当前页={}, 页面大小={}", result.getTotal(), currentPage, pageSize);
            return Result.success(pageResponse);

        } catch (Exception e) {
            log.error("Controller层 - 标签列表查询失败", e);
            return Result.error("标签列表查询失败: " + e.getMessage());
        }
    }

    /**
     * 将Tag实体转换为TagResponse
     */
    private TagResponse convertToResponse(Tag tag) {
        if (tag == null) {
            return null;
        }

        TagResponse response = new TagResponse();
        response.setId(tag.getId());
        response.setName(tag.getName());
        response.setTagType(tag.getTagType());
        response.setDescription(tag.getDescription());
        response.setStatus(tag.getStatus());
        response.setUsageCount(tag.getUsageCount());
        response.setCategoryId(tag.getCategoryId());
        response.setCreateTime(tag.getCreateTime());
        response.setUpdateTime(tag.getUpdateTime());

        return response;
    }
}
