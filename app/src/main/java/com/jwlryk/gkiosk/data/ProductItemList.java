package com.jwlryk.gkiosk.data;

import com.jwlryk.gkiosk.model.ProductItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton in-memory catalog of products available on the kiosk.
 */
public final class ProductItemList {

    private final Map<Integer, ProductItem> items = new LinkedHashMap<>(); // id -> item (stable order)
    private final Map<String, Integer> idByCode = new LinkedHashMap<>();   // productCode -> id

    private ProductItemList() { }

    private static class Holder {
        private static final ProductItemList INSTANCE = new ProductItemList();
    }

    public static ProductItemList getInstance() {
        return Holder.INSTANCE;
    }

    public synchronized void clear() {
        items.clear();
        idByCode.clear();
    }

    public synchronized void setAll(Collection<ProductItem> list) {
        items.clear();
        idByCode.clear();
        if (list == null) return;
        for (ProductItem p : list) {
            if (p != null) {
                items.put(p.getId(), p);
                if (p.getProductCode() != null) idByCode.put(p.getProductCode(), p.getId());
            }
        }
    }

    public synchronized void upsert(ProductItem p) {
        if (p == null) return;
        items.put(p.getId(), p);
        if (p.getProductCode() != null) idByCode.put(p.getProductCode(), p.getId());
    }

    public synchronized ProductItem get(int id) {
        return items.get(id);
    }

    public synchronized List<ProductItem> asList() {
        return new ArrayList<>(items.values());
    }

    public synchronized int size() {
        return items.size();
    }

    // Convenience loader from JSONArray
    public synchronized void loadFromJson(JSONArray arr) {
        if (arr == null) return;
        items.clear();
        idByCode.clear();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.optJSONObject(i);
            if (obj == null) continue;
            ProductItem p = ProductItem.fromJson(obj);
            if (p != null) {
                items.put(p.getId(), p);
                if (p.getProductCode() != null) idByCode.put(p.getProductCode(), p.getId());
            }
        }
    }

    public synchronized ProductItem getByProductCode(String productCode) {
        if (productCode == null) return null;
        Integer id = idByCode.get(productCode);
        return id == null ? null : items.get(id);
    }

    public synchronized List<ProductItem> filterByCategory(String category) {
        if (category == null || category.isEmpty()) return asList();
        List<ProductItem> r = new ArrayList<>();
        for (ProductItem p : items.values()) {
            if (category.equals(p.getCategory())) r.add(p);
        }
        return r;
    }
}
