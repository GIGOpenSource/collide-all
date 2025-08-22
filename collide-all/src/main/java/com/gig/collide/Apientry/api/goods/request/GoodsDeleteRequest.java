package com.gig.collide.Apientry.api.goods.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;

/**
 * 商品删除请求 - 简洁版
 * 逻辑删除，将状态更新为inactive
 * 
 * @author Collide
 * @version 2.0.0 (简洁版)
 * @since 2024-01-01
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GoodsDeleteRequest implements Serializable {

    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为空")
    private Long id;

    /**
     * 删除原因
     */
    private String deleteReason;

    /**
     * 操作人ID
     */
    private Long operatorId;
} 