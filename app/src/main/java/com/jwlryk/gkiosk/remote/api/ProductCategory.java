package com.jwlryk.gkiosk.remote.api;

import com.google.gson.annotations.SerializedName;

public class ProductCategory {
    @SerializedName("id") public int id;
    @SerializedName("class") public String clazz; // JSON field "class"
    @SerializedName("code") public String code;   // e.g., GKVEN-CTGRY-ALL
    @SerializedName("name") public String name;   // display label
    @SerializedName("status") public Integer status;
    @SerializedName("createdAt") public String createdAt;
    @SerializedName("updatedAt") public String updatedAt;
    @SerializedName("deletedAt") public String deletedAt;
}

