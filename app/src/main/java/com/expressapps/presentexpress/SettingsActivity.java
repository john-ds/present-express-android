package com.expressapps.presentexpress;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;

import com.expressapps.presentexpress.helper.Funcs;
import com.expressapps.presentexpress.helper.Slide;
import com.google.android.material.checkbox.MaterialCheckBox;

import androidx.appcompat.app.AppCompatActivity;

import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {
    private FirebaseAnalytics mFA;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_GradientStatusBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setSupportActionBar(findViewById(R.id.toolbar));
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.slideshow_settings);
        mFA = FirebaseAnalytics.getInstance(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        MaterialCheckBox loop_check = findViewById(R.id.loop_check);
        loop_check.setChecked(MainActivity.slideshow.info.loop);
        MaterialCheckBox timing_check = findViewById(R.id.timing_check);
        timing_check.setChecked(MainActivity.slideshow.info.useTimings);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void onBackColorClick(View v) {
        new ColorPickerDialog.Builder(SettingsActivity.this)
            .setTitle(R.string.choose_colour)
            .setPreferenceName("MyColorPickerDialog")
            .setPositiveButton("OK", (ColorEnvelopeListener) (envelope, fromUser) -> {
                ((GradientDrawable) Objects.requireNonNull(
                    ContextCompat.getDrawable(getBaseContext(), R.drawable.border))).setColor(envelope.getColor());
                MainActivity.slideshow.info.setBackColour(envelope.getColor());
            })
            .setNegativeButton(R.string.cancel, (d, i) -> d.dismiss())
            .attachAlphaSlideBar(false)
            .show();
    }

    public void onSetTimingsClick(View v) {
        final EditText input = new EditText(SettingsActivity.this);
        Funcs.showInputDialog(SettingsActivity.this, input, R.string.set_timings, (d, b) -> {
            switch (b) {
                case DialogInterface.BUTTON_POSITIVE:
                    try {
                        double timing = Funcs.convertToDouble(input.getText().toString());
                        if (timing > 10.0) {
                            timing = 10.0;
                            Funcs.newMessage(getApplicationContext(), R.string.timings_max, Toast.LENGTH_SHORT);
                        } else if (timing < 0.5) {
                            timing = 0.5;
                            Funcs.newMessage(getApplicationContext(), R.string.timings_min, Toast.LENGTH_SHORT);
                        }
                        for (Slide i : MainActivity.slideshow.slides)
                            i.setTiming(timing);

                    } catch (Exception ignored) {
                        Funcs.newMessage(getApplicationContext(), R.string.timings_error, Toast.LENGTH_LONG);
                    }
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    d.cancel();
                    break;
            }
        }, "2.0", InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER,
            Funcs.toDp(20));
    }

    public void onClearSlidesClick(View v) {
        if (!MainActivity.slideshow.slides.isEmpty()) {
            Funcs.showDialog(SettingsActivity.this, R.string.sure_clear, R.string.clear_all_slides, (d, b) -> {
                switch (b) {
                    case DialogInterface.BUTTON_POSITIVE:
                        setResult(Activity.RESULT_OK);
                        finish();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        d.cancel();
                        break;
                }
            });

        } else {
            setResult(Activity.RESULT_OK);
            finish();
        }
    }

    public void onLoopClick(View v) {
        MainActivity.slideshow.info.loop = ((MaterialCheckBox) v).isChecked();
    }

    public void onUseTimingsClick(View v) {
        MainActivity.slideshow.info.useTimings = ((MaterialCheckBox) v).isChecked();
    }

    public void onAboutClick(View v) {
        Intent intent2 = new Intent(this, AboutActivity.class);
        startActivity(intent2);
    }

    public void onHelpClick(View v) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://express.johnjds.co.uk/present/help"));
        Funcs.newEventLog(mFA, "helpOpen", "Help button clicked");
        startActivity(browserIntent);
        finish();
    }
}