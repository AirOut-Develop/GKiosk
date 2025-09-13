package com.jwlryk.ogkiosk.API;


import com.google.gson.annotations.SerializedName;

// ProductSale 모델
public class ProductSale {

    @SerializedName("productNumber")  // 새 필드 추가
    private int productNumber;
    @SerializedName("productCode")
    private String productCode;

    @SerializedName("amount")
    private int amount;

    @SerializedName("eachPrice")
    private int eachPrice;

    @SerializedName("opt00")
    private String opt00;

    @SerializedName("opt01")
    private String opt01;

    // Constructor
    public ProductSale(int productNumber, String productCode, int amount, int eachPrice, String opt00, String opt01) {
        this.productNumber = productNumber;  // 새 필드 초기화
        this.productCode = productCode;
        this.amount = amount;
        this.eachPrice = eachPrice;
        this.opt00 = opt00;
        this.opt01 = opt01;
    }

    public int getProductNumber() {
        return productNumber;
    }

    public void setProductNumber(int productNumber) {
        this.productNumber = productNumber;
    }

    // Getters and setters
    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getEachPrice() {
        return eachPrice;
    }

    public void setEachPrice(int eachPrice) {
        this.eachPrice = eachPrice;
    }

    public String getOpt00() {
        return opt00;
    }

    public void setOpt00(String opt00) {
        this.opt00 = opt00;
    }

    public String getOpt01() {
        return opt01;
    }

    public void setOpt01(String opt01) {
        this.opt01 = opt01;
    }
}