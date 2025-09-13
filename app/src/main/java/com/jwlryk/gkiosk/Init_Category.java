package com.jwlryk.gkiosk;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class Init_Category extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init_category);
        UiUtil.enableImmersiveMode(this);

        findViewById(R.id.card_component_preview).setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, ComponentPreview.class));
        });
        findViewById(R.id.card_api_debug).setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, ApiDebugActivity.class));
        });
        findViewById(R.id.card_product_list).setOnClickListener(v -> ensureProductsThenNavigate());

        // Auto-fetch products on entry, then navigate to ProductList when ready
        com.jwlryk.gkiosk.data.KioskPrefs.loadIntoSingleton(this);
        ensureProductsThenNavigate();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            UiUtil.enableImmersiveMode(this);
        }
    }

    private void maybeAutoBootstrap() {
        final com.jwlryk.gkiosk.data.KioskInfo ki = com.jwlryk.gkiosk.data.KioskInfo.getInstance();
        final String base = ki.getApiBaseUrl();
        if (base == null || base.isEmpty()) return;
        com.jwlryk.gkiosk.remote.KioskRemoteOps.resolveAndRefreshProducts(ki, new com.jwlryk.gkiosk.remote.KioskRemoteOps.Listener() {
            @Override public void onSuccess() { startProductList(); }
            @Override public void onError(@androidx.annotation.NonNull String message, @androidx.annotation.Nullable Throwable t) { /* stay on menu */ }
        });
    }

    // assignment now centralized in KioskRemoteOps

    private void startProductList() {
        android.content.Intent intent = new android.content.Intent(this, ProductListActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean isBootstrapping = false;

    private void ensureProductsThenNavigate() {
        if (isBootstrapping) return;
        if (com.jwlryk.gkiosk.data.ProductItemList.getInstance().size() > 0) {
            startActivity(new android.content.Intent(this, ProductListActivity.class));
            return;
        }
        final androidx.appcompat.app.AlertDialog progress = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(new android.widget.ProgressBar(this))
                .setMessage("상품 정보를 불러오는 중...")
                .setCancelable(false)
                .create();
        progress.show();
        isBootstrapping = true;

        final com.jwlryk.gkiosk.data.KioskInfo ki = com.jwlryk.gkiosk.data.KioskInfo.getInstance();
        // Allow proceeding even if baseUrl is empty; ApiClient uses a safe default
        com.jwlryk.gkiosk.remote.KioskRemoteOps.resolveAndRefreshProducts(ki, new com.jwlryk.gkiosk.remote.KioskRemoteOps.Listener() {
            @Override public void onSuccess() {
                isBootstrapping = false;
                progress.dismiss();
                startActivity(new android.content.Intent(Init_Category.this, ProductListActivity.class));
            }
            @Override public void onError(@androidx.annotation.NonNull String message, @androidx.annotation.Nullable Throwable t) {
                isBootstrapping = false;
                progress.dismiss();
                android.widget.Toast.makeText(Init_Category.this, "상품 로딩 실패: " + message, android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }
}
