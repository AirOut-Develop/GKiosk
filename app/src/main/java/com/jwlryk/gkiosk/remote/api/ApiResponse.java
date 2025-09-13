package com.jwlryk.gkiosk.remote.api;

import com.google.gson.annotations.SerializedName;

public class ApiResponse<T> {
    @SerializedName("isSuccess")
    private boolean isSuccess;

    @SerializedName("code")
    private int code;

    @SerializedName("msg")
    private String message;

    @SerializedName("payload")
    private T payload;

    public boolean isSuccess() { return isSuccess; }
    public int getCode() { return code; }
    public String getMessage() { return message; }
    public T getPayload() { return payload; }
}

