package com.gig.collide.service.Impl;


import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gig.collide.Apientry.api.common.response.PageResponse;
import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.Apientry.api.goods.request.GoodsCreateRequest;
import com.gig.collide.Apientry.api.goods.response.GoodsResponse;
import com.gig.collide.domain.Goods;
import com.gig.collide.domain.GoodsQuery;
import com.gig.collide.mapper.GoodsMapper;
import com.gig.collide.service.GoodsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 商品业务服务实现类 - 缓存增强版
 * 基于JetCache的高性能商品服务
 *
 * @author GIG Team
 * @version 2.0.0 (缓存增强版)
 * @since 2024-01-31
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoodsServiceImpl implements GoodsService {

    private final GoodsMapper goodsMapper;

    // =================== 基础CRUD操作 ===================

    @Override
    @Transactional(rollbackFor = Exception.class)
//    @CacheInvalidate(name = GoodsCacheConstant.GOODS_LIST_CACHE)
//    @CacheInvalidate(name = GoodsCacheConstant.GOODS_HOT_CACHE)
//    @CacheInvalidate(name = GoodsCacheConstant.GOODS_STATISTICS_CACHE)
    public Long createGoods(Goods goods) {
        log.info("创建商品: name={}, type={}, type={}, sellerId={}",
                goods.getName(), goods.getGoodsType(), goods.getSellerId());

        // 验证数据完整性
        Map<String, Object> validation = validateGoodsData(goods);
        if (!(Boolean) validation.get("valid")) {
            throw new IllegalArgumentException((String) validation.get("message"));
        }

        // 设置默认值
        setDefaultValues(goods);

        // 保存商品
        int result = goodsMapper.insert(goods);
        if (result > 0) {
            log.info("商品创建成功: id={}, name={}", goods.getId(), goods.getName());
            return goods.getId();
        } else {
            throw new RuntimeException("商品创建失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
//    @CacheInvalidate(name = GoodsCacheConstant.GOODS_LIST_CACHE)
//    @CacheInvalidate(name = GoodsCacheConstant.GOODS_HOT_CACHE)
//    @CacheInvalidate(name = GoodsCacheConstant.GOODS_STATISTICS_CACHE)
    public Long createGoods(GoodsCreateRequest request) {
        log.info("创建商品（从请求）: name={}, type={}, sellerId={}",
                request.getName(), request.getGoodsType(), request.getSellerId());

        // 验证请求数据
        request.validateTypeSpecificFields();

        // 转换为实体对象
        Goods goods = convertToEntity(request);

        // 调用原有的创建方法
        return createGoods(goods);
    }

    @Override
//    @Cached(name = GoodsCacheConstant.GOODS_DETAIL_CACHE,
//            key = GoodsCacheConstant.GOODS_DETAIL_KEY + "#id",
//            expire = GoodsCacheConstant.DETAIL_EXPIRE,
//            timeUnit = TimeUnit.MINUTES)
    public Goods getGoodsById(Long id) {
        log.debug("查询商品详情: id={}", id);

        if (id == null || id <= 0) {
            return null;
        }

        Goods goods = goodsMapper.selectById(id);
        if (goods != null) {
            log.debug("商品查询成功: id={}, name={}, type={}",
                    goods.getId(), goods.getName(), goods.getGoodsType());
        }

        return goods;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
//    @CacheUpdate(name = GoodsCacheConstant.GOODS_DETAIL_CACHE,
//            key = GoodsCacheConstant.GOODS_DETAIL_KEY + "#goods.id",
//            value = "#goods")
//    @CacheInvalidate(name = GoodsCacheConstant.GOODS_LIST_CACHE)
//    @CacheInvalidate(name = GoodsCacheConstant.GOODS_HOT_CACHE)
    public boolean updateGoods(Goods goods) {
        log.info("更新商品: id={}, name={}", goods.getId(), goods.getName());

        if (goods.getId() == null || goods.getId() <= 0) {
            throw new IllegalArgumentException("商品ID不能为空");
        }

        // 验证商品是否存在
        Goods existingGoods = goodsMapper.selectById(goods.getId());
        if (existingGoods == null) {
            throw new IllegalArgumentException("商品不存在: id=" + goods.getId());
        }

        // 设置更新时间
        goods.setUpdateTime(LocalDateTime.now());

        int result = goodsMapper.updateById(goods);
        if (result > 0) {
            log.info("商品更新成功: id={}", goods.getId());
            return true;
        } else {
            log.warn("商品更新失败: id={}", goods.getId());
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
//    @CacheInvalidate(name = GoodsCacheConstant.GOODS_DETAIL_CACHE,
//            key = GoodsCacheConstant.GOODS_DETAIL_KEY + "#id")
//    @CacheInvalidate(name = GoodsCacheConstant.GOODS_LIST_CACHE)
//    @CacheInvalidate(name = GoodsCacheConstant.GOODS_HOT_CACHE)
    public boolean deleteGoods(Long id) {
        log.info("删除商品: id={}", id);

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("商品ID不能为空");
        }

        // 软删除：更新状态为inactive
        LambdaUpdateWrapper<Goods> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Goods::getId, id)
                .set(Goods::getStatus, Goods.GoodsStatus.INACTIVE)
                .set(Goods::getUpdateTime, LocalDateTime.now());

        int result = goodsMapper.update(null, updateWrapper);
        if (result > 0) {
            log.info("商品删除成功: id={}", id);
            return true;
        } else {
            log.warn("商品删除失败: id={}", id);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
//    @CacheInvalidate(name = GoodsCacheConstant.GOODS_LIST_CACHE)
//    @CacheInvalidate(name = GoodsCacheConstant.GOODS_HOT_CACHE)
    public boolean batchDeleteGoods(List<Long> ids) {
        log.info("批量删除商品: count={}", ids.size());

        if (CollectionUtils.isEmpty(ids)) {
            return true;
        }

        LambdaUpdateWrapper<Goods> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(Goods::getId, ids)
                .set(Goods::getStatus, Goods.GoodsStatus.INACTIVE)
                .set(Goods::getUpdateTime, LocalDateTime.now());

        int result = goodsMapper.update(null, updateWrapper);
        log.info("批量删除商品完成: 目标={}, 实际={}", ids.size(), result);
        return result > 0;
    }

    // =================== 查询操作 ===================

    @Override
//    @Cached(name = GoodsCacheConstant.GOODS_LIST_CACHE,
//            key = "T(com.gig.collide.goods.infrastructure.cache.GoodsCacheConstant).buildListKey(#goodsType, #status, #page.current, #page.size)",
//            expire = GoodsCacheConstant.LIST_EXPIRE,
//            timeUnit = TimeUnit.MINUTES)
    public IPage<Goods> queryGoods(Page<Goods> page, String goodsType, String status) {
        log.debug("分页查询商品: type={}, status={}, page={}, size={}",
                goodsType, status, page.getCurrent(), page.getSize());

        return goodsMapper.selectByTypeAndStatus(page, goodsType, status);
    }

    @Override
//    @Cached(name = GoodsCacheConstant.GOODS_CATEGORY_CACHE,
//            key = "T(com.gig.collide.goods.infrastructure.cache.GoodsCacheConstant).buildCategoryKey(#categoryId, #page.current, #page.size)",
//            expire = GoodsCacheConstant.LIST_EXPIRE,
//            timeUnit = TimeUnit.MINUTES)
    public IPage<Goods> getGoodsByCategory(Page<Goods> page, Long categoryId, String status) {
        log.debug("根据分类查询商品: categoryId={}, status={}, page={}, size={}",
                categoryId, status, page.getCurrent(), page.getSize());

        return goodsMapper.selectByCategoryAndStatus(page, categoryId, status);
    }

    @Override
//    @Cached(name = GoodsCacheConstant.GOODS_SELLER_CACHE,
//            key = "T(com.gig.collide.goods.infrastructure.cache.GoodsCacheConstant).buildSellerKey(#sellerId, #page.current, #page.size)",
//            expire = GoodsCacheConstant.LIST_EXPIRE,
//            timeUnit = TimeUnit.MINUTES)
    public IPage<Goods> getGoodsBySeller(Page<Goods> page, Long sellerId, String status) {
        log.debug("根据商家查询商品: sellerId={}, status={}, page={}, size={}",
                sellerId, status, page.getCurrent(), page.getSize());

        return goodsMapper.selectBySellerAndStatus(page, sellerId, status);
    }

    @Override
//    @Cached(name = GoodsCacheConstant.GOODS_CONTENT_CACHE,
//            key = "T(com.gig.collide.goods.infrastructure.cache.GoodsCacheConstant).buildContentKey(#contentId, #goodsType)",
//            expire = GoodsCacheConstant.DETAIL_EXPIRE,
//            timeUnit = TimeUnit.MINUTES)
    public Goods getGoodsByContentId(Long contentId, String goodsType) {
        log.debug("根据内容ID查询商品: contentId={}, goodsType={}", contentId, goodsType);

        if (contentId == null || contentId <= 0) {
            log.warn("内容ID无效: {}", contentId);
            return null;
        }

        String type = StringUtils.hasText(goodsType) ? goodsType : "content";
        return goodsMapper.selectByContentId(contentId, type);
    }

    @Override
//    @Cached(name = GoodsCacheConstant.GOODS_HOT_CACHE,
//            key = "T(com.gig.collide.goods.infrastructure.cache.GoodsCacheConstant).buildHotKey(#goodsType, #page.current, #page.size)",
//            expire = GoodsCacheConstant.HOT_EXPIRE,
//            timeUnit = TimeUnit.MINUTES)
    public IPage<Goods> getHotGoods(Page<Goods> page, String goodsType) {
        log.debug("查询热门商品: type={}, page={}, size={}",
                goodsType, page.getCurrent(), page.getSize());

        return goodsMapper.selectHotGoods(page, goodsType);
    }

    @Override
//    @Cached(name = GoodsCacheConstant.GOODS_SEARCH_CACHE,
//            key = "T(com.gig.collide.goods.infrastructure.cache.GoodsCacheConstant).buildSearchKey(#keyword, #page.current, #page.size)",
//            expire = GoodsCacheConstant.SEARCH_EXPIRE,
//            timeUnit = TimeUnit.MINUTES)
    public IPage<Goods> searchGoods(Page<Goods> page, String keyword, String status) {
        log.debug("搜索商品: keyword={}, status={}, page={}, size={}",
                keyword, status, page.getCurrent(), page.getSize());

        if (!StringUtils.hasText(keyword)) {
            return new Page<>(page.getCurrent(), page.getSize());
        }

        return goodsMapper.searchGoods(page, keyword.trim(), status);
    }

    @Override
    public IPage<Goods> getGoodsByPriceRange(Page<Goods> page, Object minPrice, Object maxPrice, String goodsType) {
        log.debug("按价格区间查询商品: min={}, max={}, type={}, page={}, size={}",
                minPrice, maxPrice, goodsType, page.getCurrent(), page.getSize());

        return goodsMapper.selectByPriceRange(page, minPrice, maxPrice, goodsType);
    }

    // =================== 库存管理 ===================

    @Override
    public boolean checkStock(Long goodsId, Integer quantity) {
        log.debug("检查库存: goodsId={}, quantity={}", goodsId, quantity);

        Goods goods = getGoodsById(goodsId);
        if (goods == null) {
            return false;
        }

        return goods.hasStock(quantity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
//    @CacheInvalidate(name = GoodsCacheConstant.GOODS_DETAIL_CACHE,
//            key = GoodsCacheConstant.GOODS_DETAIL_KEY + "#goodsId")
    public boolean reduceStock(Long goodsId, Integer quantity) {
        log.info("扣减库存: goodsId={}, quantity={}", goodsId, quantity);

        if (goodsId == null || quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("参数无效");
        }

        // 检查库存
        if (!checkStock(goodsId, quantity)) {
            log.warn("库存不足: goodsId={}, 需求={}", goodsId, quantity);
            return false;
        }

        int result = goodsMapper.reduceStock(goodsId, quantity);
        if (result > 0) {
            log.info("库存扣减成功: goodsId={}, quantity={}", goodsId, quantity);
            return true;
        } else {
            log.warn("库存扣减失败: goodsId={}, quantity={}", goodsId, quantity);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchReduceStock(Map<Long, Integer> stockMap) {
        log.info("批量扣减库存: count={}", stockMap.size());

        if (CollectionUtils.isEmpty(stockMap)) {
            return true;
        }

        // 检查所有商品库存
        for (Map.Entry<Long, Integer> entry : stockMap.entrySet()) {
            if (!checkStock(entry.getKey(), entry.getValue())) {
                log.warn("批量库存检查失败: goodsId={}, 需求={}", entry.getKey(), entry.getValue());
                return false;
            }
        }

        // 批量扣减
        boolean allSuccess = true;
        for (Map.Entry<Long, Integer> entry : stockMap.entrySet()) {
            if (!reduceStock(entry.getKey(), entry.getValue())) {
                allSuccess = false;
                break;
            }
        }

        log.info("批量库存扣减完成: success={}", allSuccess);
        return allSuccess;
    }

    @Override
//    @Cached(name = GoodsCacheConstant.GOODS_LOW_STOCK_CACHE,
//            expire = GoodsCacheConstant.LOW_STOCK_EXPIRE,
//            timeUnit = TimeUnit.MINUTES)
    public List<Goods> getLowStockGoods(Integer threshold) {
        log.debug("查询低库存商品: threshold={}", threshold);

        if (threshold == null || threshold < 0) {
            threshold = 10; // 默认阈值
        }

        return goodsMapper.selectLowStockGoods(threshold);
    }

    // =================== 统计操作 ===================

    @Override
//    @CacheInvalidate(name = GoodsCacheConstant.GOODS_DETAIL_CACHE,
//            key = GoodsCacheConstant.GOODS_DETAIL_KEY + "#goodsId")
//    @CacheInvalidate(name = GoodsCacheConstant.GOODS_HOT_CACHE)
    public boolean increaseSalesCount(Long goodsId, Long count) {
        log.debug("增加销量: goodsId={}, count={}", goodsId, count);

        if (goodsId == null || count == null || count <= 0) {
            return false;
        }

        int result = goodsMapper.increaseSalesCount(goodsId, count);
        return result > 0;
    }

    @Override
//    @CacheInvalidate(name = GoodsCacheConstant.GOODS_DETAIL_CACHE,
//            key = GoodsCacheConstant.GOODS_DETAIL_KEY + "#goodsId")
    public boolean increaseViewCount(Long goodsId, Long count) {
        log.debug("增加浏览量: goodsId={}, count={}", goodsId, count);

        if (goodsId == null || count == null || count <= 0) {
            return false;
        }

        int result = goodsMapper.increaseViewCount(goodsId, count);
        return result > 0;
    }

    @Override
    public boolean batchIncreaseViewCount(Map<Long, Long> viewMap) {
        log.debug("批量增加浏览量: count={}", viewMap.size());

        if (CollectionUtils.isEmpty(viewMap)) {
            return true;
        }

        boolean allSuccess = true;
        for (Map.Entry<Long, Long> entry : viewMap.entrySet()) {
            if (!increaseViewCount(entry.getKey(), entry.getValue())) {
                allSuccess = false;
            }
        }

        return allSuccess;
    }

    @Override
//    @Cached(name = GoodsCacheConstant.GOODS_STATISTICS_CACHE,
//            expire = GoodsCacheConstant.STATISTICS_EXPIRE,
//            timeUnit = TimeUnit.MINUTES)
    public List<Map<String, Object>> countByTypeAndStatus() {
        log.debug("按类型和状态统计商品");
        return goodsMapper.countByTypeAndStatus();
    }

    @Override
    public List<Map<String, Object>> getGoodsStatistics() {
        log.debug("获取商品统计信息");
        // 调用基础统计方法，可以在此基础上添加更多统计逻辑
        return countByTypeAndStatus();
    }

    @Override
    public long countByCategory(Long categoryId, String status) {
        log.debug("根据分类统计商品数量: categoryId={}, status={}", categoryId, status);

        if (categoryId == null || categoryId <= 0) {
            return 0;
        }

        return goodsMapper.countByCategory(categoryId, status);
    }

    @Override
    public long countBySeller(Long sellerId, String status) {
        log.debug("根据商家统计商品数量: sellerId={}, status={}", sellerId, status);

        if (sellerId == null || sellerId <= 0) {
            return 0;
        }

        return goodsMapper.countBySeller(sellerId, status);
    }

    @Override
    public IPage<Goods> findWithConditions(Page<Goods> page, Long categoryId, Long sellerId, String goodsType,
                                           String nameKeyword, Object minPrice, Object maxPrice,
                                           Object minCoinPrice, Object maxCoinPrice, Boolean hasStock,
                                           String status, String orderBy, String orderDirection) {
        log.debug("复合条件查询商品: categoryId={}, sellerId={}, type={}, keyword={}, page={}, size={}",
                categoryId, sellerId, goodsType, nameKeyword, page.getCurrent(), page.getSize());

        return goodsMapper.findWithConditions(page, categoryId, sellerId, goodsType, nameKeyword,
                minPrice, maxPrice, minCoinPrice, maxCoinPrice, hasStock, status, orderBy, orderDirection);
    }

    // =================== 状态管理 ===================

    @Override
    @Transactional(rollbackFor = Exception.class)
//    @CacheInvalidate(name = GoodsCacheConstant.GOODS_DETAIL_CACHE,
//            key = GoodsCacheConstant.GOODS_DETAIL_KEY + "#goodsId")
//    @CacheInvalidate(name = GoodsCacheConstant.GOODS_LIST_CACHE)
    public boolean publishGoods(Long goodsId) {
        log.info("上架商品: goodsId={}", goodsId);
        return updateGoodsStatus(goodsId, Goods.GoodsStatus.ACTIVE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
//    @CacheInvalidate(name = GoodsCacheConstant.GOODS_DETAIL_CACHE,
//            key = GoodsCacheConstant.GOODS_DETAIL_KEY + "#goodsId")
//    @CacheInvalidate(name = GoodsCacheConstant.GOODS_LIST_CACHE)
    public boolean unpublishGoods(Long goodsId) {
        log.info("下架商品: goodsId={}", goodsId);
        return updateGoodsStatus(goodsId, Goods.GoodsStatus.INACTIVE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
//    @CacheInvalidate(name = GoodsCacheConstant.GOODS_LIST_CACHE)
    public boolean batchPublishGoods(List<Long> goodsIds) {
        log.info("批量上架商品: count={}", goodsIds.size());
        return batchUpdateGoodsStatus(goodsIds, Goods.GoodsStatus.ACTIVE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
//    @CacheInvalidate(name = GoodsCacheConstant.GOODS_LIST_CACHE)
    public boolean batchUnpublishGoods(List<Long> goodsIds) {
        log.info("批量下架商品: count={}", goodsIds.size());
        return batchUpdateGoodsStatus(goodsIds, Goods.GoodsStatus.INACTIVE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
//    @CacheInvalidate(name = GoodsCacheConstant.GOODS_LIST_CACHE)
//    @CacheInvalidate(name = GoodsCacheConstant.GOODS_HOT_CACHE)
    public int batchUpdateStatus(List<Long> goodsIds, String status) {
        log.info("批量更新商品状态: count={}, status={}", goodsIds.size(), status);

        if (CollectionUtils.isEmpty(goodsIds)) {
            return 0;
        }

        // 直接调用Mapper的批量更新方法
        int result = goodsMapper.batchUpdateStatus(goodsIds, status);
        log.info("批量状态更新完成: 目标={}, 实际={}", goodsIds.size(), result);
        return result;
    }

    // =================== 业务验证 ===================

    @Override
    public Map<String, Object> validatePurchase(Long goodsId, Integer quantity) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 获取商品信息
            Goods goods = getGoodsById(goodsId);
            if (goods == null) {
                result.put("valid", false);
                result.put("message", "商品不存在");
                return result;
            }

            // 检查商品状态
            if (goods.getStatus() != Goods.GoodsStatus.ACTIVE) {
                result.put("valid", false);
                result.put("message", "商品已下架");
                return result;
            }

            // 检查库存
            if (!goods.hasStock(quantity)) {
                result.put("valid", false);
                result.put("message", "库存不足");
                return result;
            }

            result.put("valid", true);
            result.put("goods", goods);

        } catch (Exception e) {
            log.error("购买验证失败: goodsId={}, quantity={}", goodsId, quantity, e);
            result.put("valid", false);
            result.put("message", "验证失败: " + e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> validateGoodsData(Goods goods) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 基础字段验证
            if (!StringUtils.hasText(goods.getName())) {
                result.put("valid", false);
                result.put("message", "商品名称不能为空");
                return result;
            }

            if (goods.getGoodsType() == null) {
                result.put("valid", false);
                result.put("message", "商品类型不能为空");
                return result;
            }

            if (goods.getSellerId() == null) {
                result.put("valid", false);
                result.put("message", "商家ID不能为空");
                return result;
            }

            // 根据商品类型验证特定字段
            switch (goods.getGoodsType()) {
                case COIN:
                    if (goods.getPrice() == null || goods.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                        result.put("valid", false);
                        result.put("message", "金币商品价格必须大于0");
                        return result;
                    }
                    if (goods.getCoinAmount() == null || goods.getCoinAmount() <= 0) {
                        result.put("valid", false);
                        result.put("message", "金币数量必须大于0");
                        return result;
                    }
                    break;

                case CONTENT:
                    if (goods.getCoinPrice() == null || goods.getCoinPrice() <= 0) {
                        result.put("valid", false);
                        result.put("message", "内容商品金币价格必须大于0");
                        return result;
                    }
                    if (goods.getContentId() == null) {
                        result.put("valid", false);
                        result.put("message", "内容ID不能为空");
                        return result;
                    }
                    break;

                case SUBSCRIPTION:
                    if (goods.getPrice() == null || goods.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                        result.put("valid", false);
                        result.put("message", "订阅商品价格必须大于0");
                        return result;
                    }
                    if (goods.getSubscriptionDuration() == null || goods.getSubscriptionDuration() <= 0) {
                        result.put("valid", false);
                        result.put("message", "订阅时长必须大于0");
                        return result;
                    }
                    break;

                case GOODS:
                    if (goods.getPrice() == null || goods.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                        result.put("valid", false);
                        result.put("message", "实体商品价格必须大于0");
                        return result;
                    }
                    break;
            }

            result.put("valid", true);

        } catch (Exception e) {
            log.error("商品数据验证失败: {}", e.getMessage(), e);
            result.put("valid", false);
            result.put("message", "数据验证失败: " + e.getMessage());
        }

        return result;
    }

    // =================== 私有方法 ===================

    /**
     * 转换请求为实体对象
     */
    private Goods convertToEntity(GoodsCreateRequest request) {
        Goods goods = new Goods();
        goods.setName(request.getName());
        goods.setDescription(request.getDescription());
        goods.setCategoryId(request.getCategoryId());
        goods.setCategoryName(request.getCategoryName());
        goods.setGoodsType(Goods.GoodsType.valueOf(request.getGoodsType().toUpperCase()));
        goods.setPrice(request.getPrice());
        goods.setOriginalPrice(request.getOriginalPrice());
        goods.setCoinPrice(request.getCoinPrice());
        goods.setCoinAmount(request.getCoinAmount());
        goods.setContentId(request.getContentId());
        goods.setContentTitle(request.getContentTitle());
        goods.setSubscriptionDuration(request.getSubscriptionDuration());
        goods.setSubscriptionType(request.getSubscriptionType());
        goods.setStock(request.getStock());
        goods.setCoverUrl(request.getCoverUrl());

        // 处理图片列表
        if (!CollectionUtils.isEmpty(request.getImages())) {
            goods.setImages(String.join(",", request.getImages()));
        }

        goods.setSellerId(request.getSellerId());
        goods.setSellerName(request.getSellerName());

        if (request.getStatus() != null) {
            goods.setStatus(Goods.GoodsStatus.valueOf(request.getStatus().toUpperCase()));
        }

        return goods;
    }

    /**
     * 设置商品默认值
     */
    private void setDefaultValues(Goods goods) {
        if (goods.getStatus() == null) {
            goods.setStatus(Goods.GoodsStatus.ACTIVE);
        }

        if (goods.getSalesCount() == null) {
            goods.setSalesCount(0L);
        }

        if (goods.getViewCount() == null) {
            goods.setViewCount(0L);
        }

        // 虚拟商品设置无限库存
        if (goods.isVirtual() && goods.getStock() == null) {
            goods.setStock(-1);
        }

        // 非内容类商品设置金币价格为0
        if (goods.getGoodsType() != Goods.GoodsType.CONTENT && goods.getCoinPrice() == null) {
            goods.setCoinPrice(0L);
        }

        // 非金币类商品设置金币数量为null
        if (goods.getGoodsType() != Goods.GoodsType.COIN) {
            goods.setCoinAmount(null);
        }
    }

    /**
     * 更新商品状态
     */
    private boolean updateGoodsStatus(Long goodsId, Goods.GoodsStatus status) {
        LambdaUpdateWrapper<Goods> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Goods::getId, goodsId)
                .set(Goods::getStatus, status)
                .set(Goods::getUpdateTime, LocalDateTime.now());

        return goodsMapper.update(null, updateWrapper) > 0;
    }

    /**
     * 批量更新商品状态
     */
    private boolean batchUpdateGoodsStatus(List<Long> goodsIds, Goods.GoodsStatus status) {
        if (CollectionUtils.isEmpty(goodsIds)) {
            return true;
        }

        LambdaUpdateWrapper<Goods> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(Goods::getId, goodsIds)
                .set(Goods::getStatus, status)
                .set(Goods::getUpdateTime, LocalDateTime.now());

        return goodsMapper.update(null, updateWrapper) > 0;
    }

    // =================== Controller专用方法实现 ===================

    @Override
    public Result<GoodsResponse> getGoodsByIdForController(Long id) {
        try {
            log.info("Controller层 - 获取商品详情: id={}", id);

            Goods goods = getGoodsById(id);
            if (goods != null) {
                GoodsResponse response = convertToResponse(goods);
                return Result.success(response);
            } else {
                return Result.error("商品不存在");
            }
        } catch (Exception e) {
            log.error("Controller层 - 获取商品详情失败", e);
            return Result.error("获取商品详情失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Void> createGoodsForController(GoodsCreateRequest request) {
        try {
            log.info("Controller层 - 创建商品: name={}, type={}", request.getName(), request.getGoodsType());

            Long goodsId = createGoods(request);
            if (goodsId != null && goodsId > 0) {
                log.info("Controller层 - 商品创建成功: id={}", goodsId);
                return Result.success(null);
            } else {
                return Result.error("商品创建失败");
            }
        } catch (Exception e) {
            log.error("Controller层 - 创建商品失败", e);
            return Result.error("创建商品失败: " + e.getMessage());
        }
    }

    @Override
    public Result<GoodsResponse> updateGoodsForController(Long id, GoodsCreateRequest request) {
        try {
            log.info("Controller层 - 更新商品: id={}, name={}", id, request.getName());

            // 获取现有商品
            Goods existingGoods = getGoodsById(id);
            if (existingGoods == null) {
                return Result.error("商品不存在");
            }

            // 更新商品信息
            Goods updatedGoods = convertToEntity(request);
            updatedGoods.setId(id);
            updatedGoods.setUpdateTime(LocalDateTime.now());

            boolean success = updateGoods(updatedGoods);
            if (success) {
                GoodsResponse response = convertToResponse(updatedGoods);
                return Result.success(response);
            } else {
                return Result.error("商品更新失败");
            }
        } catch (Exception e) {
            log.error("Controller层 - 更新商品失败", e);
            return Result.error("更新商品失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Void> deleteGoodsForController(Long id) {
        try {
            log.info("Controller层 - 删除商品: id={}", id);

            boolean success = deleteGoods(id);
            if (success) {
                return Result.success(null);
            } else {
                return Result.error("商品删除失败");
            }
        } catch (Exception e) {
            log.error("Controller层 - 删除商品失败", e);
            return Result.error("删除商品失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Void> batchDeleteGoodsForController(List<Long> ids) {
        try {
            log.info("Controller层 - 批量删除商品: count={}", ids.size());

            boolean success = batchDeleteGoods(ids);
            if (success) {
                return Result.success(null);
            } else {
                return Result.error("批量删除商品失败");
            }
        } catch (Exception e) {
            log.error("Controller层 - 批量删除商品失败", e);
            return Result.error("批量删除商品失败: " + e.getMessage());
        }
    }

    @Override
    public Result<PageResponse<GoodsResponse>> queryGoodsForController(GoodsQuery request) {
        try {
            log.info("Controller层 - 分页查询商品: type={}, page={}, size={}",
                    request.getGoodsType(), request.getCurrentPage(), request.getPageSize());

            Page<Goods> page = new Page<>(request.getCurrentPage(), request.getPageSize());
            IPage<Goods> goodsPage = queryGoods(page, request.getGoodsType(), request.getStatus());

            List<GoodsResponse> responses = goodsPage.getRecords().stream()
                    .map(this::convertToResponse)
                    .collect(java.util.stream.Collectors.toList());

            // 安全地获取当前页码和页面大小
            int currentPageValue = 1;
            int pageSizeValue = 20;

            if (Objects.nonNull(page.getCurrent())) {
                currentPageValue = (int) page.getCurrent();
            }
            if (Objects.nonNull(page.getSize())) {
                pageSizeValue = (int) page.getSize();
            }
            
            PageResponse<GoodsResponse> pageResponse = PageResponse.of(
                    responses, goodsPage.getTotal(), currentPageValue, pageSizeValue
            );

            return Result.success(pageResponse);
        } catch (Exception e) {
            log.error("Controller层 - 分页查询商品失败", e);
            return Result.error("分页查询商品失败: " + e.getMessage());
        }
    }


    @Override
    public Result<PageResponse<GoodsResponse>> getGoodsByCategoryForController(Long categoryId, Integer currentPage, Integer pageSize) {
        try {
            log.info("Controller层 - 根据分类查询商品: categoryId={}, page={}, size={}", categoryId, currentPage, pageSize);

            Page<Goods> page = new Page<>(currentPage, pageSize);
            IPage<Goods> goodsPage = getGoodsByCategory(page, categoryId, null);

            List<GoodsResponse> responses = goodsPage.getRecords().stream()
                    .map(this::convertToResponse)
                    .collect(java.util.stream.Collectors.toList());

            // 安全地获取当前页码和页面大小
            int currentPageValue = 1;
            int pageSizeValue = 20;

            if (Objects.nonNull(page.getCurrent())) {
                currentPageValue = (int) page.getCurrent();
            }
            if (Objects.nonNull(page.getSize())) {
                pageSizeValue = (int) page.getSize();
            }
            
            PageResponse<GoodsResponse> pageResponse = PageResponse.of(
                    responses, goodsPage.getTotal(), currentPageValue, pageSizeValue
            );

            return Result.success(pageResponse);
        } catch (Exception e) {
            log.error("Controller层 - 根据分类查询商品失败", e);
            return Result.error("根据分类查询商品失败: " + e.getMessage());
        }
    }

    @Override
    public Result<PageResponse<GoodsResponse>> getGoodsBySellerForController(Long sellerId, Integer currentPage, Integer pageSize) {
        try {
            log.info("Controller层 - 根据商家查询商品: sellerId={}, page={}, size={}", sellerId, currentPage, pageSize);

            Page<Goods> page = new Page<>(currentPage, pageSize);
            IPage<Goods> goodsPage = getGoodsBySeller(page, sellerId, null);

            List<GoodsResponse> responses = goodsPage.getRecords().stream()
                    .map(this::convertToResponse)
                    .collect(java.util.stream.Collectors.toList());

            // 安全地获取当前页码和页面大小
            int currentPageValue = 1;
            int pageSizeValue = 20;

            if (Objects.nonNull(page.getCurrent())) {
                currentPageValue = (int) page.getCurrent();
            }
            if (Objects.nonNull(page.getSize())) {
                pageSizeValue = (int) page.getSize();
            }
            
            PageResponse<GoodsResponse> pageResponse = PageResponse.of(
                    responses, goodsPage.getTotal(), currentPageValue, pageSizeValue
            );

            return Result.success(pageResponse);
        } catch (Exception e) {
            log.error("Controller层 - 根据商家查询商品失败", e);
            return Result.error("根据商家查询商品失败: " + e.getMessage());
        }
    }

    @Override
    public Result<PageResponse<GoodsResponse>> getHotGoodsForController(String goodsType, Integer currentPage, Integer pageSize) {
        try {
            log.info("Controller层 - 获取热门商品: type={}, page={}, size={}", goodsType, currentPage, pageSize);

            Page<Goods> page = new Page<>(currentPage, pageSize);
            IPage<Goods> goodsPage = getHotGoods(page, goodsType);

            List<GoodsResponse> responses = goodsPage.getRecords().stream()
                    .map(this::convertToResponse)
                    .collect(java.util.stream.Collectors.toList());

            // 安全地获取当前页码和页面大小
            int currentPageValue = 1;
            int pageSizeValue = 20;

            if (Objects.nonNull(page.getCurrent())) {
                currentPageValue = (int) page.getCurrent();
            }
            if (Objects.nonNull(page.getSize())) {
                pageSizeValue = (int) page.getSize();
            }
            
            PageResponse<GoodsResponse> pageResponse = PageResponse.of(
                    responses, goodsPage.getTotal(), currentPageValue, pageSizeValue
            );

            return Result.success(pageResponse);
        } catch (Exception e) {
            log.error("Controller层 - 获取热门商品失败", e);
            return Result.error("获取热门商品失败: " + e.getMessage());
        }
    }

    @Override
    public Result<PageResponse<GoodsResponse>> searchGoodsForController(String keyword, Integer currentPage, Integer pageSize) {
        try {
            log.info("Controller层 - 搜索商品: keyword={}, page={}, size={}", keyword, currentPage, pageSize);

            Page<Goods> page = new Page<>(currentPage, pageSize);
            IPage<Goods> goodsPage = searchGoods(page, keyword, null);

            List<GoodsResponse> responses = goodsPage.getRecords().stream()
                    .map(this::convertToResponse)
                    .collect(java.util.stream.Collectors.toList());

            PageResponse<GoodsResponse> pageResponse = PageResponse.of(
                    responses, goodsPage.getTotal(),
                    (int) goodsPage.getCurrent(), (int) goodsPage.getSize()
            );

            return Result.success(pageResponse);
        } catch (Exception e) {
            log.error("Controller层 - 搜索商品失败", e);
            return Result.error("搜索商品失败: " + e.getMessage());
        }
    }

    @Override
    public Result<PageResponse<GoodsResponse>> getGoodsByPriceRangeForController(String goodsType, Object minPrice, Object maxPrice, Integer currentPage, Integer pageSize) {
        try {
            log.info("Controller层 - 按价格区间查询商品: type={}, minPrice={}, maxPrice={}, page={}, size={}",
                    goodsType, minPrice, maxPrice, currentPage, pageSize);

            Page<Goods> page = new Page<>(currentPage, pageSize);
            IPage<Goods> goodsPage = getGoodsByPriceRange(page, minPrice, maxPrice, goodsType);

            List<GoodsResponse> responses = goodsPage.getRecords().stream()
                    .map(this::convertToResponse)
                    .collect(java.util.stream.Collectors.toList());

            // 安全地获取当前页码和页面大小
            int currentPageValue = 1;
            int pageSizeValue = 20;

            if (Objects.nonNull(page.getCurrent())) {
                currentPageValue = (int) page.getCurrent();
            }
            if (Objects.nonNull(page.getSize())) {
                pageSizeValue = (int) page.getSize();
            }
            
            PageResponse<GoodsResponse> pageResponse = PageResponse.of(
                    responses, goodsPage.getTotal(), currentPageValue, pageSizeValue
            );

            return Result.success(pageResponse);
        } catch (Exception e) {
            log.error("Controller层 - 按价格区间查询商品失败", e);
            return Result.error("按价格区间查询商品失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Void> publishGoodsForController(Long goodsId) {
        try {
            log.info("Controller层 - 上架商品: id={}", goodsId);

            boolean success = publishGoods(goodsId);
            if (success) {
                return Result.success(null);
            } else {
                return Result.error("商品上架失败");
            }
        } catch (Exception e) {
            log.error("Controller层 - 上架商品失败", e);
            return Result.error("上架商品失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Void> unpublishGoodsForController(Long goodsId) {
        try {
            log.info("Controller层 - 下架商品: id={}", goodsId);

            boolean success = unpublishGoods(goodsId);
            if (success) {
                return Result.success(null);
            } else {
                return Result.error("商品下架失败");
            }
        } catch (Exception e) {
            log.error("Controller层 - 下架商品失败", e);
            return Result.error("下架商品失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Void> batchPublishGoodsForController(List<Long> goodsIds) {
        try {
            log.info("Controller层 - 批量上架商品: count={}", goodsIds.size());

            boolean success = batchPublishGoods(goodsIds);
            if (success) {
                return Result.success(null);
            } else {
                return Result.error("批量上架商品失败");
            }
        } catch (Exception e) {
            log.error("Controller层 - 批量上架商品失败", e);
            return Result.error("批量上架商品失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Void> batchUnpublishGoodsForController(List<Long> goodsIds) {
        try {
            log.info("Controller层 - 批量下架商品: count={}", goodsIds.size());

            boolean success = batchUnpublishGoods(goodsIds);
            if (success) {
                return Result.success(null);
            } else {
                return Result.error("批量下架商品失败");
            }
        } catch (Exception e) {
            log.error("Controller层 - 批量下架商品失败", e);
            return Result.error("批量下架商品失败: " + e.getMessage());
        }
    }

    /**
     * 转换商品实体为响应对象
     */
    private GoodsResponse convertToResponse(Goods goods) {
        GoodsResponse response = new GoodsResponse();
        response.setId(goods.getId());
        response.setName(goods.getName());
        response.setDescription(goods.getDescription());
        response.setCategoryId(goods.getCategoryId());
        response.setCategoryName(goods.getCategoryName());
        response.setGoodsType(goods.getGoodsType().getCode());
        response.setPrice(goods.getPrice());
        response.setOriginalPrice(goods.getOriginalPrice());
        response.setCoinPrice(goods.getCoinPrice());
        response.setCoinAmount(goods.getCoinAmount());
        response.setContentId(goods.getContentId());
        response.setContentTitle(goods.getContentTitle());
        response.setSubscriptionDuration(goods.getSubscriptionDuration());
        response.setSubscriptionType(goods.getSubscriptionType());
        response.setStock(goods.getStock());
        response.setCoverUrl(goods.getCoverUrl());

        if (goods.getImages() != null) {
            response.setImages(java.util.Arrays.asList(goods.getImages().split(",")));
        }

        response.setSellerId(goods.getSellerId());
        response.setSellerName(goods.getSellerName());
        response.setStatus(goods.getStatus().getCode());
        response.setSalesCount(goods.getSalesCount());
        response.setViewCount(goods.getViewCount());
        response.setCreateTime(goods.getCreateTime());
        response.setUpdateTime(goods.getUpdateTime());

        return response;
    }

    // =================== 快捷查询方法实现 ===================

    @Override
    public Result<Map<String, Object>> getGoodsPurchaseInfo(Long goodsId, Integer quantity) {
        try {
            log.info("Controller层 - 获取商品购买信息: goodsId={}, quantity={}", goodsId, quantity);

            Map<String, Object> validation = validatePurchase(goodsId, quantity);
            if (!(Boolean) validation.get("valid")) {
                return Result.error((String) validation.get("message"));
            }

            Goods goods = (Goods) validation.get("goods");
            Map<String, Object> purchaseInfo = new HashMap<>();
            purchaseInfo.put("goodsId", goodsId);
            purchaseInfo.put("goodsName", goods.getName());
            purchaseInfo.put("quantity", quantity);
            purchaseInfo.put("unitPrice", goods.getPrice());
            purchaseInfo.put("totalPrice", goods.getPrice().multiply(new BigDecimal(quantity)));
            purchaseInfo.put("coinPrice", goods.getCoinPrice());
            purchaseInfo.put("totalCoinPrice", goods.getCoinPrice() * quantity);
            purchaseInfo.put("stock", goods.getStock());
            purchaseInfo.put("available", goods.hasStock(quantity));

            return Result.success(purchaseInfo);
        } catch (Exception e) {
            log.error("Controller层 - 获取商品购买信息失败", e);
            return Result.error("获取商品购买信息失败: " + e.getMessage());
        }
    }

    @Override
    public Result<PageResponse<GoodsResponse>> getCoinPackages(Integer currentPage, Integer pageSize) {
        try {
            log.info("Controller层 - 获取金币充值包: page={}, size={}", currentPage, pageSize);

            Page<Goods> page = new Page<>(currentPage, pageSize);
            IPage<Goods> goodsPage = queryGoods(page, "COIN", "ACTIVE");

            List<GoodsResponse> responses = goodsPage.getRecords().stream()
                    .map(this::convertToResponse)
                    .collect(java.util.stream.Collectors.toList());

            PageResponse<GoodsResponse> pageResponse = PageResponse.of(
                    responses, goodsPage.getTotal(),
                    (int) goodsPage.getCurrent(), (int) goodsPage.getSize()
            );

            return Result.success(pageResponse);
        } catch (Exception e) {
            log.error("Controller层 - 获取金币充值包失败", e);
            return Result.error("获取金币充值包失败: " + e.getMessage());
        }
    }

    @Override
    public Result<PageResponse<GoodsResponse>> getSubscriptionServices(Integer currentPage, Integer pageSize) {
        try {
            log.info("Controller层 - 获取订阅服务: page={}, size={}", currentPage, pageSize);

            Page<Goods> page = new Page<>(currentPage, pageSize);
            IPage<Goods> goodsPage = queryGoods(page, "SUBSCRIPTION", "ACTIVE");

            List<GoodsResponse> responses = goodsPage.getRecords().stream()
                    .map(this::convertToResponse)
                    .collect(java.util.stream.Collectors.toList());

            PageResponse<GoodsResponse> pageResponse = PageResponse.of(
                    responses, goodsPage.getTotal(),
                    (int) goodsPage.getCurrent(), (int) goodsPage.getSize()
            );

            return Result.success(pageResponse);
        } catch (Exception e) {
            log.error("Controller层 - 获取订阅服务失败", e);
            return Result.error("获取订阅服务失败: " + e.getMessage());
        }
    }

    @Override
    public Result<PageResponse<GoodsResponse>> getContentGoods(Integer currentPage, Integer pageSize) {
        try {
            log.info("Controller层 - 获取付费内容: page={}, size={}", currentPage, pageSize);

            Page<Goods> page = new Page<>(currentPage, pageSize);
            IPage<Goods> goodsPage = queryGoods(page, "CONTENT", "ACTIVE");

            List<GoodsResponse> responses = goodsPage.getRecords().stream()
                    .map(this::convertToResponse)
                    .collect(java.util.stream.Collectors.toList());

            PageResponse<GoodsResponse> pageResponse = PageResponse.of(
                    responses, goodsPage.getTotal(),
                    (int) goodsPage.getCurrent(), (int) goodsPage.getSize()
            );

            return Result.success(pageResponse);
        } catch (Exception e) {
            log.error("Controller层 - 获取付费内容失败", e);
            return Result.error("获取付费内容失败: " + e.getMessage());
        }
    }

    @Override
    public Result<PageResponse<GoodsResponse>> getPhysicalGoods(Integer currentPage, Integer pageSize) {
        try {
            log.info("Controller层 - 获取实体商品: page={}, size={}", currentPage, pageSize);

            Page<Goods> page = new Page<>(currentPage, pageSize);
            IPage<Goods> goodsPage = queryGoods(page, "GOODS", "ACTIVE");

            List<GoodsResponse> responses = goodsPage.getRecords().stream()
                    .map(this::convertToResponse)
                    .collect(java.util.stream.Collectors.toList());

            PageResponse<GoodsResponse> pageResponse = PageResponse.of(
                    responses, goodsPage.getTotal(),
                    (int) goodsPage.getCurrent(), (int) goodsPage.getSize()
            );

            return Result.success(pageResponse);
        } catch (Exception e) {
            log.error("Controller层 - 获取实体商品失败", e);
            return Result.error("获取实体商品失败: " + e.getMessage());
        }
    }

    // =================== 内容同步管理方法实现 ===================

    @Override
    public Result<Void> createGoodsFromContent(Long contentId, String contentTitle, String contentDesc,
                                               Long categoryId, String categoryName, Long authorId, String authorNickname,
                                               String coverUrl, Long coinPrice, String contentStatus) {
        try {
            log.info("Controller层 - 根据内容创建商品: contentId={}, title={}", contentId, contentTitle);

            // 检查是否已存在对应的商品
            Goods existingGoods = getGoodsByContentId(contentId, "CONTENT");
            if (existingGoods != null) {
                return Result.error("该内容已存在对应的商品记录");
            }

            // 创建商品请求对象
            GoodsCreateRequest request = new GoodsCreateRequest();
            request.setName(contentTitle);
            request.setDescription(contentDesc);
            request.setCategoryId(categoryId);
            request.setCategoryName(categoryName);
            request.setGoodsType("CONTENT");
            request.setContentId(contentId);
            request.setContentTitle(contentTitle);
            request.setCoinPrice(coinPrice != null ? coinPrice : 0L);
            request.setSellerId(authorId);
            request.setSellerName(authorNickname);
            request.setCoverUrl(coverUrl);
            request.setStatus("ACTIVE");

            Long goodsId = createGoods(request);
            if (goodsId != null && goodsId > 0) {
                log.info("Controller层 - 内容商品创建成功: contentId={}, goodsId={}", contentId, goodsId);
                return Result.success(null);
            } else {
                return Result.error("内容商品创建失败");
            }
        } catch (Exception e) {
            log.error("Controller层 - 根据内容创建商品失败", e);
            return Result.error("根据内容创建商品失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Void> syncContentToGoods(Long contentId, String contentTitle, String contentDesc,
                                           Long categoryId, String categoryName, Long authorId, String authorNickname,
                                           String coverUrl) {
        try {
            log.info("Controller层 - 同步内容信息到商品: contentId={}, title={}", contentId, contentTitle);

            Goods goods = getGoodsByContentId(contentId, "CONTENT");
            if (goods == null) {
                return Result.error("未找到对应的商品记录");
            }

            // 更新商品信息
            goods.setName(contentTitle);
            goods.setDescription(contentDesc);
            goods.setCategoryId(categoryId);
            goods.setCategoryName(categoryName);
            goods.setContentTitle(contentTitle);
            goods.setSellerId(authorId);
            goods.setSellerName(authorNickname);
            goods.setCoverUrl(coverUrl);
            goods.setUpdateTime(LocalDateTime.now());

            boolean success = updateGoods(goods);
            if (success) {
                log.info("Controller层 - 内容信息同步成功: contentId={}", contentId);
                return Result.success(null);
            } else {
                return Result.error("内容信息同步失败");
            }
        } catch (Exception e) {
            log.error("Controller层 - 同步内容信息到商品失败", e);
            return Result.error("同步内容信息到商品失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Void> syncContentStatusToGoods(Long contentId, String contentStatus) {
        try {
            log.info("Controller层 - 同步内容状态到商品: contentId={}, status={}", contentId, contentStatus);

            Goods goods = getGoodsByContentId(contentId, "CONTENT");
            if (goods == null) {
                return Result.error("未找到对应的商品记录");
            }

            // 根据内容状态设置商品状态
            Goods.GoodsStatus goodsStatus;
            switch (contentStatus.toUpperCase()) {
                case "PUBLISHED":
                    goodsStatus = Goods.GoodsStatus.ACTIVE;
                    break;
                case "DRAFT":
                case "ARCHIVED":
                    goodsStatus = Goods.GoodsStatus.INACTIVE;
                    break;
                default:
                    goodsStatus = Goods.GoodsStatus.INACTIVE;
            }

            goods.setStatus(goodsStatus);
            goods.setUpdateTime(LocalDateTime.now());

            boolean success = updateGoods(goods);
            if (success) {
                log.info("Controller层 - 内容状态同步成功: contentId={}, status={}", contentId, contentStatus);
                return Result.success(null);
            } else {
                return Result.error("内容状态同步失败");
            }
        } catch (Exception e) {
            log.error("Controller层 - 同步内容状态到商品失败", e);
            return Result.error("同步内容状态到商品失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Void> syncContentPriceToGoods(Long contentId, Long coinPrice, Boolean isActive) {
        try {
            log.info("Controller层 - 同步内容价格到商品: contentId={}, coinPrice={}, isActive={}",
                    contentId, coinPrice, isActive);

            Goods goods = getGoodsByContentId(contentId, "CONTENT");
            if (goods == null) {
                return Result.error("未找到对应的商品记录");
            }

            // 更新金币价格
            goods.setCoinPrice(coinPrice != null ? coinPrice : 0L);

            // 根据是否启用付费设置商品状态
            if (isActive != null) {
                goods.setStatus(isActive ? Goods.GoodsStatus.ACTIVE : Goods.GoodsStatus.INACTIVE);
            }

            goods.setUpdateTime(LocalDateTime.now());

            boolean success = updateGoods(goods);
            if (success) {
                log.info("Controller层 - 内容价格同步成功: contentId={}, coinPrice={}", contentId, coinPrice);
                return Result.success(null);
            } else {
                return Result.error("内容价格同步失败");
            }
        } catch (Exception e) {
            log.error("Controller层 - 同步内容价格到商品失败", e);
            return Result.error("同步内容价格到商品失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Map<String, Object>> batchSyncContentGoods(Integer batchSize) {
        try {
            log.info("Controller层 - 批量同步内容商品: batchSize={}", batchSize);

            // 这里应该实现批量同步逻辑
            // 由于涉及复杂的内容查询和同步，这里提供一个基础实现
            Map<String, Object> result = new HashMap<>();
            result.put("totalProcessed", 0);
            result.put("successCount", 0);
            result.put("failureCount", 0);
            result.put("message", "批量同步功能待实现");

            log.info("Controller层 - 批量同步内容商品完成: {}", result);
            return Result.success(result);
        } catch (Exception e) {
            log.error("Controller层 - 批量同步内容商品失败", e);
            return Result.error("批量同步内容商品失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Void> deleteGoodsByContentId(Long contentId) {
        try {
            log.info("Controller层 - 删除内容对应的商品: contentId={}", contentId);

            Goods goods = getGoodsByContentId(contentId, "CONTENT");
            if (goods == null) {
                return Result.error("未找到对应的商品记录");
            }

            boolean success = deleteGoods(goods.getId());
            if (success) {
                log.info("Controller层 - 内容商品删除成功: contentId={}", contentId);
                return Result.success(null);
            } else {
                return Result.error("内容商品删除失败");
            }
        } catch (Exception e) {
            log.error("Controller层 - 删除内容对应的商品失败", e);
            return Result.error("删除内容对应的商品失败: " + e.getMessage());
        }
    }

    @Override
    public Result<GoodsResponse> getGoodsByContentIdForController(Long contentId, String goodsType) {
        try {
            log.info("Controller层 - 根据内容ID获取商品: contentId={}, goodsType={}", contentId, goodsType);

            Goods goods = getGoodsByContentId(contentId, goodsType);
            if (goods != null) {
                GoodsResponse response = convertToResponse(goods);
                return Result.success(response);
            } else {
                return Result.error("未找到对应的商品记录");
            }
        } catch (Exception e) {
            log.error("Controller层 - 根据内容ID获取商品失败", e);
            return Result.error("根据内容ID获取商品失败: " + e.getMessage());
        }
    }

    // =================== 库存管理Controller专用方法实现 ===================

    @Override
    public Result<Boolean> checkStockForController(Long goodsId, Integer quantity) {
        try {
            log.info("Controller层 - 检查库存: goodsId={}, quantity={}", goodsId, quantity);

            boolean hasStock = checkStock(goodsId, quantity);
            return Result.success(hasStock);
        } catch (Exception e) {
            log.error("Controller层 - 检查库存失败", e);
            return Result.error("检查库存失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Void> reduceStockForController(Long goodsId, Integer quantity) {
        try {
            log.info("Controller层 - 扣减库存: goodsId={}, quantity={}", goodsId, quantity);

            boolean success = reduceStock(goodsId, quantity);
            if (success) {
                return Result.success(null);
            } else {
                return Result.error("库存扣减失败");
            }
        } catch (Exception e) {
            log.error("Controller层 - 扣减库存失败", e);
            return Result.error("扣减库存失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Void> batchReduceStockForController(Map<Long, Integer> stockMap) {
        try {
            log.info("Controller层 - 批量扣减库存: count={}", stockMap.size());

            boolean success = batchReduceStock(stockMap);
            if (success) {
                return Result.success(null);
            } else {
                return Result.error("批量扣减库存失败");
            }
        } catch (Exception e) {
            log.error("Controller层 - 批量扣减库存失败", e);
            return Result.error("批量扣减库存失败: " + e.getMessage());
        }
    }

    @Override
    public Result<List<GoodsResponse>> getLowStockGoodsForController(Integer threshold) {
        try {
            log.info("Controller层 - 查询低库存商品: threshold={}", threshold);

            List<Goods> goodsList = getLowStockGoods(threshold);
            List<GoodsResponse> responses = goodsList.stream()
                    .map(this::convertToResponse)
                    .collect(java.util.stream.Collectors.toList());

            return Result.success(responses);
        } catch (Exception e) {
            log.error("Controller层 - 查询低库存商品失败", e);
            return Result.error("查询低库存商品失败: " + e.getMessage());
        }
    }

    // =================== 统计操作Controller专用方法实现 ===================

    @Override
    public Result<Void> increaseSalesCountForController(Long goodsId, Long count) {
        try {
            log.info("Controller层 - 增加销量: goodsId={}, count={}", goodsId, count);

            boolean success = increaseSalesCount(goodsId, count);
            if (success) {
                return Result.success(null);
            } else {
                return Result.error("增加销量失败");
            }
        } catch (Exception e) {
            log.error("Controller层 - 增加销量失败", e);
            return Result.error("增加销量失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Void> increaseViewCountForController(Long goodsId, Long count) {
        try {
            log.info("Controller层 - 增加浏览量: goodsId={}, count={}", goodsId, count);

            boolean success = increaseViewCount(goodsId, count);
            if (success) {
                return Result.success(null);
            } else {
                return Result.error("增加浏览量失败");
            }
        } catch (Exception e) {
            log.error("Controller层 - 增加浏览量失败", e);
            return Result.error("增加浏览量失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Void> batchIncreaseViewCountForController(Map<Long, Long> viewMap) {
        try {
            log.info("Controller层 - 批量增加浏览量: count={}", viewMap.size());

            boolean success = batchIncreaseViewCount(viewMap);
            if (success) {
                return Result.success(null);
            } else {
                return Result.error("批量增加浏览量失败");
            }
        } catch (Exception e) {
            log.error("Controller层 - 批量增加浏览量失败", e);
            return Result.error("批量增加浏览量失败: " + e.getMessage());
        }
    }

    @Override
    public Result<List<Map<String, Object>>> getGoodsStatisticsForController() {
        try {
            log.info("Controller层 - 获取商品统计信息");

            List<Map<String, Object>> statistics = getGoodsStatistics();
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("Controller层 - 获取商品统计信息失败", e);
            return Result.error("获取商品统计信息失败: " + e.getMessage());
        }
    }

    @Override
    public Result<List<Map<String, Object>>> countByTypeAndStatusForController() {
        try {
            log.info("Controller层 - 按类型和状态统计商品");

            List<Map<String, Object>> statistics = countByTypeAndStatus();
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("Controller层 - 按类型和状态统计商品失败", e);
            return Result.error("按类型和状态统计商品失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Long> countByCategoryForController(Long categoryId, String status) {
        try {
            log.info("Controller层 - 根据分类统计商品数量: categoryId={}, status={}", categoryId, status);

            long count = countByCategory(categoryId, status);
            return Result.success(count);
        } catch (Exception e) {
            log.error("Controller层 - 根据分类统计商品数量失败", e);
            return Result.error("根据分类统计商品数量失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Long> countBySellerForController(Long sellerId, String status) {
        try {
            log.info("Controller层 - 根据商家统计商品数量: sellerId={}, status={}", sellerId, status);

            long count = countBySeller(sellerId, status);
            return Result.success(count);
        } catch (Exception e) {
            log.error("Controller层 - 根据商家统计商品数量失败", e);
            return Result.error("根据商家统计商品数量失败: " + e.getMessage());
        }
    }

    // =================== 查询操作Controller专用方法实现 ===================

    @Override
    public Result<PageResponse<GoodsResponse>> findWithConditionsForController(Long categoryId, Long sellerId, String goodsType,
                                                                               String nameKeyword, Object minPrice, Object maxPrice,
                                                                               Object minCoinPrice, Object maxCoinPrice, Boolean hasStock,
                                                                               String status, String orderBy, String orderDirection,
                                                                               Integer currentPage, Integer pageSize) {
        try {
            log.info("Controller层 - 复合条件查询商品: categoryId={}, sellerId={}, type={}, keyword={}, page={}, size={}",
                    categoryId, sellerId, goodsType, nameKeyword, currentPage, pageSize);

            Page<Goods> page = new Page<>(currentPage, pageSize);
            IPage<Goods> goodsPage = findWithConditions(page, categoryId, sellerId, goodsType, nameKeyword,
                    minPrice, maxPrice, minCoinPrice, maxCoinPrice, hasStock, status, orderBy, orderDirection);

            List<GoodsResponse> responses = goodsPage.getRecords().stream()
                    .map(this::convertToResponse)
                    .collect(java.util.stream.Collectors.toList());

            PageResponse<GoodsResponse> pageResponse = PageResponse.of(
                    responses, goodsPage.getTotal(),
                    (int) goodsPage.getCurrent(), (int) goodsPage.getSize()
            );

            return Result.success(pageResponse);
        } catch (Exception e) {
            log.error("Controller层 - 复合条件查询商品失败", e);
            return Result.error("复合条件查询商品失败: " + e.getMessage());
        }
    }

    // =================== 状态管理Controller专用方法实现 ===================

    @Override
    public Result<Integer> batchUpdateStatusForController(List<Long> goodsIds, String status) {
        try {
            log.info("Controller层 - 批量更新商品状态: count={}, status={}", goodsIds.size(), status);

            int affectedRows = batchUpdateStatus(goodsIds, status);
            return Result.success(affectedRows);
        } catch (Exception e) {
            log.error("Controller层 - 批量更新商品状态失败", e);
            return Result.error("批量更新商品状态失败: " + e.getMessage());
        }
    }

    // =================== 商品列表查询Controller专用方法实现 ===================

    @Override
    public Result<PageResponse<GoodsResponse>> listGoodsForController(String goodsType, String status, Long categoryId, Long sellerId, 
                                                                     String keyword, Long minPrice, Long maxPrice, Boolean hasStock,
                                                                     String orderBy, String orderDirection, Integer currentPage, Integer pageSize) {
        try {
            log.info("Controller层 - 商品列表查询: type={}, status={}, categoryId={}, sellerId={}, keyword={}, minPrice={}, maxPrice={}, hasStock={}, orderBy={}, orderDirection={}, page={}, size={}",
                    goodsType, status, categoryId, sellerId, keyword, minPrice, maxPrice, hasStock, orderBy, orderDirection, currentPage, pageSize);

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
            Page<Goods> page = new Page<>(currentPage, pageSize);
            
            // 调用Mapper查询
            IPage<Goods> goodsPage = goodsMapper.selectGoodsList(page, goodsType, status, categoryId, sellerId, keyword, minPrice, maxPrice, hasStock, orderBy, orderDirection);
            
            // 转换为Response对象
            List<GoodsResponse> responses = goodsPage.getRecords().stream()
                    .map(this::convertToResponse)
                    .collect(java.util.stream.Collectors.toList());
            
            // 构建分页响应
            PageResponse<GoodsResponse> pageResponse = new PageResponse<>();
            pageResponse.setRecords(responses);
            pageResponse.setTotal(goodsPage.getTotal());
            pageResponse.setCurrentPage(currentPage);
            pageResponse.setPageSize(pageSize);
            pageResponse.setTotalPages((int) Math.ceil((double) goodsPage.getTotal() / pageSize));
            
            log.info("商品列表查询成功: 总数={}, 当前页={}, 页面大小={}", goodsPage.getTotal(), currentPage, pageSize);
            return Result.success(pageResponse);
            
        } catch (Exception e) {
            log.error("Controller层 - 商品列表查询失败", e);
            return Result.error("商品列表查询失败: " + e.getMessage());
        }
    }
}
