package com.gig.collide.Apientry.api.search.request;

import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.io.Serializable;

/**
 * 搜索历史查询请求 - 简洁版
 *
 * @author GIG Team
 * @version 2.0.0
 */
@Data
public class SearchHistoryQueryRequest implements Serializable {

    private Integer currentPage = 1;
    private Integer pageSize = 10;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 搜索类型：content、goods、user
     */
    private String searchType;

    /**
     * 关键词搜索（模糊搜索）
     */
    private String keyword;

    /**
     * 排序方式：create_time、result_count
     */
    private String sortBy = "create_time";

    /**
     * 排序方向：asc、desc
     */
    private String sortDirection = "desc";

    /**
     * 转换为PageRequest对象
     */
    public PageRequest toPageRequest() {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        return PageRequest.of(currentPage - 1, pageSize, sort);
    }
}
