package com.gig.collide.Apientry.api.payment.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付响应 - 简洁版
 * 基于t_payment表结构
 * 
 * @author GIG Team
 * @version 2.0.0
 */
@Data
public class PaymentResponse implements Serializable {

    private Long id;

    private String paymentNo;

    private Long orderId;

    private String orderNo;

    private Long userId;

    private String userNickname;

    private BigDecimal amount;

    private String payMethod;

    private String payChannel;

    private String thirdPartyNo;

    private String status;

    private LocalDateTime payTime;

    private LocalDateTime notifyTime;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 支付二维码或链接（用于前端展示）
     */
    private String payUrl;

    /**
     * 支付状态描述
     */
    private String statusDesc;

    /**
     * 是否可以取消
     */
    private Boolean cancellable;
} 