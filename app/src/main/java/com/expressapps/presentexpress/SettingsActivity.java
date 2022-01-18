package com.expressapps.presentexpress;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.expressapps.presentexpress.databinding.ActivitySettingsBinding;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_GradientStatusBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(R.string.slideshow_settings);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        MaterialCheckBox loop_check = findViewById(R.id.loop_check);
        loop_check.setChecked(MainActivity.loop);
        MaterialCheckBox timing_check = findViewById(R.id.timing_check);
        timing_check.setChecked(MainActivity.timings);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void onBackColorClick(View v) {
        Intent intent = new Intent(this, ColorActivity.class);
        startActivity(intent);
    }

    public void onSetTimingsClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        builder.setTitle(R.string.set_timings);

        LinearLayout container = new LinearLayout(SettingsActivity.this);
        container.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(toDp(20), 0, toDp(20), 0);

        final EditText input = new EditText(SettingsActivity.this);
        input.setLayoutParams(lp);
        input.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
        input.setText("2.0");
        container.addView(input);
        builder.setView(container);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    double timing = MainActivity.convertToDouble(input.getText().toString());
                    if (timing > 10.0) {
                        timing = 10.0;
                        newMessage(getString(R.string.timings_max), Toast.LENGTH_SHORT);
                    } else if (timing < 0.5) {
                        timing = 0.5;
                        newMessage(getString(R.string.timings_min), Toast.LENGTH_SHORT);
                    }
                    for (HashMap<String, Object> i : MainActivity.AllSlides) {
                        i.put("timing", timing);
                    }

                } catch (Exception ignored) {
                    newMessage(getString(R.string.timings_error), Toast.LENGTH_LONG);
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(false);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setLetterSpacing(0);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setAllCaps(false);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setLetterSpacing(0);
        input.requestFocus();
        input.selectAll();
    }

    private void newMessage(String s, int length) {
        Toast toast = Toast.makeText(getApplicationContext(), s, length);
        toast.show();
    }

    private int toDp(int i) {
        final float scale = this.getResources().getDisplayMetrics().density;
        return (int) (i * scale + 0.5f);
    }

    public void onClearSlidesClick(View v) {
        if (MainActivity.AllSlides.size() > 0) {
            DialogInterface.OnClickListener dialogClickListener2 = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            MainActivity.clear_slides = true;
                            finishActivity(6);
                            finish();
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            break;
                    }
                }
            };
            AlertDialog.Builder builder2 = new AlertDialog.Builder(SettingsActivity.this);
            builder2.setMessage(R.string.sure_clear).setTitle(R.string.clear_all_slides)
                    .setPositiveButton(R.string.yes, dialogClickListener2).setNegativeButton(R.string.no, dialogClickListener2);

            AlertDialog dialog2 = builder2.create();
            dialog2.show();
            dialog2.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(false);
            dialog2.getButton(AlertDialog.BUTTON_POSITIVE).setLetterSpacing(0);
            dialog2.getButton(AlertDialog.BUTTON_NEGATIVE).setAllCaps(false);
            dialog2.getButton(AlertDialog.BUTTON_NEGATIVE).setLetterSpacing(0);

        } else {
            finishActivity(6);
            finish();
        }
    }

    public void onLoopClick(View v) {
        MainActivity.loop = ((MaterialCheckBox) v).isChecked();
    }

    public void onUseTimingsClick(View v) {
        MainActivity.timings = ((MaterialCheckBox) v).isChecked();
    }

    public void onAboutClick(View v) {
        Intent intent2 = new Intent(this, AboutActivity.class);
        startActivity(intent2);
    }

    public void onHelpClick(View v) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://express.johnjds.co.uk/present/help"));
        newEventLog("helpOpen", "Help button clicked");
        startActivity(browserIntent);
        finish();
    }

    private void newEventLog(String id, String type) {
        try {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, type);
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        } catch (Exception ignored) {}
    }
}