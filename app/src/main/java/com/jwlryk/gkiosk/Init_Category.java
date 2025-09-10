package com.jwlryk.gkiosk;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.List;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.imageview.ShapeableImageView;
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

public class Init_Category extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init_category);
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void bindProductToCard(View cardRoot, JSONObject obj) throws JSONException {
        TextView brand = cardRoot.findViewById(R.id.view_brand);
        TextView title = cardRoot.findViewById(R.id.view_title);
        TextView price = cardRoot.findViewById(R.id.view_price);
        ChipGroup tags = cardRoot.findViewById(R.id.view_tags);
        ShapeableImageView image = cardRoot.findViewById(R.id.view_image);

        if (brand != null) brand.setText(obj.optString("brand", "BRAND"));
        if (title != null) title.setText(obj.optString("title", "상품명"));
        if (price != null) price.setText(formatPrice(obj.optLong("price", 0)));

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

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            UiUtil.enableImmersiveMode(this);
        }
    }
}
