package com.gig.collide.domain;

public class GoodsQuery {
    private String goodsType;
    private Integer currentPage;
    private Integer pageSize;

    // Getters
    public String getGoodsType() {
        return goodsType;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    // Setters（如需）
    public void setGoodsType(String goodsType) {
        this.goodsType = goodsType;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
