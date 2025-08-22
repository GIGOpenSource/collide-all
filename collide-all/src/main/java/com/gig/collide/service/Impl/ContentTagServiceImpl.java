package com.gig.collide.service.Impl;


import com.gig.collide.domain.ContentTag;
import com.gig.collide.mapper.ContentTagMapper;
import com.gig.collide.service.ContentTagService;
import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.Apientry.api.common.response.PageResponse;
import com.gig.collide.Apientry.api.tag.response.ContentTagResponse;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 内容标签服务实现�?- 严格对应ContentTagMapper
 *
 * @author GIG Team
 * @version 3.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContentTagServiceImpl implements ContentTagService {

    private final ContentTagMapper contentTagMapper;

    // =================== 基础CRUD操作 ===================

    @Override
    @Transactional
    public ContentTag createContentTag(ContentTag contentTag) {
        log.debug("创建内容标签关联: 内容ID={}, 标签ID={}",
                contentTag.getContentId(), contentTag.getTagId());

        int result = contentTagMapper.insert(contentTag);
        if (result > 0) {
            log.info("内容标签关联创建成功: ID={}, 内容ID={}, 标签ID={}",
                    contentTag.getId(), contentTag.getContentId(), contentTag.getTagId());
            return contentTag;
        } else {
            throw new RuntimeException("内容标签关联创建失败");
        }
    }

    @Override
    @Transactional
    public ContentTag updateContentTag(ContentTag contentTag) {
        log.debug("更新内容标签关联: ID={}", contentTag.getId());

        int result = contentTagMapper.updateById(contentTag);
        if (result > 0) {
            log.info("内容标签关联更新成功: ID={}", contentTag.getId());
            return contentTag;
        } else {
            throw new RuntimeException("内容标签关联更新失败");
        }
    }

    @Override
    @Transactional
    public boolean deleteContentTagById(Long id) {
        log.debug("删除内容标签关联: ID={}", id);

        int result = contentTagMapper.deleteById(id);
        boolean success = result > 0;
        if (success) {
            log.info("内容标签关联删除成功: ID={}", id);
        } else {
            log.warn("内容标签关联删除失败: ID={}", id);
        }
        return success;
    }

    @Override
    public ContentTag getContentTagById(Long id) {
        log.debug("查询内容标签关联: ID={}", id);
        return contentTagMapper.selectById(id);
    }

    @Override
    public List<ContentTag> getAllContentTags() {
        log.debug("查询所有内容标签关�?");
        return contentTagMapper.selectList(null);
    }

    // =================== 核心查询方法 ===================

    @Override
    public List<ContentTag> selectByContentId(Long contentId) {
        log.debug("获取内容的标签列�? 内容ID={}", contentId);
        return contentTagMapper.selectByContentId(contentId);
    }

    @Override
    public List<ContentTag> selectByTagId(Long tagId) {
        log.debug("获取标签的内容列�? 标签ID={}", tagId);
        return contentTagMapper.selectByTagId(tagId);
    }

    // =================== 统计和计数方�?===================

    @Override
    public boolean existsByContentIdAndTagId(Long contentId, Long tagId) {
        log.debug("检查内容是否已有此标签: 内容ID={}, 标签ID={}", contentId, tagId);
        int count = contentTagMapper.countByContentIdAndTagId(contentId, tagId);
        return count > 0;
    }

    @Override
    public int countContentsByTagId(Long tagId) {
        log.debug("统计标签被使用的内容数量: 标签ID={}", tagId);
        return contentTagMapper.countContentsByTagId(tagId);
    }

    @Override
    public int countTagsByContentId(Long contentId) {
        log.debug("统计内容的标签数�? 内容ID={}", contentId);
        return contentTagMapper.countTagsByContentId(contentId);
    }

    @Override
    public List<Map<String, Object>> getContentTagSummary(List<Long> contentIds) {
        log.debug("批量获取内容标签关联信息: 内容数量={}", contentIds.size());
        return contentTagMapper.getContentTagSummary(contentIds);
    }

    @Override
    public List<Map<String, Object>> getRecentContentTags(Integer limit) {
        log.debug("获取最新的内容标签关联: 限制数量={}", limit);
        return contentTagMapper.getRecentContentTags(limit);
    }

    // =================== 删除操作方法 ===================

    @Override
    @Transactional
    public int deleteByContentId(Long contentId) {
        log.debug("批量删除内容的所有标�? 内容ID={}", contentId);

        int result = contentTagMapper.deleteByContentId(contentId);
        log.info("批量删除内容标签完成: 内容ID={}, 删除数量={}", contentId, result);
        return result;
    }

    @Override
    @Transactional
    public int deleteByTagId(Long tagId) {
        log.debug("批量删除标签的所有关�? 标签ID={}", tagId);

        int result = contentTagMapper.deleteByTagId(tagId);
        log.info("批量删除标签关联完成: 标签ID={}, 删除数量={}", tagId, result);
        return result;
    }

    // =================== 业务逻辑方法 ===================

    @Override
    @Transactional
    public ContentTag addContentTagSafely(Long contentId, Long tagId) {
        log.debug("安全为内容添加标�? 内容ID={}, 标签ID={}", contentId, tagId);

        // 检查是否已存在
        if (existsByContentIdAndTagId(contentId, tagId)) {
            throw new RuntimeException("内容已有此标�? 内容ID=" + contentId + ", 标签ID=" + tagId);
        }

        ContentTag contentTag = new ContentTag();
        contentTag.setContentId(contentId);
        contentTag.setTagId(tagId);

        return createContentTag(contentTag);
    }

    @Override
    @Transactional
    public boolean removeContentTag(Long contentId, Long tagId) {
        log.debug("移除内容标签: 内容ID={}, 标签ID={}", contentId, tagId);

        List<ContentTag> contentTags = selectByContentId(contentId);
        ContentTag targetTag = contentTags.stream()
                .filter(ct -> tagId.equals(ct.getTagId()))
                .findFirst()
                .orElse(null);

        if (targetTag != null) {
            return deleteContentTagById(targetTag.getId());
        }

        return false;
    }

    @Override
    @Transactional
    public int batchAddContentTags(Long contentId, List<Long> tagIds) {
        log.debug("批量为内容添加标�? 内容ID={}, 标签数量={}", contentId, tagIds.size());

        int count = 0;
        for (Long tagId : tagIds) {
            try {
                if (!existsByContentIdAndTagId(contentId, tagId)) {
                    ContentTag contentTag = new ContentTag();
                    contentTag.setContentId(contentId);
                    contentTag.setTagId(tagId);
                    createContentTag(contentTag);
                    count++;
                }
            } catch (Exception e) {
                log.warn("批量添加内容标签失败: 内容ID={}, 标签ID={}", contentId, tagId, e);
            }
        }

        log.info("批量添加内容标签完成: 成功数量={}", count);
        return count;
    }

    @Override
    @Transactional
    public int batchRemoveContentTags(Long contentId, List<Long> tagIds) {
        log.debug("批量移除内容标签: 内容ID={}, 标签数量={}", contentId, tagIds.size());

        int count = 0;
        for (Long tagId : tagIds) {
            try {
                if (removeContentTag(contentId, tagId)) {
                    count++;
                }
            } catch (Exception e) {
                log.warn("批量移除内容标签失败: 内容ID={}, 标签ID={}", contentId, tagId, e);
            }
        }

        log.info("批量移除内容标签完成: 成功数量={}", count);
        return count;
    }

    @Override
    @Transactional
    public int replaceContentTags(Long contentId, List<Long> newTagIds) {
        log.debug("替换内容的所有标�? 内容ID={}, 新标签数�?{}", contentId, newTagIds.size());

        // 先删除所有现有标�?
        deleteByContentId(contentId);

        // 再添加新标签
        return batchAddContentTags(contentId, newTagIds);
    }

    @Override
    @Transactional
    public int copyContentTags(Long sourceContentId, Long targetContentId) {
        log.debug("复制内容标签: 源内容ID={}, 目标内容ID={}", sourceContentId, targetContentId);

        List<ContentTag> sourceTags = selectByContentId(sourceContentId);
        List<Long> tagIds = sourceTags.stream()
                .map(ContentTag::getTagId)
                .collect(Collectors.toList());

        return batchAddContentTags(targetContentId, tagIds);
    }

    @Override
    public List<Long> getContentTagIds(Long contentId) {
        log.debug("获取内容标签ID列表: 内容ID={}", contentId);

        List<ContentTag> contentTags = selectByContentId(contentId);
        return contentTags.stream()
                .map(ContentTag::getTagId)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> getTagContentIds(Long tagId) {
        log.debug("获取标签关联的内容ID列表: 标签ID={}", tagId);

        List<ContentTag> contentTags = selectByTagId(tagId);
        return contentTags.stream()
                .map(ContentTag::getContentId)
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasCommonTags(Long contentId1, Long contentId2) {
        log.debug("检查两个内容是否有共同标签: 内容1={}, 内容2={}", contentId1, contentId2);

        List<Long> tags1 = getContentTagIds(contentId1);
        List<Long> tags2 = getContentTagIds(contentId2);

        Set<Long> tagSet1 = new HashSet<>(tags1);
        return tags2.stream().anyMatch(tagSet1::contains);
    }

    @Override
    public int getCommonTagCount(Long contentId1, Long contentId2) {
        log.debug("获取两个内容的共同标签数�? 内容1={}, 内容2={}", contentId1, contentId2);

        List<Long> tags1 = getContentTagIds(contentId1);
        List<Long> tags2 = getContentTagIds(contentId2);

        Set<Long> tagSet1 = new HashSet<>(tags1);
        return (int) tags2.stream().filter(tagSet1::contains).count();
    }

    @Override
    public List<Long> getRelatedContentIds(Long contentId, Integer limit) {
        log.debug("获取内容的相关内�? 内容ID={}, 限制数量={}", contentId, limit);

        List<Long> contentTagIds = getContentTagIds(contentId);
        if (contentTagIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取有共同标签的其他内容
        Map<Long, Integer> contentTagCount = new HashMap<>();

        for (Long tagId : contentTagIds) {
            List<Long> relatedContentIds = getTagContentIds(tagId);
            for (Long relatedContentId : relatedContentIds) {
                if (!relatedContentId.equals(contentId)) {
                    contentTagCount.put(relatedContentId,
                            contentTagCount.getOrDefault(relatedContentId, 0) + 1);
                }
            }
        }

        // 按共同标签数量排序并限制数量
        return contentTagCount.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(limit != null ? limit : 10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public int cleanupInvalidContentTags() {
        log.debug("清理无效的内容标签关�?");

        // 这里需要结合具体的业务逻辑来实�?
        // 例如：检查content表和tag表中的记录是否存�?
        // 当前仅提供框架，具体实现需要根据实际数据库结构调整

        log.warn("清理无效内容标签关联功能需要根据实际业务逻辑实现");
        return 0;
    }

    // =================== Controller专用方法 ===================

    @Override
    public Result<PageResponse<ContentTagResponse>> listContentTagsForController(
            Long contentId, Long tagId, String relationType, String status, String keyword,
            String orderBy, String orderDirection, Integer currentPage, Integer pageSize) {
        
        log.info("Controller请求 - 内容标签关联列表查询: contentId={}, tagId={}, relationType={}, status={}, keyword={}, orderBy={}, orderDirection={}, page={}/{}",
                contentId, tagId, relationType, status, keyword, orderBy, orderDirection, currentPage, pageSize);

        try {
            // 参数验证和默认值设置
            if (currentPage == null || currentPage < 1) {
                currentPage = 1;
            }
            if (pageSize == null || pageSize < 1 || pageSize > 100) {
                pageSize = 20;
            }
            if (orderBy == null || orderBy.trim().isEmpty()) {
                orderBy = "createTime";
            }
            if (orderDirection == null || orderDirection.trim().isEmpty()) {
                orderDirection = "DESC";
            }

            // 构建查询条件
            Page<ContentTag> page = new Page<>(currentPage, pageSize);
            
            // 调用Mapper查询
            IPage<ContentTag> contentTagPage = contentTagMapper.selectContentTagList(page, contentId, tagId, relationType, status, keyword, orderBy, orderDirection);
            
            // 转换为Response对象
            List<ContentTagResponse> responses = contentTagPage.getRecords().stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            
            // 构建分页响应
            PageResponse<ContentTagResponse> pageResponse = new PageResponse<>();
            pageResponse.setRecords(responses);
            pageResponse.setTotal(contentTagPage.getTotal());
            pageResponse.setCurrentPage(currentPage);
            pageResponse.setPageSize(pageSize);
            pageResponse.setTotalPages((int) Math.ceil((double) contentTagPage.getTotal() / pageSize));
            
            log.info("内容标签关联列表查询成功: 总数={}, 当前页={}, 页面大小={}", contentTagPage.getTotal(), currentPage, pageSize);
            return Result.success(pageResponse);
            
        } catch (Exception e) {
            log.error("内容标签关联列表查询失败", e);
            return Result.error("内容标签关联列表查询失败: " + e.getMessage());
        }
    }

    /**
     * 将ContentTag实体转换为ContentTagResponse
     */
    private ContentTagResponse convertToResponse(ContentTag contentTag) {
        if (contentTag == null) {
            return null;
        }
        
        ContentTagResponse response = new ContentTagResponse();
        response.setId(contentTag.getId());
        response.setContentId(contentTag.getContentId());
        response.setTagId(contentTag.getTagId());
        response.setCreateTime(contentTag.getCreateTime());
        response.setUpdateTime(contentTag.getUpdateTime());
        
        return response;
    }
}
