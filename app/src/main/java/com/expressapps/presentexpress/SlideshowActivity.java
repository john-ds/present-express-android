package com.expressapps.presentexpress;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.expressapps.presentexpress.helper.Funcs;
import com.expressapps.presentexpress.helper.Slide;
import com.expressapps.presentexpress.helper.Transition;
import com.expressapps.presentexpress.helper.TransitionCategory;
import com.expressapps.presentexpress.helper.TransitionDirection;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SlideshowActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private GestureDetector gestureDetector;
    View.OnTouchListener gestureListener;

    /**
     * Some older devices need a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private FrameLayout mContentView;

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };

    private final Runnable mShowPart2Runnable = () -> {
        // Delayed display of UI elements
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.show();
    };

    private boolean mVisible;
    private final Runnable mHideRunnable = this::hide;

    Timer timer = new Timer();
    int currentSlide = -1;
    private boolean transitionRunning = false;
    private boolean loopOn;
    private boolean useTimings;
    private List<Slide> slides;

    private ImageView photoImg;
    private ImageView photoImgOther;
    private FrameLayout photoGrid;
    private FrameLayout photoGridOther;
    private ValueAnimator valAnimator;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        mVisible = true;
        mContentView = findViewById(R.id.back);

        photoGrid = findViewById(R.id.photo_grid_1);
        photoGridOther = findViewById(R.id.photo_grid_2);
        photoImg = findViewById(R.id.photo_img_1);
        photoImgOther = findViewById(R.id.photo_img_2);

        photoGrid.setBackgroundColor(MainActivity.slideshow.info.getBackColour());
        photoGridOther.setBackgroundColor(MainActivity.slideshow.info.getBackColour());

        loopOn = MainActivity.slideshow.info.loop;
        useTimings = MainActivity.slideshow.info.useTimings;
        slides = MainActivity.slideshow.slides;

        gestureDetector = new GestureDetector(this, new MyGestureDetector());
        gestureListener = (v, event) -> gestureDetector.onTouchEvent(event);

        mContentView.setOnClickListener(SlideshowActivity.this);
        mContentView.setOnTouchListener(gestureListener);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide();
        mHideHandler.postDelayed(this::loadNext, 200);
    }

    private void loadNext() {
        stopTimer();
        currentSlide++;

        if (transitionRunning) {
            stopTransition();

            if (currentSlide >= slides.size() && !loopOn) {
                endSlideshow();
            } else {
                photoImg.setImageBitmap(slides.get(currentSlide).bitmap);
                photoGrid.setAlpha(1);
                transitionFinished();
            }
            return;
        }

        if (currentSlide >= slides.size()) {
            if (loopOn) {
                loadStart();
            } else {
                endSlideshow();
            }
        } else {
            ImageView tempImg = photoImg;
            photoImg = photoImgOther;
            photoImgOther = tempImg;

            FrameLayout tempGrid = photoGrid;
            photoGrid = photoGridOther;
            photoGridOther = tempGrid;

            photoImg.setImageBitmap(slides.get(currentSlide).bitmap);
            photoGrid.setAlpha(1);
            loadTransition();
        }
    }

    private void loadPrevious() {
        if (currentSlide > 0) {
            currentSlide -= 2;
            loadNext();

        } else {
            findViewById(R.id.photo_grid_1).setAlpha(0);
            findViewById(R.id.photo_grid_2).setAlpha(0);
            ((ImageView) findViewById(R.id.photo_img_1)).setImageBitmap(null);
            ((ImageView) findViewById(R.id.photo_img_2)).setImageBitmap(null);

            transitionRunning = false;
            loadStart();
        }
    }

    private void loadStart() {
        currentSlide = -1;
        loadNext();
    }

    @SuppressLint({"CutPasteId", "RtlHardcoded"})
    private void loadTransition() {
        Transition trans = slides.get(currentSlide).transition;
        stopTransition();

        if (slides.get(currentSlide).transition.getCategory() == TransitionCategory.UNCOVER) {
            photoGrid.setTranslationZ(1);
            photoGridOther.setTranslationZ(2);
        }

        switch (trans.getType()) {
            case FADE:
                photoGrid.setAlpha(0f);
                photoGrid.animate().alpha(1).setDuration(trans.getDurationMs())
                        .withEndAction(this::transitionFinished);
                break;

            case FADE_THROUGH_BLACK:
                photoGridOther.setAlpha(1f);
                photoGridOther.animate().alpha(0).setDuration(trans.getDurationMs() / 2);

                photoGrid.setAlpha(0f);
                photoGrid.animate().alpha(1).setStartDelay(trans.getDurationMs() / 2)
                        .setDuration(trans.getDurationMs() / 2).withEndAction(this::transitionFinished);
                break;

            case PUSH_LEFT:
            case PUSH_RIGHT:
                photoGridOther.setTranslationX(0);
                photoGridOther.animate()
                        .translationX(trans.getDirection() == TransitionDirection.LEFT ?
                                photoGrid.getWidth() : -photoGrid.getWidth())
                        .setDuration(trans.getDurationMs());

                photoGrid.setTranslationX(trans.getDirection() == TransitionDirection.LEFT ?
                        -photoGrid.getWidth() : photoGrid.getWidth());
                photoGrid.animate().translationX(0).setDuration(trans.getDurationMs())
                        .withEndAction(this::transitionFinished);
                break;

            case PUSH_TOP:
            case PUSH_BOTTOM:
                photoGridOther.setTranslationY(0);
                photoGridOther.animate()
                        .translationY(trans.getDirection() == TransitionDirection.TOP ?
                                photoGrid.getHeight() : -photoGrid.getHeight())
                        .setDuration(trans.getDurationMs());

                photoGrid.setTranslationY(trans.getDirection() == TransitionDirection.TOP ?
                        -photoGrid.getHeight() : photoGrid.getHeight());
                photoGrid.animate().translationY(0).setDuration(trans.getDurationMs())
                        .withEndAction(this::transitionFinished);
                break;

            case WIPE_LEFT:
                photoImg.getLayoutParams().width = findViewById(R.id.back).getWidth();

                valAnimator = ValueAnimator.ofInt(0, findViewById(R.id.back).getWidth());
                valAnimator.setDuration(trans.getDurationMs());
                valAnimator.addUpdateListener(animation -> {
                    photoGrid.getLayoutParams().width = (int) animation.getAnimatedValue();
                    photoGrid.requestLayout();
                });
                valAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        transitionFinished();
                    }
                });
                valAnimator.start();
                break;

            case WIPE_RIGHT:
                photoGrid.setLayoutParams(new FrameLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.RIGHT));
                photoImg.setLayoutParams(new FrameLayout.LayoutParams(
                        findViewById(R.id.back).getWidth(), ViewGroup.LayoutParams.MATCH_PARENT, Gravity.LEFT));

                valAnimator = ValueAnimator.ofInt(0, findViewById(R.id.back).getWidth());
                valAnimator.setDuration(trans.getDurationMs());
                valAnimator.addUpdateListener(animation -> {
                    photoGrid.getLayoutParams().width = (int) animation.getAnimatedValue();
                    photoImg.setTranslationX(-findViewById(R.id.back).getWidth() + (int) animation.getAnimatedValue());
                    photoGrid.requestLayout();
                });
                valAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        transitionFinished();
                    }
                });
                valAnimator.start();
                break;

            case WIPE_TOP:
                photoImg.getLayoutParams().height = findViewById(R.id.back).getHeight();

                valAnimator = ValueAnimator.ofInt(0, findViewById(R.id.back).getHeight());
                valAnimator.setDuration(trans.getDurationMs());
                valAnimator.addUpdateListener(animation -> {
                    photoGrid.getLayoutParams().height = (int) animation.getAnimatedValue();
                    photoGrid.requestLayout();
                });
                valAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        transitionFinished();
                    }
                });
                valAnimator.start();
                break;

            case WIPE_BOTTOM:
                photoGrid.setLayoutParams(new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 0, Gravity.BOTTOM));
                photoImg.setLayoutParams(new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, findViewById(R.id.back).getHeight(), Gravity.TOP));

                valAnimator = ValueAnimator.ofInt(0, findViewById(R.id.back).getHeight());
                valAnimator.setDuration(trans.getDurationMs());
                valAnimator.addUpdateListener(animation -> {
                    photoGrid.getLayoutParams().height = (int) animation.getAnimatedValue();
                    photoImg.setTranslationY(-findViewById(R.id.back).getHeight() + (int) animation.getAnimatedValue());
                    photoGrid.requestLayout();
                });
                valAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        transitionFinished();
                    }
                });
                valAnimator.start();
                break;

            case UNCOVER_LEFT:
            case UNCOVER_RIGHT:
                photoGridOther.setTranslationX(0);
                photoGridOther.animate()
                        .translationX(trans.getDirection() == TransitionDirection.LEFT ?
                                findViewById(R.id.back).getWidth() : -findViewById(R.id.back).getWidth())
                        .setDuration(trans.getDurationMs()).withEndAction(this::transitionFinished);
                break;

            case UNCOVER_TOP:
            case UNCOVER_BOTTOM:
                photoGridOther.setTranslationY(0);
                photoGridOther.animate()
                        .translationY(trans.getDirection() == TransitionDirection.TOP ?
                                findViewById(R.id.back).getHeight() : -findViewById(R.id.back).getHeight())
                        .setDuration(trans.getDurationMs()).withEndAction(this::transitionFinished);
                break;

            case COVER_LEFT:
            case COVER_RIGHT:
                photoGrid.setTranslationX(trans.getDirection() == TransitionDirection.LEFT ?
                        -findViewById(R.id.back).getWidth() : findViewById(R.id.back).getWidth());
                photoGrid.animate().translationX(0).setDuration(trans.getDurationMs())
                        .withEndAction(this::transitionFinished);
                break;

            case COVER_TOP:
            case COVER_BOTTOM:
                photoGrid.setTranslationY(trans.getDirection() == TransitionDirection.TOP ?
                        -findViewById(R.id.back).getHeight() : findViewById(R.id.back).getHeight());
                photoGrid.animate().translationY(0).setDuration(trans.getDurationMs())
                        .withEndAction(this::transitionFinished);
                break;

            case NONE:
                transitionFinished();
                return;
        }
        transitionRunning = true;
    }

    private void transitionFinished() {
        transitionRunning = false;
        stopTransition();

        if (useTimings) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    SlideshowActivity.this.runOnUiThread(() -> loadNext());
                }
            }, Math.round(slides.get(currentSlide).getTiming() * 1000));
        }
    }

    private void stopTransition() {
        photoGrid.animate().cancel();
        photoGridOther.animate().cancel();
        if (valAnimator != null) {
            valAnimator.removeAllListeners();
            valAnimator.cancel();
        }

        // restore defaults
        photoGrid.setTranslationZ(2);
        photoGridOther.setTranslationZ(1);

        photoGrid.setTranslationX(0);
        photoGridOther.setTranslationX(0);
        photoGrid.setTranslationY(0);
        photoGridOther.setTranslationY(0);

        photoImg.setTranslationX(0);
        photoImgOther.setTranslationX(0);
        photoImg.setTranslationY(0);
        photoImgOther.setTranslationY(0);

        photoImg.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));
        photoImgOther.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));

        photoGrid.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        photoGridOther.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void endSlideshow() {
        Funcs.newMessage(getApplicationContext(), R.string.end, Toast.LENGTH_SHORT);
        timer.cancel();
        timer.purge();
        finish();
    }

    private void stopTimer() {
        timer.cancel();
        timer.purge();
        timer = new Timer();
    }

    @Override
    public void onClick(View view) {
    }

    @Override
    public void onBackPressed() {
        stopTransition();
        timer.cancel();
        timer.purge();
        super.onBackPressed();
    }

    class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) return false;

                if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    loadNext();
                } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    loadPrevious();
                }
            } catch (Exception ignored) {
            }
            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            toggle();
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide() {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, 100);
    }
}