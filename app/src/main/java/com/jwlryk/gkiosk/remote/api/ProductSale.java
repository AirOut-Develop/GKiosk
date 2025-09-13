package com.jwlryk.gkiosk.remote.api;

import com.google.gson.annotations.SerializedName;

public class ProductSale {
    @SerializedName("productNumber") public int productNumber;
    @SerializedName("productCode") public String productCode;
    @SerializedName("amount") public int amount;
    @SerializedName("eachPrice") public int eachPrice;
    @SerializedName("opt00") public String opt00;
    @SerializedName("opt01") public String opt01;

    public ProductSale(int productNumber, String productCode, int amount, int eachPrice, String opt00, String opt01) {
        this.productNumber = productNumber;
        this.productCode = productCode;
        this.amount = amount;
        this.eachPrice = eachPrice;
        this.opt00 = opt00;
        this.opt01 = opt01;
    }
}

