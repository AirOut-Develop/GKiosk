package com.jwlryk.gkiosk.remote.api;

import com.google.gson.annotations.SerializedName;

public class Store {
    @SerializedName("id") public int id;
    @SerializedName("class") public String storeClass;
    @SerializedName("code") public String code;
    @SerializedName("name") public String name;
    @SerializedName("type") public String type;
    @SerializedName("status") public int status;
    @SerializedName("companyNumber") public String companyNumber;
    @SerializedName("cardVanType") public String cardVanType;
    @SerializedName("cardVanCATID") public String cardVanCATID;
    @SerializedName("createdAt") public String createdAt;
    @SerializedName("updatedAt") public String updatedAt;
    @SerializedName("deletedAt") public String deletedAt;
}

