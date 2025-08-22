//package com.gig.collide.controller;
//
//import com.gig.collide.Apientry.api.category.CategoryFacadeService;
//import com.gig.collide.Apientry.api.category.request.CategoryQueryRequest;
//import com.gig.collide.Apientry.api.category.response.CategoryResponse;
//import com.gig.collide.Apientry.api.common.response.PageResponse;
//import com.gig.collide.Apientry.api.common.response.Result;
//import com.gig.collide.service.CategoryService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.Parameter;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
///**
// * 分类REST控制器 - 规范版
// * 参考Content模块设计，直接返回层的Result包装
// * 专注于客户端使用的查询功能
// *
// * @author Collide
// * @version 5.0.0 (与Content模块一致版)
// * @since 2024-01-01
// *//*
//
//@Slf4j
//@RestController
//@RequestMapping("/api/v1/categories")
//@RequiredArgsConstructor
//@Tag(name = "分类管理", description = "分类的增删改查、层级管理、统计分析等功能")
//public class CategoryController {
//
//    private final CategoryService categoryService;
//
//    // =================== 基础查询 ===================
//
//    */
///**
//     * 分页查询分类列表
//     *//*
//
//    @GetMapping("/query")
//    @Operation(summary = "分页查询分类", description = "支持按父分类、状态等条件分页查询")
//    public Result<PageResponse<CategoryResponse>> queryCategories(
//            @Parameter(description = "父分类ID") @RequestParam(required = false) Long parentId,
//            @Parameter(description = "状态") @RequestParam(required = false) String status,
//            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
//            @Parameter(description = "页面大小") @RequestParam(defaultValue = "20") Integer pageSize,
//            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createTime") String orderBy,
//            @Parameter(description = "排序方向") @RequestParam(defaultValue = "DESC") String orderDirection) {
//        log.info("REST请求 - 分页查询分类: parentId={}, status={}, page={}/{}", parentId, status, currentPage, pageSize);
//        return categoryService.queryCategoriesForController(parentId, status, currentPage, pageSize, orderBy, orderDirection);
//    }
//
//    */
///**
//     * 搜索分类
//     *//*
//
//    @GetMapping("/search")
//    @Operation(summary = "搜索分类", description = "根据关键词搜索分类")
//    public Result<PageResponse<CategoryResponse>> searchCategories(
//            @Parameter(description = "搜索关键词", required = true) @RequestParam String keyword,
//            @Parameter(description = "父分类ID") @RequestParam(required = false) Long parentId,
//            @Parameter(description = "状态") @RequestParam(required = false) String status,
//            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
//            @Parameter(description = "页面大小") @RequestParam(defaultValue = "20") Integer pageSize,
//            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createTime") String orderBy,
//            @Parameter(description = "排序方向") @RequestParam(defaultValue = "DESC") String orderDirection) {
//        log.info("REST请求 - 搜索分类: keyword={}, parentId={}, status={}, page={}/{}", keyword, parentId, status, currentPage, pageSize);
//        return categoryService.searchCategoriesForController(keyword, parentId, status, currentPage, pageSize, orderBy, orderDirection);
//    }
//
//    // =================== 层级查询 ===================
//
//    */
///**
//     * 获取根分类列表
//     *//*
//
//    @GetMapping("/root")
//    @Operation(summary = "获取根分类列表", description = "获取所有顶级分类")
//    public Result<PageResponse<CategoryResponse>> getRootCategories(
//            @Parameter(description = "状态") @RequestParam(required = false) String status,
//            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
//            @Parameter(description = "页面大小") @RequestParam(defaultValue = "20") Integer pageSize,
//            @Parameter(description = "排序字段") @RequestParam(defaultValue = "sortOrder") String orderBy,
//            @Parameter(description = "排序方向") @RequestParam(defaultValue = "ASC") String orderDirection) {
//        log.info("REST请求 - 获取根分类: status={}, page={}/{}", status, currentPage, pageSize);
//        return categoryService.getRootCategoriesForController(status, currentPage, pageSize, orderBy, orderDirection);
//    }
//
//    */
///**
//     * 获取子分类列表
//     *//*
//
//    @GetMapping("/{parentId}/children")
//    @Operation(summary = "获取子分类列表", description = "获取指定父分类的子分类")
//    public Result<PageResponse<CategoryResponse>> getChildCategories(
//            @Parameter(description = "父分类ID", required = true) @PathVariable Long parentId,
//            @Parameter(description = "状态") @RequestParam(required = false) String status,
//            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
//            @Parameter(description = "页面大小") @RequestParam(defaultValue = "20") Integer pageSize,
//            @Parameter(description = "排序字段") @RequestParam(defaultValue = "sortOrder") String orderBy,
//            @Parameter(description = "排序方向") @RequestParam(defaultValue = "ASC") String orderDirection) {
//        log.info("REST请求 - 获取子分类: parentId={}, status={}, page={}/{}", parentId, status, currentPage, pageSize);
//        return categoryService.getChildCategoriesForController(parentId, status, currentPage, pageSize, orderBy, orderDirection);
//    }
//
//    */
///**
//     * 获取分类树
//     *//*
//
//    @GetMapping("/tree")
//    @Operation(summary = "获取分类树", description = "获取完整的分类层级结构")
//    public Result<List<CategoryResponse>> getCategoryTree(
//            @Parameter(description = "根分类ID") @RequestParam(required = false) Long rootId,
//            @Parameter(description = "最大层级深度") @RequestParam(defaultValue = "3") Integer maxDepth,
//            @Parameter(description = "状态") @RequestParam(required = false) String status,
//            @Parameter(description = "排序字段") @RequestParam(defaultValue = "sortOrder") String orderBy,
//            @Parameter(description = "排序方向") @RequestParam(defaultValue = "ASC") String orderDirection) {
//        log.info("REST请求 - 获取分类树: rootId={}, maxDepth={}, status={}", rootId, maxDepth, status);
//        return categoryService.getCategoryTreeForController(rootId, maxDepth, status, orderBy, orderDirection);
//    }
//
//    */
///**
//     * 获取分类路径
//     *//*
//
//    @GetMapping("/{categoryId}/path")
//    @Operation(summary = "获取分类路径", description = "获取从根分类到指定分类的完整路径")
//    public Result<List<CategoryResponse>> getCategoryPath(
//            @Parameter(description = "分类ID", required = true) @PathVariable Long categoryId) {
//        log.info("REST请求 - 获取分类路径: categoryId={}", categoryId);
//        return categoryService.getCategoryPathForController(categoryId);
//    }
//
//    // =================== 快捷查询 ===================
//
//    */
///**
//     * 获取所有活跃分类
//     *//*
//
//    @GetMapping("/active")
//    @Operation(summary = "获取活跃分类", description = "获取所有状态为活跃的分类")
//    public Result<List<CategoryResponse>> getActiveCategories(
//            @Parameter(description = "父分类ID") @RequestParam(required = false) Long parentId,
//            @Parameter(description = "排序字段") @RequestParam(defaultValue = "sortOrder") String orderBy,
//            @Parameter(description = "排序方向") @RequestParam(defaultValue = "ASC") String orderDirection) {
//        log.info("REST请求 - 获取活跃分类: parentId={}", parentId);
//        return categoryService.getActiveCategoriesForController(parentId, orderBy, orderDirection);
//    }
//
//    */
///**
//     * 获取热门分类
//     */
//
//    @GetMapping("/popular")
//    @Operation(summary = "获取热门分类", description = "获取使用频率最高的分类")
//    public Result<List<CategoryResponse>> getPopularCategories(
//            @Parameter(description = "限制数量") @RequestParam(defaultValue = "10") Integer limit,
//            @Parameter(description = "父分类ID") @RequestParam(required = false) Long parentId) {
//        log.info("REST请求 - 获取热门分类: limit={}, parentId={}", limit, parentId);
//        return categoryService.getPopularCategoriesForController(limit, parentId);
//    }
//}
