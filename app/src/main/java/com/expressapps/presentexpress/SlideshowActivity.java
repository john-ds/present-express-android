package com.expressapps.presentexpress;

import android.annotation.SuppressLint;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Slide;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SlideshowActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private GestureDetector gestureDetector;
    View.OnTouchListener gestureListener;

    Timer timer = new Timer();
    int currentSlide = 0;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private ImageView mContentView;

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };

    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        FrameLayout background = findViewById(R.id.back);
        background.setBackgroundColor(MainActivity.backColor);

        mVisible = true;
        mContentView = findViewById(R.id.fullscreen_content);

        gestureDetector = new GestureDetector(this, new MyGestureDetector());
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        };

        mContentView.setOnClickListener(SlideshowActivity.this);
        mContentView.setOnTouchListener(gestureListener);

        mContentView.setImageBitmap((Bitmap) MainActivity.AllSlides.get(0).get("bmp"));
    }

    @Override
    public void onClick(View view) {}

    @Override
    public void onBackPressed() {
        timer.cancel();
        timer.purge();
        super.onBackPressed();
    }

    class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) return false;

                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    timer.cancel();
                    timer.purge();
                    timer = new Timer();

                    currentSlide++;
                    if (currentSlide >= MainActivity.AllSlides.size()) {
                        if (MainActivity.loop) {
                            currentSlide = 0;
                            mContentView.setImageBitmap((Bitmap) MainActivity.AllSlides.get(0).get("bmp"));
                            if (MainActivity.timings) next();

                        } else {
                            Toast toast = Toast.makeText(getApplicationContext(), R.string.end, Toast.LENGTH_SHORT);
                            toast.show();
                            timer.cancel();
                            timer.purge();
                            finish();
                        }

                    } else {
                        mContentView.setImageBitmap((Bitmap) MainActivity.AllSlides.get(currentSlide).get("bmp"));
                        if (MainActivity.timings) next();
                    }

                } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    timer.cancel();
                    timer.purge();
                    timer = new Timer();

                    if (currentSlide > 0) currentSlide--;
                    mContentView.setImageBitmap((Bitmap) MainActivity.AllSlides.get(currentSlide).get("bmp"));
                    if (MainActivity.timings) next();
                }

            } catch (Exception ignored) {}
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

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
        if (MainActivity.timings) next();
    }

    private void next() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                SlideshowActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        currentSlide++;
                        if (currentSlide >= MainActivity.AllSlides.size()) {
                            if (MainActivity.loop) {
                                currentSlide = 0;
                                mContentView.setImageBitmap((Bitmap) MainActivity.AllSlides.get(0).get("bmp"));
                                if (MainActivity.timings) next();

                            } else {
                                Toast toast = Toast.makeText(getApplicationContext(), R.string.end, Toast.LENGTH_SHORT);
                                toast.show();
                                timer.cancel();
                                timer.purge();
                                finish();
                            }

                        } else {
                            mContentView.setImageBitmap((Bitmap) MainActivity.AllSlides.get(currentSlide).get("bmp"));
                            next();
                        }
                    }
                });

            }
        }, Math.round(((Double) MainActivity.AllSlides.get(currentSlide).get("timing")) * 1000));
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
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}