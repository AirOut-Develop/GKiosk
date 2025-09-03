package com.jwlryk.gkiosk;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class Init_Category extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init_category);
        UiUtil.enableImmersiveMode(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            UiUtil.enableImmersiveMode(this);
        }
    }
}
