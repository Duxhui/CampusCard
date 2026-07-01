package com.example.campus_card_backend.entity;

import java.io.Serializable;

public class Merchant implements Serializable {
    private String merchantId;
    private String merchantName;
    private String merchantType;
    private String location;
    private Integer businessStatus;

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getBusinessStatus() {
        return businessStatus;
    }

    public void setBusinessStatus(Integer businessStatus) {
        this.businessStatus = businessStatus;
    }
}