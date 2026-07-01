package com.example.campus_card_backend.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RechargeRecord implements Serializable {
    private Long rechargeId;
    private String cardNumber;
    private BigDecimal amount;
    private Integer rechargeMethod;
    private LocalDateTime rechargeTime;
    private Integer status;

    // 关联查询时显示持卡人姓名
    private String userName;

    // 关联查询时显示用户ID
    private Long userId;

    public Long getRechargeId() {
        return rechargeId;
    }

    public void setRechargeId(Long rechargeId) {
        this.rechargeId = rechargeId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getRechargeMethod() {
        return rechargeMethod;
    }

    public void setRechargeMethod(Integer rechargeMethod) {
        this.rechargeMethod = rechargeMethod;
    }

    public LocalDateTime getRechargeTime() {
        return rechargeTime;
    }

    public void setRechargeTime(LocalDateTime rechargeTime) {
        this.rechargeTime = rechargeTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}