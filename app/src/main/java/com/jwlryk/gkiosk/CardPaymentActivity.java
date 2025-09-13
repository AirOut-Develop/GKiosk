package com.jwlryk.gkiosk;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class CardPaymentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_payment);
        UiUtil.enableImmersiveMode(this);

        TextView tvTitle = findViewById(R.id.cp_title);
        TextView tvPrice = findViewById(R.id.cp_price);
        ImageView iv = findViewById(R.id.cp_image);

        String title = getIntent().getStringExtra(ProductPurchaseActivity.EXTRA_TITLE);
        long price = getIntent().getLongExtra(ProductPurchaseActivity.EXTRA_PRICE, 0);
        String img = getIntent().getStringExtra(ProductPurchaseActivity.EXTRA_IMAGE);

        if (tvTitle != null) tvTitle.setText(title != null ? title : "");
        if (tvPrice != null) tvPrice.setText(formatPrice(price));
        if (iv != null && img != null && (img.startsWith("http://") || img.startsWith("https://"))) {
            Glide.with(this).load(img).placeholder(R.drawable.grad_01).centerCrop().into(iv);
        }

        findViewById(R.id.cp_btn_pay).setOnClickListener(v ->
                Toast.makeText(this, getString(R.string.card_pay_started), Toast.LENGTH_SHORT).show());
        findViewById(R.id.cp_btn_cancel).setOnClickListener(v -> finish());
    }

    private String formatPrice(long price) {
        java.text.NumberFormat nf = java.text.NumberFormat.getCurrencyInstance(java.util.Locale.KOREA);
        return nf.format(price);
    }
}

