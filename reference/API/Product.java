package com.jwlryk.ogkiosk.API;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Product implements Serializable {

    @SerializedName("id")
    private int id;

    @SerializedName("DeviceCode")
    private String deviceCode;

    @SerializedName("ProductCode")
    private String productCode;

    @SerializedName("name")
    private String name;

    @SerializedName("positionIndex")
    private int positionIndex;

    @SerializedName("displayIndex")
    private int displayIndex;

    @SerializedName("price")
    private int price;

    @SerializedName("priceRetail")
    private int priceRetail;

    @SerializedName("priceStore")
    private int priceStore;

    @SerializedName("priceSupply")
    private int priceSupply;

    @SerializedName("imageMain")
    private String imageMain;

    @SerializedName("imageDetail")
    private String imageDetail;

    @SerializedName("brand")
    private String brand;

    @SerializedName("category")
    private String category;

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

    @SerializedName("type")
    private int type;

    @SerializedName("stock")
    private int stock;

    @SerializedName("status")
    private int status;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    @SerializedName("deletedAt")
    private String deletedAt;

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPositionIndex() {
        return positionIndex;
    }

    public void setPositionIndex(int positionIndex) {
        this.positionIndex = positionIndex;
    }

    public int getDisplayIndex() {
        return displayIndex;
    }

    public void setDisplayIndex(int displayIndex) {
        this.displayIndex = displayIndex;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getPriceRetail() {
        return priceRetail;
    }

    public void setPriceRetail(int priceRetail) {
        this.priceRetail = priceRetail;
    }

    public int getPriceStore() {
        return priceStore;
    }

    public void setPriceStore(int priceStore) {
        this.priceStore = priceStore;
    }

    public int getPriceSupply() {
        return priceSupply;
    }

    public void setPriceSupply(int priceSupply) {
        this.priceSupply = priceSupply;
    }

    public String getImageMain() {
        return imageMain;
    }

    public void setImageMain(String imageMain) {
        this.imageMain = imageMain;
    }

    public String getImageDetail() {
        return imageDetail;
    }

    public void setImageDetail(String imageDetail) {
        this.imageDetail = imageDetail;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
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
        return "Product{" +
                "id=" + id +
                ", deviceCode='" + deviceCode + '\'' +
                ", productCode='" + productCode + '\'' +
                ", name='" + name + '\'' +
                ", positionIndex=" + positionIndex +
                ", displayIndex=" + displayIndex +
                ", price=" + price +
                ", priceRetail=" + priceRetail +
                ", priceStore=" + priceStore +
                ", priceSupply=" + priceSupply +
                ", imageMain='" + imageMain + '\'' +
                ", imageDetail='" + imageDetail + '\'' +
                ", brand='" + brand + '\'' +
                ", category='" + category + '\'' +
                ", opt00='" + opt00 + '\'' +
                ", opt01='" + opt01 + '\'' +
                ", opt02='" + opt02 + '\'' +
                ", opt03='" + opt03 + '\'' +
                ", opt04='" + opt04 + '\'' +
                ", type=" + type +
                ", stock=" + stock +
                ", status=" + status +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                ", deletedAt='" + deletedAt + '\'' +
                '}';
    }
}
