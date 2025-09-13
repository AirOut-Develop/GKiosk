package com.jwlryk.gkiosk.remote.api;

import com.google.gson.annotations.SerializedName;

public class Device {
    @SerializedName("id") public int id;
    @SerializedName("class") public String deviceClass;
    @SerializedName("code") public String code;
    @SerializedName("name") public String name;
    @SerializedName("type") public String type;
    @SerializedName("status") public int status;
    @SerializedName("ipExternal") public String ipExternal;
    @SerializedName("ipLocal") public String ipLocal;
    @SerializedName("opt00") public String opt00;
    @SerializedName("opt01") public String opt01;
    @SerializedName("opt02") public String opt02;
    @SerializedName("createdAt") public String createdAt;
    @SerializedName("updatedAt") public String updatedAt;
    @SerializedName("deletedAt") public String deletedAt;
}

