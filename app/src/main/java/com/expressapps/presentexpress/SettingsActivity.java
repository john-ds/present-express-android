package com.expressapps.presentexpress;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.expressapps.presentexpress.helper.Funcs;
import com.expressapps.presentexpress.helper.Slide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {
    private FirebaseAnalytics mFA;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        mFA = FirebaseAnalytics.getInstance(this);

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        MaterialCheckBox loop_check = findViewById(R.id.loop_check);
        loop_check.setChecked(MainActivity.slideshow.info.loop);
        MaterialCheckBox timing_check = findViewById(R.id.timing_check);
        timing_check.setChecked(MainActivity.slideshow.info.useTimings);

        updateThemeButtonIcon();

        MaterialButton background_button = findViewById(R.id.background_button);
        background_button.setIconTint(ColorStateList.valueOf(MainActivity.slideshow.info.getBackColour()));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.toolbar), (v, insets) -> {
            Insets in = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(in.left, 0, in.right, 0);
            return insets;
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.scroller), (v, insets) -> {
            Insets in = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(in.left, 0, in.right, in.bottom);
            return insets;
        });
    }

    private void updateThemeButtonIcon(int preference) {
        MaterialButton theme_button = findViewById(R.id.theme_button);
        switch (preference) {
            case AppCompatDelegate.MODE_NIGHT_NO:
                theme_button.setIcon(ContextCompat.getDrawable(getBaseContext(), R.drawable.icon_light_mode_24));
                break;
            case AppCompatDelegate.MODE_NIGHT_YES:
                theme_button.setIcon(ContextCompat.getDrawable(getBaseContext(), R.drawable.icon_dark_mode_24));
                break;
            default:
                theme_button.setIcon(ContextCompat.getDrawable(getBaseContext(), R.drawable.icon_design_24));
                break;
        }
    }

    private void updateThemeButtonIcon() {
        updateThemeButtonIcon(Funcs.getThemePreference(this));
    }

    public void onBackColorClick(View v) {
        new ColorPickerDialog.Builder(SettingsActivity.this)
                .setTitle(R.string.choose_colour)
                .setPreferenceName("MyColorPickerDialog")
                .setPositiveButton("OK", (ColorEnvelopeListener) (envelope, fromUser) -> {
                    ((GradientDrawable) Objects.requireNonNull(
                            ContextCompat.getDrawable(getBaseContext(), R.drawable.border))).setColor(envelope.getColor());
                    MainActivity.slideshow.info.setBackColour(envelope.getColor());

                    MaterialButton background_button = findViewById(R.id.background_button);
                    background_button.setIconTint(ColorStateList.valueOf(envelope.getColor()));
                })
                .setNegativeButton(R.string.cancel, (d, i) -> d.dismiss())
                .attachAlphaSlideBar(false)
                .show();
    }

    public void onSoundtrackClick(View view) {
        Intent intent2 = new Intent(this, SoundtrackActivity.class);
        startActivity(intent2);
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
        }, "2.0", InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
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

    public void onThemeClick(View view) {
        int[] themes = new int[]{R.string.theme_light, R.string.theme_dark, R.string.theme_follow};

        int selectedTheme = 2;
        switch (Funcs.getThemePreference(this)) {
            case AppCompatDelegate.MODE_NIGHT_NO:
                selectedTheme = 0;
                break;
            case AppCompatDelegate.MODE_NIGHT_YES:
                selectedTheme = 1;
                break;
            default:
                break;
        }

        Funcs.showRadioDialog(this, themes, R.string.interface_theme, (d, which) -> {
            switch (which) {
                case 0:
                    Funcs.setThemePreference(this, AppCompatDelegate.MODE_NIGHT_NO);
                    break;
                case 1:
                    Funcs.setThemePreference(this, AppCompatDelegate.MODE_NIGHT_YES);
                    break;
                case 2:
                    Funcs.setThemePreference(this, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    break;
            }
            updateThemeButtonIcon();
        }, selectedTheme);
    }

    public void onAboutClick(View v) {
        Intent intent2 = new Intent(this, AboutActivity.class);
        startActivity(intent2);
    }

    public void onHelpClick(View v) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Funcs.getHelpGuideLink());
        Funcs.newEventLog(mFA, "helpOpen", "Help button clicked");
        startActivity(browserIntent);
        finish();
    }
}