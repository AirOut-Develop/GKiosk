package com.jwlryk.gkiosk.data;

import com.jwlryk.gkiosk.model.ProductItem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton cart manager based on ProductItem list.
 * Stores quantity per product id. Use together with {@link ProductItemList}.
 */
public final class ProductCartList {

    public static final class Line {
        public final ProductItem item;
        public final int qty;

        public Line(ProductItem item, int qty) {
            this.item = item;
            this.qty = Math.max(1, qty);
        }
    }

    private final Map<Integer, Integer> qtyById = new LinkedHashMap<>();

    private ProductCartList() { }

    private static class Holder {
        private static final ProductCartList INSTANCE = new ProductCartList();
    }

    public static ProductCartList getInstance() {
        return Holder.INSTANCE;
    }

    public synchronized void clear() {
        qtyById.clear();
    }

    public synchronized int getQty(int productId) {
        Integer q = qtyById.get(productId);
        return q == null ? 0 : q;
    }

    public synchronized void setQty(int productId, int qty) {
        if (qty <= 0) {
            qtyById.remove(productId);
        } else {
            qtyById.put(productId, qty);
        }
    }

    public synchronized void add(int productId, int delta) {
        int cur = getQty(productId);
        setQty(productId, Math.max(1, cur + Math.max(1, delta)));
    }

    public synchronized void increment(int productId) {
        add(productId, 1);
    }

    public synchronized void decrement(int productId) {
        int cur = getQty(productId);
        if (cur > 1) setQty(productId, cur - 1);
        else qtyById.remove(productId);
    }

    public synchronized int getDistinctCount() {
        return qtyById.size();
    }

    public synchronized int getTotalUnits() {
        int sum = 0;
        for (Integer q : qtyById.values()) sum += (q == null ? 0 : q);
        return sum;
    }

    public synchronized long getSubtotal() {
        long total = 0L;
        ProductItemList catalog = ProductItemList.getInstance();
        for (Map.Entry<Integer, Integer> e : qtyById.entrySet()) {
            ProductItem item = catalog.get(e.getKey());
            if (item != null) total += (long) item.getPrice() * (e.getValue() == null ? 0 : e.getValue());
        }
        return total;
    }

    public synchronized List<Line> asLines() {
        List<Line> lines = new ArrayList<>();
        ProductItemList catalog = ProductItemList.getInstance();
        for (Map.Entry<Integer, Integer> e : qtyById.entrySet()) {
            ProductItem item = catalog.get(e.getKey());
            if (item != null) lines.add(new Line(item, e.getValue() == null ? 0 : e.getValue()));
        }
        return lines;
    }
}

