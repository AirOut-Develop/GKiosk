package com.jwlryk.ogkiosk.API;

import com.google.gson.annotations.SerializedName;

public class Store {

    @SerializedName("id")
    private int id;

    @SerializedName("class")
    private String storeClass;

    @SerializedName("code")
    private String code;

    @SerializedName("name")
    private String name;

    @SerializedName("type")
    private String type;

    @SerializedName("status")
    private int status;

    @SerializedName("companyNumber")
    private String companyNumber;

    @SerializedName("cardVanType")
    private String cardVanType;

    @SerializedName("cardVanCATID")
    private String cardVanCATID;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    @SerializedName("deletedAt")
    private String deletedAt;

    // 기본 생성자
    public Store() {}

    // getter, setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStoreClass() {
        return storeClass;
    }

    public void setStoreClass(String storeClass) {
        this.storeClass = storeClass;
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

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getCardVanType() {
        return cardVanType;
    }

    public void setCardVanType(String cardVanType) {
        this.cardVanType = cardVanType;
    }

    public String getCardVanCATID() {
        return cardVanCATID;
    }

    public void setCardVanCATID(String cardVanCATID) {
        this.cardVanCATID = cardVanCATID;
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

    // 필요에 따라 toString 추가
    @Override
    public String toString() {
        return "Store{" +
                "id=" + id +
                ", storeClass='" + storeClass + '\'' +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", status=" + status +
                ", companyNumber='" + companyNumber + '\'' +
                ", cardVanType='" + cardVanType + '\'' +
                ", cardVanCATID='" + cardVanCATID + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                ", deletedAt='" + deletedAt + '\'' +
                '}';
    }
}
