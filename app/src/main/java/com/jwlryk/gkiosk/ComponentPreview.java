package com.jwlryk.gkiosk;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import android.util.TypedValue;
import android.content.res.ColorStateList;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.ImageButton;
import android.graphics.Typeface;
import android.graphics.Paint;
import java.util.List;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import android.view.ContextThemeWrapper;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams;
import androidx.constraintlayout.widget.ConstraintSet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ComponentPreview extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_component_preview);
        UiUtil.enableImmersiveMode(this);

        // Theme toggle
        SwitchMaterial toggle = findViewById(R.id.view_theme_toggle);
        if (toggle != null) {
            toggle.setChecked(Global.getThemeMode() == Global.ThemeMode.DARK);
            toggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
                Global.setThemeMode(isChecked ? Global.ThemeMode.DARK : Global.ThemeMode.LIGHT);
                Global.applyNightMode();
                recreate();
            });
        }

        // Language selection
        RadioGroup langGroup = findViewById(R.id.view_language_group);
        if (langGroup != null) {
            String stored = Global.getLanguage(getApplicationContext());
            if ("ko".equals(stored)) {
                langGroup.check(R.id.view_lang_ko);
            } else if ("en".equals(stored)) {
                langGroup.check(R.id.view_lang_en);
            } else {
                // Default to current locale
                java.util.Locale current = getResources().getConfiguration().getLocales().isEmpty() ? null : getResources().getConfiguration().getLocales().get(0);
                if (current != null && current.getLanguage().startsWith("ko")) {
                    langGroup.check(R.id.view_lang_ko);
                } else {
                    langGroup.check(R.id.view_lang_en);
                }
            }

            langGroup.setOnCheckedChangeListener((group, checkedId) -> {
                String tag = (checkedId == R.id.view_lang_ko) ? "ko" : "en";
                Global.setLanguage(getApplicationContext(), tag);
                recreate();
            });
        }

        // JSON 예시 데이터를 GridLayout의 include 카드들에 바인딩
        try {
            JSONArray products = new JSONArray(loadAssetText("products_sample.json"));
            int[] cardIds = new int[]{
                    R.id.card_inc_1, R.id.card_inc_2, R.id.card_inc_3, R.id.card_inc_4,
                    R.id.card_inc_5, R.id.card_inc_6, R.id.card_inc_7, R.id.card_inc_8
            };
            int count = Math.min(cardIds.length, products.length());
            for (int i = 0; i < count; i++) {
                View card = findViewById(cardIds[i]);
                if (card == null) continue;
                JSONObject obj = products.getJSONObject(i);
                bindProductToCard(card, obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 장바구니(수량 카드) 영역: products_cart.json 바인딩
        try {
            JSONArray cart = new JSONArray(loadAssetText("products_cart.json"));
            int[] cartIds = new int[]{ R.id.cart_inc_1, R.id.cart_inc_2 };
            int c = Math.min(cartIds.length, cart.length());
            for (int i = 0; i < c; i++) {
                View card = findViewById(cartIds[i]);
                if (card == null) continue;
                JSONObject obj = cart.getJSONObject(i);
                bindProductToCard(card, obj);
                bindCartQtyControls(card, obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Tab 섹션 초기화
        try {
            setupTabSection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void bindCartQtyControls(View cardRoot, JSONObject obj) {
        TextView qtyView = cardRoot.findViewById(R.id.view_qty);
        ImageButton minus = cardRoot.findViewById(R.id.btn_qty_minus);
        ImageButton plus = cardRoot.findViewById(R.id.btn_qty_plus);
        if (qtyView == null || minus == null || plus == null) return;

        int initial = 1;
        try {
            initial = Math.max(1, obj.optInt("qty", 1));
        } catch (Exception ignored) { }
        qtyView.setText(String.valueOf(initial));

        minus.setOnClickListener(v -> {
            int current = parseQty(qtyView.getText().toString());
            if (current > 1) {
                qtyView.setText(String.valueOf(current - 1));
            }
        });
        plus.setOnClickListener(v -> {
            int current = parseQty(qtyView.getText().toString());
            qtyView.setText(String.valueOf(current + 1));
        });
    }

    private int parseQty(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return 1; }
    }

    private void bindProductToCard(View cardRoot, JSONObject obj) throws JSONException {
        TextView brand = cardRoot.findViewById(R.id.view_brand);
        TextView title = cardRoot.findViewById(R.id.view_title);
        TextView price = cardRoot.findViewById(R.id.view_price);
        ChipGroup tags = cardRoot.findViewById(R.id.view_tags);
        ShapeableImageView image = cardRoot.findViewById(R.id.view_image);

        String brandStr = obj.optString("brand", getString(R.string.brand_placeholder));
        String titleStr = obj.optString("title", getString(R.string.product_name_placeholder));
        String priceStr = formatPrice(obj.optLong("price", 0));

        if (brand != null) {
            brand.setText(brandStr);
            brand.setContentDescription(getString(R.string.cd_brand, brandStr));
        }
        if (title != null) {
            title.setText(titleStr);
            title.setContentDescription(getString(R.string.cd_title, titleStr));
        }
        if (price != null) {
            price.setText(priceStr);
            price.setContentDescription(getString(R.string.cd_price, priceStr));
        }

        if (tags != null) {
            tags.removeAllViews();
            JSONArray arr = obj.optJSONArray("tags");
            if (arr != null) {
                for (int t = 0; t < arr.length(); t++) {
                    Chip chip = (Chip) getLayoutInflater().inflate(R.layout.view_tag_chip, tags, false);
                    chip.setText(arr.optString(t));
                    tags.addView(chip);
                }
            }
        }

        if (image != null) {
            int res = mapImage(obj.optString("image", "grad_01"));
            image.setImageResource(res);
            String ratio = obj.optString("ratio", "1:1.1");
            ViewGroup.LayoutParams lp = image.getLayoutParams();
            if (lp instanceof ConstraintLayout.LayoutParams) {
                ConstraintLayout.LayoutParams clp = (ConstraintLayout.LayoutParams) lp;
                clp.dimensionRatio = ratio;
                image.setLayoutParams(clp);
            } else {
                ConstraintSet set = new ConstraintSet();
                ConstraintLayout parent = (ConstraintLayout) image.getParent();
                set.clone(parent);
                set.setDimensionRatio(image.getId(), ratio);
                set.applyTo(parent);
            }

            // Accessibility: describe image with product title
            String cd = getString(R.string.cd_product_image);
            if (titleStr != null && !titleStr.isEmpty()) {
                cd = cd + ": " + titleStr;
            }
            image.setContentDescription(cd);
        }
    }

    private int mapImage(String name) {
        switch (name) {
            case "grad_02": return R.drawable.grad_02;
            case "grad_03": return R.drawable.grad_03;
            case "grad_04": return R.drawable.grad_04;
            case "grad_05": return R.drawable.grad_05;
            case "grad_06": return R.drawable.grad_06;
            case "grad_07": return R.drawable.grad_07;
            case "grad_08": return R.drawable.grad_08;
            case "grad_01":
            default: return R.drawable.grad_01;
        }
    }

    private String loadAssetText(String filename) throws IOException {
        InputStream is = getAssets().open(filename);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();
        is.close();
        return sb.toString();
    }

    // ---------------- Tab Section -----------------
    private JSONArray categoriesJson;
    private JSONArray itemsJson;
    private String selectedCategoryKey = "All";
    private String selectedSortKey = "recommend";
    private String selectedSubTagKey = "*"; // * means ALL

    private void setupTabSection() throws IOException, JSONException {
        // Load JSON data with dummy fallback
        try {
            String rawCats = loadAssetText("category.json");
            categoriesJson = parseCategoriesJson(rawCats);
        } catch (Exception e) {
            categoriesJson = defaultCategoriesJson();
        }
        try {
            itemsJson = new JSONArray(loadAssetText("itemsample.json"));
        } catch (Exception e) {
            itemsJson = defaultItemsJson();
        }
        if (categoriesJson == null || categoriesJson.length() == 0) {
            categoriesJson = defaultCategoriesJson();
        }
        if (itemsJson == null || itemsJson.length() == 0) {
            itemsJson = defaultItemsJson();
        }

        // Build category tabs (supports object {key,name} or string "Key:Name")
        MaterialButtonToggleGroup group = findViewById(R.id.view_category_tabs);
        if (group != null) {
            group.removeAllViews();
            for (int i = 0; i < categoriesJson.length(); i++) {
                String key = "";
                String label = "";
                // Accept both object and string formats
                Object elem = categoriesJson.get(i);
                if (elem instanceof org.json.JSONObject) {
                    JSONObject cat = (JSONObject) elem;
                    String nm = cat.optString("name", "");
                    String k = cat.optString("key", "");
                    String combined = cat.optString("label", "");
                    if (!nm.isEmpty() && !k.isEmpty()) {
                        key = k; label = nm;
                    } else if (!combined.isEmpty()) {
                        int idx = combined.indexOf(":");
                        if (idx >= 0) {
                            key = combined.substring(0, idx);
                            label = combined.substring(idx + 1);
                        } else {
                            key = k.isEmpty() ? combined : k;
                            label = nm.isEmpty() ? combined : nm;
                        }
                    } else {
                        key = k;
                        label = nm.isEmpty() ? k : nm;
                    }
                } else {
                    String s = categoriesJson.optString(i);
                    int idx = s.indexOf(":");
                    if (idx >= 0) {
                        key = s.substring(0, idx);
                        label = s.substring(idx + 1);
                    } else {
                        key = s;
                        label = s;
                    }
                }

                ContextThemeWrapper themed = new ContextThemeWrapper(this, R.style.Widget_GKiosk_TabButton_BW);
                MaterialButton btn = new MaterialButton(themed, null, 0);
                btn.setText(label);
                btn.setCheckable(true);
                applyBwTabStyle(btn);
                btn.setId(View.generateViewId());
                btn.setTag(key);
                MaterialButtonToggleGroup.LayoutParams lp = new MaterialButtonToggleGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                int m = getResources().getDimensionPixelSize(R.dimen.space_m);
                int end = (i == categoriesJson.length() - 1) ? 0 : m; // no trailing margin on last
                lp.setMargins(0, 0, end, 0);
                btn.setLayoutParams(lp);
                group.addView(btn);
                if ("All".equals(key)) {
                    group.check(btn.getId());
                }
            }
            group.addOnButtonCheckedListener((g, checkedId, isChecked) -> {
                if (isChecked) {
                    View v = g.findViewById(checkedId);
                    Object tag = v != null ? v.getTag() : null;
                    if (tag != null) {
                        selectedCategoryKey = tag.toString();
                        buildSubcategoryTabs();
                        renderTabItems();
                    }
                }
            });
        }

        // Initial subcategory/sort build and render
        buildSubcategoryTabs();
        buildSortTabs();
        renderTabItems();
    }

    private JSONArray defaultCategoriesJson() {
        JSONArray arr = new JSONArray();
        arr.put("All:전체");
        arr.put("CategoryA:카테고리A");
        arr.put("CategoryB:카테고리B");
        arr.put("CategoryC:카테고리C");
        return arr;
    }

    private JSONArray parseCategoriesJson(String raw) throws JSONException {
        if (raw == null) throw new JSONException("null");
        raw = raw.trim();
        if (raw.startsWith("[")) {
            return new JSONArray(raw);
        } else if (raw.startsWith("{")) {
            // Convert {"Key":"Name", ...} to ["Key:Name", ...]
            JSONObject obj = new JSONObject(raw);
            JSONArray arr = new JSONArray();
            java.util.Iterator<String> it = obj.keys();
            while (it.hasNext()) {
                String k = it.next();
                String v = obj.optString(k, k);
                arr.put(k + ":" + v);
            }
            return arr;
        } else {
            // Fallback: try as single string
            JSONArray arr = new JSONArray();
            arr.put(raw);
            return arr;
        }
    }

    private JSONArray defaultItemsJson() {
        JSONArray arr = new JSONArray();
        try {
            JSONObject a = new JSONObject();
            a.put("id", 1001).put("brand", "BRAND").put("title", "샘플 A").put("price", 12345)
                    .put("image", "grad_01").put("ratio", "1:1.3").put("category", "CategoryA")
                    .put("recommend", 90).put("sales", 100).put("release", "2025-01-01")
                    .put("tags", new org.json.JSONArray().put("샘플"));
            arr.put(a);

            JSONObject b = new JSONObject();
            b.put("id", 1002).put("brand", "BRAND").put("title", "샘플 B").put("price", 23456)
                    .put("image", "grad_02").put("ratio", "1:1.3").put("category", "CategoryA")
                    .put("recommend", 80).put("sales", 200).put("release", "2025-02-01")
                    .put("tags", new org.json.JSONArray().put("샘플"));
            arr.put(b);

            JSONObject c = new JSONObject();
            c.put("id", 1003).put("brand", "BRAND").put("title", "샘플 C").put("price", 34567)
                    .put("image", "grad_03").put("ratio", "1:1.3").put("category", "CategoryB")
                    .put("recommend", 85).put("sales", 150).put("release", "2025-03-01")
                    .put("tags", new org.json.JSONArray().put("샘플"));
            arr.put(c);

            JSONObject d = new JSONObject();
            d.put("id", 1004).put("brand", "BRAND").put("title", "샘플 D").put("price", 45678)
                    .put("image", "grad_04").put("ratio", "1:1.3").put("category", "CategoryB")
                    .put("recommend", 70).put("sales", 80).put("release", "2025-04-01")
                    .put("tags", new org.json.JSONArray().put("샘플"));
            arr.put(d);

            JSONObject e = new JSONObject();
            e.put("id", 1005).put("brand", "BRAND").put("title", "샘플 E").put("price", 56789)
                    .put("image", "grad_05").put("ratio", "1:1.3").put("category", "CategoryC")
                    .put("recommend", 75).put("sales", 60).put("release", "2025-05-01")
                    .put("tags", new org.json.JSONArray().put("샘플"));
            arr.put(e);

            JSONObject f = new JSONObject();
            f.put("id", 1006).put("brand", "BRAND").put("title", "샘플 F").put("price", 67890)
                    .put("image", "grad_06").put("ratio", "1:1.3").put("category", "CategoryC")
                    .put("recommend", 65).put("sales", 40).put("release", "2025-06-01")
                    .put("tags", new org.json.JSONArray().put("샘플"));
            arr.put(f);

            JSONObject g = new JSONObject();
            g.put("id", 1007).put("brand", "BRAND").put("title", "샘플 G").put("price", 78901)
                    .put("image", "grad_07").put("ratio", "1:1.3")
                    .put("recommend", 95).put("sales", 500).put("release", "2025-07-01")
                    .put("tags", new org.json.JSONArray().put("샘플"));
            arr.put(g);

            JSONObject h = new JSONObject();
            h.put("id", 1008).put("brand", "BRAND").put("title", "샘플 H").put("price", 89012)
                    .put("image", "grad_08").put("ratio", "1:1.3")
                    .put("recommend", 60).put("sales", 30).put("release", "2025-08-01")
                    .put("tags", new org.json.JSONArray().put("샘플"));
            arr.put(h);
        } catch (JSONException ignored) {}
        return arr;
    }

    private void renderTabItems() {
        androidx.gridlayout.widget.GridLayout grid = findViewById(R.id.grid_tab_items);
        if (grid == null || itemsJson == null) return;
        grid.removeAllViews();

        java.util.List<JSONObject> list = new java.util.ArrayList<>();
        for (int i = 0; i < itemsJson.length(); i++) {
            JSONObject it = itemsJson.optJSONObject(i);
            if (it == null) continue;
            String cat = it.optString("category", null);
            boolean categoryMatch = "All".equals(selectedCategoryKey) || (cat != null && cat.equals(selectedCategoryKey));
            if (!categoryMatch) continue;

            // Sub tag filter
            if (!"*".equals(selectedSubTagKey)) {
                org.json.JSONArray tags = it.optJSONArray("tags");
                boolean hasTag = false;
                if (tags != null) {
                    for (int t = 0; t < tags.length(); t++) {
                        if (selectedSubTagKey.equals(tags.optString(t))) { hasTag = true; break; }
                    }
                }
                if (!hasTag) continue;
            }
            list.add(it);
        }

        java.util.Collections.sort(list, (a, b) -> {
            switch (selectedSortKey) {
                case "sales_desc": {
                    long av = a.optLong("sales", 0); long bv = b.optLong("sales", 0);
                    return Long.compare(bv, av);
                }
                case "price_desc": {
                    long av = a.optLong("price", 0); long bv = b.optLong("price", 0);
                    return Long.compare(bv, av);
                }
                case "price_asc": {
                    long av = a.optLong("price", 0); long bv = b.optLong("price", 0);
                    return Long.compare(av, bv);
                }
                case "release_desc": {
                    String as = a.optString("release", "");
                    String bs = b.optString("release", "");
                    return bs.compareTo(as);
                }
                case "recommend":
                default: {
                    long av = a.optLong("recommend", 0); long bv = b.optLong("recommend", 0);
                    return Long.compare(bv, av);
                }
            }
        });

        int count = Math.min(8, list.size());
        for (int i = 0; i < count; i++) {
            View v = getLayoutInflater().inflate(R.layout.item_product_card, grid, false);
            try {
                bindProductToCard(v, list.get(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            androidx.gridlayout.widget.GridLayout.LayoutParams lp = new androidx.gridlayout.widget.GridLayout.LayoutParams();
            lp.width = 0;
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            lp.columnSpec = androidx.gridlayout.widget.GridLayout.spec(androidx.gridlayout.widget.GridLayout.UNDEFINED, 1f);
            v.setLayoutParams(lp);
            grid.addView(v);
        }

        // Add invisible placeholders to preserve 4-column widths when items < 4 in last row
        int remainder = count % 4;
        if (remainder != 0) {
            int toAdd = 4 - remainder;
            for (int i = 0; i < toAdd; i++) {
                View filler = getLayoutInflater().inflate(R.layout.item_product_card_placeholder, grid, false);
                androidx.gridlayout.widget.GridLayout.LayoutParams lp = new androidx.gridlayout.widget.GridLayout.LayoutParams();
                lp.width = 0;
                lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                lp.columnSpec = androidx.gridlayout.widget.GridLayout.spec(androidx.gridlayout.widget.GridLayout.UNDEFINED, 1f);
                filler.setLayoutParams(lp);
                grid.addView(filler);
            }
        }
    }

    private void buildSubcategoryTabs() {
        LinearLayout container = findViewById(R.id.view_subcategory_container);
        if (container == null || itemsJson == null) return;
        container.removeAllViews();

        // Collect unique tags from items of current category (or all)
        java.util.LinkedHashSet<String> tags = new java.util.LinkedHashSet<>();
        for (int i = 0; i < itemsJson.length(); i++) {
            JSONObject it = itemsJson.optJSONObject(i);
            if (it == null) continue;
            String cat = it.optString("category", null);
            boolean categoryMatch = "All".equals(selectedCategoryKey) || (cat != null && cat.equals(selectedCategoryKey));
            if (!categoryMatch) continue;
            org.json.JSONArray arr = it.optJSONArray("tags");
            if (arr != null) {
                for (int t = 0; t < arr.length(); t++) {
                    String tag = arr.optString(t);
                    if (tag != null && !tag.isEmpty()) tags.add(tag);
                }
            }
        }

        // Always include "전체" at start
        java.util.List<String> display = new java.util.ArrayList<>();
        display.add(getString(R.string.sub_all));
        display.addAll(tags);

        for (int i = 0; i < display.size(); i++) {
            String label = display.get(i);
            String key = (i == 0) ? "*" : label; // use label as key for tags

            ContextThemeWrapper themed = new ContextThemeWrapper(this, R.style.Widget_GKiosk_PillButton_BW);
            MaterialButton btn = new MaterialButton(themed, null, 0);
            btn.setText(label);
            btn.setCheckable(true);
            applyBwTabStyle(btn);
            btn.setChecked(key.equals(selectedSubTagKey));
            btn.setOnClickListener(v -> {
                selectedSubTagKey = key;
                // Toggle selection visually
                int childCount = container.getChildCount();
                for (int c = 0; c < childCount; c++) {
                    View child = container.getChildAt(c);
                    if (child instanceof MaterialButton) {
                        MaterialButton mb = (MaterialButton) child;
                        Object tagObj = mb.getTag();
                        mb.setChecked(tagObj != null && tagObj.equals(selectedSubTagKey));
                    }
                }
                renderTabItems();
            });
            btn.setTag(key);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            int m = getResources().getDimensionPixelSize(R.dimen.space_s);
            int end = (i == display.size() - 1) ? 0 : m; // no trailing margin on last
            lp.setMargins(0, 0, end, 0);
            btn.setLayoutParams(lp);
            container.addView(btn);

            // no pipe separators for subcategory row
        }
    }

    private void buildSortTabs() {
        LinearLayout container = findViewById(R.id.view_sort_container);
        if (container == null) return;
        container.removeAllViews();

        String[] labels = getResources().getStringArray(R.array.sort_options_labels);
        String[] keys = getResources().getStringArray(R.array.sort_options_keys);
        int m = getResources().getDimensionPixelSize(R.dimen.space_xs);

        for (int i = 0; i < labels.length && i < keys.length; i++) {
            String label = labels[i];
            String key = keys[i];

            TextView tv = new TextView(this);
            tv.setText(label);
            tv.setPadding(0, 0, 0, 0);
            tv.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 14);
            tv.setOnClickListener(v -> {
                if (!selectedSortKey.equals(key)) {
                    selectedSortKey = key;
                    buildSortTabs();
                    renderTabItems();
                }
            });

            // Selected: Light=black, Night=white. Unselected: text_secondary.
            boolean night = (getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES;
            if (key.equals(selectedSortKey)) {
                tv.setTextColor(getColorCompat(night ? R.color.white : R.color.black));
                tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
                tv.setPaintFlags(tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                tv.setContentDescription(label + ", selected");
            } else {
                tv.setTextColor(getColorCompat(night ? R.color.white_1 : R.color.text_secondary));
                tv.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
                tv.setPaintFlags(tv.getPaintFlags() & ~Paint.UNDERLINE_TEXT_FLAG);
            }

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, m, 0);
            tv.setLayoutParams(lp);
            container.addView(tv);

            if (i < labels.length - 1) {
                TextView sep = new TextView(this);
                sep.setText("|");
                sep.setTextColor(getColorCompat(R.color.text_secondary));
                LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                slp.setMargins(0, 0, m, 0);
                sep.setLayoutParams(slp);
                container.addView(sep);
            }
        }
    }

    private String formatPrice(long price) {
        java.text.NumberFormat nf = java.text.NumberFormat.getCurrencyInstance(java.util.Locale.KOREA);
        return nf.format(price);
    }

    private int getColorCompat(int resId) {
        return androidx.core.content.ContextCompat.getColor(this, resId);
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }

    private void applyBwTabStyle(MaterialButton btn) {
        ColorStateList bg = AppCompatResources.getColorStateList(this, R.color.bw_bg);
        ColorStateList txt = AppCompatResources.getColorStateList(this, R.color.bw_text);
        ColorStateList stroke = AppCompatResources.getColorStateList(this, R.color.bw_stroke);
        ColorStateList ripple = AppCompatResources.getColorStateList(this, R.color.primary_lighter);
        if (bg != null) btn.setBackgroundTintList(bg);
        if (txt != null) btn.setTextColor(txt);
        if (stroke != null) btn.setStrokeColor(stroke);
        if (ripple != null) btn.setRippleColor(ripple);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            UiUtil.enableImmersiveMode(this);
            // Scale down the whole page on small screens to fit the design width (e.g., 600dp)
            UiUtil.applyDesignScale(this, 600);
        }
    }
}
