package com.expressapps.presentexpress;

import androidx.annotation.NonNull;
import android.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.slider.Slider;

import java.util.HashMap;

public class EditorActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private Bitmap originalImage;
    private HashMap<String, Object> filtersApplied = new HashMap<>();
    private int imageIdx = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_GradientStatusBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(R.string.photo_editor);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Slider brightnessSlider = findViewById(R.id.brightness);
        brightnessSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                if (fromUser) {
                    filtersApplied.put("brightness", transformRange(value, -100f, 100f, -0.5f, 0.5f));
                    refreshImage();
                }
            }
        });

        Slider contrastSlider = findViewById(R.id.contrast);
        contrastSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                if (fromUser) {
                    if (value < 0f) {
                        filtersApplied.put("contrast", transformRange(value, -100f, 0f, 0.5f, 1f));
                    } else {
                        filtersApplied.put("contrast", transformRange(value, 0f, 100f, 1f, 2f));
                    }
                    refreshImage();
                }
            }
        });

        Intent intent = getIntent();
        imageIdx = intent.getIntExtra("idx", 0);
        if (MainActivity.AllSlides.get(imageIdx).containsKey("original")) {
            originalImage = (Bitmap) MainActivity.AllSlides.get(imageIdx).get("original");
        } else {
            originalImage = (Bitmap) MainActivity.AllSlides.get(imageIdx).get("bmp");
        }

        ImageView previewImage = findViewById(R.id.previewimg);
        previewImage.setImageBitmap(originalImage);

        if (intent.getStringExtra("filter") == null) {
            filtersApplied.put("filter", "");
        } else {
            filtersApplied.put("filter", intent.getStringExtra("filter"));
        }

        filtersApplied.put("brightness", intent.getFloatExtra("brightness", 0f));
        filtersApplied.put("contrast", intent.getFloatExtra("contrast", 1f));
        filtersApplied.put("rotation", intent.getIntExtra("rotation", 0));
        filtersApplied.put("fliph", intent.getBooleanExtra("fliph", false));
        filtersApplied.put("flipv", intent.getBooleanExtra("flipv", false));

        brightnessSlider.setValue((float)Math.round(transformRange((float)filtersApplied.get("brightness"),
                -0.5f, 0.5f, -100f, 100f)));

        if ((float)filtersApplied.get("contrast") < 1f) {
            contrastSlider.setValue((float)Math.round(transformRange((float)filtersApplied.get("contrast"),
                    0.5f, 1f, -100f, 0f)));
        } else {
            contrastSlider.setValue((float)Math.round(transformRange((float)filtersApplied.get("contrast"),
                    1f, 2f, 0f, 100f)));
        }

        MaterialCheckBox checkh = findViewById(R.id.fliphorizontal);
        checkh.setChecked((boolean)filtersApplied.get("fliph"));
        MaterialCheckBox checkv = findViewById(R.id.flipvertical);
        checkv.setChecked((boolean)filtersApplied.get("flipv"));
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

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditorActivity.this);
        builder.setTitle(R.string.close_editor);
        builder.setMessage(R.string.editor_apply_changes);

        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                applyChanges();
                finish();
            }
        });
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(false);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setLetterSpacing(0);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setAllCaps(false);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setLetterSpacing(0);
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
        MainActivity.AllSlides.get(imageIdx).put("filters", filtersApplied);
    }

    private void refreshImage() {
        LinearLayout previewLayout = findViewById(R.id.previewlayout);
        ImageView previewImage = findViewById(R.id.previewimg);

        if (previewLayout.getVisibility() == View.VISIBLE) {
            previewImage.setImageBitmap(MainActivity.applyFilters(originalImage, filtersApplied));
        }
    }

    public static float transformRange(float value, float r1min, float r1max, float r2min, float r2max) {
        float scale = (r2max - r2min) / (r1max - r1min);
        return (value - r1min) * scale + r2min;
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
        ((MaterialButton)findViewById(R.id.greyscalefilter)).setBackgroundColor(Color.WHITE);
        ((MaterialButton)findViewById(R.id.sepiafilter)).setBackgroundColor(Color.WHITE);
        ((MaterialButton)findViewById(R.id.blackwhitefilter)).setBackgroundColor(Color.WHITE);
        ((MaterialButton)findViewById(R.id.redfilter)).setBackgroundColor(Color.WHITE);
        ((MaterialButton)findViewById(R.id.greenfilter)).setBackgroundColor(Color.WHITE);
        ((MaterialButton)findViewById(R.id.bluefilter)).setBackgroundColor(Color.WHITE);
    }

    public void onGreyscaleClick(View v) {
        resetFilterButtons();
        if (filtersApplied.get("filter") == "Greyscale") {
            filtersApplied.put("filter", "");
        } else {
            ((MaterialButton) findViewById(R.id.greyscalefilter)).setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
            filtersApplied.put("filter", "Greyscale");
        }
        refreshImage();
    }

    public void onSepiaClick(View v) {
        resetFilterButtons();
        if (filtersApplied.get("filter") == "Sepia") {
            filtersApplied.put("filter", "");
        } else {
            ((MaterialButton) findViewById(R.id.sepiafilter)).setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
            filtersApplied.put("filter", "Sepia");
        }
        refreshImage();
    }

    public void onBlackWhiteClick(View v) {
        resetFilterButtons();
        if (filtersApplied.get("filter") == "BlackWhite") {
            filtersApplied.put("filter", "");
        } else {
            ((MaterialButton) findViewById(R.id.blackwhitefilter)).setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
            filtersApplied.put("filter", "BlackWhite");
        }
        refreshImage();
    }

    public void onRedTintClick(View v) {
        resetFilterButtons();
        if (filtersApplied.get("filter") == "Red") {
            filtersApplied.put("filter", "");
        } else {
            ((MaterialButton) findViewById(R.id.redfilter)).setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
            filtersApplied.put("filter", "Red");
        }
        refreshImage();
    }

    public void onGreenTintClick(View v) {
        resetFilterButtons();
        if (filtersApplied.get("filter") == "Green") {
            filtersApplied.put("filter", "");
        } else {
            ((MaterialButton) findViewById(R.id.greenfilter)).setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
            filtersApplied.put("filter", "Green");
        }
        refreshImage();
    }

    public void onBlueTintClick(View v) {
        resetFilterButtons();
        if (filtersApplied.get("filter") == "Blue") {
            filtersApplied.put("filter", "");
        } else {
            ((MaterialButton) findViewById(R.id.bluefilter)).setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
            filtersApplied.put("filter", "Blue");
        }
        refreshImage();
    }

    public void onRotateRightClick(View v) {
        if ((int) filtersApplied.get("rotation") >= 270) {
            filtersApplied.put("rotation", 0);
        } else {
            filtersApplied.put("rotation", (Integer) filtersApplied.get("rotation") + 90);
        }
        refreshImage();
    }

    public void onRotateLeftClick(View v) {
        if ((int) filtersApplied.get("rotation") <= 0) {
            filtersApplied.put("rotation", 270);
        } else {
            filtersApplied.put("rotation", (Integer) filtersApplied.get("rotation") - 90);
        }
        refreshImage();
    }

    public void onFlipHorizontalClick(View v) {
        MaterialCheckBox check = (MaterialCheckBox)v;
        filtersApplied.put("fliph", check.isChecked());
        refreshImage();
    }

    public void onFlipVerticalClick(View v) {
        MaterialCheckBox check = (MaterialCheckBox)v;
        filtersApplied.put("flipv", check.isChecked());
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

        filtersApplied.put("filter", "");
        filtersApplied.put("brightness", 0f);
        filtersApplied.put("contrast", 1f);
        filtersApplied.put("rotation", 0);
        filtersApplied.put("fliph", false);
        filtersApplied.put("flipv", false);
    }
}