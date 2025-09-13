package com.jwlryk.ogkiosk.API;

import com.google.gson.annotations.SerializedName;
import com.jwlryk.ogkiosk.NiceTransactionData;

import java.util.List;

// Sale 데이터 모델
public class Sale {

    @SerializedName("storeCode")
    private String storeCode;

    @SerializedName("deviceCode")
    private String deviceCode;

    @SerializedName("payMethod")
    private String payMethod;

    @SerializedName("realPrice")
    private int realPrice;

    @SerializedName("discountPrice")
    private int discountPrice;

    @SerializedName("totalPrice")
    private int totalPrice;

    @SerializedName("paymentData")
    private NiceTransactionData paymentData;  // 수정된 부분

    @SerializedName("opt00")
    private String opt00;

    @SerializedName("opt01")
    private String opt01;

    @SerializedName("opt02")
    private String opt02;

    @SerializedName("opt03")
    private String opt03;

    @SerializedName("opt04")
    private String opt04;

    @SerializedName("products")
    private List<ProductSale> products;

    // Getters and setters
    public String getStoreCode() {
        return storeCode;
    }

    public void setStoreCode(String storeCode) {
        this.storeCode = storeCode;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }

    public String getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
    }

    public int getRealPrice() {
        return realPrice;
    }

    public void setRealPrice(int realPrice) {
        this.realPrice = realPrice;
    }

    public int getDiscountPrice() {
        return discountPrice;
    }

    public void setDiscountPrice(int discountPrice) {
        this.discountPrice = discountPrice;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }

    public NiceTransactionData getPaymentData() {
        return paymentData;
    }

    public void setPaymentData(NiceTransactionData paymentData) {
        this.paymentData = paymentData;
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

    public String getOpt02() {
        return opt02;
    }

    public void setOpt02(String opt02) {
        this.opt02 = opt02;
    }

    public String getOpt03() {
        return opt03;
    }

    public void setOpt03(String opt03) {
        this.opt03 = opt03;
    }

    public String getOpt04() {
        return opt04;
    }

    public void setOpt04(String opt04) {
        this.opt04 = opt04;
    }

    public List<ProductSale> getProducts() {
        return products;
    }

    public void setProducts(List<ProductSale> products) {
        this.products = products;
    }
}
