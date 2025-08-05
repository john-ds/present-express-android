package com.expressapps.presentexpress;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.expressapps.presentexpress.helper.Funcs;
import com.expressapps.presentexpress.helper.Transition;
import com.expressapps.presentexpress.helper.TransitionCategory;
import com.expressapps.presentexpress.helper.TransitionType;

import java.util.Objects;

public class TransitionActivity extends AppCompatActivity {
    private int imageIdx = 0;

    private final AdapterView.OnItemSelectedListener transitionSpinnerListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            loadEffectOptions(TransitionCategory.fromValue(position));
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_GradientStatusBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transition);
        setSupportActionBar(findViewById(R.id.toolbar));
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.set_transition);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Intent intent = getIntent();
        imageIdx = intent.getIntExtra("idx", 0);
        Transition transition = MainActivity.slideshow.slides.get(imageIdx).transition;

        Spinner transitionSpinner = findViewById(R.id.transition_spinner);
        transitionSpinner.setAdapter(new ArrayAdapter<>(TransitionActivity.this,
                R.layout.support_simple_spinner_dropdown_item, new String[]{
                getString(R.string.trans_none),
                getString(R.string.trans_fade),
                getString(R.string.trans_push),
                getString(R.string.trans_wipe),
                getString(R.string.trans_uncover),
                getString(R.string.trans_cover)
        }
        ));
        transitionSpinner.setSelection(transition.getCategory().getValue(), true);
        transitionSpinner.setOnItemSelectedListener(transitionSpinnerListener);

        loadEffectOptions(transition.getType());

        ((TextView) findViewById(R.id.duration_txt)).setText(String.valueOf(MainActivity.slideshow.slides.get(imageIdx).getTiming()));
        ((TextView) findViewById(R.id.transition_duration_txt)).setText(String.valueOf(transition.getDuration()));
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
        Funcs.showDialog(TransitionActivity.this, R.string.editor_apply_changes, R.string.closing_transition_editor, (d, b) -> {
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
        applyTransition(imageIdx);

        try {
            MainActivity.slideshow.slides.get(imageIdx).setTiming(
                    Funcs.convertToDouble(((EditText) findViewById(R.id.duration_txt)).getText().toString()));
        } catch (Exception ignored) {
        }
    }

    private void applyTransition(int idx) {
        Transition transition = MainActivity.slideshow.slides.get(idx).transition;
        int selectedIdx = ((Spinner) findViewById(R.id.transition_spinner)).getSelectedItemPosition();

        switch (TransitionCategory.fromValue(selectedIdx)) {
            case FADE:
            case PUSH:
            case WIPE:
            case UNCOVER:
            case COVER:
                transition.setType((selectedIdx * 10) + ((Spinner) findViewById(R.id.effect_spinner)).getSelectedItemPosition());
                break;
            case NONE:
            default:
                transition.setType(TransitionType.NONE);
                break;
        }

        try {
            transition.setDuration(
                    Funcs.convertToDouble(((EditText) findViewById(R.id.transition_duration_txt)).getText().toString()));
        } catch (Exception ignored) {
        }
    }

    public void onApplyAllClick(View v) {
        for (int i = 0; i < MainActivity.slideshow.slides.size(); i++) {
            applyTransition(i);

            try {
                MainActivity.slideshow.slides.get(i).setTiming(
                        Funcs.convertToDouble(((EditText) findViewById(R.id.duration_txt)).getText().toString()));
            } catch (Exception ignored) {
            }
        }
        finish();
    }

    private void loadEffectOptions(TransitionCategory category) {
        loadEffectOptions(TransitionType.fromValue(category.getValue() * 10));
    }

    private void loadEffectOptions(TransitionType type) {
        String[] options;
        if (type == TransitionType.NONE) {
            findViewById(R.id.effect_spinner_lbl).setVisibility(View.GONE);
            findViewById(R.id.effect_spinner).setVisibility(View.GONE);
            findViewById(R.id.duration_panel).setVisibility(View.GONE);
            return;

        } else {
            findViewById(R.id.effect_spinner_lbl).setVisibility(View.VISIBLE);
            findViewById(R.id.effect_spinner).setVisibility(View.VISIBLE);
            findViewById(R.id.duration_panel).setVisibility(View.VISIBLE);

            if (type == TransitionType.FADE || type == TransitionType.FADE_THROUGH_BLACK) {
                options = new String[]{
                        getString(R.string.trans_smoothly),
                        getString(R.string.trans_through_black)
                };
            } else {
                options = new String[]{
                        getString(R.string.trans_from_left),
                        getString(R.string.trans_from_right),
                        getString(R.string.trans_from_top),
                        getString(R.string.trans_from_bottom)
                };
            }
        }

        Spinner effectSpinner = findViewById(R.id.effect_spinner);
        effectSpinner.setAdapter(new ArrayAdapter<>(TransitionActivity.this,
                R.layout.support_simple_spinner_dropdown_item, options));
        effectSpinner.setSelection(type.getDirection().getValue(), true);
    }
}