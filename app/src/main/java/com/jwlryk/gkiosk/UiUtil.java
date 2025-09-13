package com.jwlryk.gkiosk;

import android.app.Activity;
import android.view.View;
import android.view.Window;

import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class UiUtil {
    public static void enableImmersiveMode(Activity activity) {
        if (activity == null) return;
        Window window = activity.getWindow();
        View decor = window.getDecorView();

        // Draw content edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false);

        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, decor);
        if (controller != null) {
            controller.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            );
            controller.hide(WindowInsetsCompat.Type.systemBars());
        }
    }

    // Overload: apply immersive mode to an arbitrary Window (e.g., Dialog window)
    public static void enableImmersiveMode(Window window) {
        if (window == null) return;
        View decor = window.getDecorView();
        // Draw content edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false);

        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, decor);
        if (controller != null) {
            controller.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            );
            controller.hide(WindowInsetsCompat.Type.systemBars());
        }
    }

    // Scales the entire activity content to fit a target "design" width in dp.
    // Useful for very-small screens to render kiosk UIs proportionally smaller.
    public static void applyDesignScale(Activity activity, int designWidthDp) {
        if (activity == null || designWidthDp <= 0) return;
        View root = activity.findViewById(android.R.id.content);
        if (!(root instanceof android.view.ViewGroup)) return;
        android.view.ViewGroup vg = (android.view.ViewGroup) root;
        if (vg.getChildCount() == 0) return;
        View content = vg.getChildAt(0);
        if (content == null) return;

        android.util.DisplayMetrics dm = activity.getResources().getDisplayMetrics();
        float screenWidthPx = dm.widthPixels;
        float designWidthPx = android.util.TypedValue.applyDimension(android.util.TypedValue.COMPLEX_UNIT_DIP, designWidthDp, dm);
        float scale = screenWidthPx / designWidthPx;
        if (scale >= 1f) return; // Only scale down on small screens

        content.setPivotX(0f);
        content.setPivotY(0f);
        content.setScaleX(scale);
        content.setScaleY(scale);
    }
}
