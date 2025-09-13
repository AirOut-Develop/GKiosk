package com.jwlryk.gkiosk.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Plain model for a product item shown on kiosk. */
public class ProductItem {
    private int id;
    private String brand;
    private String title;
    private long price; // in minor units (KRW)
    private List<String> tags = new ArrayList<>();
    private String image; // drawable key
    private String detailImage; // large/detail image url
    private String ratio; // e.g., "1:1.1"
    private String category; // optional

    // Additional fields
    private String productCode;      // 고유 상품 코드 (문자열)
    private String barcode;          // 바코드(EAN/QR 등)
    private String unit;             // 단위 (ea, box, kg ...)
    private int stockQty;            // 재고 수량
    private String stockStatus;      // 재고 상태(in_stock, low, out)
    private boolean taxInclusive;    // 가격이 부가세 포함인지
    private double vatRate;          // 부가세율 (예: 0.1)
    private double discountRate;     // 할인율 (0.0~1.0)
    private String description;      // 설명
    private Map<String, String> attributes = new LinkedHashMap<>(); // 임의 속성
    private boolean active = true;   // 판매 가능
    private boolean visibility = true; // 노출 여부
    private int sortOrder = 0;       // 정렬 우선순위
    private int vendingSlotNumber;   // 자판기 슬롯 번호(몇 번 제품)

    public ProductItem() { }

    public static ProductItem fromJson(JSONObject obj) {
        if (obj == null) return null;
        ProductItem p = new ProductItem();
        p.id = obj.optInt("id", 0);
        p.brand = obj.optString("brand", "");
        p.title = obj.optString("title", "");
        p.price = obj.optLong("price", 0);
        p.image = obj.optString("image", "");
        p.ratio = obj.optString("ratio", "");
        p.category = obj.optString("category", "");
        // Prefer explicit productCode; fallback to legacy sku; lastly use id as string
        String code = obj.optString("productCode", null);
        if (code == null || code.isEmpty()) code = obj.optString("sku", null);
        if (code == null || code.isEmpty()) code = String.valueOf(p.id);
        p.productCode = code;
        p.barcode = obj.optString("barcode", "");
        p.unit = obj.optString("unit", "");
        p.stockQty = obj.optInt("stockQty", 0);
        p.stockStatus = obj.optString("stockStatus", "");
        p.taxInclusive = obj.optBoolean("taxInclusive", true);
        p.vatRate = obj.optDouble("vatRate", 0.1);
        p.discountRate = obj.optDouble("discountRate", 0.0);
        p.description = obj.optString("description", "");
        p.active = obj.optBoolean("active", true);
        p.visibility = obj.optBoolean("visibility", true);
        p.sortOrder = obj.optInt("sortOrder", 0);
        p.vendingSlotNumber = obj.optInt("vendingSlotNumber", 0);
        JSONObject attrs = obj.optJSONObject("attributes");
        if (attrs != null) {
            for (Iterator<String> it = attrs.keys(); it.hasNext(); ) {
                String key = it.next();
                String val = attrs.optString(key, null);
                if (val != null) p.attributes.put(key, val);
            }
        }
        JSONArray arr = obj.optJSONArray("tags");
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) {
                String t = arr.optString(i);
                if (t != null && !t.isEmpty()) p.tags.add(t);
            }
        }
        return p;
    }

    // Getters/setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public long getPrice() { return price; }
    public void setPrice(long price) { this.price = price; }
    public List<String> getTags() { return Collections.unmodifiableList(tags); }
    public void setTags(List<String> tags) {
        this.tags.clear();
        if (tags != null) this.tags.addAll(tags);
    }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getDetailImage() { return detailImage; }
    public void setDetailImage(String detailImage) { this.detailImage = detailImage; }
    public String getRatio() { return ratio; }
    public void setRatio(String ratio) { this.ratio = ratio; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public int getStockQty() { return stockQty; }
    public void setStockQty(int stockQty) { this.stockQty = stockQty; }
    public String getStockStatus() { return stockStatus; }
    public void setStockStatus(String stockStatus) { this.stockStatus = stockStatus; }
    public boolean isTaxInclusive() { return taxInclusive; }
    public void setTaxInclusive(boolean taxInclusive) { this.taxInclusive = taxInclusive; }
    public double getVatRate() { return vatRate; }
    public void setVatRate(double vatRate) { this.vatRate = vatRate; }
    public double getDiscountRate() { return discountRate; }
    public void setDiscountRate(double discountRate) { this.discountRate = discountRate; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Map<String, String> getAttributes() { return Collections.unmodifiableMap(attributes); }
    public void setAttributes(Map<String, String> attributes) {
        this.attributes.clear();
        if (attributes != null) this.attributes.putAll(attributes);
    }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isVisibility() { return visibility; }
    public void setVisibility(boolean visibility) { this.visibility = visibility; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
    public int getVendingSlotNumber() { return vendingSlotNumber; }
    public void setVendingSlotNumber(int vendingSlotNumber) { this.vendingSlotNumber = vendingSlotNumber; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductItem that = (ProductItem) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
