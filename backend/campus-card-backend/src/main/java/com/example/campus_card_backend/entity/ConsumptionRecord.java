package com.example.campus_card_backend.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ConsumptionRecord implements Serializable {
    private Long consumptionId;
    private String cardNumber;
    private String merchantId;
    private BigDecimal amount;
    private LocalDateTime consumptionTime;
    private String note;

    // 关联查询时显示持卡人信息
    private Long userId;
    private String userName;
    private BigDecimal balance;

    // 关联查询时显示商户信息
    private String merchantName;
    private String merchantType;
    private Integer businessStatus;

    public Long getConsumptionId() {
        return consumptionId;
    }

    public void setConsumptionId(Long consumptionId) {
        this.consumptionId = consumptionId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getConsumptionTime() {
        return consumptionTime;
    }

    public void setConsumptionTime(LocalDateTime consumptionTime) {
        this.consumptionTime = consumptionTime;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getMerchantType() {
        return merchantType;
    }

    public void setMerchantType(String merchantType) {
        this.merchantType = merchantType;
    }

    public Integer getBusinessStatus() {
        return businessStatus;
    }

    public void setBusinessStatus(Integer businessStatus) {
        this.businessStatus = businessStatus;
    }
}