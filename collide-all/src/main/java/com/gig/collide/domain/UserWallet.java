package com.gig.collide.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户钱包实体类 - 简洁版
 * 对应表：t_user_wallet
 * 支持现金+金币双货币系统
 * 
 * @author GIG Team
 * @version 2.0.0 (简洁版)
 * @since 2024-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Accessors(chain = true)
@TableName("t_user_wallet")
public class UserWallet {

    /**
     * 钱包ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 现金余额
     */
    @TableField("balance")
    private BigDecimal balance;

    /**
     * 冻结金额
     */
    @TableField("frozen_amount")
    private BigDecimal frozenAmount;

    /**
     * 金币余额（任务奖励虚拟货币）
     */
    @TableField("coin_balance")
    private Long coinBalance;

    /**
     * 累计获得金币
     */
    @TableField("coin_total_earned")
    private Long coinTotalEarned;

    /**
     * 累计消费金币
     */
    @TableField("coin_total_spent")
    private Long coinTotalSpent;

    /**
     * 总收入
     */
    @TableField("total_income")
    private BigDecimal totalIncome;

    /**
     * 总支出
     */
    @TableField("total_expense")
    private BigDecimal totalExpense;

    /**
     * 状态：active、frozen
     */
    @TableField("status")
    private String status;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // =================== 业务方法 ===================

    /**
     * 判断钱包是否活跃
     */
    public boolean isActive() {
        return "active".equals(this.status);
    }

    /**
     * 判断钱包是否被冻结
     */
    public boolean isFrozen() {
        return "frozen".equals(this.status);
    }

    /**
     * 获取可用现金余额
     */
    public BigDecimal getAvailableBalance() {
        if (this.balance == null) {
            return BigDecimal.ZERO;
        }
        if (this.frozenAmount == null) {
            return this.balance;
        }
        return this.balance.subtract(this.frozenAmount);
    }

    /**
     * 检查现金余额是否充足
     */
    public boolean hasSufficientBalance(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        return getAvailableBalance().compareTo(amount) >= 0;
    }

    /**
     * 检查金币余额是否充足
     */
    public boolean hasSufficientCoins(Long amount) {
        if (amount == null || amount <= 0) {
            return false;
        }
        return this.coinBalance != null && this.coinBalance >= amount;
    }

    /**
     * 增加现金余额
     */
    public UserWallet addBalance(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            this.balance = (this.balance != null ? this.balance : BigDecimal.ZERO).add(amount);
            this.totalIncome = (this.totalIncome != null ? this.totalIncome : BigDecimal.ZERO).add(amount);
        }
        return this;
    }

    /**
     * 减少现金余额
     */
    public UserWallet deductBalance(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0 && hasSufficientBalance(amount)) {
            this.balance = this.balance.subtract(amount);
            this.totalExpense = (this.totalExpense != null ? this.totalExpense : BigDecimal.ZERO).add(amount);
        }
        return this;
    }

    /**
     * 增加金币余额
     */
    public UserWallet addCoins(Long amount) {
        if (amount != null && amount > 0) {
            this.coinBalance = (this.coinBalance != null ? this.coinBalance : 0L) + amount;
            this.coinTotalEarned = (this.coinTotalEarned != null ? this.coinTotalEarned : 0L) + amount;
        }
        return this;
    }

    /**
     * 减少金币余额
     */
    public UserWallet deductCoins(Long amount) {
        if (amount != null && amount > 0 && hasSufficientCoins(amount)) {
            this.coinBalance = this.coinBalance - amount;
            this.coinTotalSpent = (this.coinTotalSpent != null ? this.coinTotalSpent : 0L) + amount;
        }
        return this;
    }

    /**
     * 冻结金额
     */
    public UserWallet freezeAmount(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0 && hasSufficientBalance(amount)) {
            this.frozenAmount = (this.frozenAmount != null ? this.frozenAmount : BigDecimal.ZERO).add(amount);
        }
        return this;
    }

    /**
     * 解冻金额
     */
    public UserWallet unfreezeAmount(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0 && this.frozenAmount != null) {
            if (this.frozenAmount.compareTo(amount) >= 0) {
                this.frozenAmount = this.frozenAmount.subtract(amount);
            } else {
                this.frozenAmount = BigDecimal.ZERO;
            }
        }
        return this;
    }

    /**
     * 获取钱包总资产（现金+金币）
     */
    public BigDecimal getTotalAssets() {
        BigDecimal cashValue = getAvailableBalance();
        BigDecimal coinValue = this.coinBalance != null ? new BigDecimal(this.coinBalance) : BigDecimal.ZERO;
        return cashValue.add(coinValue);
    }
}
