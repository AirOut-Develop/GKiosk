package com.jwlryk.gkiosk;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import android.app.AlertDialog;
import com.bumptech.glide.Glide;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.jwlryk.gkiosk.data.ProductItemList;
import com.jwlryk.gkiosk.data.KioskInfo;
import com.jwlryk.gkiosk.data.ProductCategoryList;
import com.jwlryk.gkiosk.remote.api.ApiClient;
import com.jwlryk.gkiosk.remote.api.ApiResponse;
import com.jwlryk.gkiosk.remote.api.ProductCategory;
// import com.jwlryk.gkiosk.remote.KioskRemoteOps; // no server-side filtering in this screen
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.jwlryk.gkiosk.model.ProductItem;

public class ProductListActivity extends AppCompatActivity {

    private String selectedCategoryKey = "All";
    private String selectedSortKey = "recommend";
    private String selectedSubTagKey = "*"; // * means all
    private static final String CATEGORY_CLASS = "GKVEN"; // backend category namespace
    private String selectedCategoryCodeFull = null; // full code like GKVEN-CTGRY-XXX; null means ALL
    private static final int PAGE_SIZE = 16; // 4x4
    private int currentPage = 1;
    private int totalPages = 1;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);
        UiUtil.enableImmersiveMode(this);

        // Build category/subcategory/sort rows
        buildSortTabs();
        fetchAndBuildCategories();
        setupPaginationControls();
        renderGrid();

        // Load banners
        ImageView top = findViewById(R.id.img_top_banner);
        ImageView left = findViewById(R.id.img_bottom_left);
        ImageView right = findViewById(R.id.img_bottom_right);
        if (top != null)
            Glide.with(this).load(TOP_AD_IMAGE).placeholder(R.drawable.grad_01).centerCrop().into(top);
        if (left != null)
            Glide.with(this).load(LEFT_AD_IMAGE).placeholder(R.drawable.grad_01).centerCrop().into(left);
        if (right != null)
            Glide.with(this).load(RIGHT_AD_IMAGE).placeholder(R.drawable.grad_01).centerCrop().into(right);
    }

    private void bindProductCard(View root, ProductItem item) {
        TextView brand = root.findViewById(R.id.view_brand);
        TextView title = root.findViewById(R.id.view_title);
        TextView price = root.findViewById(R.id.view_price);
        ChipGroup tags = root.findViewById(R.id.view_tags);
        ShapeableImageView image = root.findViewById(R.id.view_image);

        if (brand != null) brand.setText(nullTo(item.getBrand(), getString(R.string.brand_placeholder)));
        if (title != null) title.setText(nullTo(item.getTitle(), getString(R.string.product_name_placeholder)));
        if (price != null) price.setText(formatPrice(item.getPrice()));
        if (tags != null) { tags.removeAllViews(); }
        if (image != null) {
            String img = resolveImageUrl(item.getImage());
            if (img != null) {
                Glide.with(this).load(img).placeholder(R.drawable.grad_01).error(R.drawable.grad_01).centerCrop().into(image);
            } else {
                image.setImageResource(R.drawable.grad_01);
            }
        }

        // Open detail dialog on click
        root.setOnClickListener(v -> showProductDetail(item));
    }

    private String formatPrice(long price) {
        java.text.NumberFormat nf = java.text.NumberFormat.getCurrencyInstance(java.util.Locale.KOREA);
        return nf.format(price);
    }

    private static String nullTo(String s, String def) { return s == null ? def : s; }

    private String resolveImageUrl(String raw) {
        if (raw == null || raw.trim().isEmpty()) return null;
        String s = raw.trim();
        if (s.startsWith("http://") || s.startsWith("https://")) return s;
        if (s.startsWith("//")) return "https:" + s; // protocol-relative
        // Looks like a host path without scheme
        if (s.contains(".") && s.indexOf('/') > 0 && !s.startsWith("/")) {
            return "https://" + s;
        }
        // Relative path → resolve against API base URL
        String base = com.jwlryk.gkiosk.remote.api.ApiClient.get() != null ? KioskInfo.getInstance().getApiBaseUrl() : null;
        if (base == null || base.isEmpty()) base = "https://hub.airout.kr:8800/";
        if (!base.endsWith("/")) base = base + "/";
        if (s.startsWith("/")) s = s.substring(1);
        return base + s;
    }

    // Simple image loader from URL for demo (no caching)
    private static final String TOP_AD_IMAGE = "https://hub.airout.kr/banners/slide_21.jpg";
    private static final String LEFT_AD_IMAGE = "https://hub.airout.kr/banners/slide_22.jpg";
    private static final String RIGHT_AD_IMAGE = "https://hub.airout.kr/banners/slide_23.jpg";

    private void loadImage(String url, ImageView target) {
        if (target == null || url == null || url.isEmpty()) return;
        new Thread(() -> {
            try {
                java.net.URL u = new java.net.URL(url);
                java.net.HttpURLConnection c = (java.net.HttpURLConnection) u.openConnection();
                c.setConnectTimeout(5000);
                c.setReadTimeout(5000);
                c.connect();
                try (java.io.InputStream is = c.getInputStream()) {
                    final android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeStream(is);
                    target.post(() -> {
                        target.setImageBitmap(bmp);
                        target.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    });
                }
                c.disconnect();
            } catch (Exception ignored) { }
        }).start();
    }

    private void showProductDetail(ProductItem item) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(getLayoutInflater().inflate(R.layout.dialog_product_detail, null))
                .setCancelable(true)
                .create();
        dialog.show();

        // Avoid immersive here to prevent first-tap consumption; fit system windows
        if (dialog.getWindow() != null) {
            androidx.core.view.WindowCompat.setDecorFitsSystemWindows(dialog.getWindow(), true);
            android.util.DisplayMetrics dm = getResources().getDisplayMetrics();
            int width = (int) (dm.widthPixels * 0.9f);
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.getWindow().setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        android.view.View content = dialog.findViewById(android.R.id.content);
        if (content instanceof ViewGroup && ((ViewGroup) content).getChildCount() > 0) {
            content = ((ViewGroup) content).getChildAt(0);
        }
        if (content == null) return;

        TextView brand = content.findViewById(R.id.detail_brand);
        TextView title = content.findViewById(R.id.detail_title);
        TextView price = content.findViewById(R.id.detail_price);
        TextView desc = content.findViewById(R.id.detail_desc);
        ImageView topImg = content.findViewById(R.id.detail_image);
        ImageView largeImg = content.findViewById(R.id.detail_image_large);

        if (brand != null) brand.setText(nullTo(item.getBrand(), getString(R.string.brand_placeholder)));
        if (title != null) title.setText(nullTo(item.getTitle(), getString(R.string.product_name_placeholder)));
        if (price != null) price.setText(formatPrice(item.getPrice()));
        if (desc != null) desc.setText(item.getDescription() == null || item.getDescription().isEmpty() ? getString(R.string.vat_included) : item.getDescription());

        if (topImg != null) {
            String img = resolveImageUrl(item.getImage());
            if (img != null)
                Glide.with(this).load(img).placeholder(R.drawable.grad_01).centerCrop().into(topImg);
        }
        if (largeImg != null) {
            String dimg = resolveImageUrl(item.getDetailImage());
            if (dimg != null)
                Glide.with(this).load(dimg).placeholder(R.drawable.grad_01).centerCrop().into(largeImg);
        }

        View close = content.findViewById(R.id.btn_dialog_close);
        if (close != null) close.setOnClickListener(v -> dialog.dismiss());

        View buyNow = content.findViewById(R.id.btn_buy_now);
        if (buyNow != null) buyNow.setOnClickListener(v -> {
            dialog.dismiss();
            android.content.Intent intent = new android.content.Intent(ProductListActivity.this, ProductPurchaseActivity.class);
            intent.putExtra(ProductPurchaseActivity.EXTRA_ID, item.getId());
            intent.putExtra(ProductPurchaseActivity.EXTRA_CODE, item.getProductCode());
            intent.putExtra(ProductPurchaseActivity.EXTRA_TITLE, item.getTitle());
            intent.putExtra(ProductPurchaseActivity.EXTRA_PRICE, item.getPrice());
            intent.putExtra(ProductPurchaseActivity.EXTRA_IMAGE, item.getImage());
            intent.putExtra(ProductPurchaseActivity.EXTRA_BRAND, item.getBrand());
            intent.putExtra(ProductPurchaseActivity.EXTRA_DESC, item.getDescription());
            startActivity(intent);
        });
    }


    private void buildSortTabs() {
        android.widget.LinearLayout container = findViewById(R.id.view_sort_container);
        if (container == null) return;
        container.removeAllViews();
        String[] labels = getResources().getStringArray(R.array.sort_options_labels);
        String[] keys = getResources().getStringArray(R.array.sort_options_keys);
        int m = getResources().getDimensionPixelSize(R.dimen.space_xs);
        for (int i = 0; i < labels.length && i < keys.length; i++) {
            String label = labels[i]; String key = keys[i];
            TextView tv = new TextView(this);
            tv.setText(label);
            tv.setPadding(0,0,0,0);
            tv.setOnClickListener(v -> { if (!selectedSortKey.equals(key)) { selectedSortKey = key; buildSortTabs(); renderGrid(); } });
            boolean night = (getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES;
            if (key.equals(selectedSortKey)) {
                tv.setTextColor(androidx.core.content.ContextCompat.getColor(this, night ? R.color.white : R.color.black));
                tv.setTypeface(tv.getTypeface(), android.graphics.Typeface.BOLD);
            } else {
                tv.setTextColor(androidx.core.content.ContextCompat.getColor(this, night ? R.color.white_1 : R.color.text_secondary));
                tv.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL);
            }
            android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0,0,m,0);
            tv.setLayoutParams(lp);
            container.addView(tv);
            if (i < labels.length - 1) {
                TextView sep = new TextView(this); sep.setText("|");
                sep.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.text_secondary));
                android.widget.LinearLayout.LayoutParams slp = new android.widget.LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
                slp.setMargins(0,0,m,0);
                sep.setLayoutParams(slp);
                container.addView(sep);
            }
        }
    }

    private void buildSubcategoryTabs() {
        android.widget.LinearLayout container = findViewById(R.id.view_subcategory_container);
        if (container == null) return;
        container.removeAllViews();

        java.util.List<ProductCategory> cats = ProductCategoryList.getInstance().asList();
        java.util.List<String> keys = new java.util.ArrayList<>();
        java.util.List<String> labels = new java.util.ArrayList<>();
        if (cats != null && !cats.isEmpty()) {
            for (ProductCategory c : cats) {
                if (c == null) continue;
                String code = c.code == null ? null : c.code.trim();
                String label = labelFromNameOrAll(code, c.name);
                String key = label; // subcategory filters by label (tags contain label)
                if (key != null && !key.isEmpty()) { keys.add(key); labels.add(label); }
            }
        } else {
            // fallback: derive from product tags
            java.util.LinkedHashSet<String> tags = new java.util.LinkedHashSet<>();
            for (ProductItem p : ProductItemList.getInstance().asList()) { for (String t : p.getTags()) { if (t != null && !t.isEmpty()) tags.add(t); } }
            for (String t : tags) { keys.add(t); labels.add(t); }
        }

        // Always add "전체"
        addSubcategoryChip(container, "*", getString(R.string.sub_all), selectedSubTagKey);
        for (int i = 0; i < keys.size(); i++) { addSubcategoryChip(container, keys.get(i), labels.get(i), selectedSubTagKey); }
    }

    private void buildCategoryTabsFromCache() {
        com.google.android.material.button.MaterialButtonToggleGroup group = findViewById(R.id.view_category_tabs);
        if (group == null) return;
        group.removeAllViews();

        java.util.List<ProductCategory> cats = ProductCategoryList.getInstance().asList();
        if (cats != null && !cats.isEmpty()) {
            java.util.List<ProductCategory> sorted = new java.util.ArrayList<>(cats);
            java.text.Collator collator = java.text.Collator.getInstance(java.util.Locale.KOREA);
            java.util.Collections.sort(sorted, (a, b) -> {
                String ka = normalizeCategoryKey(extractCodeSuffix(a == null ? null : a.code));
                String kb = normalizeCategoryKey(extractCodeSuffix(b == null ? null : b.code));
                String la = (a == null || a.name == null || a.name.isEmpty()) ? ka : a.name;
                String lb = (b == null || b.name == null || b.name.isEmpty()) ? kb : b.name;
                int ra = rankCategory(ka, la);
                int rb = rankCategory(kb, lb);
                if (ra != rb) return Integer.compare(ra, rb);
                return collator.compare(la == null ? "" : la, lb == null ? "" : lb);
            });
            for (int i = 0; i < sorted.size(); i++) {
                ProductCategory c = sorted.get(i);
                String code = c == null ? null : c.code; // full code
                String key = code; // use full code as internal key/tag
                String label = displayLabelForCategory(code, c == null ? null : c.name);
                boolean check = isAllCategory(key, label);
                addCategoryTabButton(group, key, label, check);
            }
        }

        group.addOnButtonCheckedListener((g, checkedId, isChecked) -> {
            if (isChecked) {
                View v = g.findViewById(checkedId);
                Object tag = v != null ? v.getTag() : null;
                if (tag != null) {
                    selectedCategoryKey = tag.toString();
                    buildSubcategoryTabs();
                    selectedCategoryCodeFull = isAllCategory(selectedCategoryKey, null) ? null : ensureFullCategoryCode(selectedCategoryKey);
                    renderGrid();
                }
            }
        });

    }

    // No category dialog on item click; detail popup is used instead.

    private void addSubcategoryChip(android.widget.LinearLayout container, String key, String label, String selectedKey) {
        android.view.ContextThemeWrapper themed = new android.view.ContextThemeWrapper(this, R.style.Widget_GKiosk_PillButton_BW);
        com.google.android.material.button.MaterialButton btn = new com.google.android.material.button.MaterialButton(themed, null, 0);
        btn.setText(label);
        btn.setCheckable(true);
        btn.setId(View.generateViewId());
        btn.setTag(key);
        android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        int m = getResources().getDimensionPixelSize(R.dimen.space_s);
        lp.setMargins(0,0,m,0);
        btn.setLayoutParams(lp);
        applyBwPillStyle(btn);
        btn.setChecked(key.equals(selectedKey));
        btn.setOnClickListener(v -> {
            selectedSubTagKey = key;
            for (int c = 0; c < container.getChildCount(); c++) {
                View child = container.getChildAt(c);
                if (child instanceof com.google.android.material.button.MaterialButton) {
                    com.google.android.material.button.MaterialButton mb = (com.google.android.material.button.MaterialButton) child;
                    Object tagObj = mb.getTag();
                    mb.setChecked(tagObj != null && tagObj.equals(selectedSubTagKey));
                }
            }
            renderGrid();
        });
        container.addView(btn);
    }

    private String extractCodeSuffix(String code) {
        if (code == null) return null;
        String u = code.trim().toUpperCase(java.util.Locale.ROOT);
        int idx = u.lastIndexOf('-');
        return (idx >= 0 && idx + 1 < u.length()) ? u.substring(idx + 1) : u;
    }

    private String keyLabelToInternalKey(String key) {
        if (key == null) return null;
        return "ALL".equalsIgnoreCase(key) ? "All" : key;
    }

    private int rankCategory(String key, String label) {
        boolean isAll = isAllCategory(key, label);
        boolean isEtc = (key != null && key.equalsIgnoreCase("ETC"));
        boolean isGita = (label != null && label.contains("기타"));
        if (isAll) return 0;           // first
        if (isEtc || isGita) return 2; // last
        return 1;                       // middle
    }

    private String normalizeCategoryKey(String keyRaw) {
        if (keyRaw == null) return null;
        String s = keyRaw.trim();
        int comma = s.indexOf(',');
        if (comma >= 0) s = s.substring(0, comma);
        return s.trim();
    }

    private boolean isAllCategory(String key, String label) {
        if (key != null && key.trim().equalsIgnoreCase("ALL")) return true;
        if (label != null) {
            String l = label.trim();
            if (l.equalsIgnoreCase("ALL")) return true;
            if (l.contains("전체")) return true;
        }
        return false;
    }

    private String displayLabelForCategory(String codeRaw, String nameRaw) {
        String codeUp = codeRaw == null ? null : codeRaw.trim().toUpperCase(java.util.Locale.ROOT);
        String suffix = normalizeCategoryKey(extractCodeSuffix(codeRaw));
        // Force ALL to "전체" if code contains ALL anywhere
        if ((codeUp != null && (codeUp.contains("GKVEN-CTGRY-ALL") || codeUp.contains("GKVEN-CTGY-ALL")))
                || (suffix != null && suffix.equalsIgnoreCase("ALL"))) {
            return getString(R.string.sub_all);
        }
        // If name contains comma-separated localized part, prefer the last part
        if (nameRaw != null && !nameRaw.trim().isEmpty()) {
            String n = nameRaw.trim();
            if (n.contains(",")) {
                String[] parts = n.split(",");
                String last = parts[parts.length - 1].trim();
                if (!last.isEmpty()) return last;
            }
            return n;
        }
        // Fallback to suffix
        return suffix == null ? "" : suffix;
    }

    private String labelFromNameOrAll(String codeRaw, String nameRaw) {
        String codeUp = codeRaw == null ? null : codeRaw.trim().toUpperCase(java.util.Locale.ROOT);
        if (codeUp != null && (codeUp.contains("GKVEN-CTGRY-ALL") || codeUp.contains("GKVEN-CTGY-ALL"))) {
            return getString(R.string.sub_all);
        }
        if (nameRaw != null && !nameRaw.trim().isEmpty()) {
            String n = nameRaw.trim();
            if (n.contains(",")) {
                String[] parts = n.split(",");
                String last = parts[parts.length - 1].trim();
                if (!last.isEmpty()) return last;
            }
            return n;
        }
        return displayLabelForCategory(codeRaw, nameRaw);
    }

    private String findFullCategoryCodeByKey(String key) {
        if (key == null) return null;
        java.util.List<ProductCategory> cats = ProductCategoryList.getInstance().asList();
        if (cats == null) return null;
        for (ProductCategory c : cats) {
            String suffix = extractCodeSuffix(c == null ? null : c.code);
            if (suffix != null && suffix.equalsIgnoreCase(key)) return c.code;
        }
        return null;
    }

    private String ensureFullCategoryCode(String keyOrSuffix) {
        if (keyOrSuffix == null) return null;
        String s = keyOrSuffix.trim();
        if (s.isEmpty()) return null;
        if (s.toUpperCase(java.util.Locale.ROOT).startsWith("GKVEN-CTGRY-")) return s;
        String suffix = s.equalsIgnoreCase("All") ? "ALL" : s.toUpperCase(java.util.Locale.ROOT);
        return "GKVEN-CTGRY-" + suffix;
    }

    private void addCategoryTabButton(com.google.android.material.button.MaterialButtonToggleGroup group, String key, String label, boolean check) {
        if (key == null || label == null) return;
        android.view.ContextThemeWrapper themed = new android.view.ContextThemeWrapper(this, R.style.Widget_GKiosk_TabButton_BW);
        com.google.android.material.button.MaterialButton btn = new com.google.android.material.button.MaterialButton(themed, null, 0);
        btn.setText(label);
        btn.setCheckable(true);
        btn.setId(View.generateViewId());
        btn.setTag(key);
        android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        int m = getResources().getDimensionPixelSize(R.dimen.space_m);
        lp.setMargins(0,0,m,0);
        btn.setLayoutParams(lp);
        applyBwTabStyle(btn);
        group.addView(btn);
        if (check) group.check(btn.getId());
    }

    private void applyBwTabStyle(com.google.android.material.button.MaterialButton btn) {
        android.content.res.ColorStateList bg = androidx.appcompat.content.res.AppCompatResources.getColorStateList(this, R.color.bw_bg);
        android.content.res.ColorStateList txt = androidx.appcompat.content.res.AppCompatResources.getColorStateList(this, R.color.bw_text);
        android.content.res.ColorStateList stroke = androidx.appcompat.content.res.AppCompatResources.getColorStateList(this, R.color.bw_stroke);
        android.content.res.ColorStateList ripple = androidx.appcompat.content.res.AppCompatResources.getColorStateList(this, R.color.primary_lighter);
        if (bg != null) btn.setBackgroundTintList(bg);
        if (txt != null) btn.setTextColor(txt);
        if (stroke != null) btn.setStrokeColor(stroke);
        if (ripple != null) btn.setRippleColor(ripple);
    }

    private void applyBwPillStyle(com.google.android.material.button.MaterialButton btn) {
        android.content.res.ColorStateList bg = androidx.appcompat.content.res.AppCompatResources.getColorStateList(this, R.color.bw_bg);
        android.content.res.ColorStateList txt = androidx.appcompat.content.res.AppCompatResources.getColorStateList(this, R.color.bw_text);
        android.content.res.ColorStateList stroke = androidx.appcompat.content.res.AppCompatResources.getColorStateList(this, R.color.bw_stroke);
        if (bg != null) btn.setBackgroundTintList(bg);
        if (txt != null) btn.setTextColor(txt);
        if (stroke != null) btn.setStrokeColor(stroke);
    }

    private void fetchAndBuildCategories() {
        // First render whatever is cached (may be empty), then fetch and rebuild
        buildCategoryTabsFromCache();
        buildSubcategoryTabs();

        ApiClient.get().getProductCategories(CATEGORY_CLASS).enqueue(new Callback<ApiResponse<java.util.List<ProductCategory>>>() {
            @Override public void onResponse(Call<ApiResponse<java.util.List<ProductCategory>>> call, Response<ApiResponse<java.util.List<ProductCategory>>> response) {
                if (!response.isSuccessful() || response.body() == null) { runOnUiThread(() -> buildSubcategoryTabs()); return; }
                java.util.List<ProductCategory> list = response.body().getPayload();
                ProductCategoryList.getInstance().setAll(list);
                runOnUiThread(() -> { buildCategoryTabsFromCache(); buildSubcategoryTabs(); });
            }
            @Override public void onFailure(Call<ApiResponse<java.util.List<ProductCategory>>> call, Throwable t) {
                runOnUiThread(() -> { buildCategoryTabsFromCache(); buildSubcategoryTabs(); });
            }
        });
    }

    private void renderGrid() {
        androidx.gridlayout.widget.GridLayout grid = findViewById(R.id.grid_tab_items);
        if (grid == null) return;
        grid.removeAllViews();
        java.util.List<ProductItem> filtered = new java.util.ArrayList<>();
        for (ProductItem p : ProductItemList.getInstance().asList()) {
            boolean match = (selectedCategoryCodeFull == null)
                    || (p.getCategory() != null && p.getCategory().contains(selectedCategoryCodeFull));
            if (!match) continue;
            if (!"*".equals(selectedSubTagKey)) {
                boolean hasTag = false;
                for (String t : p.getTags()) { if (selectedSubTagKey.equals(t)) { hasTag = true; break; } }
                if (!hasTag) continue;
            }
            filtered.add(p);
        }
        java.util.Collections.sort(filtered, (a, b) -> {
            switch (selectedSortKey) {
                case "price_desc": return Long.compare(b.getPrice(), a.getPrice());
                case "price_asc": return Long.compare(a.getPrice(), b.getPrice());
                case "recommend": default: return Integer.compare(b.getSortOrder(), a.getSortOrder());
            }
        });

        // Pagination math
        int total = filtered.size();
        totalPages = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
        if (currentPage > totalPages) currentPage = totalPages;
        if (currentPage < 1) currentPage = 1;
        updatePaginationBar();

        int start = (currentPage - 1) * PAGE_SIZE;
        int end = Math.min(total, start + PAGE_SIZE);

        grid.setColumnCount(4);
        for (int i = start; i < end; i++) {
            ProductItem it = filtered.get(i);
            View v = getLayoutInflater().inflate(R.layout.item_product_card, grid, false);
            bindProductCard(v, it);
            androidx.gridlayout.widget.GridLayout.LayoutParams lp = new androidx.gridlayout.widget.GridLayout.LayoutParams();
            lp.width = 0; lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            lp.columnSpec = androidx.gridlayout.widget.GridLayout.spec(androidx.gridlayout.widget.GridLayout.UNDEFINED, 1f);
            v.setLayoutParams(lp);
            grid.addView(v);
        }
    }

    private void setupPaginationControls() {
        View prev = findViewById(R.id.btn_page_prev);
        View next = findViewById(R.id.btn_page_next);
        if (prev != null) prev.setOnClickListener(v -> { if (currentPage > 1) { currentPage--; renderGrid(); } });
        if (next != null) next.setOnClickListener(v -> { if (currentPage < totalPages) { currentPage++; renderGrid(); } });
        updatePaginationBar();
    }

    private void updatePaginationBar() {
        TextView info = findViewById(R.id.view_page_info);
        View prev = findViewById(R.id.btn_page_prev);
        View next = findViewById(R.id.btn_page_next);
        if (info != null) info.setText(currentPage + "/" + totalPages);
        if (prev != null) prev.setEnabled(currentPage > 1);
        if (next != null) next.setEnabled(currentPage < totalPages);
    }
}
