package com.jwlryk.gkiosk.remote.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Sale {
    @SerializedName("storeCode") public String storeCode;
    @SerializedName("deviceCode") public String deviceCode;
    @SerializedName("payMethod") public String payMethod;
    @SerializedName("realPrice") public int realPrice;
    @SerializedName("discountPrice") public int discountPrice;
    @SerializedName("totalPrice") public int totalPrice;
    @SerializedName("paymentData") public Object paymentData; // keep field name; structure supplied externally
    @SerializedName("opt00") public String opt00;
    @SerializedName("opt01") public String opt01;
    @SerializedName("opt02") public String opt02;
    @SerializedName("opt03") public String opt03;
    @SerializedName("opt04") public String opt04;
    @SerializedName("products") public List<ProductSale> products;
}

