package com.jwlryk.ogkiosk.API;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Device implements Serializable {

    @SerializedName("id")
    private int id;

    @SerializedName("class")
    private String deviceClass;

    @SerializedName("code")
    private String code;

    @SerializedName("name")
    private String name;

    @SerializedName("type")
    private String type;

    @SerializedName("status")
    private int status;

    @SerializedName("ipExternal")
    private String ipExternal;

    @SerializedName("ipLocal")
    private String ipLocal;

    @SerializedName("opt00")
    private String opt00;

    @SerializedName("opt01")
    private String opt01;

    @SerializedName("opt02")
    private String opt02;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    @SerializedName("deletedAt")
    private String deletedAt;

    // 기본 생성자
    public Device() {}

    // Getter 및 Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDeviceClass() {
        return deviceClass;
    }

    public void setDeviceClass(String deviceClass) {
        this.deviceClass = deviceClass;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getIpExternal() {
        return ipExternal;
    }

    public void setIpExternal(String ipExternal) {
        this.ipExternal = ipExternal;
    }

    public String getIpLocal() {
        return ipLocal;
    }

    public void setIpLocal(String ipLocal) {
        this.ipLocal = ipLocal;
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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(String deletedAt) {
        this.deletedAt = deletedAt;
    }

    @Override
    public String toString() {
        return "Device{" +
                "id=" + id +
                ", deviceClass='" + deviceClass + '\'' +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", status=" + status +
                ", ipExternal='" + ipExternal + '\'' +
                ", ipLocal='" + ipLocal + '\'' +
                ", opt00='" + opt00 + '\'' +
                ", opt01='" + opt01 + '\'' +
                ", opt02='" + opt02 + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                ", deletedAt='" + deletedAt + '\'' +
                '}';
    }
}
