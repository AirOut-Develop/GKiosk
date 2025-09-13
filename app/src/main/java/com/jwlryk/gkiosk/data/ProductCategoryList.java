package com.jwlryk.gkiosk.data;

import com.jwlryk.gkiosk.remote.api.ProductCategory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** In-memory cache for product category list fetched from API. */
public final class ProductCategoryList {
    private final Map<String, ProductCategory> byCode = new LinkedHashMap<>(); // code -> item

    private ProductCategoryList() {}
    private static class Holder { private static final ProductCategoryList I = new ProductCategoryList(); }
    public static ProductCategoryList getInstance() { return Holder.I; }

    public synchronized void setAll(List<ProductCategory> list) {
        byCode.clear();
        if (list == null) return;
        for (ProductCategory c : list) {
            if (c != null && c.code != null) byCode.put(c.code, c);
        }
    }

    public synchronized List<ProductCategory> asList() { return new ArrayList<>(byCode.values()); }
    public synchronized ProductCategory getByCode(String code) { return code == null ? null : byCode.get(code); }
    public synchronized int size() { return byCode.size(); }
    public synchronized List<ProductCategory> sorted() { return asList(); }
}

