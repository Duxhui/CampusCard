package com.example.campus_card_backend.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Card implements Serializable {
    private String cardNumber;
    private Long userId;
    private Integer cardStatus;
    private LocalDateTime issueDate;
    private LocalDateTime cancelDate;

    // 关联查询时显示持卡人姓名
    private String userName;

    // 关联查询时显示用户余额
    private String balance;

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getCardStatus() {
        return cardStatus;
    }

    public void setCardStatus(Integer cardStatus) {
        this.cardStatus = cardStatus;
    }

    public LocalDateTime getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDateTime issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDateTime getCancelDate() {
        return cancelDate;
    }

    public void setCancelDate(LocalDateTime cancelDate) {
        this.cancelDate = cancelDate;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }
}