package com.expressapps.presentexpress;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.expressapps.presentexpress.helper.FilterItem;
import com.expressapps.presentexpress.helper.Funcs;
import com.expressapps.presentexpress.helper.ImageFilter;
import com.expressapps.presentexpress.helper.ImageSlide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.slider.Slider;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import javax.annotation.Nullable;

public class EditorActivity extends AppCompatActivity {
    private Bitmap originalImage;
    private FilterItem filtersApplied = new FilterItem();
    private int imageIdx = 0;
    private static final BiMap<Integer, ImageFilter> filterMap = HashBiMap.create();

    static {
        filterMap.put(R.id.greyscalefilter, ImageFilter.GREYSCALE);
        filterMap.put(R.id.sepiafilter, ImageFilter.SEPIA);
        filterMap.put(R.id.blackwhitefilter, ImageFilter.BLACK_WHITE);
        filterMap.put(R.id.redfilter, ImageFilter.RED);
        filterMap.put(R.id.greenfilter, ImageFilter.GREEN);
        filterMap.put(R.id.bluefilter, ImageFilter.BLUE);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_editor);

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        Slider brightnessSlider = findViewById(R.id.brightness);
        brightnessSlider.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                filtersApplied.setBrightness(
                        Funcs.transformRange(value, -100f, 100f, -0.5f, 0.5f));
                refreshImage();
            }
        });

        Slider contrastSlider = findViewById(R.id.contrast);
        contrastSlider.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                if (value < 0f) {
                    filtersApplied.setContrast(
                            Funcs.transformRange(value, -100f, 0f, 0.5f, 1f));
                } else {
                    filtersApplied.setContrast(
                            Funcs.transformRange(value, 0f, 100f, 1f, 2f));
                }
                refreshImage();
            }
        });

        Intent intent = getIntent();
        imageIdx = intent.getIntExtra("idx", 0);
        originalImage = ((ImageSlide) MainActivity.slideshow.slides.get(imageIdx)).original;

        ImageView previewImage = findViewById(R.id.previewimg);
        previewImage.setImageBitmap(originalImage);

        filtersApplied.setFilter(ImageFilter.parseString(intent.getStringExtra("filter")));
        filtersApplied.setBrightness(intent.getFloatExtra("brightness", 0f));
        filtersApplied.setContrast(intent.getFloatExtra("contrast", 1f));
        filtersApplied.setRotation(intent.getIntExtra("rotation", 0));
        filtersApplied.flipHorizontal = intent.getBooleanExtra("fliph", false);
        filtersApplied.flipVertical = intent.getBooleanExtra("flipv", false);

        brightnessSlider.setValue((float) Math.round(
                Funcs.transformRange(filtersApplied.getBrightness(), -0.5f, 0.5f, -100f, 100f)));

        if (filtersApplied.getContrast() < 1f) {
            contrastSlider.setValue((float) Math.round(
                    Funcs.transformRange(filtersApplied.getContrast(), 0.5f, 1f, -100f, 0f)));
        } else {
            contrastSlider.setValue((float) Math.round(
                    Funcs.transformRange(filtersApplied.getContrast(), 1f, 2f, 0f, 100f)));
        }

        if (filtersApplied.getFilter() != ImageFilter.NONE) {
            @Nullable Integer id = filterMap.inverse().get(filtersApplied.getFilter());
            if (id != null) ((MaterialButton) findViewById(id)).setChecked(true);
        }

        MaterialCheckBox checkh = findViewById(R.id.fliphorizontal);
        checkh.setChecked(filtersApplied.flipHorizontal);
        MaterialCheckBox checkv = findViewById(R.id.flipvertical);
        checkv.setChecked(filtersApplied.flipVertical);
        refreshImage();

        ScrollView scroller = findViewById(R.id.scroller);
        ExtendedFloatingActionButton fab = findViewById(R.id.fab_done);

        OnApplyWindowInsetsListener listener = (v, insets) -> {
            Insets in = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(in.left, 0, in.right, 0);
            return insets;
        };

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.toolbar), listener);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.previewlayout), listener);

        Insets[] systemInsets = {null};
        Runnable updateScrollerPadding = () -> {
            if (systemInsets[0] != null && fab.getHeight() > 0) {
                Insets in = systemInsets[0];
                int bottomPadding = in.bottom + fab.getHeight() + Funcs.toDp(16);
                scroller.setPadding(in.left, 0, in.right, bottomPadding);
            }
        };

        ViewCompat.setOnApplyWindowInsetsListener(scroller, (v, insets) -> {
            systemInsets[0] = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout() | WindowInsetsCompat.Type.ime());
            updateScrollerPadding.run();
            return insets;
        });

        fab.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                fab.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                updateScrollerPadding.run();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(fab, (v, insets) -> {
            Insets in = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout() | WindowInsetsCompat.Type.ime());
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();

            int margin = Funcs.toDp(20);
            params.setMargins(0, 0, in.right + margin, in.bottom + margin);

            v.setLayoutParams(params);
            return insets;
        });

        // Handle back button press
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Funcs.showDialog(EditorActivity.this, R.string.editor_apply_changes, R.string.close_editor, (d, b) -> {
                    switch (b) {
                        case DialogInterface.BUTTON_POSITIVE:
                            applyChangesAndFinish();
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            finish();
                            break;
                    }
                });
            }
        });
    }

    public void onFabClick(View v) {
        applyChangesAndFinish();
    }

    private void applyChangesAndFinish() {
        ((ImageSlide) MainActivity.slideshow.slides.get(imageIdx)).filters = filtersApplied;
        finish();
    }

    private void refreshImage() {
        LinearLayout previewLayout = findViewById(R.id.previewlayout);
        ImageView previewImage = findViewById(R.id.previewimg);

        if (previewLayout.getVisibility() == View.VISIBLE)
            previewImage.setImageBitmap(MainActivity.applyFilters(originalImage, filtersApplied));
    }

    public void onShowPreviewClick(View v) {
        MaterialCheckBox previewCheck = (MaterialCheckBox) v;
        LinearLayout previewLayout = findViewById(R.id.previewlayout);

        if (previewCheck.isChecked()) {
            previewLayout.setVisibility(View.VISIBLE);
            refreshImage();
        } else {
            previewLayout.setVisibility(View.GONE);
        }
    }

    private void resetFilterButtons(@IdRes int... except) {
        for (int buttonId : filterMap.keySet()) {
            if (except.length == 0 || buttonId != except[0]) {
                MaterialButton button = findViewById(buttonId);
                button.setChecked(false);
            }
        }
    }

    private void updateChosenFilter(ImageFilter filter, @IdRes int id) {
        resetFilterButtons(id);

        if (filtersApplied.getFilter() == filter) {
            filtersApplied.setFilter(ImageFilter.NONE);
        } else {
            filtersApplied.setFilter(filter);
        }
        refreshImage();
    }

    public void onFilterButtonClick(View v) {
        int id = v.getId();
        ImageFilter filter = filterMap.get(id);
        if (filter != null) {
            updateChosenFilter(filter, id);
        }
    }

    public void onRotateRightClick(View v) {
        if (filtersApplied.getRotation() >= 270) {
            filtersApplied.setRotation(0);
        } else {
            filtersApplied.setRotation(filtersApplied.getRotation() + 90);
        }
        refreshImage();
    }

    public void onRotateLeftClick(View v) {
        if (filtersApplied.getRotation() <= 0) {
            filtersApplied.setRotation(270);
        } else {
            filtersApplied.setRotation(filtersApplied.getRotation() - 90);
        }
        refreshImage();
    }

    public void onFlipHorizontalClick(View v) {
        MaterialCheckBox check = (MaterialCheckBox) v;
        filtersApplied.flipHorizontal = check.isChecked();
        refreshImage();
    }

    public void onFlipVerticalClick(View v) {
        MaterialCheckBox check = (MaterialCheckBox) v;
        filtersApplied.flipVertical = check.isChecked();
        refreshImage();
    }

    public void onResetClick(View v) {
        resetFilterButtons();

        Slider brightnessSlider = findViewById(R.id.brightness);
        brightnessSlider.setValue(0f);
        Slider contrastSlider = findViewById(R.id.contrast);
        contrastSlider.setValue(0f);

        MaterialCheckBox checkh = findViewById(R.id.fliphorizontal);
        checkh.setChecked(false);
        MaterialCheckBox checkv = findViewById(R.id.flipvertical);
        checkv.setChecked(false);

        ImageView previewImage = findViewById(R.id.previewimg);
        previewImage.setImageBitmap(originalImage);

        filtersApplied = new FilterItem();
    }
}