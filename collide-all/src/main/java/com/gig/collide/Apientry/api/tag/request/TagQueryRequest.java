package com.gig.collide.Apientry.api.tag.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * 标签查询请求
 *
 * @author GIG Team
 * @version 3.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TagQueryRequest {

    /**
     * 标签名称（模糊搜索）
     */
    private String name;

    /**
     * 标签类型
     */
    private String tagType;

    /**
     * 所属分类ID
     */
    private Long categoryId;

    /**
     * 状态：active、inactive
     */
    private String status;

    /**
     * 最小使用次数
     */
    private Long minUsageCount;

    /**
     * 最大使用次数
     */
    private Long maxUsageCount;

    /**
     * 排序字段
     */
    private String sortBy;

    /**
     * 排序方向：asc、desc
     */
    private String sortDirection;

    /**
     * 页码，从0开始
     */
    private int page = 0;

    /**
     * 每页大小
     */
    private int size = 10;

    /**
     * 转换为Pageable对象
     */
    public Pageable toPageable() {
        return PageRequest.of(page, size);
    }
}
