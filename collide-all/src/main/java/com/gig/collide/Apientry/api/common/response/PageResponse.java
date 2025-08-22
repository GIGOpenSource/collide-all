package com.gig.collide.Apientry.api.common.response;


import lombok.Data;
import java.util.List;

/**
 * 分页响应类
 *
 * @param <T> 数据类型
 * @author Collide Team
 * @since 1.0.0
 */
@Data
public class PageResponse<T> {
    
    /**
     * 数据列表
     */
    private List<T> records;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Integer currentPage;

    /**
     * 每页大小
     */
    private Integer pageSize;

    /**
     * 总页数
     */
    private Integer totalPages;

    /**
     * 是否有下一页
     */
    private Boolean hasNext;

    /**
     * 是否有上一页
     */
    private Boolean hasPrevious;

    public PageResponse() {}

    public PageResponse(List<T> records, Long total, Integer currentPage, Integer pageSize) {
        this.records = records;
        this.total = total;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.calculatePagination();
    }

    /**
     * 创建分页响应
     */
    public static <T> PageResponse<T> of(List<T> records, Long total, Integer currentPage, Integer pageSize) {
        return new PageResponse<>(records, total, currentPage, pageSize);
    }

    /**
     * 创建空分页响应
     */
    public static <T> PageResponse<T> empty() {
        return new PageResponse<>(List.of(), 0L, 1, 20);
    }

    /**
     * 计算分页相关信息
     */
    private void calculatePagination() {
        if (this.total != null && this.pageSize != null && this.pageSize > 0) {
            this.totalPages = (int) Math.ceil((double) this.total / this.pageSize);
            this.hasNext = this.currentPage != null && this.currentPage < this.totalPages;
            this.hasPrevious = this.currentPage != null && this.currentPage > 1;
        } else {
            this.totalPages = 0;
            this.hasNext = false;
            this.hasPrevious = false;
        }
    }

    /**
     * 设置分页信息并重新计算
     */
    public void setPagination(Long total, Integer currentPage, Integer pageSize) {
        this.total = total;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.calculatePagination();
    }
}
