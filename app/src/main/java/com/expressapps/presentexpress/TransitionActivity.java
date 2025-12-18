package com.expressapps.presentexpress;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RadioGroup;
import android.widget.ScrollView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.expressapps.presentexpress.helper.Funcs;
import com.expressapps.presentexpress.helper.Transition;
import com.expressapps.presentexpress.helper.TransitionCategory;
import com.expressapps.presentexpress.helper.TransitionDirection;
import com.expressapps.presentexpress.helper.TransitionType;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.Objects;

import javax.annotation.Nullable;

public class TransitionActivity extends AppCompatActivity {
    private int imageIdx = 0;
    private TransitionCategory selectedTransitionCategory = TransitionCategory.NONE;
    private static final BiMap<Integer, TransitionCategory> transitionMap = HashBiMap.create();
    private static final BiMap<Integer, TransitionDirection> directionMap = HashBiMap.create();

    static {
        transitionMap.put(R.id.fadetrans, TransitionCategory.FADE);
        transitionMap.put(R.id.pushtrans, TransitionCategory.PUSH);
        transitionMap.put(R.id.wipetrans, TransitionCategory.WIPE);
        transitionMap.put(R.id.uncovertrans, TransitionCategory.UNCOVER);
        transitionMap.put(R.id.covertrans, TransitionCategory.COVER);

        directionMap.put(R.id.from_left, TransitionDirection.LEFT);
        directionMap.put(R.id.from_right, TransitionDirection.RIGHT);
        directionMap.put(R.id.from_top, TransitionDirection.TOP);
        directionMap.put(R.id.from_bottom, TransitionDirection.BOTTOM);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_transition);

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        Intent intent = getIntent();
        imageIdx = intent.getIntExtra("idx", 0);
        Transition transition = MainActivity.slideshow.slides.get(imageIdx).transition;

        selectedTransitionCategory = TransitionCategory.fromValue(transition.getType().getValue() / 10);
        if (selectedTransitionCategory != TransitionCategory.NONE) {
            @Nullable Integer id = transitionMap.inverse().get(selectedTransitionCategory);
            if (id != null) ((MaterialButton) findViewById(id)).setChecked(true);
        }
        loadEffectOptions(transition.getType());

        setEditFieldNumber(R.id.duration_txt, MainActivity.slideshow.slides.get(imageIdx).getTiming());
        setEditFieldNumber(R.id.transition_duration_txt, transition.getDuration());

        ScrollView scroller = findViewById(R.id.scroller);
        ExtendedFloatingActionButton fab = findViewById(R.id.fab_done);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.toolbar), (v, insets) -> {
            Insets in = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(in.left, 0, in.right, 0);
            return insets;
        });

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
                Funcs.showDialog(TransitionActivity.this, R.string.editor_apply_changes, R.string.closing_transition_editor, (d, b) -> {
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
        applyTransition(imageIdx);

        try {
            MainActivity.slideshow.slides.get(imageIdx).setTiming(getEditFieldNumber(R.id.duration_txt));
        } catch (Exception ignored) {
        }
        finish();
    }

    private void applyTransition(int idx) {
        Transition transition = MainActivity.slideshow.slides.get(idx).transition;

        if (selectedTransitionCategory == TransitionCategory.NONE) {
            transition.setType(TransitionType.NONE);
        } else if (selectedTransitionCategory == TransitionCategory.FADE) {
            RadioGroup fadeOptions = findViewById(R.id.fade_options);
            if (fadeOptions.getCheckedRadioButtonId() == R.id.through_black)
                transition.setType(TransitionType.FADE_THROUGH_BLACK);
            else
                transition.setType(TransitionType.FADE);
        } else {
            RadioGroup movementOptions = findViewById(R.id.movement_options);
            int selectedDirection = movementOptions.getCheckedRadioButtonId();
            if (selectedDirection == -1) selectedDirection = R.id.from_left;

            transition.setType(TransitionType.fromValue(selectedTransitionCategory.getValue() * 10 +
                    Objects.requireNonNull(directionMap.get(selectedDirection)).getValue()));
        }

        try {
            transition.setDuration(getEditFieldNumber(R.id.transition_duration_txt));
        } catch (Exception ignored) {
        }
    }

    public void onApplyAllClick(View v) {
        for (int i = 0; i < MainActivity.slideshow.slides.size(); i++) {
            applyTransition(i);

            try {
                MainActivity.slideshow.slides.get(i).setTiming(getEditFieldNumber(R.id.duration_txt));
            } catch (Exception ignored) {
            }
        }
        finish();
    }

    private void resetTransitionButtons(@IdRes int... except) {
        for (int buttonId : transitionMap.keySet()) {
            if (except.length == 0 || buttonId != except[0]) {
                MaterialButton button = findViewById(buttonId);
                button.setChecked(false);
            }
        }
    }

    private void updateChosenTransition(TransitionCategory transition, @IdRes int id) {
        resetTransitionButtons(id);

        if (selectedTransitionCategory == transition) {
            selectedTransitionCategory = TransitionCategory.NONE;
        } else {
            selectedTransitionCategory = transition;
        }
        loadEffectOptions(selectedTransitionCategory);
    }

    public void onTransitionButtonClick(View v) {
        int id = v.getId();
        TransitionCategory transition = transitionMap.get(id);
        if (transition != null) {
            updateChosenTransition(transition, id);
        }
    }

    private void loadEffectOptions(TransitionCategory category) {
        loadEffectOptions(TransitionType.fromValue(category.getValue() * 10));
    }

    private void loadEffectOptions(TransitionType type) {
        RadioGroup fadeOptions = findViewById(R.id.fade_options);
        RadioGroup movementOptions = findViewById(R.id.movement_options);

        if (type == TransitionType.NONE) {
            findViewById(R.id.effect_spinner_lbl).setVisibility(View.GONE);
            fadeOptions.setVisibility(View.GONE);
            movementOptions.setVisibility(View.GONE);
            findViewById(R.id.transition_duration_txt).setVisibility(View.GONE);

        } else {
            findViewById(R.id.effect_spinner_lbl).setVisibility(View.VISIBLE);
            findViewById(R.id.transition_duration_txt).setVisibility(View.VISIBLE);

            if (type == TransitionType.FADE || type == TransitionType.FADE_THROUGH_BLACK) {
                movementOptions.setVisibility(View.GONE);
                fadeOptions.setVisibility(View.VISIBLE);
                fadeOptions.check(type == TransitionType.FADE_THROUGH_BLACK ? R.id.through_black : R.id.fade_smoothly);
            } else {
                Integer selectedDirection = directionMap.inverse().get(type.getDirection());
                fadeOptions.setVisibility(View.GONE);
                movementOptions.setVisibility(View.VISIBLE);
                movementOptions.check(selectedDirection == null ? R.id.from_left : selectedDirection);
            }
        }
    }

    private double getEditFieldNumber(@IdRes int id) {
        TextInputLayout layout = findViewById(id);
        return Funcs.convertToDouble(Objects.requireNonNull(layout.getEditText()).getText().toString());
    }

    private void setEditFieldNumber(@IdRes int id, double value) {
        TextInputLayout layout = findViewById(id);
        Objects.requireNonNull(layout.getEditText()).setText(String.valueOf(value));
    }
}