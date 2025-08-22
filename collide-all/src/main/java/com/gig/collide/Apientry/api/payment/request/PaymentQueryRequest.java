package com.gig.collide.Apientry.api.payment.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付查询请求 - 简洁版
 *
 * @author GIG Team
 * @version 2.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PaymentQueryRequest implements Serializable {

    /**
     * 支付单号
     */
    private String paymentNo;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 支付方式：alipay、wechat、balance
     */
    private String payMethod;

    /**
     * 支付状态：pending、success、failed、cancelled
     */
    private String status;

    /**
     * 第三方支付单号
     */
    private String thirdPartyNo;

    /**
     * 最小金额
     */
    private BigDecimal minAmount;

    /**
     * 最大金额
     */
    private BigDecimal maxAmount;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 排序方式：create_time、amount、pay_time
     */
    private String sortBy = "create_time";

    /**
     * 排序方向：asc、desc
     */
    private String sortDirection = "desc";

    /**
     * 当前页码（从0开始）
     */
    private int currentPage = 0;

    /**
     * 页面大小
     */
    private int pageSize = 10;

    /**
     * 转换为 PageRequest 对象
     */
    public PageRequest toPageRequest() {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        return PageRequest.of(currentPage, pageSize, sort);
    }
}
