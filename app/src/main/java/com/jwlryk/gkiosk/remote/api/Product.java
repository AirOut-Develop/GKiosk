package com.jwlryk.gkiosk.remote.api;

import com.google.gson.annotations.SerializedName;

public class Product {
    @SerializedName("id") public int id;
    @SerializedName("DeviceCode") public String deviceCode;
    @SerializedName("ProductCode") public String productCode;
    @SerializedName("name") public String name;
    @SerializedName("positionIndex") public int positionIndex;
    @SerializedName("displayIndex") public int displayIndex;
    @SerializedName("price") public int price;
    @SerializedName("priceRetail") public int priceRetail;
    @SerializedName("priceStore") public int priceStore;
    @SerializedName("priceSupply") public int priceSupply;
    @SerializedName("imageMain") public String imageMain;
    @SerializedName("imageDetail") public String imageDetail;
    @SerializedName("brand") public String brand;
    @SerializedName("category") public String category;
    @SerializedName("opt00") public String opt00;
    @SerializedName("opt01") public String opt01;
    @SerializedName("opt02") public String opt02;
    @SerializedName("opt03") public String opt03;
    @SerializedName("opt04") public String opt04;
    @SerializedName("type") public int type;
    @SerializedName("stock") public int stock;
    @SerializedName("status") public int status;
    @SerializedName("createdAt") public String createdAt;
    @SerializedName("updatedAt") public String updatedAt;
    @SerializedName("deletedAt") public String deletedAt;
}

