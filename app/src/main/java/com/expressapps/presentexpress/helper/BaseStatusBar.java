package com.expressapps.presentexpress.helper;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowInsets;

public class BaseStatusBar extends View {
    private int mStatusBarHeight;

    public BaseStatusBar(Context context) {
        this(context, null);
    }

    public BaseStatusBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        setSystemUiVisibility(SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        mStatusBarHeight = dpToPx(24.0f);
        return insets.consumeSystemWindowInsets();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mStatusBarHeight);
    }

    private int dpToPx(float dp) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}