package com.jwlryk.gkiosk;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class Init_Splash extends AppCompatActivity {

    private static final long DEFAULT_DELAY_MS = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init_splash);

        long delayMs = getIntent().getLongExtra("delay_ms", DEFAULT_DELAY_MS);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(Init_Splash.this, Init_Category.class);
            startActivity(intent);
            finish();
        }, delayMs);
    }
}

