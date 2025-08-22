package com.gig.collide.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gig.collide.Apientry.api.common.response.PageResponse;
import com.gig.collide.Apientry.api.goods.request.GoodsCreateRequest;
import com.gig.collide.Apientry.api.goods.response.GoodsResponse;
import com.gig.collide.domain.Goods;
import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.domain.GoodsQuery;
import java.util.List;
import java.util.Map;

/**
 * 商品业务服务接口
 * 提供完整的商品管理功能
 *
 * @author GIG Team
 * @version 2.0.0 (扩展版)
 * @since 2024-01-31
 */
public interface GoodsService {

    // =================== 基础CRUD操作 ===================

    /**
     * 创建商品
     *
     * @param goods 商品信息
     * @return 商品ID
     */
    Long createGoods(Goods goods);

    /**
     * 创建商品（从请求对象）
     *
     * @param request 商品创建请求
     * @return 商品ID
     */
    Long createGoods(GoodsCreateRequest request);

    /**
     * 根据ID获取商品详情
     *
     * @param id 商品ID
     * @return 商品信息
     */
    Goods getGoodsById(Long id);

    /**
     * 根据ID获取商品详情（Controller专用）
     *
     * @param id 商品ID
     * @return 商品详情响应
     */
    Result<GoodsResponse> getGoodsByIdForController(Long id);

    /**
     * 更新商品信息
     *
     * @param goods 商品信息
     * @return 是否成功
     */
    boolean updateGoods(Goods goods);

    /**
     * 删除商品（软删除）
     *
     * @param id 商品ID
     * @return 是否成功
     */
    boolean deleteGoods(Long id);

    /**
     * 批量删除商品
     *
     * @param ids 商品ID列表
     * @return 是否成功
     */
    boolean batchDeleteGoods(List<Long> ids);

    // =================== 查询操作 ===================

    /**
     * 分页查询商品
     *
     * @param page      分页参数
     * @param goodsType 商品类型
     * @param status    商品状态
     * @return 分页结果
     */
    IPage<Goods> queryGoods(Page<Goods> page, String goodsType, String status);

    /**
     * 根据分类查询商品
     *
     * @param page       分页参数
     * @param categoryId 分类ID
     * @param status     商品状态
     * @return 分页结果
     */
    IPage<Goods> getGoodsByCategory(Page<Goods> page, Long categoryId, String status);

    /**
     * 根据商家查询商品
     *
     * @param page     分页参数
     * @param sellerId 商家ID
     * @param status   商品状态
     * @return 分页结果
     */
    IPage<Goods> getGoodsBySeller(Page<Goods> page, Long sellerId, String status);

    /**
     * 根据内容ID获取商品信息
     * 用于内容购买流程中获取对应的商品记录
     *
     * @param contentId 内容ID
     * @param goodsType 商品类型
     * @return 商品信息
     */
    Goods getGoodsByContentId(Long contentId, String goodsType);

    /**
     * 获取热门商品
     *
     * @param page      分页参数
     * @param goodsType 商品类型（可为空）
     * @return 分页结果
     */
    IPage<Goods> getHotGoods(Page<Goods> page, String goodsType);

    /**
     * 搜索商品
     *
     * @param page    分页参数
     * @param keyword 搜索关键词
     * @param status  商品状态
     * @return 分页结果
     */
    IPage<Goods> searchGoods(Page<Goods> page, String keyword, String status);

    /**
     * 按价格区间查询商品
     *
     * @param page      分页参数
     * @param minPrice  最低价格
     * @param maxPrice  最高价格
     * @param goodsType 商品类型
     * @return 分页结果
     */
    IPage<Goods> getGoodsByPriceRange(Page<Goods> page, Object minPrice, Object maxPrice, String goodsType);

    // =================== 库存管理 ===================

    /**
     * 检查库存是否充足
     *
     * @param goodsId  商品ID
     * @param quantity 需要数量
     * @return 是否充足
     */
    boolean checkStock(Long goodsId, Integer quantity);

    /**
     * 扣减库存
     *
     * @param goodsId  商品ID
     * @param quantity 扣减数量
     * @return 是否成功
     */
    boolean reduceStock(Long goodsId, Integer quantity);

    /**
     * 批量扣减库存
     *
     * @param stockMap 商品ID和扣减数量的映射
     * @return 是否成功
     */
    boolean batchReduceStock(Map<Long, Integer> stockMap);

    /**
     * 查询低库存商品
     *
     * @param threshold 库存阈值
     * @return 商品列表
     */
    List<Goods> getLowStockGoods(Integer threshold);

    // =================== 统计操作 ===================

    /**
     * 增加商品销量
     * 对应Mapper方法：increaseSalesCount
     *
     * @param goodsId 商品ID
     * @param count   增加数量
     * @return 是否成功
     */
    boolean increaseSalesCount(Long goodsId, Long count);

    /**
     * 增加商品浏览量
     * 对应Mapper方法：increaseViewCount
     *
     * @param goodsId 商品ID
     * @param count   增加数量
     * @return 是否成功
     */
    boolean increaseViewCount(Long goodsId, Long count);

    /**
     * 批量增加浏览量
     * 基于increaseViewCount方法的批量版本
     *
     * @param viewMap 商品ID和浏览量的映射
     * @return 是否成功
     */
    boolean batchIncreaseViewCount(Map<Long, Long> viewMap);

    /**
     * 按类型和状态统计商品
     * 对应Mapper方法：countByTypeAndStatus
     *
     * @return 统计结果
     */
    List<Map<String, Object>> countByTypeAndStatus();

    /**
     * 获取商品统计信息
     * 业务层方法，提供更丰富的统计数据
     *
     * @return 统计结果
     */
    List<Map<String, Object>> getGoodsStatistics();

    /**
     * 根据分类统计商品数量
     * 对应Mapper方法：countByCategory
     *
     * @param categoryId 分类ID
     * @param status     商品状态（可为空）
     * @return 商品数量
     */
    long countByCategory(Long categoryId, String status);

    /**
     * 根据商家统计商品数量
     * 对应Mapper方法：countBySeller
     *
     * @param sellerId 商家ID
     * @param status   商品状态（可为空）
     * @return 商品数量
     */
    long countBySeller(Long sellerId, String status);

    /**
     * 复合条件查询商品
     * 对应Mapper方法：findWithConditions
     * 支持多种查询条件和排序方式的组合
     *
     * @param page           分页参数
     * @param categoryId     分类ID（可为空）
     * @param sellerId       商家ID（可为空）
     * @param goodsType      商品类型（可为空）
     * @param nameKeyword    名称关键词（可为空）
     * @param minPrice       最低现金价格（可为空）
     * @param maxPrice       最高现金价格（可为空）
     * @param minCoinPrice   最低金币价格（可为空）
     * @param maxCoinPrice   最高金币价格（可为空）
     * @param hasStock       是否有库存（可为空）
     * @param status         商品状态（可为空）
     * @param orderBy        排序字段（可为空）
     * @param orderDirection 排序方向（可为空）
     * @return 商品列表
     */
    IPage<Goods> findWithConditions(Page<Goods> page, Long categoryId, Long sellerId, String goodsType,
                                    String nameKeyword, Object minPrice, Object maxPrice,
                                    Object minCoinPrice, Object maxCoinPrice, Boolean hasStock,
                                    String status, String orderBy, String orderDirection);

    // =================== 状态管理 ===================

    /**
     * 上架商品
     *
     * @param goodsId 商品ID
     * @return 是否成功
     */
    boolean publishGoods(Long goodsId);

    /**
     * 下架商品
     *
     * @param goodsId 商品ID
     * @return 是否成功
     */
    boolean unpublishGoods(Long goodsId);

    /**
     * 批量上架商品
     *
     * @param goodsIds 商品ID列表
     * @return 是否成功
     */
    boolean batchPublishGoods(List<Long> goodsIds);

    /**
     * 批量下架商品
     *
     * @param goodsIds 商品ID列表
     * @return 是否成功
     */
    boolean batchUnpublishGoods(List<Long> goodsIds);

    /**
     * 批量更新商品状态
     * 对应Mapper方法：batchUpdateStatus
     * 用于批量上架、下架等操作
     *
     * @param goodsIds 商品ID列表
     * @param status   新状态
     * @return 影响行数
     */
    int batchUpdateStatus(List<Long> goodsIds, String status);

    // =================== 业务验证 ===================

    /**
     * 验证商品是否可购买
     *
     * @param goodsId  商品ID
     * @param quantity 购买数量
     * @return 验证结果和错误信息
     */
    Map<String, Object> validatePurchase(Long goodsId, Integer quantity);

    /**
     * 验证商品数据完整性
     *
     * @param goods 商品信息
     * @return 验证结果和错误信息
     */
    Map<String, Object> validateGoodsData(Goods goods);

    // =================== Controller专用方法 ===================

    /**
     * 创建商品（Controller专用）
     *
     * @param request 商品创建请求
     * @return 创建结果
     */
    Result<Void> createGoodsForController(GoodsCreateRequest request);

    /**
     * 更新商品（Controller专用）
     *
     * @param id      商品ID
     * @param request 商品更新请求
     * @return 更新结果
     */
    Result<GoodsResponse> updateGoodsForController(Long id, GoodsCreateRequest request);

    /**
     * 删除商品（Controller专用）
     *
     * @param id 商品ID
     * @return 删除结果
     */
    Result<Void> deleteGoodsForController(Long id);

    /**
     * 批量删除商品（Controller专用）
     *
     * @param ids 商品ID列表
     * @return 删除结果
     */
    Result<Void> batchDeleteGoodsForController(List<Long> ids);

    /**
     * 分页查询商品（Controller专用）
     *
     * @param request 查询请求
     * @return 查询结果
     */
    Result<PageResponse<GoodsResponse>> queryGoodsForController(GoodsQuery request);

    /**
     * 根据分类查询商品（Controller专用）
     *
     * @param categoryId  分类ID
     * @param currentPage 当前页码
     * @param pageSize    页面大小
     * @return 查询结果
     */
    Result<PageResponse<GoodsResponse>> getGoodsByCategoryForController(Long categoryId, Integer currentPage, Integer pageSize);

    /**
     * 根据商家查询商品（Controller专用）
     *
     * @param sellerId    商家ID
     * @param currentPage 当前页码
     * @param pageSize    页面大小
     * @return 查询结果
     */
    Result<PageResponse<GoodsResponse>> getGoodsBySellerForController(Long sellerId, Integer currentPage, Integer pageSize);

    /**
     * 获取热门商品（Controller专用）
     *
     * @param goodsType   商品类型
     * @param currentPage 当前页码
     * @param pageSize    页面大小
     * @return 查询结果
     */
    Result<PageResponse<GoodsResponse>> getHotGoodsForController(String goodsType, Integer currentPage, Integer pageSize);

    /**
     * 搜索商品（Controller专用）
     *
     * @param keyword     搜索关键词
     * @param currentPage 当前页码
     * @param pageSize    页面大小
     * @return 查询结果
     */
    Result<PageResponse<GoodsResponse>> searchGoodsForController(String keyword, Integer currentPage, Integer pageSize);

    /**
     * 按价格区间查询商品（Controller专用）
     *
     * @param goodsType   商品类型
     * @param minPrice    最低价格
     * @param maxPrice    最高价格
     * @param currentPage 当前页码
     * @param pageSize    页面大小
     * @return 查询结果
     */
    Result<PageResponse<GoodsResponse>> getGoodsByPriceRangeForController(String goodsType, Object minPrice, Object maxPrice, Integer currentPage, Integer pageSize);

    /**
     * 上架商品（Controller专用）
     *
     * @param goodsId 商品ID
     * @return 操作结果
     */
    Result<Void> publishGoodsForController(Long goodsId);

    /**
     * 下架商品（Controller专用）
     *
     * @param goodsId 商品ID
     * @return 操作结果
     */
    Result<Void> unpublishGoodsForController(Long goodsId);

    /**
     * 批量上架商品（Controller专用）
     *
     * @param goodsIds 商品ID列表
     * @return 操作结果
     */
    Result<Void> batchPublishGoodsForController(List<Long> goodsIds);

    /**
     * 批量下架商品（Controller专用）
     *
     * @param goodsIds 商品ID列表
     * @return 操作结果
     */
    Result<Void> batchUnpublishGoodsForController(List<Long> goodsIds);

    // =================== 快捷查询方法 ===================

    /**
     * 获取商品购买信息
     *
     * @param goodsId  商品ID
     * @param quantity 购买数量
     * @return 购买信息
     */
    Result<Map<String, Object>> getGoodsPurchaseInfo(Long goodsId, Integer quantity);

    /**
     * 获取金币充值包
     *
     * @param currentPage 当前页码
     * @param pageSize    页面大小
     * @return 金币充值包列表
     */
    Result<PageResponse<GoodsResponse>> getCoinPackages(Integer currentPage, Integer pageSize);

    /**
     * 获取订阅服务
     *
     * @param currentPage 当前页码
     * @param pageSize    页面大小
     * @return 订阅服务列表
     */
    Result<PageResponse<GoodsResponse>> getSubscriptionServices(Integer currentPage, Integer pageSize);

    /**
     * 获取付费内容
     *
     * @param currentPage 当前页码
     * @param pageSize    页面大小
     * @return 付费内容列表
     */
    Result<PageResponse<GoodsResponse>> getContentGoods(Integer currentPage, Integer pageSize);

    /**
     * 获取实体商品
     *
     * @param currentPage 当前页码
     * @param pageSize    页面大小
     * @return 实体商品列表
     */
    Result<PageResponse<GoodsResponse>> getPhysicalGoods(Integer currentPage, Integer pageSize);

    // =================== 内容同步管理方法 ===================

    /**
     * 根据内容创建商品
     *
     * @param contentId       内容ID
     * @param contentTitle   内容标题
     * @param contentDesc    内容描述
     * @param categoryId     分类ID
     * @param categoryName   分类名称
     * @param authorId       作者ID
     * @param authorNickname 作者昵称
     * @param coverUrl       封面图URL
     * @param coinPrice      金币价格
     * @param contentStatus  内容状态
     * @return 创建结果
     */
    Result<Void> createGoodsFromContent(Long contentId, String contentTitle, String contentDesc,
                                        Long categoryId, String categoryName, Long authorId, String authorNickname,
                                        String coverUrl, Long coinPrice, String contentStatus);

    /**
     * 同步内容信息到商品
     *
     * @param contentId       内容ID
     * @param contentTitle   内容标题
     * @param contentDesc    内容描述
     * @param categoryId     分类ID
     * @param categoryName   分类名称
     * @param authorId       作者ID
     * @param authorNickname 作者昵称
     * @param coverUrl       封面图URL
     * @return 同步结果
     */
    Result<Void> syncContentToGoods(Long contentId, String contentTitle, String contentDesc,
                                    Long categoryId, String categoryName, Long authorId, String authorNickname,
                                    String coverUrl);

    /**
     * 同步内容状态到商品
     *
     * @param contentId     内容ID
     * @param contentStatus 内容状态
     * @return 同步结果
     */
    Result<Void> syncContentStatusToGoods(Long contentId, String contentStatus);

    /**
     * 同步内容价格到商品
     *
     * @param contentId 内容ID
     * @param coinPrice 金币价格
     * @param isActive  是否启用付费
     * @return 同步结果
     */
    Result<Void> syncContentPriceToGoods(Long contentId, Long coinPrice, Boolean isActive);

    /**
     * 批量同步内容商品
     *
     * @param batchSize 批处理大小
     * @return 同步结果
     */
    Result<Map<String, Object>> batchSyncContentGoods(Integer batchSize);

    /**
     * 删除内容对应的商品
     *
     * @param contentId 内容ID
     * @return 删除结果
     */
    Result<Void> deleteGoodsByContentId(Long contentId);

    /**
     * 根据内容ID获取商品（Controller专用）
     *
     * @param contentId 内容ID
     * @param goodsType 商品类型
     * @return 商品信息
     */
    Result<GoodsResponse> getGoodsByContentIdForController(Long contentId, String goodsType);

    // =================== 库存管理Controller专用方法 ===================

    /**
     * 检查库存是否充足（Controller专用）
     *
     * @param goodsId  商品ID
     * @param quantity 需要数量
     * @return 检查结果
     */
    Result<Boolean> checkStockForController(Long goodsId, Integer quantity);

    /**
     * 扣减库存（Controller专用）
     *
     * @param goodsId  商品ID
     * @param quantity 扣减数量
     * @return 操作结果
     */
    Result<Void> reduceStockForController(Long goodsId, Integer quantity);

    /**
     * 批量扣减库存（Controller专用）
     *
     * @param stockMap 商品ID和扣减数量的映射
     * @return 操作结果
     */
    Result<Void> batchReduceStockForController(Map<Long, Integer> stockMap);

    /**
     * 查询低库存商品（Controller专用）
     *
     * @param threshold 库存阈值
     * @return 商品列表
     */
    Result<List<GoodsResponse>> getLowStockGoodsForController(Integer threshold);

    // =================== 统计操作Controller专用方法 ===================

    /**
     * 增加商品销量（Controller专用）
     *
     * @param goodsId 商品ID
     * @param count   增加数量
     * @return 操作结果
     */
    Result<Void> increaseSalesCountForController(Long goodsId, Long count);

    /**
     * 增加商品浏览量（Controller专用）
     *
     * @param goodsId 商品ID
     * @param count   增加数量
     * @return 操作结果
     */
    Result<Void> increaseViewCountForController(Long goodsId, Long count);

    /**
     * 批量增加浏览量（Controller专用）
     *
     * @param viewMap 商品ID和浏览量的映射
     * @return 操作结果
     */
    Result<Void> batchIncreaseViewCountForController(Map<Long, Long> viewMap);

    /**
     * 获取商品统计信息（Controller专用）
     *
     * @return 统计结果
     */
    Result<List<Map<String, Object>>> getGoodsStatisticsForController();

    /**
     * 按类型和状态统计商品（Controller专用）
     *
     * @return 统计结果
     */
    Result<List<Map<String, Object>>> countByTypeAndStatusForController();

    /**
     * 根据分类统计商品数量（Controller专用）
     *
     * @param categoryId 分类ID
     * @param status     商品状态（可为空）
     * @return 统计结果
     */
    Result<Long> countByCategoryForController(Long categoryId, String status);

    /**
     * 根据商家统计商品数量（Controller专用）
     *
     * @param sellerId 商家ID
     * @param status   商品状态（可为空）
     * @return 统计结果
     */
    Result<Long> countBySellerForController(Long sellerId, String status);

    // =================== 查询操作Controller专用方法 ===================

    /**
     * 复合条件查询商品（Controller专用）
     *
     * @param categoryId     分类ID（可为空）
     * @param sellerId       商家ID（可为空）
     * @param goodsType      商品类型（可为空）
     * @param nameKeyword    名称关键词（可为空）
     * @param minPrice       最低现金价格（可为空）
     * @param maxPrice       最高现金价格（可为空）
     * @param minCoinPrice   最低金币价格（可为空）
     * @param maxCoinPrice   最高金币价格（可为空）
     * @param hasStock       是否有库存（可为空）
     * @param status         商品状态（可为空）
     * @param orderBy        排序字段（可为空）
     * @param orderDirection 排序方向（可为空）
     * @param currentPage    当前页码
     * @param pageSize       页面大小
     * @return 查询结果
     */
    Result<PageResponse<GoodsResponse>> findWithConditionsForController(Long categoryId, Long sellerId, String goodsType,
                                                                        String nameKeyword, Object minPrice, Object maxPrice,
                                                                        Object minCoinPrice, Object maxCoinPrice, Boolean hasStock,
                                                                        String status, String orderBy, String orderDirection,
                                                                        Integer currentPage, Integer pageSize);

    // =================== 状态管理Controller专用方法 ===================

    /**
     * 批量更新商品状态（Controller专用）
     *
     * @param goodsIds 商品ID列表
     * @param status   新状态
     * @return 操作结果
     */
    Result<Integer> batchUpdateStatusForController(List<Long> goodsIds, String status);

    // =================== 商品列表查询Controller专用方法 ===================

    /**
     * 商品列表查询（Controller专用）
     * 支持多种条件查询和分页，返回Result包装的分页响应
     *
     * @param goodsType 商品类型
     * @param status 商品状态
     * @param categoryId 分类ID
     * @param sellerId 卖家ID
     * @param keyword 关键词搜索
     * @param minPrice 最小价格
     * @param maxPrice 最大价格
     * @param hasStock 是否有库存
     * @param orderBy 排序字段
     * @param orderDirection 排序方向
     * @param currentPage 当前页码
     * @param pageSize 页面大小
     * @return 分页响应结果
     */
    Result<PageResponse<GoodsResponse>> listGoodsForController(String goodsType, String status, Long categoryId, Long sellerId, 
                                                              String keyword, Long minPrice, Long maxPrice, Boolean hasStock,
                                                              String orderBy, String orderDirection, Integer currentPage, Integer pageSize);
}