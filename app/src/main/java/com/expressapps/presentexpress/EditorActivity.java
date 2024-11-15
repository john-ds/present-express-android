package com.expressapps.presentexpress;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.expressapps.presentexpress.helper.FilterItem;
import com.expressapps.presentexpress.helper.Funcs;
import com.expressapps.presentexpress.helper.ImageFilter;
import com.expressapps.presentexpress.helper.ImageSlide;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.slider.Slider;

import java.util.Objects;

public class EditorActivity extends AppCompatActivity {
    private Bitmap originalImage;
    private FilterItem filtersApplied = new FilterItem();
    private int imageIdx = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_GradientStatusBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        setSupportActionBar(findViewById(R.id.toolbar));
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.photo_editor);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

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
        originalImage =((ImageSlide) MainActivity.slideshow.slides.get(imageIdx)).original;

        ImageView previewImage = findViewById(R.id.previewimg);
        previewImage.setImageBitmap(originalImage);

        filtersApplied.setFilter(ImageFilter.parseString(intent.getStringExtra("filter")));
        filtersApplied.setBrightness(intent.getFloatExtra("brightness", 0f));
        filtersApplied.setContrast(intent.getFloatExtra("contrast", 1f));
        filtersApplied.setRotation(intent.getIntExtra("rotation", 0));
        filtersApplied.flipHorizontal = intent.getBooleanExtra("fliph", false);
        filtersApplied.flipVertical = intent.getBooleanExtra("flipv", false);

        brightnessSlider.setValue((float)Math.round(
            Funcs.transformRange(filtersApplied.getBrightness(), -0.5f, 0.5f, -100f, 100f)));

        if (filtersApplied.getContrast() < 1f) {
            contrastSlider.setValue((float)Math.round(
                Funcs.transformRange(filtersApplied.getContrast(), 0.5f, 1f, -100f, 0f)));
        } else {
            contrastSlider.setValue((float)Math.round(
                Funcs.transformRange(filtersApplied.getContrast(), 1f, 2f, 0f, 100f)));
        }

        MaterialCheckBox checkh = findViewById(R.id.fliphorizontal);
        checkh.setChecked(filtersApplied.flipHorizontal);
        MaterialCheckBox checkv = findViewById(R.id.flipvertical);
        checkv.setChecked(filtersApplied.flipVertical);
        refreshImage();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_editor_drawer, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        Funcs.showDialog(EditorActivity.this, R.string.editor_apply_changes, R.string.close_editor, (d, b) -> {
            switch (b) {
                case DialogInterface.BUTTON_POSITIVE:
                    applyChanges();
                    finish();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    finish();
                    break;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.nav_apply) {
            applyChanges();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void applyChanges() {
        ((ImageSlide) MainActivity.slideshow.slides.get(imageIdx)).filters = filtersApplied;
    }

    private void refreshImage() {
        LinearLayout previewLayout = findViewById(R.id.previewlayout);
        ImageView previewImage = findViewById(R.id.previewimg);

        if (previewLayout.getVisibility() == View.VISIBLE)
            previewImage.setImageBitmap(MainActivity.applyFilters(originalImage, filtersApplied));
    }

    public void onShowPreviewClick(View v) {
        MaterialCheckBox previewCheck = (MaterialCheckBox)v;
        LinearLayout previewLayout = findViewById(R.id.previewlayout);

        if (previewCheck.isChecked()) {
            previewLayout.setVisibility(View.VISIBLE);
            refreshImage();
        } else {
            previewLayout.setVisibility(View.GONE);
        }
    }

    private void resetFilterButtons() {
        findViewById(R.id.greyscalefilter).setBackgroundColor(Color.WHITE);
        findViewById(R.id.sepiafilter).setBackgroundColor(Color.WHITE);
        findViewById(R.id.blackwhitefilter).setBackgroundColor(Color.WHITE);
        findViewById(R.id.redfilter).setBackgroundColor(Color.WHITE);
        findViewById(R.id.greenfilter).setBackgroundColor(Color.WHITE);
        findViewById(R.id.bluefilter).setBackgroundColor(Color.WHITE);
    }

    private void updateChosenFilter(ImageFilter filter, @IdRes int id) {
        resetFilterButtons();
        if (filtersApplied.getFilter() == filter) {
            filtersApplied.setFilter(ImageFilter.NONE);
        } else {
            findViewById(id).setBackgroundColor(
                ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
            filtersApplied.setFilter(filter);
        }
        refreshImage();
    }

    public void onGreyscaleClick(View v) {
        updateChosenFilter(ImageFilter.GREYSCALE, R.id.greyscalefilter);
    }

    public void onSepiaClick(View v) {
        updateChosenFilter(ImageFilter.SEPIA, R.id.sepiafilter);
    }

    public void onBlackWhiteClick(View v) {
        updateChosenFilter(ImageFilter.BLACK_WHITE, R.id.blackwhitefilter);
    }

    public void onRedTintClick(View v) {
        updateChosenFilter(ImageFilter.RED, R.id.redfilter);
    }

    public void onGreenTintClick(View v) {
        updateChosenFilter(ImageFilter.GREEN, R.id.greenfilter);
    }

    public void onBlueTintClick(View v) {
        updateChosenFilter(ImageFilter.BLUE, R.id.bluefilter);
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
        MaterialCheckBox check = (MaterialCheckBox)v;
        filtersApplied.flipHorizontal = check.isChecked();
        refreshImage();
    }

    public void onFlipVerticalClick(View v) {
        MaterialCheckBox check = (MaterialCheckBox)v;
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